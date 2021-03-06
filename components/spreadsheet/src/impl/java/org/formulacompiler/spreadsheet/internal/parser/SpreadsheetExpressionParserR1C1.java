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

package org.formulacompiler.spreadsheet.internal.parser;

import org.formulacompiler.compiler.internal.expressions.parser.CellRefFormat;
import org.formulacompiler.compiler.internal.expressions.parser.Token;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellRefParser;

public class SpreadsheetExpressionParserR1C1 extends SpreadsheetExpressionParser
{

	public SpreadsheetExpressionParserR1C1( String _exprText, CellIndex _parseRelativeTo )
	{
		super( _exprText, _parseRelativeTo, CellRefFormat.R1C1 );
	}

	@Override
	protected final Object makeCell( Token _cell, Object _baseCell )
	{
		final CellIndex relativeTo = _baseCell != null ? (CellIndex) _baseCell : this.cellIndex;
		return CellRefParser.R1C1.getCellIndexForCanonicalName( _cell.image, relativeTo );
	}

	@Override
	protected Object makeCellRange( final Token _range )
	{
		throw new UnsupportedOperationException();
	}

}
