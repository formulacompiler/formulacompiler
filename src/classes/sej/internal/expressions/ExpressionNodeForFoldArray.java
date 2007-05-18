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
package sej.internal.expressions;

import java.io.IOException;

import sej.describable.DescriptionBuilder;

public final class ExpressionNodeForFoldArray extends ExpressionNodeForAbstractFold
{
	private final String indexName;


	private ExpressionNodeForFoldArray(String _accumulatorName, String _elementName, String _indexName)
	{
		super( _accumulatorName, _elementName, false );
		this.indexName = _indexName;
	}

	public ExpressionNodeForFoldArray(String _accumulatorName, ExpressionNode _initialAccumulatorValue,
			String _elementName, String _indexName, ExpressionNode _accumulatingStep, ExpressionNode _array)
	{
		this( _accumulatorName, _elementName, _indexName );
		addArgument( _initialAccumulatorValue );
		addArgument( _accumulatingStep );
		addArgument( _array );
	}


	public final String indexName()
	{
		return this.indexName;
	}

	public final ExpressionNode array()
	{
		return elements().iterator().next();
	}


	@Override
	public ExpressionNode innerCloneWithoutArguments()
	{
		return new ExpressionNodeForFoldArray( accumulatorName(), elementName(), indexName() );
	}


	@Override
	protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg ) throws IOException
	{
		_to.append( "_FOLD_ARRAY( " ).append( accumulatorName() ).append( ": " );
		initialAccumulatorValue().describeTo( _to, _cfg );
		_to.append( "; " ).append( elementName() ).append( ", " ).append( indexName() ).append( ": " );
		accumulatingStep().describeTo( _to, _cfg );
		_to.append( "; " );
		describeElements( _to, _cfg );
	}

}
