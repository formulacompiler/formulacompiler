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
import java.util.Date;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.CallFrame;
import sej.CompilerError;
import sej.expressions.ExpressionNode;
import sej.internal.model.CellModel;


final class ByteCodeCellCompiler extends ByteCodeSectionMethodCompiler
{
	private final ByteCodeCellComputation cellComputation;


	ByteCodeCellCompiler(ByteCodeCellComputation _computation)
	{
		super( _computation.getSection(), _computation.getMethodName() );
		this.cellComputation = _computation;
	}


	@Override
	protected void compileBody() throws CompilerError
	{
		final CellModel cell = this.cellComputation.getCell();

		if (cell.isOutput()) {
			compileOutputGetter();
		}

		if (cell.isInput()) {
			if (shouldCache( cell )) {
				compileCacheBegin();
				compileInput( cell.getCallChainToCall() );
				compileCacheEnd();
			}
			else {
				compileInput( cell.getCallChainToCall() );
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
		return getSection().getEngineCompiler().canCache() && _cell.isCachingCandidate();
	}


	private String cachedIndicatorName = "h$" + getMethodName();
	private String cacheName = "c$" + getMethodName();
	private Label skipCachedComputation;


	private void compileCacheBegin()
	{
		// private boolean h$<x>
		cw().visitField( Opcodes.ACC_PRIVATE, this.cachedIndicatorName, Type.BOOLEAN_TYPE.getDescriptor(), null, null )
				.visitEnd();

		// private <type> c$<x>
		cw().visitField( Opcodes.ACC_PRIVATE, this.cacheName, getNumericType().getDescriptor(), null, null ).visitEnd();

		// if (!h$<x>) {
		this.skipCachedComputation = mv().newLabel();
		mv().loadThis();
		mv().getField( getEngineType(), this.cachedIndicatorName, Type.BOOLEAN_TYPE );
		mv().visitJumpInsn( Opcodes.IFNE, this.skipCachedComputation );

		// c$<x> = ...
		mv().loadThis();
	}


	private void compileCacheEnd()
	{
		final String cachedIndicatorName = "h$" + getMethodName();
		final String cacheName = "c$" + getMethodName();

		// this and computed value is on stack, so
		// c$<x> = <value>;
		mv().putField( getEngineType(), cacheName, getNumericType().getType() );

		// h$<x> = true;
		mv().loadThis();
		mv().push( true );
		mv().putField( getEngineType(), cachedIndicatorName, Type.BOOLEAN_TYPE );

		// }
		// return c$<x>;
		mv().mark( this.skipCachedComputation );
		mv().loadThis();
		mv().getField( getEngineType(), cacheName, getNumericType().getType() );

		// In reset(), do:
		// h$<x> = false;
		GeneratorAdapter r = getSection().getResetter();
		r.loadThis();
		r.push( false );
		r.putField( getEngineType(), cachedIndicatorName, Type.BOOLEAN_TYPE );
	}


	private void compileOutputGetter() throws CompilerError
	{
		final CellModel cell = this.cellComputation.getCell();
		for (CallFrame callFrame : cell.getCallsToImplement()) {

			if (callFrame.getHead() != callFrame) throw new IllegalArgumentException();

			final Method method = callFrame.getMethod();
			final Class returnClass = method.getReturnType();
			final Type returnType = Type.getReturnType( method );

			if (0 == callFrame.getArgs().length) {
				MethodVisitor mv = this.cellComputation.getSection().cw().visitMethod(
						Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, method.getName(), "()" + returnType.getDescriptor(), null,
						null );
				mv.visitCode();

				compileRef( mv, this.cellComputation );

				if (CellModel.UNLIMITED != cell.getMaxFractionalDigits()) {
					mv.visitLdcInsn( cell.getMaxFractionalDigits() );
					getNumericType().compileRound( mv );
				}

				if (returnClass == getNumericType().getNumericType().getValueType()) {
					mv.visitInsn( getNumericType().getReturnOpcode() );
				}
				else if (Date.class == returnClass) {
					getNumericType().compileDateFromExcel( mv );
					mv.visitInsn( Opcodes.ARETURN );
				}
				else if (Boolean.TYPE == returnClass) {
					getNumericType().compileBooleanFromExcel( mv );
					mv.visitInsn( Opcodes.IRETURN );
				}
				else {
					throw new CompilerError.UnsupportedDataType( "Output type for '"
							+ callFrame.toString() + "' is not supported" );
				}

				mv.visitMaxs( 0, 0 );
				mv.visitEnd();
			}
			else throw new IllegalArgumentException();
		}
	}


	private Type getEngineType()
	{
		return getSection().engine;
	}

}