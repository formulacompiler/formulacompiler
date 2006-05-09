package sej.engine.compiler.model.optimizer.expreval;

import sej.engine.compiler.model.Aggregation;
import sej.engine.compiler.model.Aggregation.NonNullCountingAggregation;
import sej.engine.compiler.model.util.InterpretedNumericType;
import sej.expressions.ExpressionNode;
import sej.expressions.Operator;

public class EvalAverage extends EvalAggregator
{

	public EvalAverage(ExpressionNode _node, InterpretedNumericType _type)
	{
		super( _node, _type );
	}

	@Override
	protected Aggregation newAggregation()
	{
		final Aggregation.NonNullCountingAggregation result = new Aggregation.NonNullCountingAggregation();
		result.accumulator = getType().adjustConstantValue( 0 );
		return result;
	}

	@Override
	protected void aggregate( Aggregation _agg, Object _value )
	{
		final NonNullCountingAggregation agg = (NonNullCountingAggregation) _agg;
		if (null != _value) {
			agg.accumulator = getType().compute( Operator.PLUS, agg.accumulator, _value );
			agg.numberOfNonNullArguments++;
		}
	}

	@Override
	protected Object resultOf( Aggregation _agg )
	{
		final NonNullCountingAggregation agg = (NonNullCountingAggregation) _agg;
		return getType().compute( Operator.DIV, agg.accumulator,
				getType().adjustConstantValue( agg.numberOfNonNullArguments ) );
	}

}
