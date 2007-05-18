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
package sej.internal.model.rewriting;

import java.util.List;

import sej.compiler.CompilerException;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForSubstitution;
import sej.internal.model.AbstractComputationModelVisitor;
import sej.internal.model.CellModel;
import sej.runtime.New;


public final class SubstitutionInliner extends AbstractComputationModelVisitor
{

	public SubstitutionInliner()
	{
		super();
	}


	@Override
	protected boolean visitCell( CellModel _cell ) throws CompilerException
	{
		final ExpressionNode expr = _cell.getExpression();
		if (null != expr) {
			inline( expr );
		}
		return true;
	}


	private void inline( ExpressionNode _expr )
	{
		if (null != _expr) {
			_expr.replaceArguments( inline( _expr.arguments() ) );
		}
	}


	private List<ExpressionNode> inline( List<ExpressionNode> _list )
	{
		List<ExpressionNode> result = New.newList();
		for (final ExpressionNode node : _list) {
			inline( node, result );
		}
		return result;
	}


	private void inline( ExpressionNode _node, List<ExpressionNode> _result )
	{
		if (_node instanceof ExpressionNodeForSubstitution) {
			for (final ExpressionNode elt : _node.arguments()) {
				inline( elt, _result );
			}
		}
		else {
			inline( _node );
			_result.add( _node );
		}
	}


}
