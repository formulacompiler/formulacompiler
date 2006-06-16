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

import java.util.List;

import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.expressions.ExpressionNodeShadow;
import sej.internal.model.util.InterpretedNumericType;

public abstract class EvalShadow extends ExpressionNodeShadow
{
	
	public static Object evaluate( ExpressionNode _expr, InterpretedNumericType _type )
	{
		EvalShadow shadow = (EvalShadow) ExpressionNodeShadow.shadow( _expr, new EvalShadowBuilder( _type ) );
		return shadow.eval();
	}

	
	private final InterpretedNumericType type;

	EvalShadow(ExpressionNode _node, InterpretedNumericType _type)
	{
		super( _node );
		this.type = _type;
	}

	public InterpretedNumericType getType()
	{
		return this.type;
	}


	public Object eval()
	{
		final Object[] argValues = evaluateArguments();
		return evaluateToConstOrExprWithConstantArgsFixed( argValues );
	}


	protected int cardinality()
	{
		return getArguments().size();
	}


	private Object[] evaluateArguments()
	{
		final int card = cardinality();
		final Object[] argValues = new Object[ card ];
		for (int iArg = 0; iArg < card; iArg++) {
			argValues[ iArg ] = evaluateArgument( iArg );
		}
		return argValues;
	}


	protected Object evaluateArgument( int _index )
	{
		final EvalShadow argShadow = (EvalShadow) getArguments().get( _index );
		return argShadow.eval();
	}


	protected Object evaluateToConstOrExprWithConstantArgsFixed( Object[] _args )
	{
		if (hasOnlyConstantArgs( _args )) {
			return evaluateToConst( _args );
		}
		else {
			return nodeWithConstantArgsFixed( _args );
		}
	}


	protected boolean hasOnlyConstantArgs( Object[] _args )
	{
		for (Object arg : _args) {
			if (!isConstant( arg )) return false;
		}
		return true;
	}

	protected boolean isConstant( final Object firstArg )
	{
		return !(firstArg instanceof ExpressionNode);
	}

	protected Object nodeWithConstantArgsFixed( Object[] _args )
	{
		final List<ExpressionNode> argNodes = getNode().getArguments();
		int iArg = 0;
		for (Object arg : _args) {
			if (!(arg instanceof ExpressionNode)) {
				argNodes.set( iArg, new ExpressionNodeForConstantValue( arg ) );
			}
			iArg++;
		}
		return getNode();
	}


	protected abstract Object evaluateToConst( Object[] _args );


}