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
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldDatabase;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;


final class EvalFoldDatabase extends EvalShadow
{
	private final String[] colNames;


	public EvalFoldDatabase( ExpressionNodeForFoldDatabase _node, InterpretedNumericType _type )
	{
		super( _node, _type );
		this.colNames = _node.filterColumnNames();
	}
	
	
	@Override
	protected Object eval() throws CompilerException
	{
		final int card = cardinality();
		final Object[] argValues = new Object[ card ];
		for (int iArg = 0; iArg < card; iArg++) {
			argValues[ iArg ] = (iArg == 1)? evalFilter() : evaluateArgument( iArg );
		}
		return evaluateToConstOrExprWithConstantArgsFixed( argValues );
	}
	
	
	@Override
	protected Object evaluateToConst( Object... _args ) throws CompilerException
	{
		return evaluateToNode( _args );
	}
	
	
	private Object evalFilter() throws CompilerException
	{
		for (int iCol = 0; iCol < this.colNames.length; iCol++) {
			letDict().let( this.colNames[ iCol ], null, EvalLetVar.UNDEF );
		}
		try {
			return evaluateArgument( 1 ); // filter
		}
		finally {
			letDict().unlet( this.colNames.length );
		}
	}

}
