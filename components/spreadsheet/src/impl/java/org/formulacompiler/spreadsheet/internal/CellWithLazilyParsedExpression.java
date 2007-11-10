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

import java.io.IOException;

import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.describable.DescriptionBuilder;
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
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		final ExpressionNode expr = this.expression;

		if (_to instanceof SpreadsheetDescriptionBuilder) {
			final SpreadsheetDescriptionBuilder b = (SpreadsheetDescriptionBuilder) _to;
			final CellIndex wasRelativeTo = b.getRelativeTo();
			b.setRelativeTo( getCellIndex() );
			try {
				Object exprValue = expr;
				if (null == exprValue && null != this.expressionParser) {
					try {
						exprValue = getExpression();
					}
					catch (SpreadsheetException e) {
						exprValue = "** " + e.toString();
					}
				}
				_to.vn( "expr" ).append( '=' ).v( exprValue ).lf(); // always shown, so don't use nv()
			}
			finally {
				b.setRelativeTo( wasRelativeTo );
			}
		}
		else {
			_to.nv( "expr", "=", expr ).lf();
			if (null == expr && null != this.expressionParser) {
				_to.nv( "source", "=", this.expressionParser );
			}
		}
		_to.nv( "value", getValue() );
		super.describeTo( _to );
	}


	public CellRefFormat getCellRefFormat()
	{
		return getRow().getSheet().getSpreadsheet().getCellRefFormat();
	}

}
