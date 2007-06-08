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
package org.formulacompiler.compiler.internal.model.rewriting;

import java.util.List;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.expressions.InnerExpressionException;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;


final class ExpressionRewriter extends AbstractExpressionRewriter
{
	private final GeneratedFunctionRewriter generatedRules;
	private final InterpretedNumericType numericType;


	public ExpressionRewriter(InterpretedNumericType _type)
	{
		super();
		this.numericType = _type;
		this.generatedRules = new GeneratedFunctionRewriter();
	}


	public final ExpressionNode rewrite( ExpressionNode _expr ) throws CompilerException
	{
		ExpressionNode result = _expr;
		try {
			if (_expr instanceof ExpressionNodeForFunction) {
				result = rewriteFun( (ExpressionNodeForFunction) _expr );
			}
		}
		catch (InnerExpressionException e) {
			throw e;
		}
		catch (CompilerException e) {
			throw new InnerExpressionException( _expr, e );
		}
		return rewriteArgsOf( result );
	}


	private ExpressionNode rewriteArgsOf( ExpressionNode _expr ) throws CompilerException
	{
		if (null == _expr) {
			return null;
		}
		else {
			final List<ExpressionNode> args = _expr.arguments();
			for (int iArg = 0; iArg < args.size(); iArg++) {
				final ExpressionNode arg = args.get( iArg );
				final ExpressionNode rewritten = rewrite( arg );
				if (rewritten != arg) {
					args.set( iArg, rewritten );
				}
			}
			return _expr;
		}
	}

	
	private ExpressionNode rewriteFun( ExpressionNodeForFunction _fun ) throws CompilerException
	{
		ExpressionNodeForFunction curr = _fun; 
		ExpressionNode rewritten = rewriteFunOnce( curr );
		while (rewritten != curr && rewritten instanceof ExpressionNodeForFunction) {
			curr = (ExpressionNodeForFunction) rewritten;
			rewritten = rewriteFunOnce( curr );
		}
		return rewritten;
	}
	

	private ExpressionNode rewriteFunOnce( ExpressionNodeForFunction _fun ) throws CompilerException
	{
		switch (_fun.getFunction()) {
			case DSUM:
				return new FunctionRewriterForDSUM( _fun, this.numericType ).rewrite();
			case DPRODUCT:
				return new FunctionRewriterForDPRODUCT( _fun, this.numericType ).rewrite();
			case DCOUNT:
				return new FunctionRewriterForDCOUNT( _fun, this.numericType ).rewrite();
			case DMIN:
				return new FunctionRewriterForDMIN( _fun, this.numericType ).rewrite();
			case DMAX:
				return new FunctionRewriterForDMAX( _fun, this.numericType ).rewrite();
		}
		return this.generatedRules.rewrite( _fun );
	}


}
