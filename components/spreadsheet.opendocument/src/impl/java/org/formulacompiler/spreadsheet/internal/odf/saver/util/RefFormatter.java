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

package org.formulacompiler.spreadsheet.internal.odf.saver.util;

import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellRange;

public class RefFormatter
{

	public static String format( CellIndex _cell, CellIndex _baseCell )
	{
		final StringBuilder stringBuilder = new StringBuilder();
		append( stringBuilder, _cell, _baseCell );
		return stringBuilder.toString();
	}

	public static String format( CellRange _range, CellIndex _baseCell )
	{
		final StringBuilder stringBuilder = new StringBuilder();
		append( stringBuilder, _range, _baseCell );
		return stringBuilder.toString();
	}

	static void append( StringBuilder _stringBuilder, CellIndex _cell, CellIndex _baseCell )
	{
		try {
			if (_baseCell == null || _baseCell.getSheetIndex() != _cell.getSheetIndex()) {
				_stringBuilder.append( '$' );
				final String sheetName = _cell.getSheet().getName();
				final boolean quoted = needsQuotes( sheetName );
				if (quoted) _stringBuilder.append( '\'' );
				_stringBuilder.append( sheetName );
				if (quoted) _stringBuilder.append( '\'' );
			}
		} catch (SpreadsheetException.BrokenReference e) {
			_stringBuilder.append( "#REF!" );
		}
		_stringBuilder.append( '.' );
		CellIndex.appendNameA1ForCellIndex( _stringBuilder, _cell );
	}

	private static boolean needsQuotes( String _name )
	{
		for (int i = 0; i < _name.length(); i++) {
			final char ch = _name.charAt( i );
			if (!(Character.isLetterOrDigit( ch ) || ch == '_')) {
				return true;
			}
		}
		return false;
	}

	public static void append( StringBuilder _stringBuilder, CellRange _range, CellIndex _baseCell )
	{
		append( _stringBuilder, _range.getFrom(), _baseCell );
		if (!(_range instanceof CellIndex)) {
			_stringBuilder.append( ':' );
			append( _stringBuilder, _range.getTo(), _range.getFrom() );
		}
	}

}
