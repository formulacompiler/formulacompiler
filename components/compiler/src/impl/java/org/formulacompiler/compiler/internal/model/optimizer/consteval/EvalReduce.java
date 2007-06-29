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
package org.formulacompiler.compiler.internal.model.optimizer.consteval;

import java.util.Collection;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFold;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForReduce;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;


final class EvalReduce extends EvalAbstractFold
{
	private static final Object NO_VALUE = new Object();

	public EvalReduce(ExpressionNodeForReduce _node, InterpretedNumericType _type)
	{
		super( _node, _type );
	}


	@Override
	protected Object initial( Object[] _args )
	{
		return NO_VALUE;
	}


	@Override
	protected Object foldOne( Object _acc, Object _val, Collection<ExpressionNode> _dynArgs ) throws CompilerException
	{
		if (_acc == NO_VALUE) {
			if (isConstant( _val )) {
				return _val;
			}
			else {
				_dynArgs.add( (ExpressionNode) _val );
				return _acc;
			}
		}
		else {
			return super.foldOne( _acc, _val, _dynArgs );
		}
	}


	@Override
	protected ExpressionNode partialFold( Object _acc, boolean _accChanged, Object[] _args,
			Collection<ExpressionNode> _dynArgs )
	{
		if (_acc != NO_VALUE) {
			ExpressionNodeForFold result = new ExpressionNodeForFold( this.accName, new ExpressionNodeForConstantValue(
					_acc ), this.eltName, node().argument( 1 ).clone(), false );
			result.arguments().addAll( _dynArgs );
			return result;
		}
		else {
			ExpressionNodeForReduce result = (ExpressionNodeForReduce) node().cloneWithoutArguments();
			result.addArgument( valueToNode( _args[ 0 ] ) ); // empty
			result.addArgument( valueToNode( _args[ 1 ] ) ); // fold
			result.arguments().addAll( _dynArgs );
			return result;
		}
	}

}
