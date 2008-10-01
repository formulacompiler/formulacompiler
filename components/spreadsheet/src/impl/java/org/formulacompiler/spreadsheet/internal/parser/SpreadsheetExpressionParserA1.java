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

package org.formulacompiler.spreadsheet.internal.parser;

import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.parser.Token;
import org.formulacompiler.spreadsheet.internal.CellIndex;

public class SpreadsheetExpressionParserA1 extends SpreadsheetExpressionParser
{

	public SpreadsheetExpressionParserA1( String _exprText, CellIndex _parseRelativeTo )
	{
		super( _exprText, _parseRelativeTo );
	}


	/*
	 * The following is a workaround for a parser problem. Since the A1 parser also has the
	 * R1C1-tokenizer in it, it will tokenize and parse the string "RC1" as a R1C1 reference.
	 * However, in A1 mode this is a valid cell reference meaning something else entirely. In
	 * A1-style, "RC1" is row 1, column <some big number> (much like "AA1").
	 * 
	 * In R1C1-style, "RC1", when in a formula for cell B4, is equivalent to $A4 in A1-style. It
	 * means same row, fixed column 1.
	 */

	@Override
	protected ExpressionNode makeCellR1C1( Token _cell, ExpressionNode _node )
	{
		return super.makeCellA1( _cell, _node );
	}


}
