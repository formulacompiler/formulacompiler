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
import org.formulacompiler.runtime.internal.RuntimeDouble_v2;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.SpreadsheetLoader;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.CellRange;
import org.formulacompiler.spreadsheet.internal.CellRefFormat;
import org.formulacompiler.spreadsheet.internal.CellWithConstant;
import org.formulacompiler.spreadsheet.internal.CellWithError;
import org.formulacompiler.spreadsheet.internal.CellWithLazilyParsedExpression;
import org.formulacompiler.spreadsheet.internal.RowImpl;
import org.formulacompiler.spreadsheet.internal.SheetImpl;
import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;
import org.formulacompiler.spreadsheet.internal.loader.SpreadsheetLoaderDispatcher;
import org.formulacompiler.spreadsheet.internal.parser.LazySpreadsheetExpressionParser;

import jxl.BooleanFormulaCell;
import jxl.Cell;
import jxl.CellType;
import jxl.DateCell;
import jxl.DateFormulaCell;
import jxl.NumberFormulaCell;
import jxl.StringFormulaCell;
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

		public SpreadsheetLoader newInstance( Config _config )
		{
			return new ExcelXLSLoader( _config );
		}

		public boolean canHandle( String _fileName )
		{
			return _fileName.toLowerCase().endsWith( ".xls" );
		}

	}


	private final Config config;

	public ExcelXLSLoader( Config _config )
	{
		this.config = _config;
	}


	public Spreadsheet loadFrom( String _originalFileName, InputStream _stream ) throws IOException,
			SpreadsheetException
	{
		final WorkbookSettings xlsSettings = new WorkbookSettings();
		xlsSettings.setLocale( Locale.ENGLISH );
		xlsSettings.setExcelDisplayLanguage( "EN" );
		xlsSettings.setExcelRegionalSettings( "EN" );
		xlsSettings.setEncoding( "ISO-8859-1" );
		xlsSettings.setAutoFilterDisabled( true );
		xlsSettings.setCellValidationDisabled( true );
		xlsSettings.setDrawingsDisabled( true );
		xlsSettings.setMergedCellChecking( false );
		xlsSettings.setPropertySets( false );
		xlsSettings.setSuppressWarnings( true );
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
			final jxl.Cell[] xlsRow = _xlsSheet.getRow( iRow );
			if (null == xlsRow) {
				_sheet.getRowList().add( null );
			}
			else {
				final RowImpl row = new RowImpl( _sheet );
				for (int iCell = 0; iCell < xlsRow.length; iCell++) {
					final jxl.Cell xlsCell = xlsRow[ iCell ];
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
		final jxl.CellType xlsType = _xlsCell.getType();

		if (_xlsCell instanceof jxl.FormulaCell) {
			final jxl.FormulaCell xlsFormulaCell = (jxl.FormulaCell) _xlsCell;
			CellWithLazilyParsedExpression exprCell = new CellWithLazilyParsedExpression( _row );
			try {
				exprCell.setExpressionParser( new LazySpreadsheetExpressionParser( exprCell, xlsFormulaCell.getFormula(), CellRefFormat.A1 ) );
			}
			catch (jxl.biff.formula.FormulaException e) {
				throw new SpreadsheetException.LoadError( e );
			}
			if (xlsFormulaCell instanceof NumberFormulaCell) {
				final NumberFormulaCell xlsNumFormulaCell = ((NumberFormulaCell) xlsFormulaCell);
				exprCell.applyNumberFormat( convertNumberFormat( xlsNumFormulaCell, xlsNumFormulaCell.getNumberFormat() ) );
			}
			if (this.config.loadAllCellValues) {
				if (xlsFormulaCell instanceof NumberFormulaCell) {
					exprCell.setValue( ((NumberFormulaCell) xlsFormulaCell).getValue() );
				}
				else if (xlsFormulaCell instanceof DateFormulaCell) {
					exprCell.setValue( ((DateFormulaCell) xlsFormulaCell).getValue() );
				}
				else if (xlsFormulaCell instanceof BooleanFormulaCell) {
					exprCell.setValue( ((BooleanFormulaCell) xlsFormulaCell).getValue() );
				}
				else if (xlsFormulaCell instanceof StringFormulaCell) {
					exprCell.setValue( ((StringFormulaCell) xlsFormulaCell).getString() );
				}
			}
		}
		else if (jxl.CellType.EMPTY == xlsType) {
			_row.getCellList().add( null );
		}
		else if (jxl.CellType.BOOLEAN == xlsType) {
			new CellWithConstant( _row, ((jxl.BooleanCell) _xlsCell).getValue() );
		}
		else if (jxl.CellType.NUMBER == xlsType) {
			final jxl.NumberCell xlsNumCell = (jxl.NumberCell) _xlsCell;
			final CellInstance numCell = new CellWithConstant( _row, xlsNumCell.getValue() );
			numCell.applyNumberFormat( convertNumberFormat( xlsNumCell, xlsNumCell.getNumberFormat() ) );
		}
		else if (CellType.DATE == xlsType) {
			final DateCell xlsDateCell = (jxl.DateCell) _xlsCell;
			final Object value;
			if (null != this.globalTimeFormat
					&& this.globalTimeFormat.equals( xlsDateCell.getCellFormat().getFormat().getFormatString() )) {
				value = RuntimeDouble_v2.dateFromNum( xlsDateCell.getValue(), this.globalTimeZone );
			}
			else {
				value = new LocalExcelDate( xlsDateCell.getValue() );
			}
			new CellWithConstant( _row, value );
		}
		else if (jxl.CellType.LABEL == xlsType) {
			new CellWithConstant( _row, ((jxl.LabelCell) _xlsCell).getString() );
		}
		else if (jxl.CellType.ERROR == xlsType) {
			final int errorCode = ((jxl.ErrorCell) _xlsCell).getErrorCode();
			switch (errorCode) {
				case 7:
					new CellWithError( _row, CellWithError.DIV0 );
					break;
				case 15:
					new CellWithError( _row, CellWithError.VALUE );
					break;
				case 23:
					new CellWithError( _row, CellWithError.REF );
					break;
				case 36:
					new CellWithError( _row, CellWithError.NUM );
					break;
				case 42:
					new CellWithError( _row, CellWithError.NA );
					break;
				default:
					new CellWithError( _row, "#ERR:" + errorCode );
			}
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
				final CellIndex end = new CellIndex( _workbook, xlsEndSheet, xlsEnd.getColumn(), xlsEnd.getRow() );
				final CellRange range = CellRange.getCellRange( start, end );
				_workbook.addToNameMap( name, range );
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
