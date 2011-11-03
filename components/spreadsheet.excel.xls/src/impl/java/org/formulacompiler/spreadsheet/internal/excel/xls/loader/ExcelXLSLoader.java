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
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.formulacompiler.compiler.internal.Duration;
import org.formulacompiler.compiler.internal.LocalDate;
import org.formulacompiler.compiler.internal.expressions.parser.CellRefFormat;
import org.formulacompiler.compiler.internal.expressions.parser.ExpressionParser;
import org.formulacompiler.compiler.internal.expressions.parser.ParseException;
import org.formulacompiler.runtime.ComputationMode;
import org.formulacompiler.runtime.internal.RuntimeDouble_v2;
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
import org.formulacompiler.spreadsheet.internal.parser.SpreadsheetExpressionParserA1OOXML;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;


/**
 * Spreadsheet file loader implementation for the Microsoft Excel .xls format. Call the
 * {@code register()} method to register the loader with the central {@link SpreadsheetLoader}.
 *
 * @author peo
 */
public final class ExcelXLSLoader implements SpreadsheetLoader
{
	private static final Pattern TIME_PATTERN = Pattern.compile( "[hs]|(?:AM/PM)|(?:A/P)|(?:h\\W*m)(?:m\\W*s)" );

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

	private ExcelXLSLoader( Config _config )
	{
		this.config = _config;
	}


	public Spreadsheet loadFrom( String _originalFileName, InputStream _stream ) throws IOException, SpreadsheetException
	{
		final Workbook xlsWorkbook = new HSSFWorkbook( _stream );
		loadConfig( xlsWorkbook );
		final SpreadsheetBuilder spreadsheetBuilder = new SpreadsheetBuilder( ComputationMode.EXCEL );

		final int numberOfSheets = xlsWorkbook.getNumberOfSheets();
		for (int i = 0; i < numberOfSheets; i++) {
			final Sheet xlsSheet = xlsWorkbook.getSheetAt( i );
			final SheetBuilder sheetBuilder = spreadsheetBuilder.beginSheet( xlsSheet.getSheetName() );
			loadRows( xlsSheet, sheetBuilder );
			sheetBuilder.endSheet();
		}

		final BaseSpreadsheet spreadsheet = spreadsheetBuilder.getSpreadsheet();
		loadNames( xlsWorkbook, spreadsheet );
		return spreadsheet;
	}

	private String globalTimeFormat = null;
	private TimeZone globalTimeZone = TimeZone.getDefault();

	private void loadConfig( Workbook _xlsWorkbook )
	{
		final SpreadsheetBuilder spreadsheetBuilder = new SpreadsheetBuilder( ComputationMode.EXCEL );
		final int numberOfSheets = _xlsWorkbook.getNumberOfSheets();
		for (int i = 0; i < numberOfSheets; i++) {
			spreadsheetBuilder.beginSheet( _xlsWorkbook.getSheetAt( i ).getSheetName() );
			spreadsheetBuilder.endSheet();
		}
		final BaseSpreadsheet spreadsheet = spreadsheetBuilder.getSpreadsheet();

		final Cell gtFormatCell = getCellByName( "GlobalTimeFormat", _xlsWorkbook, spreadsheet );
		if (null != gtFormatCell) {
			this.globalTimeFormat = gtFormatCell.getCellStyle().getDataFormatString();
		}
		final Cell gtZoneNameCell = getCellByName( "GlobalTimeZoneName", _xlsWorkbook, spreadsheet );
		if (null != gtZoneNameCell) {
			this.globalTimeZone = TimeZone.getTimeZone( gtZoneNameCell.getStringCellValue() );
		}
	}

	private void loadNames( Workbook _xlsWorkbook, BaseSpreadsheet _spreadsheet )
	{
		final int numberOfNames = _xlsWorkbook.getNumberOfNames();
		for (int nameIndex = 0; nameIndex < numberOfNames; nameIndex++) {
			final Name name = _xlsWorkbook.getNameAt( nameIndex );
			if (name.isFunctionName()) continue;

			final String cellRangeAddress = name.getRefersToFormula();
			final String rangeName = name.getNameName();

			final ExpressionParser parser = new SpreadsheetExpressionParserA1OOXML( cellRangeAddress, _spreadsheet );
			try {
				final CellRange cellRange = (CellRange) parser.rangeOrCellRefA1();
				_spreadsheet.defineModelRangeName( rangeName, cellRange );
			}
			catch (ParseException e) {
				// Ignore all non 'named range' names
			}
		}
	}

