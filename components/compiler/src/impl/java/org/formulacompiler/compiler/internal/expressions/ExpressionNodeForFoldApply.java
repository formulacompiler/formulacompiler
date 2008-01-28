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
package org.formulacompiler.compiler.internal.expressions;

import java.util.Collection;
import java.util.Iterator;

import org.formulacompiler.compiler.internal.DescriptionBuilder;

public abstract class ExpressionNodeForFoldApply extends ExpressionNode
{

	protected ExpressionNodeForFoldApply()
	{
		super();
	}

	public ExpressionNodeForFoldApply( ExpressionNode _definition, Collection<ExpressionNode> _args )
	{
		this();
		addArgument( _definition );
		arguments().addAll( _args );
	}

	public ExpressionNodeForFoldApply( ExpressionNode _definition, ExpressionNode... _args )
	{
		this();
		addArgument( _definition );
		addArguments( _args );
	}

	
	public final ExpressionNodeForFoldDefinition fold()
	{
		return (ExpressionNodeForFoldDefinition) argument( 0 );
	}

	public final Iterable<ExpressionNode> elements()
	{
		return new Iterable<ExpressionNode>()
		{
			public Iterator<ExpressionNode> iterator()
			{
				Iterator<ExpressionNode> result = arguments().iterator();
				result.next();
				return result;
			}
		};
	}
	
	protected final void describeElements( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg )
	{
		boolean first = true;
		for (final ExpressionNode element : elements()) {
			if (first) first = false;
			else _to.append( ", " );
			element.describeTo( _to, _cfg );
		}
	}

	
	@Override
	protected int countValuesCore( Collection<ExpressionNode> _uncountables )
	{
		return 1;
	}

	@Override
	protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg )
	{
		_to.append( "apply (" );
		describeArgumentTo( _to, _cfg, 0 );
		_to.append(  ") to " );
	}

}
