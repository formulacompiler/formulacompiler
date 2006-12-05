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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import sej.Function;
import sej.Operator;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.expressions.ExpressionNodeForFold;
import sej.internal.expressions.ExpressionNodeForReduce;
import sej.internal.expressions.ExpressionNodeForFoldArray;
import sej.internal.expressions.ExpressionNodeForFunction;
import sej.internal.expressions.ExpressionNodeForLet;
import sej.internal.expressions.ExpressionNodeForLetVar;
import sej.internal.expressions.ExpressionNodeForOperator;
import sej.internal.expressions.ExpressionNodeForSubstitution;

abstract class AbstractExpressionRewriter
{


	protected final ExpressionNode substitution( ExpressionNode _expr )
	{
		if (_expr instanceof ExpressionNodeForSubstitution) {
			return _expr;
		}
		return new ExpressionNodeForSubstitution( _expr );
	}

	protected final ExpressionNode substitution( Collection<ExpressionNode> _exprs )
	{
		if (_exprs.size() == 1) {
			return substitution( _exprs.iterator().next() );
		}
		return new ExpressionNodeForSubstitution( _exprs );
	}

	protected final ExpressionNode substitution( Iterator<ExpressionNode> _exprs )
	{
		Collection<ExpressionNode> coll = new ArrayList<ExpressionNode>();
		while (_exprs.hasNext())
			coll.add( _exprs.next() );
		return substitution( coll );
	}


	protected final ExpressionNodeForOperator op( Operator _op, String _a, String _b )
	{
		return new ExpressionNodeForOperator( _op, var( _a ), var( _b ) );
	}


	protected final ExpressionNodeForOperator op( Operator _op, ExpressionNode... _args )
	{
		return new ExpressionNodeForOperator( _op, _args );
	}


	protected final ExpressionNodeForFunction fun( Function _fun, ExpressionNode... _args )
	{
		return new ExpressionNodeForFunction( _fun, _args );
	}


	protected final ExpressionNode fold( String _acc, ExpressionNode _init, String _x, ExpressionNode _fold,
			boolean _canInlineFirst, ExpressionNode _xs )
	{
		return new ExpressionNodeForFold( _acc, _init, _x, _fold, _canInlineFirst, _xs );
	}


	protected final ExpressionNode reduce( String _acc, String _xi, ExpressionNode _reduce, ExpressionNode _empty,
			ExpressionNode _xs )
	{
		return new ExpressionNodeForReduce( _acc, _xi, _reduce, _empty, _xs );
	}


	protected final ExpressionNode folda( String _acc, ExpressionNode _init, String _x, String _i, ExpressionNode _fold,
			ExpressionNode _xs )
	{
		return new ExpressionNodeForFoldArray( _acc, _init, _x, _i, _fold, _xs );
	}


	protected final ExpressionNode let( String _n, ExpressionNode _value, ExpressionNode _in )
	{
		return new ExpressionNodeForLet( _n, _value, _in );
	}


	protected final ExpressionNodeForLetVar var( String _a )
	{
		return new ExpressionNodeForLetVar( _a );
	}


	protected final ExpressionNodeForConstantValue cst( Object _value )
	{
		return new ExpressionNodeForConstantValue( _value );
	}


}
