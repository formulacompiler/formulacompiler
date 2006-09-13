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
package sej.internal.bytecode.compiler;

import java.lang.reflect.Method;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.CallFrame;
import sej.CompilerException;
import sej.internal.expressions.ExpressionNode;
import sej.internal.model.CellModel;


final class CellMethodCompiler extends ValueMethodCompiler
{
	private final CellComputation cellComputation;


	CellMethodCompiler(CellComputation _computation)
	{
		super( _computation.getSection(), _computation.getMethodName(), _computation.getCell().getDataType() );
		this.cellComputation = _computation;
	}


	@Override
	protected void compileBody() throws CompilerException
	{
		final CellModel cell = this.cellComputation.getCell();

		try {

			if (cell.isOutput()) {
				compileOutputGetter();
			}

			if (cell.isInput()) {
				if (shouldCache( cell )) {
					compileCacheBegin();
					compileNumericInput( cell.getCallChainToCall() );
					compileCacheEnd();
				}
				else {
					compileNumericInput( cell.getCallChainToCall() );
				}
			}
			else {
				final ExpressionNode cellExpr = cell.getExpression();
				if (null != cellExpr) {
					try {
						if (shouldCache( cell )) {
							compileCacheBegin();
							compileExpression( cellExpr );
							compileCacheEnd();
						}
						else {
							compileExpression( cellExpr );
						}
					}
					catch (InnerExpressionException e) {
						final CompilerException cause = e.getCause();
						final ExpressionNode errorNode = e.getErrorNode();
						if (null != errorNode) {
							cause.addMessageContext( errorNode.getContext( errorNode ) );
						}
						else {
							cause.addMessageContext( cellExpr.getContext( null ) );
						}
						throw cause;
					}
					catch (CompilerException e) {
						e.addMessageContext( cellExpr.getContext( null ) );
						throw e;
					}
				}
				else {
					final Object constantValue = cell.getConstantValue();
					expressionCompiler().compileConst( constantValue );
				}
			}

		}
		catch (CompilerException e) {
			e.addMessageContext( "\nReferenced by cell " + cell + "." );
			throw e;
		}
	}


	private final void compileNumericInput( CallFrame _callChainToCall ) throws CompilerException
	{
		compileInputGetterCall( _callChainToCall );
		expressionCompiler().compileConversionFromResultOf( _callChainToCall.getMethod() );
	}


	private final boolean shouldCache( CellModel _cell )
	{
		return section().engineCompiler().canCache() && _cell.isCachingCandidate();
	}


	private String cachedIndicatorName = "h$" + methodName();
	private String cacheName = "c$" + methodName();
	private Label skipCachedComputation;


	private final void compileCacheBegin()
	{
		// private boolean h$<x>
		cw().visitField( Opcodes.ACC_PRIVATE, this.cachedIndicatorName, Type.BOOLEAN_TYPE.getDescriptor(), null, null )
				.visitEnd();

		// private <type> c$<x>
		cw().visitField( Opcodes.ACC_PRIVATE, this.cacheName, typeCompiler().typeDescriptor(), null, null ).visitEnd();

		// if (!h$<x>) {
		this.skipCachedComputation = mv().newLabel();
		mv().loadThis();
		mv().getField( classType(), this.cachedIndicatorName, Type.BOOLEAN_TYPE );
		mv().visitJumpInsn( Opcodes.IFNE, this.skipCachedComputation );

		// c$<x> = ...
		mv().loadThis();
	}


	private final void compileCacheEnd()
	{
		final String cachedIndicatorName = "h$" + methodName();
		final String cacheName = "c$" + methodName();

		// this and computed value is on stack, so
		// c$<x> = <value>;
		mv().putField( classType(), cacheName, typeCompiler().type() );

		// h$<x> = true;
		mv().loadThis();
		mv().push( true );
		mv().putField( classType(), cachedIndicatorName, Type.BOOLEAN_TYPE );

		// }
		// return c$<x>;
		mv().mark( this.skipCachedComputation );
		mv().loadThis();
		mv().getField( classType(), cacheName, typeCompiler().type() );

		// In reset(), do:
		// h$<x> = false;
		GeneratorAdapter r = section().resetter();
		r.loadThis();
		r.push( false );
		r.putField( classType(), cachedIndicatorName, Type.BOOLEAN_TYPE );
	}


	private final void compileOutputGetter() throws CompilerException
	{
		final CellModel cell = this.cellComputation.getCell();
		for (CallFrame callFrame : cell.getCallsToImplement()) {

			if (callFrame.getHead() != callFrame) throw new IllegalArgumentException();

			final Method method = callFrame.getMethod();
			if (0 == callFrame.getArgs().length) {
				compileOutputMethod( cell, method.getName(), method );
			}
			else {
				OutputDistributorCompiler dist = this.section().getOutputDistributorFor( method );
				final String caseName = dist.compileCase( callFrame );
				compileOutputMethod( cell, caseName, method );
			}
		}
	}


	private final void compileOutputMethod( CellModel _cell, String _name, Method _method ) throws CompilerException
	{
		final Type returnType = Type.getReturnType( _method );
		final String sig = "()" + returnType.getDescriptor();
		new OutputMethodCompiler( section(), _name, sig, this.cellComputation, _method ).compile();
	}


	private final Type classType()
	{
		return section().classType();
	}

}