	private Cell getCellByName( String _name, Workbook _workbook, BaseSpreadsheet _spreadsheet )
	{
		final Name name = _workbook.getName( _name );
		if (name == null) return null;
		if (name.isFunctionName()) return null;

		final String cellRangeAddress = name.getRefersToFormula();

		final ExpressionParser parser = new SpreadsheetExpressionParserA1OOXML( cellRangeAddress, _spreadsheet );
		try {
			final CellRange range = (CellRange) parser.rangeOrCellRefA1();
			if (!(range instanceof CellIndex)) {
				return null;
			}
			final CellIndex cellIndex = (CellIndex) range;
			final int sheetIndex = cellIndex.getSheetIndex();
			final int rowIndex = cellIndex.getRowIndex();
			final int columnIndex = cellIndex.getColumnIndex();
			return _workbook.getSheetAt( sheetIndex ).getRow( rowIndex ).getCell( columnIndex );
		}
		catch (ParseException e) {
			// Ignore all non 'named range' names
			return null;
		}
	}

	private void loadRows( Sheet _xlsSheet, SheetBuilder _sheetBuilder )
	{
		int currentRowIndex = 0;
		for (final Row row : _xlsSheet) {
			final int rowIndex = row.getRowNum();
			while (rowIndex > currentRowIndex) {
				_sheetBuilder.beginRow();
				_sheetBuilder.endRow();
				currentRowIndex++;
			}
			final RowBuilder rowBuilder = _sheetBuilder.beginRow();
			int currentColIndex = 0;
			for (Cell cell : row) {
				final int columnIndex = cell.getColumnIndex();
				while (columnIndex > currentColIndex) {
					rowBuilder.addEmptyCell();
					currentColIndex++;
				}
				loadCell( cell, rowBuilder );
				currentColIndex++;
			}
			rowBuilder.endRow();
			currentRowIndex++;
		}
	}

	private boolean isTime( Cell cell )
	{
		final CellStyle style = cell.getCellStyle();
		if (style != null) {
			final String format = cell.getCellStyle().getDataFormatString();
			if (format != null) {
				final Matcher dtMatcher = TIME_PATTERN.matcher( format );
				return dtMatcher.find();
			}
		}
		return false;
	}


	private void loadCell( Cell _xlsCell, RowBuilder _rowBuilder )
	{
		final int xlsType = _xlsCell.getCellType();
		if (xlsType == Cell.CELL_TYPE_FORMULA) {
			final String expression;
			expression = _xlsCell.getCellFormula();
			_rowBuilder.addCellWithExpression( new LazySpreadsheetExpressionParser( expression, CellRefFormat.A1 ) );

			if (this.config.loadAllCellValues) {
				final int cachedFormulaResultType = _xlsCell.getCachedFormulaResultType();
				if (Cell.CELL_TYPE_NUMERIC == cachedFormulaResultType) {
					_rowBuilder.setValue( getNumberValue( _xlsCell ) );
				}
				else if (Cell.CELL_TYPE_BOOLEAN == cachedFormulaResultType) {
					_rowBuilder.setValue( _xlsCell.getBooleanCellValue() );
				}
				else if (Cell.CELL_TYPE_STRING == cachedFormulaResultType) {
					_rowBuilder.setValue( _xlsCell.getStringCellValue() );
				}
			}
		}
		else if (Cell.CELL_TYPE_BLANK == xlsType) {
			_rowBuilder.addEmptyCell();
		}
		else if (Cell.CELL_TYPE_BOOLEAN == xlsType) {
			_rowBuilder.addCellWithConstant( _xlsCell.getBooleanCellValue() );
		}

		else if (Cell.CELL_TYPE_NUMERIC == xlsType) {
			_rowBuilder.addCellWithConstant( getNumberValue( _xlsCell ) );

		}
		else if (Cell.CELL_TYPE_STRING == xlsType) {
			_rowBuilder.addCellWithConstant( _xlsCell.getStringCellValue() );
		}
		else if (xlsType == Cell.CELL_TYPE_ERROR) {
			final int errorCode = _xlsCell.getErrorCellValue();
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

	private Object getNumberValue( Cell _xlsCell )
	{
		final double value = _xlsCell.getNumericCellValue();
		final boolean isDate = DateUtil.isCellDateFormatted( _xlsCell );
		final boolean isTime = isTime( _xlsCell );
		if (isDate || isTime) {
			if (null != this.globalTimeFormat
					&& this.globalTimeFormat.equals( _xlsCell.getCellStyle().getDataFormatString() )) {
				return RuntimeDouble_v2.dateFromNum( _xlsCell.getNumericCellValue(), this.globalTimeZone, ComputationMode.EXCEL );
			}
			if ((isDate && value < 1) || (isTime && value < 365)) {
				return new Duration( value );
			}
			else {
				return new LocalDate( value );
			}
		}
		else {
			return value;
		}
	}
}
