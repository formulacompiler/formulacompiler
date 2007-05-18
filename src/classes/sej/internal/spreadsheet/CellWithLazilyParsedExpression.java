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
package sej.internal.spreadsheet;

import java.io.IOException;

import sej.describable.Describable;
import sej.describable.DescriptionBuilder;
import sej.internal.expressions.ExpressionNode;
import sej.spreadsheet.SpreadsheetException;


public final class CellWithLazilyParsedExpression extends CellInstance
{
	private ExpressionNode expression;
	private LazyExpressionParser expressionParser;


	public CellWithLazilyParsedExpression(RowImpl _row)
	{
		super( _row );
	}


	public CellWithLazilyParsedExpression(RowImpl _row, ExpressionNode _expression)
	{
		super( _row );
		setExpression( _expression );
	}


	@Override
	public Object getValue()
	{
		return null;
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
	protected void describeDefinitionTo( DescriptionBuilder _to ) throws IOException
	{
		_to.append( "<expr>" );
		if (null != this.expression) {
			this.expression.describeTo( _to );
		}
		else if (this.expressionParser instanceof Describable) {
			((Describable) this.expressionParser).describeTo( _to );
		}
		_to.append( "</expr>" );
		_to.newLine();
	}


	public CellRefFormat getCellRefFormat()
	{
		return getRow().getSheet().getSpreadsheet().getCellRefFormat();
	}

}
