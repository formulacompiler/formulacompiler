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
package org.formulacompiler.compiler.internal.expressions;

import static org.formulacompiler.compiler.internal.expressions.DataType.*;

import java.util.Collection;
import java.util.Iterator;

import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.Operator;
import org.formulacompiler.runtime.New;

/**
 * Contains static convenience methods for building expressions.
 * 
 * @author peo
 */
public final class ExpressionBuilder
{
	public static final ExpressionNode ZERO = cst( 0, NUMERIC );
	public static final ExpressionNode ONE = cst( 1, NUMERIC );
	public static final ExpressionNode TWO = cst( 2, NUMERIC );
	public static final ExpressionNode THREE = cst( 3, NUMERIC );
	public static final ExpressionNode EMPTY_STRING = cst( "", STRING );
	public static final ExpressionNode TRUE = cst( Boolean.TRUE, NUMERIC );
	public static final ExpressionNode FALSE = cst( Boolean.FALSE, NUMERIC );


	public static ExpressionNode cst( Object _value )
	{
		return new ExpressionNodeForConstantValue( _value );
	}

	public static ExpressionNode cst( Object _value, DataType _type )
	{
		return new ExpressionNodeForConstantValue( _value, _type );
	}


	public static ExpressionNode op( Operator _op, ExpressionNode... _args )
	{
		return new ExpressionNodeForOperator( _op, _args );
	}

	public static ExpressionNode fun( Function _fun, ExpressionNode... _args )
	{
		return new ExpressionNodeForFunction( _fun, _args );
	}


	public static ExpressionNode let( String _n, ExpressionNode _value, ExpressionNode _in )
	{
		return new ExpressionNodeForLet( _n, _value, _in );
	}

	public static ExpressionNode letByName( String _n, ExpressionNode _value, ExpressionNode _in )
	{
		return new ExpressionNodeForLet( ExpressionNodeForLet.Type.BYNAME, _n, _value, _in );
	}

	public static ExpressionNode letSymbol( String _n, ExpressionNode _value, ExpressionNode _in )
	{
		return new ExpressionNodeForLet( ExpressionNodeForLet.Type.SYMBOLIC, _n, _value, _in );
	}

	public static ExpressionNodeForLetVar var( String _a )
	{
		return new ExpressionNodeForLetVar( _a );
	}


	public static ExpressionNode substitution( ExpressionNode _expr )
	{
		if (_expr instanceof ExpressionNodeForSubstitution) {
			return _expr;
		}
		return new ExpressionNodeForSubstitution( _expr );
	}

	public static ExpressionNode substitution( Collection<ExpressionNode> _exprs )
	{
		if (_exprs.size() == 1) {
			return substitution( _exprs.iterator().next() );
		}
		return new ExpressionNodeForSubstitution( _exprs );
	}

	public static ExpressionNode substitution( Iterator<ExpressionNode> _exprs )
	{
		Collection<ExpressionNode> coll = New.collection();
		while (_exprs.hasNext())
			coll.add( _exprs.next() );
		return substitution( coll );
	}


	public static ExpressionNode[] exprs( ExpressionNode... _exprs )
	{
		return _exprs;
	}


	public static ExpressionNode vector( ExpressionNode... _exprs )
	{
		return vector( (Object[]) _exprs );
	}

	public static ExpressionNode vector( Object... _exprs )
	{
		return matrix( new Object[][] { _exprs } );
	}

	public static ExpressionNode matrix( ExpressionNode[]... _rows )
	{
		return matrix( (Object[][]) _rows );
	}

	public static ExpressionNode matrix( Object[]... _rows )
	{
		final int nrows = _rows.length;
		final int ncols = _rows[ 0 ].length;
		final ArrayDescriptor desc = new ArrayDescriptor( 1, nrows, ncols );
		final ExpressionNode result = new ExpressionNodeForArrayReference( desc );
		for (Object[] row : _rows) {
			for (Object elt : row) {
				if (elt instanceof ExpressionNode) {
					result.addArgument( (ExpressionNode) elt );
				}
				else {
					result.addArgument( cst( elt ) );
				}
			}
		}
		return result;
	}

	
	/**
	 * Not to be instantiated.
	 */
	private ExpressionBuilder()
	{
		super();
	}

}
