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

import java.io.IOException;
import java.util.Collection;

import org.formulacompiler.describable.DescriptionBuilder;

public final class ExpressionNodeForFoldWrapping extends ExpressionNode
{
	private final int[] idxChain;

	private ExpressionNodeForFoldWrapping( int[] _argumentIndexChainToFoldNode )
	{
		super();
		assert _argumentIndexChainToFoldNode.length > 0;
		this.idxChain = _argumentIndexChainToFoldNode.clone();
	}

	public ExpressionNodeForFoldWrapping( ExpressionNode _outer, int... _argumentIndexChainToFoldNode )
	{
		this( _argumentIndexChainToFoldNode );
		addArgument( _outer );
	}


	public ExpressionNodeForFoldDefinition fold()
	{
		ExpressionNode n = argument( 0 );
		for (int i : this.idxChain)
			n = n.argument( i );
		return (ExpressionNodeForFoldDefinition) n;
	}

	public ExpressionNode inject( ExpressionNodeForFoldApply _apply )
	{
		ExpressionNode root = argument( 0 ).clone();
		ExpressionNode parent = null;
		int indexInParent = -1;
		ExpressionNode fold = root;
		for (int i : this.idxChain) {
			parent = fold;
			indexInParent = i;
			fold = parent.argument( i );
		}
		parent.arguments().set( indexInParent, _apply );
		return root;
	}


	@Override
	protected int countValuesCore( Collection<ExpressionNode> _uncountables )
	{
		return 1;
	}

	@Override
	protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg ) throws IOException
	{
		_to.append( "wrap " );
		describeArgumentTo( _to, _cfg, 0 );
	}

	@Override
	protected ExpressionNode innerCloneWithoutArguments()
	{
		throw new IllegalArgumentException( "ExpressionNodeForFoldWrapping cannot be cloned" );
	}

}
