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
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForDatabaseFold;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;


final class EvalDatabaseFold extends EvalAbstractFold
{
	private final String[] colNames;


	public EvalDatabaseFold(ExpressionNodeForDatabaseFold _node, InterpretedNumericType _type)
	{
		super( _node, _type );
		this.colNames = _node.filterColumnNames();
	}


	@Override
	protected int evalFixedArgs( Object[] _args, int _i0 ) throws CompilerException
	{
		int i0 = super.evalFixedArgs( _args, _i0 );
		_args[ i0++ ] = evalFilter( _args );
		_args[ i0++ ] = evaluateArgument( 3 ); // column
		return i0;
	}

	private Object evalFilter( Object[] _args ) throws CompilerException
	{
		for (int iCol = 0; iCol < this.colNames.length; iCol++) {
			letDict().let( this.colNames[ iCol ], null, EvalLetVar.UNDEF );
		}
		try {
			return evaluateArgument( 2 ); // filter
		}
		finally {
			letDict().unlet( this.colNames.length );
		}
	}


	@Override
	protected Object evaluateToConstOrExprWithConstantArgsFixed( Object[] _args, int _firstFoldedArg ) throws CompilerException
	{
		return evaluateToNode( _args );
	}


	@Override
	protected ExpressionNode partialFold( Object _acc, boolean _accChanged, Object[] _args,
			Collection<ExpressionNode> _dynArgs )
	{
		throw new AbstractMethodError();
	}

}
