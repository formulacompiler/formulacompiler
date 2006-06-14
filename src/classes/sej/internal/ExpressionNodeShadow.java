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
package sej.internal;

import java.util.ArrayList;
import java.util.List;

import sej.expressions.ExpressionNode;

public abstract class ExpressionNodeShadow
{
	private final ExpressionNode node;
	private final List<ExpressionNodeShadow> arguments = new ArrayList<ExpressionNodeShadow>();

	public ExpressionNodeShadow(ExpressionNode _node)
	{
		super();
		this.node = _node;
	}

	public ExpressionNode getNode()
	{
		return this.node;
	}

	public List<ExpressionNodeShadow> getArguments()
	{
		return this.arguments;
	}

	public static ExpressionNodeShadow shadow( ExpressionNode _node, Builder _builder )
	{
		final ExpressionNodeShadow result = _builder.shadow( _node );
		final List<ExpressionNodeShadow> resultArgs = result.getArguments();
		for (ExpressionNode argNode : _node.getArguments()) {
			resultArgs.add( shadow( argNode, _builder ) );
		}
		return result;
	}

	public static interface Builder
	{
		ExpressionNodeShadow shadow( ExpressionNode _node );
	}

}
