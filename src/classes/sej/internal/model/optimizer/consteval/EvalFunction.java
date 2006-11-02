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

import sej.Function;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForFunction;
import sej.internal.model.util.EvalNotPossibleException;
import sej.internal.model.util.InterpretedNumericType;

public class EvalFunction extends EvalShadow
{

	EvalFunction(ExpressionNode _node, InterpretedNumericType _type)
	{
		super( _node, _type );
	}


	@Override
	protected Object eval()
	{
		final Function function = ((ExpressionNodeForFunction) node()).getFunction();
		switch (function) {

			case AND:
				return evalBooleanSequence( false );

			case OR:
				return evalBooleanSequence( true );

			default:
				return super.eval();

		}
	}


	private final Object evalBooleanSequence( boolean _returnThisIfFound )
	{
		final InterpretedNumericType type = type();
		final List<ExpressionNode> args = node().arguments();

		// Since AND and OR are strict in spreadsheets, we can start at the back.
		for (int i = cardinality() - 1; i >= 0; i--) {
			final Object arg = evaluateArgument( i );
			if (isConstant( arg )) {
				final boolean value = type.toBoolean( arg );
				if (value == _returnThisIfFound) {
					return _returnThisIfFound;
				}
				else {
					args.remove( i );
					arguments().remove( i );
				}
			}
		}

		if (args.size() > 0) {
			return node();
		}
		else {
			return !_returnThisIfFound;
		}
	}


	@Override
	protected Object evaluateToConst( Object[] _args )
	{
		final Function function = ((ExpressionNodeForFunction) node()).getFunction();
		if (function.isVolatile()) {
			return nodeWithConstantArgsFixed( _args );
		}
		else {
			try {
				return type().compute( function, _args );
			}
			catch (EvalNotPossibleException e) {
				return nodeWithConstantArgsFixed( _args );
			}
		}
	}


}
