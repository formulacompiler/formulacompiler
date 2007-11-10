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
package org.formulacompiler.spreadsheet.internal.parser;

import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.parser.Token;
import org.formulacompiler.spreadsheet.internal.CellInstance;

public class SpreadsheetExpressionParserA1 extends SpreadsheetExpressionParser
{

	public SpreadsheetExpressionParserA1( String _exprText, CellInstance _parseRelativeTo )
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
	protected ExpressionNode makeCellR1C1( Token _cell )
	{
		return super.makeCellA1( _cell );
	}

	@Override
	protected ExpressionNode makeCellR1C1( Token _cell, Token _sheet )
	{
		return super.makeCellA1( _cell, _sheet );
	}


}
