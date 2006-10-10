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
package sej.internal.model.rewriting;

import java.util.Collection;
import java.util.List;

import sej.Aggregator;
import sej.Operator;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForAggregator;
import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.expressions.ExpressionNodeForFold;
import sej.internal.expressions.ExpressionNodeForLet;
import sej.internal.expressions.ExpressionNodeForLetVar;
import sej.internal.expressions.ExpressionNodeForOperator;
import sej.internal.model.ExpressionNodeForSubExpr;

public final class ExpressionRewriter
{

	public ExpressionRewriter()
	{
		super();
	}


	public final ExpressionNode rewrite( ExpressionNode _expr )
	{
		ExpressionNode result = _expr;
		if (_expr instanceof ExpressionNodeForAggregator) {
			result = rewriteAgg( (ExpressionNodeForAggregator) _expr );
		}
		return rewriteArgsOf( result );
	}


	private ExpressionNode rewriteArgsOf( ExpressionNode _expr )
	{
		final List<ExpressionNode> args = _expr.arguments();
		for (int iArg = 0; iArg < args.size(); iArg++) {
			final ExpressionNode arg = args.get( iArg );
			final ExpressionNode rewritten = rewrite( arg );
			if (rewritten != arg) {
				args.set( iArg, rewritten );
			}
		}
		return _expr;
	}


	private ExpressionNode rewriteAgg( ExpressionNodeForAggregator _agg )
	{
		switch (_agg.getAggregator()) {
			case SUM:
				return rewriteSUM( _agg );
			case AVERAGE:
				return rewriteAVERAGE( _agg );
			case VARP:
				return rewriteVARP( _agg );
		}
		return _agg;
	}


	private ExpressionNode rewriteSUM( ExpressionNodeForAggregator _agg )
	{
		// °FOLD( acc: 0; xi: acc + xi; xs )
		final ExpressionNode args = subExpr( _agg.arguments() );
		final ExpressionNode acc = op( Operator.PLUS, "acc", "xi" );
		return new ExpressionNodeForFold( "acc", cst( 0 ), "xi", acc, args );
	}


	private ExpressionNode rewriteAVERAGE( ExpressionNodeForAggregator _agg )
	{
		// SUM(xs) / COUNT(xs)
		final ExpressionNode args = subExpr( _agg.arguments() );
		final ExpressionNode count = new ExpressionNodeForAggregator( Aggregator.COUNT, args );
		return rewriteAVERAGEgivenN( args, count );
	}


	private ExpressionNode rewriteAVERAGEgivenN( ExpressionNode _args, ExpressionNode _n )
	{
		// SUM(xs) / n
		final ExpressionNode sum = new ExpressionNodeForAggregator( Aggregator.SUM, _args );
		return new ExpressionNodeForOperator( Operator.DIV, sum, _n );
	}


	private ExpressionNode rewriteVARP( ExpressionNodeForAggregator _agg )
	{
		// °LET( c: COUNT(xs); °LET( m: SUM(xs) / c; °FOLD( acc: 0; xi: °LET( ei: xi - m; acc + ei*ei
		// );
		// xs ) / c )
		final ExpressionNode args = subExpr( _agg.arguments() );
		final ExpressionNode avg = rewriteAVERAGEgivenN( args, var( "c" ) );
		final ExpressionNode ei = op( Operator.MINUS, "xi", "m" );
		final ExpressionNode acc = op( Operator.PLUS, var( "acc" ), op( Operator.TIMES, "ei", "ei" ) );
		final ExpressionNode let_ei = new ExpressionNodeForLet( "ei", ei, acc );
		final ExpressionNode fold = new ExpressionNodeForFold( "acc", cst( 0 ), "xi", let_ei, args );
		final ExpressionNodeForLet let_m = new ExpressionNodeForLet( "m", avg, fold );
		final ExpressionNode count = new ExpressionNodeForAggregator( Aggregator.COUNT, args );
		final ExpressionNodeForLet let_c = new ExpressionNodeForLet( "c", count, op( Operator.DIV, let_m, var( "c" ) ) );
		return let_c;
	}


	private ExpressionNode subExpr( ExpressionNode _expr )
	{
		if (_expr instanceof ExpressionNodeForSubExpr) {
			return _expr;
		}
		return new ExpressionNodeForSubExpr( _expr );
	}

	private ExpressionNode subExpr( Collection<ExpressionNode> _exprs )
	{
		if (_exprs.size() == 1) {
			return subExpr( _exprs.iterator().next() );
		}
		return new ExpressionNodeForSubExpr( _exprs );
	}


	private ExpressionNodeForOperator op( Operator _op, String _a, String _b )
	{
		return new ExpressionNodeForOperator( _op, var( _a ), var( _b ) );
	}

	private ExpressionNodeForOperator op( Operator _op, ExpressionNode _a, ExpressionNode _b )
	{
		return new ExpressionNodeForOperator( _op, _a, _b );
	}

	private ExpressionNodeForLetVar var( String _a )
	{
		return new ExpressionNodeForLetVar( _a );
	}

	private ExpressionNodeForConstantValue cst( final int _value )
	{
		return new ExpressionNodeForConstantValue( _value );
	}


}
