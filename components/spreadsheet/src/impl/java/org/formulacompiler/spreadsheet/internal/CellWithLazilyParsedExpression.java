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

import org.formulacompiler.compiler.internal.DescriptionBuilder;
import org.formulacompiler.compiler.internal.YamlBuilder;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.spreadsheet.SpreadsheetException;


public final class CellWithLazilyParsedExpression extends CellInstance
{
	private ExpressionNode expression;
	private LazyExpressionParser expressionParser;


	public CellWithLazilyParsedExpression( RowImpl _row )
	{
		super( _row );
	}


	public CellWithLazilyParsedExpression( RowImpl _row, ExpressionNode _expression )
	{
		super( _row );
		setExpression( _expression );
	}


	@Override
	public synchronized ExpressionNode getExpression() throws SpreadsheetException
	{
		final ExpressionNode own = this.expression;
		if (null != own) {
			return own;
		}
		else {
			try {
				final ExpressionNode parsed = this.expressionParser.parseExpression( this );
				setExpression( parsed );
				return parsed;
			}
			catch (Throwable e) {
				final SpreadsheetException.UnsupportedExpression thrown = new SpreadsheetException.UnsupportedExpression( e );
				thrown.addMessageContext( "\nCell containing expression is " + getCanonicalName() + "." );
				throw thrown;
			}
		}
	}


	public void setExpression( ExpressionNode _value )
	{
		this.expression = _value;
		if (null != _value) {
			_value.setContextProviderOnThisAndArgumentsRecursively( new CellExpressionContextProvider( this, _value ) );
		}
		/*
		 * We don't free the expression parser here because the reference tests need access to the
		 * original expression text. And freeing it would yield minimal benefits, it seems.
		 */
	}


	public LazyExpressionParser getExpressionParser()
	{
		return this.expressionParser;
	}


	public void setExpressionParser( LazyExpressionParser _expressionParser )
	{
		this.expressionParser = _expressionParser;
	}


	@Override
	public void describeTo( DescriptionBuilder _to )
	{
		final ExpressionNode expr = this.expression;
		if (null != expr) {
			_to.append( expr );
		}
		else if (null != this.expressionParser) {
			/*
			 * Avoid the cost and side effects of parsing when we are simply inspecting this one cell.
			 */
			_to.append( this.expressionParser );
		}
		final Object v = getValue();
		if (null != v) {
			_to.append( " (value=" ).append( v ).append( ")" );
		}
	}


	@Override
	public void yamlTo( YamlBuilder _to )
	{
		_to.desc().pushContext( new DescribeR1C1Style( getCellIndex() ) );
		try {
			final ExpressionNode expr = this.expression;
			Object exprValue = expr;
			if (null == exprValue && null != this.expressionParser) {
				try {
					exprValue = getExpression();
				}
				catch (SpreadsheetException e) {
					final String exDesc = e.toString();
					exprValue = "** " + exDesc.replace( "\r\n", "\n" );
				}
			}
			_to.vn( "expr" ).s( '=' ).v( exprValue ).lf(); // always shown, so don't use nv()
		}
		finally {
			_to.desc().popContext();
		}
		_to.nv( "value", getValue() );
		super.yamlTo( _to );
	}

}
