package sej.engine.compiler.model.optimizer.expreval;

import sej.engine.compiler.model.Aggregation;
import sej.engine.compiler.model.util.InterpretedNumericType;
import sej.expressions.ExpressionNodeForAggregator;
import sej.expressions.Operator;

public class EvalMapReduce extends EvalAggregator
{
	private final Operator reductor;


	public EvalMapReduce(ExpressionNodeForAggregator _node, Operator _reductor, InterpretedNumericType _type)
	{
		super( _node, _type );
		this.reductor = _reductor;
	}


	@Override
	protected Aggregation newAggregation()
	{
		final Aggregation agg = new Aggregation();
		return agg;
	}


	@Override
	protected void aggregate( Aggregation _agg, Object _value )
	{
		if (null != _value) {
			if (null == _agg.accumulator) {
				_agg.accumulator = _value;
			}
			else {
				_agg.accumulator = getType().compute( this.reductor, _agg.accumulator, _value );
			}
		}
	}


	@Override
	protected Object resultOf( Aggregation _agg )
	{
		return _agg.accumulator;
	}

}
