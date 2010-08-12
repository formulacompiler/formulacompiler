/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * This file is part of the Abacus Formula Compiler (AFC).
 *
 * For commercial licensing, please contact sales(at)formulacompiler.com.
 *
 * AFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AFC.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.formulacompiler.spreadsheet.internal.excel.xls.loader;

import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.TimeZone;

import org.formulacompiler.compiler.internal.Duration;
import org.formulacompiler.compiler.internal.LocalDate;
import org.formulacompiler.compiler.internal.expressions.parser.CellRefFormat;
import org.formulacompiler.runtime.ComputationMode;
import org.formulacompiler.runtime.internal.RuntimeDouble_v2;
import org.formulacompiler.runtime.internal.spreadsheet.CellAddressImpl;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.SpreadsheetLoader;
import org.formulacompiler.spreadsheet.internal.BaseSpreadsheet;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellRange;
import org.formulacompiler.spreadsheet.internal.CellWithError;
import org.formulacompiler.spreadsheet.internal.loader.SpreadsheetLoaderDispatcher;
import org.formulacompiler.spreadsheet.internal.loader.builder.RowBuilder;
import org.formulacompiler.spreadsheet.internal.loader.builder.SheetBuilder;
import org.formulacompiler.spreadsheet.internal.loader.builder.SpreadsheetBuilder;
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
			final SpreadsheetBuilder spreadsheetBuilder = new SpreadsheetBuilder( ComputationMode.EXCEL );

			loadConfig( xlsWorkbook );

			for (final jxl.Sheet xlsSheet : xlsWorkbook.getSheets()) {
				final SheetBuilder sheetBuilder = spreadsheetBuilder.beginSheet( xlsSheet.getName() );
				loadRows( xlsSheet, sheetBuilder );
				sheetBuilder.endSheet();
			}

			final BaseSpreadsheet workbook = spreadsheetBuilder.getSpreadsheet();
			loadNames( xlsWorkbook, workbook );
			return workbook;
		}
		catch (jxl.read.biff.BiffException e) {
			throw new SpreadsheetException.LoadError( "Error parsing " + _originalFileName, e );
		}
		catch (SpreadsheetException.LoadError e) {
			throw new SpreadsheetException.LoadError( "Error parsing " + _originalFileName, e );
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


	private void loadRows( jxl.Sheet _xlsSheet, SheetBuilder _sheetBuilder ) throws SpreadsheetException
	{
		for (int iRow = 0; iRow < _xlsSheet.getRows(); iRow++) {
			final RowBuilder rowBuilder = _sheetBuilder.beginRow();
			final jxl.Cell[] xlsRow = _xlsSheet.getRow( iRow );
			if (xlsRow != null) {
				for (final Cell xlsCell : xlsRow) {
					if (null == xlsCell) {
						rowBuilder.addEmptyCell();
					}
					else {
						loadCell( _xlsSheet.getName(), xlsCell, rowBuilder );
					}
				}
			}
			rowBuilder.endRow();
		}
	}


	private void loadCell( final String _sheetName, Cell _xlsCell, RowBuilder _rowBuilder ) throws SpreadsheetException
	{
		final jxl.CellType xlsType = _xlsCell.getType();

		if (_xlsCell instanceof jxl.FormulaCell) {
			final jxl.FormulaCell xlsFormulaCell = (jxl.FormulaCell) _xlsCell;
			final String expression;
			try {
				expression = xlsFormulaCell.getFormula();
			}
			catch (jxl.biff.formula.FormulaException e) {
				final CellAddressImpl cellIndex = new CellAddressImpl( _sheetName, _xlsCell.getColumn(), _xlsCell.getRow() );
				throw new SpreadsheetException.LoadError( "Error parsing cell " + cellIndex, e );
			}
			_rowBuilder.addCellWithExpression( new LazySpreadsheetExpressionParser( expression, CellRefFormat.A1 ) );
			if (xlsFormulaCell instanceof NumberFormulaCell) {
				final NumberFormulaCell xlsNumFormulaCell = ((NumberFormulaCell) xlsFormulaCell);
				_rowBuilder.applyNumberFormat( convertNumberFormat( xlsNumFormulaCell, xlsNumFormulaCell.getNumberFormat() ) );
			}
			if (this.config.loadAllCellValues) {
				if (xlsFormulaCell instanceof NumberFormulaCell) {
					_rowBuilder.setValue( ((NumberFormulaCell) xlsFormulaCell).getValue() );
				}
				else if (xlsFormulaCell instanceof DateFormulaCell) {
					final Object value = getDateTimeValue( (DateFormulaCell) xlsFormulaCell );
					_rowBuilder.setValue( value );
				}
				else if (xlsFormulaCell instanceof BooleanFormulaCell) {
					_rowBuilder.setValue( ((BooleanFormulaCell) xlsFormulaCell).getValue() );
				}
				else if (xlsFormulaCell instanceof StringFormulaCell) {
					_rowBuilder.setValue( ((StringFormulaCell) xlsFormulaCell).getString() );
				}
			}
		}
		else if (jxl.CellType.EMPTY == xlsType) {
			_rowBuilder.addEmptyCell();
		}
		else if (jxl.CellType.BOOLEAN == xlsType) {
			_rowBuilder.addCellWithConstant( ((jxl.BooleanCell) _xlsCell).getValue() );
		}
		else if (jxl.CellType.NUMBER == xlsType) {
			final jxl.NumberCell xlsNumCell = (jxl.NumberCell) _xlsCell;
			_rowBuilder.addCellWithConstant( xlsNumCell.getValue() )
					.applyNumberFormat( convertNumberFormat( xlsNumCell, xlsNumCell.getNumberFormat() ) );
		}
		else if (CellType.DATE == xlsType) {
			final DateCell xlsDateCell = (jxl.DateCell) _xlsCell;
			final Object value = getDateTimeValue( xlsDateCell );
			_rowBuilder.addCellWithConstant( value );
		}
		else if (jxl.CellType.LABEL == xlsType) {
			_rowBuilder.addCellWithConstant( ((jxl.LabelCell) _xlsCell).getString() );
		}
		else if (jxl.CellType.ERROR == xlsType) {
			final int errorCode = ((jxl.ErrorCell) _xlsCell).getErrorCode();
			switch (errorCode) {
				case 7:
					_rowBuilder.addCellWithError( CellWithError.DIV0 );
					break;
				case 15:
					_rowBuilder.addCellWithError( CellWithError.VALUE );
					break;
				case 23:
					_rowBuilder.addCellWithError( CellWithError.REF );
					break;
				case 36:
					_rowBuilder.addCellWithError( CellWithError.NUM );
					break;
				case 42:
					_rowBuilder.addCellWithError( CellWithError.NA );
					break;
				default:
					_rowBuilder.addCellWithError( "#ERR:" + errorCode );
			}
		}
	}

	private Object getDateTimeValue( final DateCell _xlsDateCell )
	{
		final Object value;
		if (null != this.globalTimeFormat
				&& this.globalTimeFormat.equals( _xlsDateCell.getCellFormat().getFormat().getFormatString() )) {
			value = RuntimeDouble_v2.dateFromNum( _xlsDateCell.getValue(), this.globalTimeZone, ComputationMode.EXCEL );
		}
		else if (_xlsDateCell.isTime()) {
			value = new Duration( _xlsDateCell.getValue() );
		}
		else {
			value = new LocalDate( _xlsDateCell.getValue() );
		}
		return value;
	}


	private void loadNames( jxl.Workbook _xlsWorkbook, BaseSpreadsheet _workbook )
	{
		for (final String name : _xlsWorkbook.getRangeNames()) {
			final jxl.Range[] xlsRange = _xlsWorkbook.findByName( name );
			if (1 == xlsRange.length) {
				final int xlsStartSheet = xlsRange[ 0 ].getFirstSheetIndex();
				final int xlsEndSheet = xlsRange[ 0 ].getLastSheetIndex();
				final jxl.Cell xlsStart = xlsRange[ 0 ].getTopLeft();
				final jxl.Cell xlsEnd = xlsRange[ 0 ].getBottomRight();
				final CellIndex start = new CellIndex( _workbook, xlsStartSheet, xlsStart.getColumn(), true, xlsStart.getRow(), true );
				final CellIndex end = new CellIndex( _workbook, xlsEndSheet, xlsEnd.getColumn(), true, xlsEnd.getRow(), true );
				final CellRange range = CellRange.getCellRange( start, end );
				_workbook.defineModelRangeName( name, range );
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
