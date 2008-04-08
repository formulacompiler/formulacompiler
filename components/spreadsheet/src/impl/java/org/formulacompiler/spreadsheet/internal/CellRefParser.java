/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.Spreadsheet;

public abstract class CellRefParser
{
	private static final String SHEET_NAME_REGEXP = "(?:(\\w+)|'((?:''|[^'])+)')";

	private static final Map<CellRefFormat, CellRefParser> PARSERS = New.map();

	static {
		PARSERS.put( CellRefFormat.A1, new CellRefParserA1() );
		PARSERS.put( CellRefFormat.A1_ODF, new CellRefParserA1ODF() );
		PARSERS.put( CellRefFormat.R1C1, new CellRefParserR1C1() );
	}

	public static CellRefParser getInstance( CellRefFormat _format )
	{
		return PARSERS.get( _format );
	}

	public abstract CellIndex getCellIndexForCanonicalName( String _canonicalName, CellIndex _relativeTo );

	protected int getSheetIndex( final CellIndex _relativeTo, final String _nameUnquoted, final String _nameQuoted, final SpreadsheetImpl _spreadsheet )
	{
		final int sheetIndex;
		if ("#REF!".equals( _nameUnquoted )) {
			sheetIndex = CellIndex.BROKEN_REF;
		}
		else {
			final String sheetName = isNotEmpty( _nameQuoted ) ? convertQuotes( _nameQuoted ) : _nameUnquoted;
			final Spreadsheet.Sheet sheet = isNotEmpty( sheetName ) ? _spreadsheet.getSheet( sheetName ) : _relativeTo.getSheet();
			sheetIndex = sheet.getSheetIndex();
		}
		return sheetIndex;
	}

	private static boolean isNotEmpty( String s )
	{
		return s != null && !"".equals( s );
	}

	private static String convertQuotes( String s )
	{
		return s.replace( "''", "'" );
	}

	private static class CellRefParserA1 extends CellRefParser
	{
		private static final String A1_CELL_REGEXP = "(\\$?)([A-Z]+)(\\$?)(\\d+)";

		private static final Pattern PATTERN = Pattern.compile(
				"^(?:" + SHEET_NAME_REGEXP + "!)?" + A1_CELL_REGEXP + "$" );

		protected Pattern getPattern()
		{
			return PATTERN;
		}

		@Override
		public CellIndex getCellIndexForCanonicalName( String _canonicalName, CellIndex _relativeTo )
		{
			final Matcher matcher = getPattern().matcher( _canonicalName );
			if (!matcher.matches()) {
				throw new CellRefParseException( "Invalid A1-style cell reference: " + _canonicalName );
			}
			final String nameUnquoted = matcher.group( 1 );
			final String nameQuoted = matcher.group( 2 );
			final String ciAbs = matcher.group( 3 );
			final String ci = matcher.group( 4 );
			final String riAbs = matcher.group( 5 );
			final String ri = matcher.group( 6 );

			final SpreadsheetImpl spreadsheet = _relativeTo.spreadsheet;
			final int sheetIndex = getSheetIndex( _relativeTo, nameUnquoted, nameQuoted, spreadsheet );

			final boolean colIndexAbsolute = "$".equals( ciAbs );
			final boolean rowIndexAbsolute = "$".equals( riAbs );
			final int colIndex = getColIndex( ci );
			final int rowIndex = getRowIndex( ri );
			return new CellIndex( spreadsheet, sheetIndex,
					colIndex, colIndexAbsolute, rowIndex, rowIndexAbsolute );
		}

		protected int getColIndex( final String _ci )
		{
			int colIndex = _ci.charAt( 0 ) - ('A' - 1);
			if (_ci.length() > 1) {
				colIndex = 26 * colIndex + _ci.charAt( 1 ) - ('A' - 1);
			}
			return colIndex - 1;
		}

		protected int getRowIndex( final String _ri )
		{
			return Integer.parseInt( _ri ) - 1;
		}

	}

	private static class CellRefParserA1ODF extends CellRefParserA1
	{
		private static final String SHEET_NAME_REGEXP = "(?:(\\w+|#REF!)|'((?:''|[^'])+)')";
		private static final String A1_CELL_REGEXP = "(\\$?)([A-Z]+|#REF!)(\\$?)(\\d+|#REF!)";

		private static final Pattern PATTERN = Pattern.compile(
				"^(?:\\$?" + SHEET_NAME_REGEXP + ")?." + A1_CELL_REGEXP + "$" );

		@Override
		protected Pattern getPattern()
		{
			return PATTERN;
		}

		@Override
		protected int getColIndex( final String _ci )
		{
			final int colIndex;
			if ("#REF!".equals( _ci )) {
				colIndex = CellIndex.BROKEN_REF;
			}
			else {
				colIndex = super.getColIndex( _ci );
			}
			return colIndex;
		}

		@Override
		protected int getRowIndex( final String _ri )
		{
			final int rowIndex;
			if ("#REF!".equals( _ri )) {
				rowIndex = CellIndex.BROKEN_REF;
			}
			else {
				rowIndex = super.getRowIndex( _ri );
			}
			return rowIndex;
		}

	}

	private static class CellRefParserR1C1 extends CellRefParser
	{
		private static final String INDEX_REGEXP = "(?:(-?\\d+)|\\[(-?\\d+)\\])?";

		private static final Pattern PATTERN = Pattern.compile(
				"^(?:" + SHEET_NAME_REGEXP + "!)?R" + INDEX_REGEXP + "C" + INDEX_REGEXP + "$" );

		@Override
		public CellIndex getCellIndexForCanonicalName( String _canonicalName, CellIndex _relativeTo )
		{
			final Matcher matcher = PATTERN.matcher( _canonicalName );
			if (!matcher.matches()) {
				throw new CellRefParseException( "Invalid R1C1-style cell reference: " + _canonicalName );
			}
			final String nameUnquoted = matcher.group( 1 );
			final String nameQuoted = matcher.group( 2 );
			final String riAbs = matcher.group( 3 );
			final String riRel = matcher.group( 4 );
			final String ciAbs = matcher.group( 5 );
			final String ciRel = matcher.group( 6 );

			final SpreadsheetImpl spreadsheet = _relativeTo.getSheet().getSpreadsheet();
			final int sheetIndex = getSheetIndex( _relativeTo, nameUnquoted, nameQuoted, spreadsheet );

			final int colIndex;
			final boolean colIndexAbsolute;
			if (isNotEmpty( ciAbs )) {
				colIndexAbsolute = true;
				colIndex = Integer.parseInt( ciAbs ) - 1;
			}
			else {
				colIndexAbsolute = false;
				colIndex = _relativeTo.columnIndex == CellIndex.BROKEN_REF ? CellIndex.BROKEN_REF :
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
				rowIndex = _relativeTo.rowIndex == CellIndex.BROKEN_REF ? CellIndex.BROKEN_REF :
						_relativeTo.rowIndex + (isNotEmpty( riRel ) ? Integer.parseInt( riRel ) : 0);
			}

			return new CellIndex( spreadsheet, sheetIndex,
					colIndex, colIndexAbsolute, rowIndex, rowIndexAbsolute );
		}

	}

}
