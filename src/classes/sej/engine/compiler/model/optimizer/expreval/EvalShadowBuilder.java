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
package sej.engine.compiler.model.optimizer.expreval;

import sej.engine.compiler.model.ExpressionNodeForCellModel;
import sej.engine.compiler.model.ExpressionNodeForParentSectionModel;
import sej.engine.compiler.model.ExpressionNodeForRangeValue;
import sej.engine.compiler.model.util.InterpretedNumericType;
import sej.expressions.Aggregator;
import sej.expressions.ExpressionNode;
import sej.expressions.ExpressionNodeForAggregator;
import sej.expressions.ExpressionNodeForConstantValue;
import sej.expressions.ExpressionNodeForFunction;
import sej.expressions.ExpressionNodeForOperator;
import sej.expressions.ExpressionNodeShadow;
import sej.expressions.Operator;

public class EvalShadowBuilder implements ExpressionNodeShadow.Builder
{
	private final InterpretedNumericType type;

	public EvalShadowBuilder(InterpretedNumericType _type)
	{
		super();
		this.type = _type;
	}

	public ExpressionNodeShadow shadow( ExpressionNode _node )
	{
		if (_node instanceof ExpressionNodeForConstantValue) return new EvalConstantValue( _node, this.type );
		if (_node instanceof ExpressionNodeForRangeValue) return new EvalRangeValue( _node, this.type );
		if (_node instanceof ExpressionNodeForOperator) return newEvalOperator( (ExpressionNodeForOperator) _node );
		if (_node instanceof ExpressionNodeForFunction) return newEvalFunction( (ExpressionNodeForFunction) _node );
		if (_node instanceof ExpressionNodeForAggregator)
			return newEvalAggregator( (ExpressionNodeForAggregator) _node );
		if (_node instanceof ExpressionNodeForCellModel) return new EvalCell( _node, this.type );
		if (_node instanceof ExpressionNodeForParentSectionModel) return new EvalPassthrough( _node );
		return new EvalNonFoldable( _node );
	}

	private ExpressionNodeShadow newEvalOperator( ExpressionNodeForOperator _node )
	{
		switch (_node.getOperator()) {
			case AND:
				return new EvalAnd( _node, this.type );
			case OR:
				return new EvalOr( _node, this.type );
		}
		return new EvalOperator( _node, this.type );
	}

	private ExpressionNodeShadow newEvalFunction( ExpressionNodeForFunction _node )
	{
		switch (_node.getFunction()) {
			case IF:
				return new EvalIf( _node, this.type );
		}
		return new EvalFunction( _node, this.type );
	}

	private ExpressionNodeShadow newEvalAggregator( ExpressionNodeForAggregator _node )
	{
		final Aggregator aggregator = _node.getAggregator();
		switch (aggregator) {
			case AVERAGE:
				return new EvalAverage( _node, this.type );
		}

		final Operator reductor = aggregator.getReductor();
		if (null != reductor) {
			return new EvalMapReduce( _node, reductor, this.type );
		}

		return new EvalNonFoldable( _node );
	}

}