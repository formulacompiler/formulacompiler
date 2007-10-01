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
package org.formulacompiler.compiler.internal.bytecode;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.runtime.internal.RuntimePrecisionBigDecimal_v2;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

public class TypeCompilerForPrecisionBigDecimals extends TypeCompilerForBigDecimals
{
	private static final Type RUNTIME_TYPE = Type.getType( RuntimePrecisionBigDecimal_v2.class );
	private static final Type RUNTIME_CONTEXT_TYPE = Type.getType( MathContext.class );

	static final String RUNTIME_CONTEXT_DESCRIPTOR = RUNTIME_CONTEXT_TYPE.getDescriptor();
	static final String RUNTIME_CONTEXT_NAME = "mathContext";

	protected TypeCompilerForPrecisionBigDecimals( ByteCodeEngineCompiler _engineCompiler, NumericType _numericType )
	{
		super( _engineCompiler, _numericType );
	}

	@Override
	protected Type runtimeType()
	{
		return RUNTIME_TYPE;
	}


	@Override
	void compileAdjustment( GeneratorAdapter _mv )
	{
		// No adjustment as precision is only a minimum, not an absolute.
	}

	@Override
	boolean needsAdjustment()
	{
		return false;
	}

	@Override
	boolean needsAdjustment( BigDecimal _value )
	{
		return false;
	}


	private boolean staticContextBuilt = false;

	final void buildStaticContext()
	{
		if (this.staticContextBuilt) return;
		this.staticContextBuilt = true;

		final SectionCompiler root = engineCompiler().rootCompiler();
		final ClassWriter cw = root.cw();
		final FieldVisitor fv = cw.visitField( Opcodes.ACC_STATIC + Opcodes.ACC_FINAL, RUNTIME_CONTEXT_NAME,
				RUNTIME_CONTEXT_TYPE.getDescriptor(), null, null );
		fv.visitEnd();

		final GeneratorAdapter mv = root.initializer();
		final MathContext mc = numericType().mathContext();
		if (mc == MathContext.DECIMAL32) {
			compilePredefinedMathContext( mv, "DECIMAL32" );
		}
		else if (mc == MathContext.DECIMAL64) {
			compilePredefinedMathContext( mv, "DECIMAL64" );
		}
		else if (mc == MathContext.DECIMAL128) {
			compilePredefinedMathContext( mv, "DECIMAL128" );
		}
		else if (mc == MathContext.UNLIMITED) {
			compilePredefinedMathContext( mv, "UNLIMITED" );
		}
		else {
			mv.visitTypeInsn( Opcodes.NEW, RUNTIME_CONTEXT_TYPE.getInternalName() );
			mv.visitInsn( Opcodes.DUP );
			mv.push( mc.getPrecision() );
			final Type modeType = Type.getType( RoundingMode.class );
			mv.getStatic( modeType, mc.getRoundingMode().name(), modeType );
			mv.visitMethodInsn( Opcodes.INVOKESPECIAL, RUNTIME_CONTEXT_TYPE.getInternalName(), "<init>",
					"(ILjava/math/RoundingMode;)V" );
		}
		mv.visitFieldInsn( Opcodes.PUTSTATIC, root.classInternalName(), RUNTIME_CONTEXT_NAME, RUNTIME_CONTEXT_DESCRIPTOR );
	}

	private void compilePredefinedMathContext( GeneratorAdapter _mv, String _fieldName )
	{
		Type mcType = Type.getType( MathContext.class );
		_mv.getStatic( mcType, _fieldName, mcType );
	}


}
