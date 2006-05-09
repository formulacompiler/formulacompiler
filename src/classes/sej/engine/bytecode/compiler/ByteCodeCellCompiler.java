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
package sej.engine.bytecode.compiler;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Date;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import sej.CallFrame;
import sej.ModelError;
import sej.Settings;
import sej.engine.compiler.model.CellModel;
import sej.expressions.ExpressionNode;


final class ByteCodeCellCompiler extends ByteCodeMethodCompiler
{
	private final ByteCodeCellComputation cellComputation;


	static {
		Settings.setDebugCompilationEnabled( true );
	}


	public ByteCodeCellCompiler(ByteCodeCellComputation _computation)
	{
		super( _computation.getSection(), _computation.getMethodName() );
		this.cellComputation = _computation;
	}


	@Override
	protected void compileBody() throws ModelError
	{
		final CellModel cell = this.cellComputation.getCell();

		if (cell.isOutput()) {
			compileOutputGetter();
		}

		if (cell.isInput()) {
			compileInput( cell.getCallChainToCall() );
		}
		else {
			final ExpressionNode cellExpr = cell.getExpression();
			if (null != cellExpr) {
				compileExpr( cellExpr );
			}
			else {
				final Object constantValue = cell.getConstantValue();
				compileConst( constantValue );
			}
		}
	}


	private void compileOutputGetter() throws ModelError
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
					mv.visitMethodInsn( Opcodes.INVOKESTATIC, getRuntimeType().getInternalName(), "round", getNumericType()
							.getRoundMethodSignature() );
				}

				if (Double.TYPE == returnClass) {
					mv.visitInsn( Opcodes.DRETURN );
				}
				else if (BigDecimal.class == returnClass) {
					mv.visitInsn( Opcodes.ARETURN );
				}
				else if (Date.class == returnClass) {
					mv.visitMethodInsn( Opcodes.INVOKESTATIC, getRuntimeType().getInternalName(), "dateFromExcel", "("
							+ getNumericType().getDescriptor() + ")Ljava/util/Date;" );
					mv.visitInsn( Opcodes.ARETURN );
				}
				else if (Boolean.TYPE == returnClass) {
					mv.visitMethodInsn( Opcodes.INVOKESTATIC, getRuntimeType().getInternalName(), "booleanFromExcel", "("
							+ getNumericType().getDescriptor() + ")Z" );
					mv.visitInsn( Opcodes.IRETURN );
				}
				else {
					throw new ModelError.UnsupportedDataType( "Output type for '"
							+ callFrame.toString() + "' is not supported" );
				}

				mv.visitMaxs( 0, 0 );
				mv.visitEnd();
			}
			else throw new IllegalArgumentException();
		}
	}


}