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

import sej.CompilerException;
import sej.internal.Settings;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForArrayReference;
import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.expressions.ExpressionNodeForSubstitution;
import sej.internal.expressions.ExpressionNodeShadow;
import sej.internal.expressions.LetDictionary;
import sej.internal.logging.Log;
import sej.internal.model.ExpressionNodeForSubSectionModel;
import sej.internal.model.util.InterpretedNumericType;

public abstract class EvalShadow extends ExpressionNodeShadow
{
	public static final Log LOG = Settings.LOG_CONSTEVAL;


	public static Object evaluate( ExpressionNode _expr, InterpretedNumericType _type ) throws CompilerException
	{
		EvalShadow shadow = shadow( _expr, _type );
		return shadow.evalIn( new EvalShadowContext() );
	}

	protected static EvalShadow shadow( ExpressionNode _expr, InterpretedNumericType _type )
	{
		return (EvalShadow) ExpressionNodeShadow.shadow( _expr, new EvalShadowBuilder( _type ) );
	}


	private final InterpretedNumericType type;

	EvalShadow(ExpressionNode _node, InterpretedNumericType _type)
	{
		super( _node );
		this.type = _type;
	}

	public final InterpretedNumericType type()
	{
		return this.type;
	}


	private EvalShadowContext context;

	protected final Object evalIn( EvalShadowContext _context ) throws CompilerException
	{
		this.context = _context;

		if (LOG.e()) LOG.a( "Eval " ).a( node() ).lf().i();

		final Object res = eval();

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


	protected Object eval() throws CompilerException
	{
		final Object[] argValues = evaluateArguments();
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


	private final Object[] evaluateArguments() throws CompilerException
	{
		final int card = cardinality();
		final Object[] argValues = new Object[ card ];
		for (int iArg = 0; iArg < card; iArg++) {
			argValues[ iArg ] = evaluateArgument( iArg );
		}
		return argValues;
	}


	protected final Object evaluateArgument( int _index ) throws CompilerException
	{
		return evaluateArgument( (EvalShadow) arguments().get( _index ) );
	}

	protected final Object evaluateArgument( EvalShadow _arg ) throws CompilerException
	{
		return (_arg == null) ? null : _arg.evalIn( context() );
	}


	protected Object evaluateToConstOrExprWithConstantArgsFixed( Object... _args ) throws CompilerException
	{
		if (hasOnlyConstantArgs( _args )) {
			return evaluateToConst( _args );
		}
		else {
			return evaluateToNode( _args );
		}
	}


	protected final boolean hasOnlyConstantArgs( Object... _args )
	{
		for (Object arg : _args) {
			if (!isConstant( arg )) return false;
		}
		return true;
	}

	protected final boolean isConstant( Object _arg )
	{
		return !(_arg instanceof ExpressionNode)
				|| ((_arg instanceof ExpressionNodeForSubstitution) && areConstant( ((ExpressionNode) _arg).arguments() ))
				|| ((_arg instanceof ExpressionNodeForArrayReference) && areConstant( ((ExpressionNode) _arg).arguments() ));
	}
	private final boolean areConstant( Iterable<ExpressionNode> _args )
	{
		for (ExpressionNode arg : _args) {
			if (!isConstant( arg ) && !(arg instanceof ExpressionNodeForConstantValue)) return false;
		}
		return true;
	}


	protected final boolean isInSubSection( Object _arg )
	{
		return (_arg instanceof ExpressionNodeForSubSectionModel);
	}

	protected Object evaluateToNode( Object... _args )
	{
		ExpressionNode result = node().cloneWithoutArguments();
		for (final Object arg : _args) {
			if (arg instanceof ExpressionNode) {
				result.addArgument( (ExpressionNode) arg );
			}
			else {
				result.addArgument( new ExpressionNodeForConstantValue( arg ) );
			}
		}
		return result;
	}

	protected abstract Object evaluateToConst( Object... _args ) throws CompilerException;


	protected final ExpressionNode valueToNode( Object _value )
	{
		return (_value instanceof ExpressionNode) ? (ExpressionNode) _value : new ExpressionNodeForConstantValue( _value );
	}

	protected final Object valueOf( Object _valueOrNode )
	{
		if (_valueOrNode instanceof ExpressionNodeForConstantValue) {
			ExpressionNodeForConstantValue cst = (ExpressionNodeForConstantValue) _valueOrNode;
			return cst.value();
		}
		return _valueOrNode;
	}

}