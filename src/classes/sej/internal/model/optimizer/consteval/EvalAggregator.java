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
package sej.internal.model.optimizer.consteval;

import sej.Aggregator;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForAggregator;
import sej.internal.model.Aggregation;
import sej.internal.model.ExpressionNodeForPartialAggregation;
import sej.internal.model.util.InterpretedNumericType;

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
		final Aggregator aggregator = ((ExpressionNodeForAggregator) node()).getAggregator();
		final boolean argsDontMatter = aggregator.areArgumentValuesIrrelevant();
		boolean areAllConst = true;
		boolean areSomeConst = false;
		for (Object arg : _args) {
			if ((argsDontMatter && !isInSubSection( arg )) || isConstant( arg )) {
				aggregate( agg, arg );
				areSomeConst = true;
			}
			else if (areAllConst) {
				if (!aggregator.isPartialAggregationSupported()) {
					return node();
				}
				areAllConst = false;
			}
		}
		if (areAllConst) {
			final Object result = resultOf( agg );
			return (null != result) ? result : type().adjustConstantValue( 0 );
		}
		else if (areSomeConst) {
			return newPartialAggregationNode( agg, _args );
		}
		else {
			return node();
		}
	}


	protected abstract Aggregation newAggregation();
	protected abstract void aggregate( Aggregation _agg, Object _value );
	protected abstract Object resultOf( Aggregation _agg );


	protected Object newPartialAggregationNode( final Aggregation _partialAggregation, Object[] _args )
	{
		final Aggregator aggregator = ((ExpressionNodeForAggregator) node()).getAggregator();
		final ExpressionNode result = new ExpressionNodeForPartialAggregation( aggregator, _partialAggregation );
		for (Object arg : _args) {
			if (arg instanceof ExpressionNode) {
				final ExpressionNode argNode = (ExpressionNode) arg;
				result.arguments().add( argNode );
			}
		}
		return result;
	}


	@Override
	protected Object evaluateToConst( Object[] _args )
	{
		throw new IllegalStateException( "Internal error: EvalAggregator.evaluateToConst() should never be called" );
	}


}
