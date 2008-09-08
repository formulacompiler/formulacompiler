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

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.spreadsheet.SpreadsheetException;


public final class CellWithLazilyParsedExpression extends CellWithExpression
{
	private LazyExpressionParser expressionParser;


	public CellWithLazilyParsedExpression( RowImpl _row )
	{
		super( _row );
	}

	@Override
	public ExpressionNode getExpression() throws SpreadsheetException
	{
		final ExpressionNode own = super.getExpression();
		if (null != own) {
			return own;
		}
		else {
			try {
				final ExpressionNode parsed = this.expressionParser.parseExpression( this );
				setExpression( parsed );
				return parsed;
			}
			catch (CompilerException e) {
				final SpreadsheetException.UnsupportedExpression thrown = new SpreadsheetException.UnsupportedExpression( e );
				thrown.addMessageContext( "\nCell containing expression is " + getCanonicalName() + "." );
				throw thrown;
			}
		}
	}

	public LazyExpressionParser getExpressionParser()
	{
		return this.expressionParser;
	}

	public void setExpressionParser( LazyExpressionParser _expressionParser )
	{
		this.expressionParser = _expressionParser;
	}
}
