package sej.engine.compiler.model.optimizer.expreval;

import sej.engine.compiler.model.Aggregation;
import sej.engine.compiler.model.ExpressionNodeForPartialAggregation;
import sej.engine.compiler.model.util.InterpretedNumericType;
import sej.expressions.Aggregator;
import sej.expressions.ExpressionNode;
import sej.expressions.ExpressionNodeForAggregator;

public abstract class EvalAggregator extends EvalShadow
{

	EvalAggregator(ExpressionNode _node, InterpretedNumericType _type)
	{
		super( _node, _type );
	}

	
	@Override
	protected Object evaluateToConstOrExprWithConstantArgsFixed( Object[] _args )
	{
		final Aggregation agg = newAggregation();
		boolean areAllConst = true;
		boolean areSomeConst = false;
		for (Object arg : _args) {
			if (isConstant( arg )) {
				aggregate( agg, arg );
				areSomeConst = true;
			}
			else if (areAllConst) {
				final Aggregator aggregator = ((ExpressionNodeForAggregator) getNode()).getAggregator();
				if (!aggregator.isPartialAggregationSupported()) {
					return getNode();
				}
				areAllConst = false;
			}
		}
		if (areAllConst) {
			final Object result = resultOf( agg );
			return (null != result) ? result : getType().adjustConstantValue( 0 );
		}
		else if (areSomeConst) {
			return newPartialAggregationNode( agg, _args );
		}
		else {
			return getNode();
		}
	}


	protected abstract Aggregation newAggregation();
	protected abstract void aggregate( Aggregation _agg, Object _value );
	protected abstract Object resultOf( Aggregation _agg );


	protected Object newPartialAggregationNode( final Aggregation _partialAggregation, Object[] _args )
	{
		final Aggregator aggregator = ((ExpressionNodeForAggregator) getNode()).getAggregator();
		final ExpressionNode result = new ExpressionNodeForPartialAggregation( aggregator, _partialAggregation );
		for (Object arg : _args) {
			if (arg instanceof ExpressionNode) {
				final ExpressionNode argNode = (ExpressionNode) arg;
				result.getArguments().add( argNode );
			}
		}
		return result;
	}

	
	@Override
	protected Object evaluateToConst( Object[] _args )
	{
		throw new IllegalStateException( "Should never be called" );
	}


}
