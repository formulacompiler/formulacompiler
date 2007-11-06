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
package org.formulacompiler.tests.reference.base;

import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.CellRefFormat;
import org.formulacompiler.spreadsheet.internal.CellWithLazilyParsedExpression;
import org.formulacompiler.spreadsheet.internal.parser.SpreadsheetExpressionParser;
import org.formulacompiler.spreadsheet.internal.saver.excel.xls.ExcelXLSExpressionFormatter;

public class ExpressionFormattingTestCase extends AbstractContextTestCase
{

	public ExpressionFormattingTestCase( Context _cx )
	{
		super( "Format and reparse expression", _cx );
	}

	@Override
	protected void runTest() throws Throwable
	{
		final CellIndex cellIndex = (CellIndex) cx().getOutputCell();
		final CellInstance cell = cellIndex.getCell();
		if (cell instanceof CellWithLazilyParsedExpression) {
			CellWithLazilyParsedExpression exprCell = (CellWithLazilyParsedExpression) cell;
			final ExpressionNode expr = exprCell.getExpression();
			final ExcelXLSExpressionFormatter formatter = new ExcelXLSExpressionFormatter();
			final String expected = formatter.format( expr );
			final SpreadsheetExpressionParser parser = SpreadsheetExpressionParser.newParser( expected, exprCell,
					CellRefFormat.A1 );
			final ExpressionNode parsed = parser.parse();
			final String actual = formatter.format( parsed );
			assertEquals( expected, actual );
		}

	}

}
