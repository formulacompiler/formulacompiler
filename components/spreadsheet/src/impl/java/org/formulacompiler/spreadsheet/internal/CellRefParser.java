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

import org.formulacompiler.runtime.New;

public abstract class CellRefParser
{
	private static final Map<CellRefFormat, CellRefParser> PARSERS = New.map();

	static {
		PARSERS.put( CellRefFormat.A1, new CellRefParserA1() );
		PARSERS.put( CellRefFormat.A1_ODF, new CellRefParserA1() );
		PARSERS.put( CellRefFormat.R1C1, new CellRefParserR1C1() );
	}

	public static CellRefParser getInstance( CellRefFormat _format )
	{
		return PARSERS.get( _format );
	}

	public abstract CellIndex getCellIndexForCanonicalName( String _canonicalName, SheetImpl _sheet,
			CellIndex _relativeTo );

	private static class CellRefParserA1 extends CellRefParser
	{
		@Override
		public CellIndex getCellIndexForCanonicalName( String _canonicalName, SheetImpl _sheet, CellIndex _relativeTo )
		{
			int colIndex = 0;
			int rowIndex = 0;
			for (int iCh = 0; iCh < _canonicalName.length(); iCh++) {
				char ch = _canonicalName.charAt( iCh );
				if ((ch >= 'A') && (ch <= 'Z')) {
					colIndex = 26 * colIndex + Character.getNumericValue( ch ) - 9;
				}
				else if ((ch >= '0') && (ch <= '9')) {
					rowIndex = 10 * rowIndex + Character.getNumericValue( ch );
				}
			}
			return new CellIndex( _sheet.getSpreadsheet(), _sheet.getSheetIndex(), colIndex - 1, rowIndex - 1 );
		}
	}

	private static class CellRefParserR1C1 extends CellRefParser
	{
		@Override
		public CellIndex getCellIndexForCanonicalName( String _canonicalName, SheetImpl _sheet, CellIndex _relativeTo )
		{
			final int rowIndex = parseRCIndex( _relativeTo.rowIndex + 1, _canonicalName, 1 );
			final int colIndex = parseRCIndex( _relativeTo.columnIndex + 1, _canonicalName,
					_canonicalName.indexOf( 'C' ) + 1 );
			return new CellIndex( _sheet.getSpreadsheet(), _sheet.getSheetIndex(), colIndex - 1, rowIndex - 1 );
		}

		private static int parseRCIndex( int _relativeTo, String _canonicalName, int _at )
		{
			int result = _relativeTo;
			int at = _at;
			if (at < _canonicalName.length()) {
				char ch = _canonicalName.charAt( at );
				if ('[' == ch) {
					ch = _canonicalName.charAt( ++at );
				}
				else if ('C' != ch) {
					result = 0;
				}
				StringBuilder sb = new StringBuilder();
				while (ch == '-' || (ch >= '0' && ch <= '9')) {
					sb.append( ch );
					if (++at >= _canonicalName.length()) break;
					ch = _canonicalName.charAt( at );
				}
				if (sb.length() > 0) {
					result += Integer.parseInt( sb.toString() );
				}
			}
			return result;
		}
	}
}
