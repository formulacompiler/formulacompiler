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

package org.formulacompiler.spreadsheet.internal;

import static org.formulacompiler.runtime.internal.spreadsheet.CellAddressImpl.BROKEN_REF;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class CellRefParser
{
	private static final String BROKEN_REF_ERR = "#REF!";
	private static final String SHEET_NAME_REGEXP = "(?:(\\w+)|'((?:''|[^'])+)')";
	private static final String BROKEN_SHEET_NAME_REGEXP = "(?:(\\w+|" + BROKEN_REF_ERR + ")|'((?:''|[^'])+)')";

	public static class R1C1
	{
		private static final String INDEX_REGEXP = "(?:(-?\\d+)|\\[(-?\\d+)\\])?";

		private static final Pattern PATTERN = Pattern.compile(
				"^(?:" + SHEET_NAME_REGEXP + "!)?R" + INDEX_REGEXP + "C" + INDEX_REGEXP + "$" );

		public static CellIndex getCellIndexForCanonicalName( String _canonicalName, CellIndex _relativeTo )
		{
			final Matcher matcher = PATTERN.matcher( _canonicalName );
			if (!matcher.matches()) {
				throw new CellRefParseException( "Invalid R1C1-style cell reference: " + _canonicalName );
			}
			final String riAbs = matcher.group( 3 );
			final String riRel = matcher.group( 4 );
			final String ciAbs = matcher.group( 5 );
			final String ciRel = matcher.group( 6 );

			final SpreadsheetImpl spreadsheet = _relativeTo.getSheet().getSpreadsheet();
			final int sheetIndex = getSheetIndex( matcher, 1, _relativeTo.sheetIndex, spreadsheet );

			final int colIndex;
			final boolean colIndexAbsolute;
			if (isNotEmpty( ciAbs )) {
				colIndexAbsolute = true;
				colIndex = Integer.parseInt( ciAbs ) - 1;
			}
			else {
				colIndexAbsolute = false;
				colIndex = _relativeTo.columnIndex == BROKEN_REF ? BROKEN_REF :
						_relativeTo.columnIndex + (isNotEmpty( ciRel ) ? Integer.parseInt( ciRel ) : 0);
			}

			final int rowIndex;
			final boolean rowIndexAbsolute;
			if (isNotEmpty( riAbs )) {
				rowIndexAbsolute = true;
				rowIndex = Integer.parseInt( riAbs ) - 1;
			}
			else {
				rowIndexAbsolute = false;
				rowIndex = _relativeTo.rowIndex == BROKEN_REF ? BROKEN_REF :
						_relativeTo.rowIndex + (isNotEmpty( riRel ) ? Integer.parseInt( riRel ) : 0);
			}

			return new CellIndex( spreadsheet, sheetIndex,
					colIndex, colIndexAbsolute, rowIndex, rowIndexAbsolute );
		}

	}

	public static class A1
	{
		private static final String COL_REGEXP = "(\\$?)([A-Z]+)";
		private static final String ROW_REGEXP = "(\\$?)(\\d+)";
		private static final String CELL_REGEXP = COL_REGEXP + ROW_REGEXP;

		private static final String FQ_CELL_REGEXP = "(?:" + SHEET_NAME_REGEXP + "!)?" + CELL_REGEXP;
		private static final Pattern CELL_PATTERN = Pattern.compile(
				"^" + FQ_CELL_REGEXP + "$" );

		public static CellIndex parseCellA1( String _cellRef, CellIndex _relativeTo )
		{
			return parseCellA1( CELL_PATTERN, _cellRef, _relativeTo );
		}

		private static final Pattern ODF_CELL_PATTERN = Pattern.compile(
				"^(?:\\$?" + BROKEN_SHEET_NAME_REGEXP + ")?.(\\$?)([A-Z]+|" + BROKEN_REF_ERR + ")(\\$?)(\\d+|" + BROKEN_REF_ERR + ")$" );

		public static CellIndex parseCellA1ODF( String _cellRef, CellIndex _relativeTo )
		{
			return parseCellA1( ODF_CELL_PATTERN, _cellRef, _relativeTo );
		}

		private static CellIndex parseCellA1( final Pattern _pattern, final String _cellRef, final CellIndex _relativeTo )
		{
			final Matcher matcher = _pattern.matcher( _cellRef );
			if (!matcher.matches()) {
				throw new CellRefParseException( "Invalid A1-style cell reference: " + _cellRef );
			}
			final SpreadsheetImpl spreadsheet = _relativeTo.spreadsheet;
			final int sheetIndex = getSheetIndex( matcher, 1, _relativeTo.sheetIndex, spreadsheet );
			final CellIndex cellIndex = getCellIndex( matcher, 3, sheetIndex, spreadsheet );
			if (cellIndex != null) {
				return cellIndex;
			}
			throw new CellRefParseException.InternalParserError( _cellRef );
		}

		private static CellIndex getCellIndex( Matcher _matcher, int _group, int _sheetIndex, final SpreadsheetImpl _spreadsheet )
		{
			final String ciAbs = _matcher.group( _group );
			final String ci = _matcher.group( _group + 1 );
			final String riAbs = _matcher.group( _group + 2 );
			final String ri = _matcher.group( _group + 3 );

			if (isEmpty( ci ) && isEmpty( ri )) return null;
			if (isEmpty( ci ) || isEmpty( ri ))
				throw new CellRefParseException.InternalParserError( _matcher.group( 0 ) );


			final boolean colIndexAbsolute = "$".equals( ciAbs );
			final boolean rowIndexAbsolute = "$".equals( riAbs );
			final int colIndex = getColIndex( ci );
			final int rowIndex = getRowIndex( ri );
			return new CellIndex( _spreadsheet, _sheetIndex,
					colIndex, colIndexAbsolute, rowIndex, rowIndexAbsolute );
		}

		private static CellRange getColRange( Matcher _matcher, int _group, int _fromSheetIndex, int _toSheetIndex, final SpreadsheetImpl _spreadsheet )
		{
			final String fromAbs = _matcher.group( _group );
			final String from = _matcher.group( _group + 1 );
			final String toAbs = _matcher.group( _group + 2 );
			final String to = _matcher.group( _group + 3 );

			if (isEmpty( from ) && isEmpty( to )) return null;
			if (isEmpty( from ) || isEmpty( to ))
				throw new CellRefParseException.InternalParserError( _matcher.group( 0 ) );

			final boolean fromIndexAbsolute = "$".equals( fromAbs );
			final boolean toIndexAbsolute = "$".equals( toAbs );
			final int fromIndex = getColIndex( from );
			final int toIndex = getColIndex( to );
			final CellIndex fromCell = new CellIndex( _spreadsheet, _fromSheetIndex, fromIndex, fromIndexAbsolute, 0, true );
			final CellIndex toCell = new CellIndex( _spreadsheet, _toSheetIndex, toIndex, toIndexAbsolute, CellIndex.MAX_INDEX, true );
			return CellRange.getCellRange( fromCell, toCell );
		}

		private static CellRange getRowRange( Matcher _matcher, int _group, int _fromSheetIndex, int _toSheetIndex, final SpreadsheetImpl _spreadsheet )
		{
			final String fromAbs = _matcher.group( _group );
			final String from = _matcher.group( _group + 1 );
			final String toAbs = _matcher.group( _group + 2 );
			final String to = _matcher.group( _group + 3 );

			if (isEmpty( from ) && isEmpty( to )) return null;
			if (isEmpty( from ) || isEmpty( to ))
				throw new CellRefParseException.InternalParserError( _matcher.group( 0 ) );

			final boolean fromIndexAbsolute = "$".equals( fromAbs );
			final boolean toIndexAbsolute = "$".equals( toAbs );
			final int fromIndex = getRowIndex( from );
			final int toIndex = getRowIndex( to );
			final CellIndex fromCell = new CellIndex( _spreadsheet, _fromSheetIndex, 0, true, fromIndex, fromIndexAbsolute );
			final CellIndex toCell = new CellIndex( _spreadsheet, _toSheetIndex, CellIndex.MAX_INDEX, true, toIndex, toIndexAbsolute );
			return CellRange.getCellRange( fromCell, toCell );
		}

		private static CellRange getBrokenRange( Matcher _matcher, int _group, int _fromSheetIndex, int _toSheetIndex, final SpreadsheetImpl _spreadsheet )
		{
			if (BROKEN_REF_ERR.equals( _matcher.group( _group ) )) {
				final CellIndex fromCellIndex = new CellIndex( _spreadsheet, _fromSheetIndex, BROKEN_REF, BROKEN_REF );
				if (_fromSheetIndex != _toSheetIndex) {
					final CellIndex toCellIndex = new CellIndex( _spreadsheet, _toSheetIndex, BROKEN_REF, BROKEN_REF );
					return CellRange.getCellRange( fromCellIndex, toCellIndex );
				}
				else {
					return fromCellIndex;
				}
			}
			else {
				return null;
			}
		}

		private static int getColIndex( final String _ci )
		{
			final int len = _ci.length();
			assert len > 0;
			final int colIndex;
			if (BROKEN_REF_ERR.equals( _ci )) {
				colIndex = BROKEN_REF;
			}
			else {
				int i = _ci.charAt( 0 ) - 'A';
				for (int j = 1; j < len; j++) {
					i = 26 * (i + 1) + _ci.charAt( j ) - 'A';
				}
				colIndex = i;
			}
			return colIndex;
		}

		private static int getRowIndex( final String _ri )
		{
			final int rowIndex;
			if (BROKEN_REF_ERR.equals( _ri )) {
				rowIndex = BROKEN_REF;
			}
			else {
				rowIndex = Integer.parseInt( _ri ) - 1;
			}
			return rowIndex;
		}

		private static final String CELL_OR_RANGE_REGEXP = CELL_REGEXP + "(?::" + CELL_REGEXP + ")?";
		private static final String COL_RANGE_REGEXP = COL_REGEXP + ":" + COL_REGEXP;
		private static final String ROW_RANGE_REGEXP = ROW_REGEXP + ":" + ROW_REGEXP;
		private static final Pattern OOXML_RANGE_PATTERN = Pattern.compile(
				"^(?:" + SHEET_NAME_REGEXP + "(?::" + SHEET_NAME_REGEXP + ")?!|(" + BROKEN_REF_ERR + "))?(?:" + CELL_OR_RANGE_REGEXP + "|" + COL_RANGE_REGEXP + "|" + ROW_RANGE_REGEXP + "|(" + BROKEN_REF_ERR + "))$" );

		public static CellRange parseCellRangeA1OOXML( String _range, CellIndex _relativeTo )
		{
			final Matcher matcher = OOXML_RANGE_PATTERN.matcher( _range );
			if (!matcher.matches()) {
				throw new CellRefParseException( "Invalid OOXML A1-style range or cell reference: " + _range );
			}
			final SpreadsheetImpl spreadsheet = _relativeTo.spreadsheet;
			final int fromSheetIndex;
			final int toSheetIndex;
			if (BROKEN_REF_ERR.equals( matcher.group( 5 ) )) {
				fromSheetIndex = BROKEN_REF;
				toSheetIndex = BROKEN_REF;
			}
			else {
				fromSheetIndex = getSheetIndex( matcher, 1, _relativeTo.sheetIndex, spreadsheet );
				toSheetIndex = getSheetIndex( matcher, 3, fromSheetIndex, spreadsheet );
			}
			final CellIndex fromCellIndex = getCellIndex( matcher, 6, fromSheetIndex, spreadsheet );
			if (fromCellIndex != null) {
				final CellIndex toCellIndex = getCellIndex( matcher, 10, toSheetIndex, spreadsheet );
				if (toCellIndex != null) {
					return CellRange.getCellRange( fromCellIndex, toCellIndex );
				}
				else {
					return fromCellIndex;
				}
			}

			final CellRange colRange = getColRange( matcher, 14, fromSheetIndex, toSheetIndex, spreadsheet );
			if (colRange != null) {
				return colRange;
			}

			final CellRange rowRange = getRowRange( matcher, 18, fromSheetIndex, toSheetIndex, spreadsheet );
			if (rowRange != null) {
				return rowRange;
			}

			final CellRange brokenRange = getBrokenRange( matcher, 22, fromSheetIndex, toSheetIndex, spreadsheet );
			if (brokenRange != null) {
				return brokenRange;
			}

			throw new CellRefParseException.InternalParserError( _range );
		}

		private static final Pattern RANGE_PATTERN = Pattern.compile(
				"^(?:" + SHEET_NAME_REGEXP + "(?::" + SHEET_NAME_REGEXP + ")?!)?(?:" + CELL_OR_RANGE_REGEXP + ")$" );

		public static CellRange parseCellRangeA1( String _range, CellIndex _relativeTo )
		{
			final Matcher matcher = RANGE_PATTERN.matcher( _range );
			if (!matcher.matches()) {
				throw new CellRefParseException( "Invalid A1-style range or cell reference: " + _range );
			}
			final SpreadsheetImpl spreadsheet = _relativeTo.spreadsheet;
			final int fromSheetIndex = getSheetIndex( matcher, 1, _relativeTo.sheetIndex, spreadsheet );
			final int toSheetIndex = getSheetIndex( matcher, 3, fromSheetIndex, spreadsheet );
			final CellIndex fromCellIndex = getCellIndex( matcher, 5, fromSheetIndex, spreadsheet );
			if (fromCellIndex != null) {
				final CellIndex toCellIndex = getCellIndex( matcher, 9, toSheetIndex, spreadsheet );
				if (toCellIndex != null) {
					return CellRange.getCellRange( fromCellIndex, toCellIndex );
				}
				else {
					return fromCellIndex;
				}
			}

			throw new CellRefParseException.InternalParserError( _range );
		}
	}

	private static int getSheetIndex( Matcher _matcher, int _group, int _defaultSheetIndex, SpreadsheetImpl _spreadsheet )
	{
		final String nameUnquoted = _matcher.group( _group );
		final String nameQuoted = _matcher.group( _group + 1 );
		final int sheetIndex;
		if (BROKEN_REF_ERR.equals( nameUnquoted )) {
			sheetIndex = BROKEN_REF;
		}
		else {
			final String sheetName = isNotEmpty( nameQuoted ) ? convertQuotes( nameQuoted ) : nameUnquoted;
			sheetIndex = isNotEmpty( sheetName ) ? _spreadsheet.getSheet( sheetName ).getSheetIndex() : _defaultSheetIndex;
		}
		return sheetIndex;
	}

	private static boolean isNotEmpty( String s )
	{
		return s != null && !"".equals( s );
	}

	private static boolean isEmpty( String s )
	{
		return s == null || "".equals( s );
	}

	private static String convertQuotes( String s )
	{
		return s.replace( "''", "'" );
	}

}
