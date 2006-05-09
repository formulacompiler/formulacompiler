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