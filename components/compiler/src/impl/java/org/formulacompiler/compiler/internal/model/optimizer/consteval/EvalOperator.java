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

import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForOperator;
import org.formulacompiler.compiler.internal.expressions.TypedResult;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;
import org.formulacompiler.compiler.internal.model.interpreter.InterpreterException;
import org.formulacompiler.runtime.New;


public class EvalOperator extends EvalShadow
{

	EvalOperator( ExpressionNode _node, InterpretedNumericType _type )
	{
		super( _node, _type );
	}


	@Override
	protected TypedResult evaluateToConst( TypedResult... _args ) throws InterpreterException
	{
		final Operator operator = ((ExpressionNodeForOperator) node()).getOperator();
		return new ConstResult( type().compute( operator, valuesOf( _args ) ), node().getDataType() );
	}


	@Override
	protected TypedResult evaluateToNode( TypedResult... _args ) throws InterpreterException
	{
		final TypedResult result = super.evaluateToNode( _args );
		if (result instanceof ExpressionNodeForOperator) {
			ExpressionNodeForOperator opNode = (ExpressionNodeForOperator) result;
			if (opNode.getOperator() == Operator.CONCAT) {
				return concatConsecutiveConstArgsOf( opNode );
			}
		}
		return result;
	}


	private final ExpressionNodeForOperator concatConsecutiveConstArgsOf( ExpressionNodeForOperator _opNode )
			throws InterpreterException
	{
		final Collection<ExpressionNode> newArgs = New.collection( _opNode.arguments().size() );
		boolean modified = false;
		StringBuilder buildUp = null;
		for (final ExpressionNode arg : _opNode.arguments()) {
			boolean isConst = false;
			if (arg instanceof ExpressionNodeForConstantValue) {
				try {
					final ExpressionNodeForConstantValue constArg = (ExpressionNodeForConstantValue) arg;
					if (buildUp == null) {
						buildUp = new StringBuilder( type().toString( constArg.value() ) );
					}
					else {
						buildUp.append( type().toString( constArg.value() ) );
						modified = true;
					}
					isConst = true;
				}
				catch (InterpreterException.IsRuntimeEnvironmentDependent e) {
					isConst = false;
				}
			}
			if (!isConst) {
				if (buildUp != null) {
					newArgs.add( new ExpressionNodeForConstantValue( buildUp.toString() ) );
					buildUp = null;
				}
				newArgs.add( arg );
			}
		}
		if (modified) {
			if (buildUp != null) {
				newArgs.add( new ExpressionNodeForConstantValue( buildUp.toString() ) );
			}
			return new ExpressionNodeForOperator( Operator.CONCAT, newArgs );
		}
		else {
			return _opNode;
		}
	}

}
