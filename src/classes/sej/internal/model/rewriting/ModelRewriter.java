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
package sej.internal.model.rewriting;

import sej.compiler.CompilerException;
import sej.internal.expressions.ExpressionNode;
import sej.internal.model.AbstractComputationModelVisitor;
import sej.internal.model.CellModel;
import sej.internal.model.interpreter.InterpretedNumericType;


public final class ModelRewriter extends AbstractComputationModelVisitor
{
	private final ExpressionRewriter rewriter;

	public ModelRewriter(InterpretedNumericType _type)
	{
		super();
		this.rewriter = new ExpressionRewriter( _type );
	}


	@Override
	protected boolean visitCell( CellModel _cell ) throws CompilerException
	{
		final ExpressionNode expr = _cell.getExpression();
		if (null != expr) {
			_cell.setExpression( this.rewriter.rewrite( expr ) );
		}
		return true;
	}

}
