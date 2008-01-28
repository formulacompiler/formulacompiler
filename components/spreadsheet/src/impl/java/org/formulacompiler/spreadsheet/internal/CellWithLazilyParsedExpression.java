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
