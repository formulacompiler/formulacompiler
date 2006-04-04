/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are prohibited, unless you have been explicitly granted 
 * more rights by Abacus Research AG.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS 
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND 
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR 
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package sej.loader.excel.xml;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import sej.Spreadsheet;
import sej.SpreadsheetLoader;
import sej.loader.excel.ExcelFormatError;
import sej.loader.excel.ExcelLazyExpressionParser;
import sej.loader.excel.ExcelNumberFormat;
import sej.model.CellInstance;
import sej.model.CellWithConstant;
import sej.model.CellWithLazilyParsedExpression;
import sej.model.Row;
import sej.model.Sheet;
import sej.model.Workbook;


/**
 * Spreadsheet file loader implementation for the Microsoft Excel .xml format. Call the
 * {@code register()} method to register the loader with the central {@link SpreadsheetLoader}.
 * 
 * @author peo
 */
public class ExcelXMLLoader implements SpreadsheetLoader.FileLoader
{
	private Namespace xnsDef = Namespace.getNamespace( "urn:schemas-microsoft-com:office:spreadsheet" );
	private Namespace xnsSS = Namespace.getNamespace( "ss", "urn:schemas-microsoft-com:office:spreadsheet" );
	private XPath xpathSelectStyles;
	private XPath xpathSelectTable;
	private XPath xpathSelectRows;
	private XPath xpathSelectCells;
	private Map<String, String> numberFormatStringsByStyleID = new HashMap<String, String>();
	private Map<String, NumberFormat> numberFormatsByStyleID = new HashMap<String, NumberFormat>();


	public static void register()
	{
		SpreadsheetLoader.registerLoader( new SpreadsheetLoader.Factory()
		{

			public SpreadsheetLoader.FileLoader newWorkbookLoader()
			{
				try {
					return new ExcelXMLLoader();
				}
				catch (JDOMException e) {
					e.printStackTrace();
					return null;
				}
			}

			public boolean canHandle( File _file )
			{
				return _file.getName().toLowerCase().endsWith( ".xml" );
			}

		} );
	}


	public ExcelXMLLoader() throws JDOMException
	{
		this.xpathSelectStyles = XPath.newInstance( "//ss:Styles/ss:Style" );
		this.xpathSelectTable = XPath.newInstance( "//ss:Table" );
		this.xpathSelectRows = XPath.newInstance( "ss:Row" );
		this.xpathSelectRows.addNamespace( this.xnsSS );
		this.xpathSelectCells = XPath.newInstance( "ss:Cell" );
		this.xpathSelectCells.addNamespace( this.xnsSS );
	}


	public Spreadsheet loadFile( File _file ) throws IOException
	{
		Document doc;
		try {
			doc = new SAXBuilder().build( _file );
			return parseDocument( doc );
		}
		catch (JDOMException e) {
			e.printStackTrace();
			return null;
		}
	}


	private Workbook parseDocument( Document _doc ) throws JDOMException
	{
		Element xmlRoot = _doc.getRootElement();
		parseStyles( xmlRoot );
		Element xmlTable = (Element) this.xpathSelectTable.selectSingleNode( xmlRoot );
		Workbook workbook = new Workbook();
		Sheet sheet = new Sheet( workbook );
		parseTableRowsIntoSheet( sheet, xmlTable );
		return workbook;
	}


	@SuppressWarnings("unchecked")
	private void parseStyles( Element _xmlRoot ) throws JDOMException
	{
		List<Element> xmlStyles = this.xpathSelectStyles.selectNodes( _xmlRoot );
		for (Element xmlStyle : xmlStyles) {
			String xmlID = xmlStyle.getAttributeValue( "ID", this.xnsSS );
			Element xmlNumFormat = xmlStyle.getChild( "NumberFormat", this.xnsDef );
			if (null != xmlNumFormat) {
				String excelFormatString = xmlNumFormat.getAttributeValue( "Format", this.xnsSS );
				if (null != excelFormatString) {
					this.numberFormatStringsByStyleID.put( xmlID, excelFormatString );
				}
			}
		}
	}


