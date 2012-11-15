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

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.Settings;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeShadow;
import org.formulacompiler.compiler.internal.expressions.LetDictionary;
import org.formulacompiler.compiler.internal.expressions.TypedResult;
import org.formulacompiler.compiler.internal.logging.Log;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;
import org.formulacompiler.compiler.internal.model.interpreter.InterpreterException;
import org.formulacompiler.runtime.ComputationException;
import org.formulacompiler.runtime.spreadsheet.CellAddress;


public abstract class EvalShadow<E extends ExpressionNode> extends ExpressionNodeShadow<E, EvalShadow<? extends ExpressionNode>>
{
	public static final Log LOG = Settings.LOG_CONSTEVAL;


	public static TypedResult evaluate( ExpressionNode _expr, InterpretedNumericType _type, CellAddress _cellAddress ) throws CompilerException
	{
		EvalShadow shadow = shadow( _expr, _type );
		return shadow.evalIn( new EvalShadowContext( _cellAddress ) );
	}

	private static EvalShadow shadow( ExpressionNode _expr, InterpretedNumericType _type )
	{
		return ExpressionNodeShadow.shadow( _expr, new EvalShadowBuilder( _type ) );
	}


	private final InterpretedNumericType type;

	EvalShadow( E _node, InterpretedNumericType _type )
	{
		super( _node );
		this.type = _type;
	}

	public final InterpretedNumericType type()
	{
		return this.type;
	}


	private EvalShadowContext context;

	protected final TypedResult evalIn( EvalShadowContext _context ) throws CompilerException
	{
		this.context = _context;

		if (LOG.e()) LOG.a( "Eval " ).a( node() ).lf().i();

		final TypedResult res = eval();

		if (LOG.e()) LOG.o().a( "Got " ).a( res ).lf();

		return res;
	}

	protected final EvalShadowContext context()
	{
		return this.context;
	}

	protected final LetDictionary<TypedResult> letDict()
	{
		return this.context.letDict;
	}


	protected TypedResult eval() throws CompilerException
	{
		final TypedResult[] argValues = evaluateArguments();
		return evaluateToConstOrExprWithConstantArgsFixed( argValues );
	}


	protected final int cardinality()
	{
		int result = arguments().size();
		while ((result > 0) && (arguments().get( result - 1 ) == null)) {
			result--;
		}
		return result;
	}


	protected final EvalShadow unsubstitutedArgument( int _index )
	{
		return arguments().get( _index ).unsubstituted();
	}

	protected EvalShadow unsubstituted()
	{
		return this;
	}


	private final TypedResult[] evaluateArguments() throws CompilerException
	{
		final int card = cardinality();
		final TypedResult[] argValues = new TypedResult[ card ];
		for (int iArg = 0; iArg < card; iArg++) {
			argValues[ iArg ] = evaluateArgument( iArg );
		}
		return argValues;
	}


	protected final TypedResult evaluateArgument( int _index ) throws CompilerException
	{
		return evaluateArgument( arguments().get( _index ) );
	}

	private TypedResult evaluateArgument( EvalShadow _arg ) throws CompilerException
	{
		return (_arg == null) ? null : _arg.evalIn( context() );
	}

	protected final TypedResult evaluateArgument( int _index, EvalShadowContext _context ) throws CompilerException
	{
		this.context = _context;
		return evaluateArgument( _index );
	}


	protected TypedResult evaluateToConstOrExprWithConstantArgsFixed( TypedResult... _args ) throws CompilerException
	{
		if (areConstant( _args )) {
			try {
				final TypedResult constResult = evaluateToConst( _args );
				assert null != constResult;
				if (constResult.hasConstantValue()) {
					final Object constValue = constResult.getConstantValue();
					if (constValue instanceof Double) {
						final Double doubleResult = (Double) constValue;
						if (doubleResult.isInfinite() || doubleResult.isNaN()) {
							return evaluateToNode( _args );
						}
					}
				}
				return constResult;
			}
			catch (InterpreterException.IsRuntimeEnvironmentDependent e) {
				return evaluateToNode( _args );
			}
			catch (ArithmeticException e) {
				return evaluateToNode( _args );
			}
			catch (ComputationException e) {
				return evaluateToNode( _args );
			}
		}
		else {
			return evaluateToNode( _args );
		}
	}


	protected final boolean areConstant( TypedResult[] _args )
	{
		for (TypedResult arg : _args)
			if (null != arg && !arg.isConstant()) return false;
		return true;
	}


	@SuppressWarnings( "unused" )
	protected TypedResult evaluateToNode( TypedResult... _args ) throws InterpreterException
	{
		// No need to clone leaf nodes.
		if (_args.length == 0) return node();

		ExpressionNode result = node().cloneWithoutArguments();
		for (final TypedResult arg : _args) {
			result.addArgument( valueToNode( arg ) );
		}
		return result;
	}

	protected abstract TypedResult evaluateToConst( TypedResult... _args ) throws CompilerException;


	protected final ExpressionNode valueToNode( TypedResult _value )
	{
		if (null == _value) {
			return new ExpressionNodeForConstantValue( null, DataType.NULL );
		}
		if (_value instanceof ExpressionNode) {
			return (ExpressionNode) _value;
		}
		return new ExpressionNodeForConstantValue( _value.getConstantValue(), _value.getDataType() );
	}

	protected final Object[] valuesOf( TypedResult... _values )
	{
		final Object[] vals = new Object[ _values.length ];
		for (int i = 0; i < _values.length; i++) {
			final TypedResult val = _values[ i ];
			vals[ i ] = (null == val) ? null : val.getConstantValue();
		}
		return vals;
	}

}