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

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.AbstractDescribable;
import org.formulacompiler.compiler.internal.DescriptionBuilder;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.CellRefFormat;
import org.formulacompiler.spreadsheet.internal.CellWithLazilyParsedExpression;
import org.formulacompiler.spreadsheet.internal.LazyExpressionParser;

public class LazySpreadsheetExpressionParser extends AbstractDescribable implements LazyExpressionParser
{
	private final CellInstance cell;
	private final String expressionText;
	private final CellRefFormat format;


	public LazySpreadsheetExpressionParser( CellWithLazilyParsedExpression _cell, String _expressionText,
			CellRefFormat _format )
	{
		this.cell = _cell;
		this.expressionText = _expressionText;
		this.format = _format;
	}


	public ExpressionNode parseExpression( CellWithLazilyParsedExpression _cell ) throws CompilerException
	{
		return SpreadsheetExpressionParser.newParser( this.expressionText, this.cell, this.format ).parse();
	}


	public String getSource()
	{
		return this.expressionText;
	}


	@Override
	public void describeTo( DescriptionBuilder _to )
	{
		_to.append( "src=" );
		_to.append( this.expressionText );
	}

}
