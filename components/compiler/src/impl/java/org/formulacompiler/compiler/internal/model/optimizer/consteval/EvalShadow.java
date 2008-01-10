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

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.Settings;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeShadow;
import org.formulacompiler.compiler.internal.expressions.LetDictionary;
import org.formulacompiler.compiler.internal.expressions.TypedResult;
import org.formulacompiler.compiler.internal.logging.Log;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForSubSectionModel;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;
import org.formulacompiler.compiler.internal.model.interpreter.InterpreterException;
import org.formulacompiler.runtime.ComputationException;


public abstract class EvalShadow extends ExpressionNodeShadow
{
	public static final Log LOG = Settings.LOG_CONSTEVAL;


	public static TypedResult evaluate( ExpressionNode _expr, InterpretedNumericType _type ) throws CompilerException
	{
		EvalShadow shadow = shadow( _expr, _type );
		return shadow.evalIn( new EvalShadowContext() );
	}

	protected static EvalShadow shadow( ExpressionNode _expr, InterpretedNumericType _type )
	{
		return (EvalShadow) ExpressionNodeShadow.shadow( _expr, new EvalShadowBuilder( _type ) );
	}


	private final InterpretedNumericType type;

	EvalShadow( ExpressionNode _node, InterpretedNumericType _type )
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

	protected final LetDictionary letDict()
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
		return ((EvalShadow) arguments().get( _index )).unsubstituted();
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
		return evaluateArgument( (EvalShadow) arguments().get( _index ) );
	}

	protected final TypedResult evaluateArgument( EvalShadow _arg ) throws CompilerException
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
				if (null == constResult) return null;
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


	protected final boolean isInSubSection( TypedResult _arg )
	{
		return (_arg instanceof ExpressionNodeForSubSectionModel);
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