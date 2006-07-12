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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.CallFrame;
import sej.CompilerException;
import sej.internal.expressions.ExpressionNode;
import sej.internal.model.CellModel;


final class ByteCodeCellCompiler extends ByteCodeSectionNumericMethodCompiler
{
	private final ByteCodeCellComputation cellComputation;


	ByteCodeCellCompiler(ByteCodeCellComputation _computation)
	{
		super( _computation.getSection(), _computation.getMethodName() );
		this.cellComputation = _computation;
	}


	@Override
	protected void compileBody() throws CompilerException
	{
		final CellModel cell = this.cellComputation.getCell();

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
				if (shouldCache( cell )) {
					compileCacheBegin();
					compileExpr( cellExpr );
					compileCacheEnd();
				}
				else {
					compileExpr( cellExpr );
				}
			}
			else {
				final Object constantValue = cell.getConstantValue();
				compileConst( constantValue );
			}
		}
	}


	private boolean shouldCache( CellModel _cell )
	{
		return section().engineCompiler().canCache() && _cell.isCachingCandidate();
	}


	private String cachedIndicatorName = "h$" + methodName();
	private String cacheName = "c$" + methodName();
	private Label skipCachedComputation;


	private void compileCacheBegin()
	{
		// private boolean h$<x>
		cw().visitField( Opcodes.ACC_PRIVATE, this.cachedIndicatorName, Type.BOOLEAN_TYPE.getDescriptor(), null, null )
				.visitEnd();

		// private <type> c$<x>
		cw().visitField( Opcodes.ACC_PRIVATE, this.cacheName, numericType().descriptor(), null, null ).visitEnd();

		// if (!h$<x>) {
		this.skipCachedComputation = mv().newLabel();
		mv().loadThis();
		mv().getField( classType(), this.cachedIndicatorName, Type.BOOLEAN_TYPE );
		mv().visitJumpInsn( Opcodes.IFNE, this.skipCachedComputation );

		// c$<x> = ...
		mv().loadThis();
	}


	private void compileCacheEnd()
	{
		final String cachedIndicatorName = "h$" + methodName();
		final String cacheName = "c$" + methodName();

		// this and computed value is on stack, so
		// c$<x> = <value>;
		mv().putField( classType(), cacheName, numericType().type() );

		// h$<x> = true;
		mv().loadThis();
		mv().push( true );
		mv().putField( classType(), cachedIndicatorName, Type.BOOLEAN_TYPE );

		// }
		// return c$<x>;
		mv().mark( this.skipCachedComputation );
		mv().loadThis();
		mv().getField( classType(), cacheName, numericType().type() );

		// In reset(), do:
		// h$<x> = false;
		GeneratorAdapter r = section().resetter();
		r.loadThis();
		r.push( false );
		r.putField( classType(), cachedIndicatorName, Type.BOOLEAN_TYPE );
	}


	private void compileOutputGetter() throws CompilerException
	{
		final CellModel cell = this.cellComputation.getCell();
		for (CallFrame callFrame : cell.getCallsToImplement()) {

			if (callFrame.getHead() != callFrame) throw new IllegalArgumentException();

			final Method method = callFrame.getMethod();
			if (0 == callFrame.getArgs().length) {
				compileOutputMethod( cell, method.getName(), method );
			}
			else {
				ByteCodeOutputDistributorCompiler dist = this.section().getOutputDistributorFor( method );
				final String caseName = dist.compileCase( callFrame );
				compileOutputMethod( cell, caseName, method );
			}
		}
	}


	private void compileOutputMethod( CellModel _cell, String _name, Method _method ) throws CompilerException
	{
		final Type returnType = Type.getReturnType( _method );
		final String sig = "()" + returnType.getDescriptor();
		final int access = Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL;

		MethodVisitor mtd = this.cellComputation.getSection().cw().visitMethod( access, _name, sig, null, null );
		GeneratorAdapter mv = new GeneratorAdapter( mtd, access, _name, sig );
		mv.visitCode();

		compileRef( mv, this.cellComputation );

		if (CellModel.UNLIMITED != _cell.getMaxFractionalDigits()) {
			mv.visitLdcInsn( _cell.getMaxFractionalDigits() );
			numericType().compileRound( mv );
		}

		numericType().compileReturnFromNum( mv, _method );

		mv.visitMaxs( 0, 0 );
		mv.visitEnd();
	}


	private Type classType()
	{
		return section().classType();
	}

}