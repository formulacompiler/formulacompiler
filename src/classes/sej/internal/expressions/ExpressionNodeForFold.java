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

import sej.describable.DescriptionBuilder;

public final class ExpressionNodeForFold extends AbstractExpressionNodeForFold
{

	private ExpressionNodeForFold(String _accumulatorName, String _elementName, boolean _canInlineFirst)
	{
		super( _accumulatorName, _elementName, _canInlineFirst );
	}

	public ExpressionNodeForFold(String _accumulatorName, ExpressionNode _initialAccumulatorValue, String _elementName,
			ExpressionNode _accumulatingStep, boolean _canInlineFirst, ExpressionNode... _elements)
	{
		this( _accumulatorName, _elementName, _canInlineFirst );
		addArgument( _initialAccumulatorValue );
		addArgument( _accumulatingStep );
		addArguments( _elements );
	}

	public ExpressionNodeForFold(String _accumulatorName, ExpressionNode _initialAccumulatorValue, String _elementName,
			ExpressionNode _accumulatingStep, boolean _canInlineFirst, Collection<ExpressionNode> _elements)
	{
		this( _accumulatorName, _elementName, _canInlineFirst );
		addArgument( _initialAccumulatorValue );
		addArgument( _accumulatingStep );
		arguments().addAll( _elements );
	}

	
	public final void neverInlineFirst()
	{
		setCanInlineFirst( false );
	}
	

	@Override
	public ExpressionNode innerCloneWithoutArguments()
	{
		return new ExpressionNodeForFold( accumulatorName(), elementName(), canInlineFirst() );
	}


	@Override
	protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg ) throws IOException
	{
		_to.append( canInlineFirst() ? "_FOLD_1STOK( " : "_FOLD( " ).append( accumulatorName() ).append( ": " );
		initialAccumulatorValue().describeTo( _to, _cfg );
		_to.append( "; " ).append( elementName() ).append( ": " );
		accumulatingStep().describeTo( _to, _cfg );
		_to.append( "; " );
		describeElements( _to, _cfg );
	}

}
