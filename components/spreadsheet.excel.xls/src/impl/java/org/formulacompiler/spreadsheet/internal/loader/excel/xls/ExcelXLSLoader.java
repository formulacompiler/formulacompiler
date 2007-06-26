/*
 * Copyright (c) 2006 by Abacus Research AG, Switzerland.
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
package org.formulacompiler.spreadsheet.internal.loader.excel.xls;

import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.TimeZone;

import org.formulacompiler.compiler.internal.LocalExcelDate;
import org.formulacompiler.runtime.internal.RuntimeDouble_v1;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.SpreadsheetLoader;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.CellRange;
import org.formulacompiler.spreadsheet.internal.CellWithConstant;
import org.formulacompiler.spreadsheet.internal.CellWithLazilyParsedExpression;
import org.formulacompiler.spreadsheet.internal.RowImpl;
import org.formulacompiler.spreadsheet.internal.SheetImpl;
import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;
import org.formulacompiler.spreadsheet.internal.loader.SpreadsheetLoaderDispatcher;
import org.formulacompiler.spreadsheet.internal.parser.LazySpreadsheetExpressionParser;

import jxl.Cell;
import jxl.CellType;
import jxl.DateCell;
import jxl.NumberFormulaCell;
import jxl.Workbook;
import jxl.WorkbookSettings;


/**
 * Spreadsheet file loader implementation for the Microsoft Excel .xls format. Call the
 * {@code register()} method to register the loader with the central {@link SpreadsheetLoader}.
 * 
 * @author peo
 */
public final class ExcelXLSLoader implements SpreadsheetLoader
{

	public static final class Factory implements SpreadsheetLoaderDispatcher.Factory
	{

		public SpreadsheetLoader newInstance()
		{
			return new ExcelXLSLoader();
		}

		public boolean canHandle( String _fileName )
		{
			return _fileName.toLowerCase().endsWith( ".xls" );
		}

	}


	public Spreadsheet loadFrom( String _originalFileName, InputStream _stream ) throws IOException,
			SpreadsheetException
	{
		final WorkbookSettings xlsSettings = new WorkbookSettings();
		xlsSettings.setLocale( Locale.ENGLISH );
		xlsSettings.setExcelDisplayLanguage( "EN" );
		xlsSettings.setExcelRegionalSettings( "EN" );
		xlsSettings.setEncoding( "ISO-8859-1" );
		try {
			final jxl.Workbook xlsWorkbook = jxl.Workbook.getWorkbook( _stream, xlsSettings );
			final SpreadsheetImpl workbook = new SpreadsheetImpl();

			loadConfig( xlsWorkbook );

			for (final jxl.Sheet xlsSheet : xlsWorkbook.getSheets()) {
				final SheetImpl sheet = new SheetImpl( workbook, xlsSheet.getName() );
				loadRows( xlsSheet, sheet );
			}

			loadNames( xlsWorkbook, workbook );
			workbook.trim();

			return workbook;
		}
		catch (jxl.read.biff.BiffException e) {
			throw new SpreadsheetException.LoadError( e );
		}
	}


	private String globalTimeFormat = null;
	private TimeZone globalTimeZone = TimeZone.getDefault();

	private void loadConfig( Workbook _xlsWorkbook )
	{
		final Cell gtFormatCell = _xlsWorkbook.findCellByName( "GlobalTimeFormat" );
		if (null != gtFormatCell) {
			this.globalTimeFormat = gtFormatCell.getCellFormat().getFormat().getFormatString();
		}
		final Cell gtZoneNameCell = _xlsWorkbook.findCellByName( "GlobalTimeZoneName" );
		if (null != gtZoneNameCell) {
			this.globalTimeZone = TimeZone.getTimeZone( gtZoneNameCell.getContents() );
		}
	}


	private void loadRows( jxl.Sheet _xlsSheet, SheetImpl _sheet ) throws SpreadsheetException
	{
		for (int iRow = 0; iRow < _xlsSheet.getRows(); iRow++) {
			jxl.Cell[] xlsRow = _xlsSheet.getRow( iRow );
			if (null == xlsRow) {
				_sheet.getRowList().add( null );
			}
			else {
				RowImpl row = new RowImpl( _sheet );
				for (int iCell = 0; iCell < xlsRow.length; iCell++) {
					jxl.Cell xlsCell = xlsRow[ iCell ];
					if (null == xlsCell) {
						row.getCellList().add( null );
					}
					else {
						loadCell( xlsCell, row );
					}
				}
			}
		}
	}


