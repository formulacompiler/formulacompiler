/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
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
package sej.internal.spreadsheet.loader.excel;

import java.io.IOException;

import sej.SpreadsheetException;
import sej.describable.AbstractDescribable;
import sej.describable.DescriptionBuilder;
import sej.internal.expressions.ExpressionNode;
import sej.internal.spreadsheet.CellInstance;
import sej.internal.spreadsheet.CellWithLazilyParsedExpression;
import sej.internal.spreadsheet.LazyExpressionParser;


public final class ExcelLazyExpressionParser extends AbstractDescribable implements LazyExpressionParser
{
	private CellInstance cell;
	private String expressionText;


	public ExcelLazyExpressionParser(CellWithLazilyParsedExpression _cell, String _expressionText)
	{
		this.cell = _cell;
		this.expressionText = _expressionText;
	}


	public ExpressionNode parseExpression( CellWithLazilyParsedExpression _cell ) throws SpreadsheetException
	{
		return new ExcelExpressionParser( this.cell ).parseText( this.expressionText, _cell.getCellRefFormat() );
	}
	
	
	public String getSource()
	{
		return this.expressionText;
	}


	@Override
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		_to.append( "src=" );
		_to.append( this.expressionText );
	}


}