	private NumberFormat getNumberFormatForStyle( String _styleID, CellInstance _usedInCell )
	{
		NumberFormat numFormat = this.numberFormatsByStyleID.get( _styleID );
		if (null == numFormat) {
			String excelFormatString = this.numberFormatStringsByStyleID.get( _styleID );
			if (null != excelFormatString) {

				try {
					numFormat = ExcelNumberFormat.getNumberFormatForExcelFormat( excelFormatString );
				}
				catch (IllegalArgumentException e) {
					throw new ExcelFormatError( "The Excel number format, "
							+ excelFormatString + ", used in cell " + _usedInCell.getCanonicalName()
							+ " cannot be handled by SEJ." );
				}

				this.numberFormatsByStyleID.put( _styleID, numFormat );
			}
		}
		return numFormat;
	}


	@SuppressWarnings("unchecked")
	private void parseTableRowsIntoSheet( Sheet _sheet, Element _xmlTable ) throws JDOMException
	{
		List<Element> xmlRows = this.xpathSelectRows.selectNodes( _xmlTable );
		for (Element xmlRow : xmlRows) {
			padListUptoIndexOf( _sheet.getRows(), xmlRow );
			Row row = new Row( _sheet );
			parseTableCellsIntoRow( _sheet, row, xmlRow );
		}
	}


	@SuppressWarnings("unchecked")
	private void parseTableCellsIntoRow( Sheet _sheet, Row _row, Element _xmlRow ) throws JDOMException
	{
		List<Element> xmlCells = this.xpathSelectCells.selectNodes( _xmlRow );
		for (Element xmlCell : xmlCells) {
			padListUptoIndexOf( _row.getCells(), xmlCell );
			parseTableCell( _row, xmlCell );
		}
	}


	private CellInstance parseTableCell( Row _row, Element _xmlCell )
	{
		String xmlFormula = _xmlCell.getAttributeValue( "Formula", this.xnsSS );
		CellInstance result;
		if (null != xmlFormula) {
			result = parseTableCellWithFormula( _row, _xmlCell, xmlFormula.substring( 1 ) );
		}
		else {
			result = parseTableCellWithValue( _row, _xmlCell );
		}
		parseNameOfCell( result, _xmlCell );
		parseFormatOfCell( result, _xmlCell );
		return result;
	}


	private CellInstance parseTableCellWithValue( Row _row, Element _xmlCell )
	{
		CellWithConstant cell = new CellWithConstant( _row, null );
		Element xmlData = _xmlCell.getChild( "Data", this.xnsDef );
		String xmlValue = xmlData.getValue();
		String xmlType = xmlData.getAttributeValue( "Type", this.xnsSS );
		Object cellValue;
		if (xmlType.equalsIgnoreCase( "NUMBER" )) {
			cellValue = Double.parseDouble( xmlValue );
		}
		else {
			cellValue = xmlValue;
		}
		cell.setValue( cellValue );
		return cell;
	}


	private CellInstance parseTableCellWithFormula( Row _row, Element _xmlCell, String _xmlFormula )
	{
		CellWithLazilyParsedExpression cell = new CellWithLazilyParsedExpression( _row );
		cell.setExpressionParser( new ExcelLazyExpressionParser( cell, _xmlFormula ) );
		return cell;
	}


	private void parseNameOfCell( CellInstance _cell, Element _xmlCell )
	{
		Element xmlName = _xmlCell.getChild( "NamedCell", this.xnsDef );
		if (null != xmlName) {
			String cellName = xmlName.getAttributeValue( "Name", this.xnsSS );
			_cell.getRow().getSheet().getWorkbook().getNameMap().put( cellName, _cell.getCellIndex() );
		}
	}


	private void parseFormatOfCell( CellInstance _cell, Element _xmlCell )
	{
		String xmlStyleID = _xmlCell.getAttributeValue( "StyleID", this.xnsSS );
		if (null != xmlStyleID) {
			_cell.setNumberFormat( getNumberFormatForStyle( xmlStyleID, _cell ) );
		}
	}


	@SuppressWarnings("unchecked")
	private int padListUptoIndexOf( List _list, Element _xmlElement )
	{
		String xmlIndex = _xmlElement.getAttributeValue( "Index", this.xnsSS );
		if (null != xmlIndex) {
			int index = Integer.parseInt( xmlIndex ) - 1 - _list.size();
			while (index-- > 0) {
				_list.add( null );
			}
			return index;
		}
		return _list.size();
	}
}
