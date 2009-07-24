/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * This file is part of the Abacus Formula Compiler (AFC).
 *
 * For commercial licensing, please contact sales(at)formulacompiler.com.
 *
 * AFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AFC.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.formulacompiler.compiler.internal.model.optimizer.consteval;

import java.util.Collection;

import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.internal.expressions.DataType;
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
			if (arg != null && arg.hasConstantValue()) {
				try {
					if (buildUp == null) {
						buildUp = new StringBuilder( type().toString( arg.getConstantValue() ) );
					}
					else {
						buildUp.append( type().toString( arg.getConstantValue() ) );
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
					newArgs.add( new ExpressionNodeForConstantValue( buildUp.toString(), DataType.STRING ) );
					buildUp = null;
				}
				newArgs.add( arg );
			}
		}
		if (modified) {
			if (buildUp != null) {
				newArgs.add( new ExpressionNodeForConstantValue( buildUp.toString(), DataType.STRING ) );
			}
			final ExpressionNodeForOperator res = new ExpressionNodeForOperator( Operator.CONCAT, newArgs );
			res.setDataType( DataType.STRING );
			return res;
		}
		else {
			return _opNode;
		}
	}

}
