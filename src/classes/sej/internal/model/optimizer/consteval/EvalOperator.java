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

import java.util.ArrayList;
import java.util.Collection;

import sej.Operator;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.expressions.ExpressionNodeForOperator;
import sej.internal.model.util.InterpretedNumericType;

public class EvalOperator extends EvalShadow
{

	EvalOperator(ExpressionNode _node, InterpretedNumericType _type)
	{
		super( _node, _type );
	}


	@Override
	protected Object evaluateToConst( Object[] _args )
	{
		final Operator operator = ((ExpressionNodeForOperator) node()).getOperator();
		return type().compute( operator, _args );
	}


	@Override
	protected Object nodeWithConstantArgsFixed( Object[] _args )
	{
		final Object result = super.nodeWithConstantArgsFixed( _args );
		if (result instanceof ExpressionNodeForOperator) {
			ExpressionNodeForOperator opNode = (ExpressionNodeForOperator) result;
			if (opNode.getOperator() == Operator.CONCAT) {
				return concatConsecutiveConstArgsOf( opNode );
			}
		}
		return result;
	}


	private final ExpressionNodeForOperator concatConsecutiveConstArgsOf( ExpressionNodeForOperator _opNode )
	{
		final Collection<ExpressionNode> newArgs = new ArrayList<ExpressionNode>( _opNode.arguments().size() );
		boolean modified = false;
		StringBuilder buildUp = null;
		for (final ExpressionNode arg : _opNode.arguments()) {
			if (arg instanceof ExpressionNodeForConstantValue) {
				final ExpressionNodeForConstantValue constArg = (ExpressionNodeForConstantValue) arg;
				if (buildUp == null) {
					buildUp = new StringBuilder( type().toString( constArg.getValue() ) );
				}
				else {
					buildUp.append( type().toString( constArg.getValue() ) );
					modified = true;
				}
			}
			else {
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