	private void loadCell( jxl.Cell _xlsCell, RowImpl _row ) throws SpreadsheetException
	{
		jxl.CellType xlsType = _xlsCell.getType();

		if (_xlsCell instanceof jxl.FormulaCell) {
			jxl.FormulaCell xlsFormulaCell = (jxl.FormulaCell) _xlsCell;
			CellWithLazilyParsedExpression exprCell = new CellWithLazilyParsedExpression( _row );
			try {
				exprCell.setExpressionParser( new LazySpreadsheetExpressionParser( exprCell, xlsFormulaCell.getFormula() ) );
			}
			catch (jxl.biff.formula.FormulaException e) {
				throw new SpreadsheetException.LoadError( e );
			}
			if (xlsFormulaCell instanceof NumberFormulaCell) {
				NumberFormulaCell xlsNumFormulaCell = ((NumberFormulaCell) xlsFormulaCell);
				exprCell.setNumberFormat( convertNumberFormat( xlsNumFormulaCell, xlsNumFormulaCell.getNumberFormat() ) );
			}
		}
		else if (jxl.CellType.EMPTY == xlsType) {
			_row.getCellList().add( null );
		}
		else if (jxl.CellType.BOOLEAN == xlsType) {
			new CellWithConstant( _row, ((jxl.BooleanCell) _xlsCell).getValue() );
		}
		else if (jxl.CellType.NUMBER == xlsType) {
			jxl.NumberCell xlsNumCell = (jxl.NumberCell) _xlsCell;
			CellInstance numCell = new CellWithConstant( _row, xlsNumCell.getValue() );
			numCell.setNumberFormat( convertNumberFormat( xlsNumCell, xlsNumCell.getNumberFormat() ) );
		}
		else if (CellType.DATE == xlsType) {
			final DateCell xlsDateCell = (jxl.DateCell) _xlsCell;
			Object value;
			if (null != this.globalTimeFormat
					&& this.globalTimeFormat.equals( xlsDateCell.getCellFormat().getFormat().getFormatString() )) {
				value = RuntimeDouble_v1.dateFromNum( xlsDateCell.getValue(), this.globalTimeZone );
			}
			else {
				value = new LocalExcelDate( xlsDateCell.getValue() );
			}
			new CellWithConstant( _row, value );
		}
		else if (jxl.CellType.LABEL == xlsType) {
			new CellWithConstant( _row, ((jxl.LabelCell) _xlsCell).getString() );
		}
	}


	private void loadNames( jxl.Workbook _xlsWorkbook, SpreadsheetImpl _workbook )
	{
		for (final String name : _xlsWorkbook.getRangeNames()) {
			final jxl.Range[] xlsRange = _xlsWorkbook.findByName( name );
			if (1 == xlsRange.length) {
				final int xlsStartSheet = xlsRange[ 0 ].getFirstSheetIndex();
				final int xlsEndSheet = xlsRange[ 0 ].getLastSheetIndex();
				final jxl.Cell xlsStart = xlsRange[ 0 ].getTopLeft();
				final jxl.Cell xlsEnd = xlsRange[ 0 ].getBottomRight();
				final CellIndex start = new CellIndex( _workbook, xlsStartSheet, xlsStart.getColumn(), xlsStart.getRow() );
				if ((xlsStart.getColumn() == xlsEnd.getColumn())
						&& (xlsStart.getRow() == xlsEnd.getRow()) && (xlsStartSheet == xlsEndSheet)) {
					_workbook.addToNameMap( name, start );
				}
				else {
					final CellIndex end = new CellIndex( _workbook, xlsEndSheet, xlsEnd.getColumn(), xlsEnd.getRow() );
					final CellRange range = new CellRange( start, end );
					_workbook.addToNameMap( name, range );
				}
			}
		}
	}


	private NumberFormat convertNumberFormat( jxl.Cell _xlsCell, NumberFormat _numberFormat )
	{
		String formatString = _xlsCell.getCellFormat().getFormat().getFormatString();
		if (formatString.equals( "" )) {
			return null;
		}
		return _numberFormat;
	}


}
