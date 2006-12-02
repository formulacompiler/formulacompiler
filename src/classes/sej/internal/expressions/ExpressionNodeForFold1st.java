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
package sej.internal.expressions;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import sej.describable.DescriptionBuilder;

public class ExpressionNodeForFold1st extends ExpressionNodeForAbstractFold
{
	private final String firstName;

	private ExpressionNodeForFold1st(String _firstName, String _accumulatorName, String _elementName)
	{
		super( _accumulatorName, _elementName, true );
		this.firstName = _firstName;
	}

	public ExpressionNodeForFold1st(String _firstName, ExpressionNode _firstValue, String _accumulatorName,
			String _elementName, ExpressionNode _accumulatingStep, ExpressionNode _emptyValue, ExpressionNode... _elements)
	{
		this( _firstName, _accumulatorName, _elementName );
		addArgument( _emptyValue );
		addArgument( _accumulatingStep );
		addArgument( _firstValue );
		addArguments( _elements );
	}

	public ExpressionNodeForFold1st(String _firstName, ExpressionNode _firstValue, String _accumulatorName,
			String _elementName, ExpressionNode _accumulatingStep, ExpressionNode _emptyValue,
			Collection<ExpressionNode> _elements)
	{
		this( _firstName, _accumulatorName, _elementName );
		addArgument( _emptyValue );
		addArgument( _accumulatingStep );
		addArgument( _firstValue );
		arguments().addAll( _elements );
	}


	public final String firstName()
	{
		return this.firstName;
	}

	public final ExpressionNode firstValue()
	{
		return argument( 2 );
	}

	public final ExpressionNode emptyValue()
	{
		return initialAccumulatorValue();
	}


	@Override
	protected void skipToElements( Iterator<ExpressionNode> _iterator )
	{
		super.skipToElements( _iterator );
		_iterator.next();
	}


	@Override
	public ExpressionNode innerCloneWithoutArguments()
	{
		return new ExpressionNodeForFold1st( this.firstName, accumulatorName(), elementName() );
	}

	@Override
	protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg ) throws IOException
	{
		_to.append( "_FOLD_1ST( " ).append( firstName() ).append( ": " );
		firstValue().describeTo( _to, _cfg );
		_to.append( "; " ).append( accumulatorName() ).append( ' ' ).append( elementName() ).append( ": " );
		accumulatingStep().describeTo( _to, _cfg );
		_to.append( "; " );
		emptyValue().describeTo( _to, _cfg );
		_to.append( "; " );
		describeElements( _to, _cfg );
	}

}
