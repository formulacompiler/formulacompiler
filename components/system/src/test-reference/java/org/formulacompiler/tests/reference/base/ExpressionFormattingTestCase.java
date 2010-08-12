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

package org.formulacompiler.tests.reference.base;

import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.parser.CellRefFormat;
import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.CellWithExpression;
import org.formulacompiler.spreadsheet.internal.excel.xls.saver.ExpressionFormatter;
import org.formulacompiler.spreadsheet.internal.parser.SpreadsheetExpressionParser;

public class ExpressionFormattingTestCase extends AbstractContextTestCase
{

	public ExpressionFormattingTestCase( Context _cx )
	{
		super( _cx );
	}

	@Override
	protected String getOwnName()
	{
		return "Format and reparse expression";
	}

	@Override
	protected void runTest() throws Throwable
	{
		final CellInstance cell = cx().getOutputCell().getCell();
		if (cell instanceof CellWithExpression) {
			CellWithExpression exprCell = (CellWithExpression) cell;
			final ExpressionNode expr = exprCell.getExpression();
			final String expected = ExpressionFormatter.format( expr, exprCell.getCellIndex() );
			final SpreadsheetExpressionParser parser = SpreadsheetExpressionParser.newParser(
					expected, exprCell.getCellIndex(), CellRefFormat.A1 );
			final ExpressionNode parsed = parser.parse();
			exprCell.setExpression( parsed );
			final String actual = ExpressionFormatter.format( parsed, exprCell.getCellIndex() );
			assertEquals( expected, actual );
		}

	}

}
