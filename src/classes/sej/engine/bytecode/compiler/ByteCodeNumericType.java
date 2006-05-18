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

import java.math.BigDecimal;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.ModelError;
import sej.NumericType;
import sej.expressions.Function;
import sej.expressions.Operator;


abstract class ByteCodeNumericType
{
	private final NumericType num;
	private final ByteCodeSectionCompiler compiler;


	ByteCodeNumericType(NumericType _type, ByteCodeSectionCompiler _compiler)
	{
		super();
		this.num = _type;
		this.compiler = _compiler;
	}

	public static ByteCodeNumericType typeFor( NumericType _type, ByteCodeSectionCompiler _compiler )
	{
		if (Double.TYPE == _type.getValueType()) {
			return new ByteCodeDoubleType( _type, _compiler );
		}
		else if (Long.TYPE == _type.getValueType()) {
			return new ByteCodeScaledLongType( _type, _compiler );
		}
		else if (BigDecimal.class == _type.getValueType()) {
			return new ByteCodeBigDecimalType( _type, _compiler );
		}
		else {
			throw new IllegalArgumentException( "Unsupported numeric type for byte code compilation." );
		}
	}


	public NumericType getNumericType()
	{
		return this.num;
	}

	public ByteCodeSectionCompiler getCompiler()
	{
		return this.compiler;
	}

	public abstract Type getType();
	public abstract int getReturnOpcode();
	public abstract Type getRuntimeType();


	public String getDescriptor()
	{
		return getType().getDescriptor();
	}


	protected abstract String getRoundMethodSignature();


	protected void compileRuntimeMethod( MethodVisitor _mv, String _methodName, String _methodSig )
	{
		_mv.visitMethodInsn( Opcodes.INVOKESTATIC, getRuntimeType().getInternalName(), _methodName, _methodSig );
	}

	public boolean buildStaticMembers( ClassWriter _writer )
	{
		return false;
	}

	public void compileStaticInitialization( GeneratorAdapter _mv, Type _engineType )
	{
		// overridable placeholder
	}

	public void finalizeStaticInitialization( GeneratorAdapter _mv, Type _engineType )
	{
		// overridable placeholder
	}

	public void compile( GeneratorAdapter _mv, Operator _operator, int _numberOfArguments ) throws ModelError
	{
		switch (_operator) {

			case NOOP:
				return;

			default:
				throw new ModelError.UnsupportedOperator( "The operator '"
						+ _operator.getSymbol() + "' is not supported here." );
		}
	}

	public void compileConst( GeneratorAdapter _mv, Object _constantValue ) throws ModelError
	{
		throw new ModelError.UnsupportedDataType( "The data type "
				+ _constantValue.getClass().getName() + " is not supported for constant " + _constantValue.toString() );
	}

	public abstract void compileZero( GeneratorAdapter _mv );

	public abstract void compileComparison( GeneratorAdapter _mv, int _comparisonOpcode );

	public void compileStdFunction( GeneratorAdapter _mv, Function _function, String _argumentDescriptor )
	{
		compileRuntimeMethod( _mv, "std" + _function.getName(), "(" + _argumentDescriptor + ")" + getDescriptor() );
	}

	public void compileRound( MethodVisitor _mv )
	{
		compileRuntimeMethod( _mv, "round", getRoundMethodSignature() );
	}

	public void compileDateToExcel( MethodVisitor _mv )
	{
		compileRuntimeMethod( _mv, "dateToExcel", "(Ljava/util/Date;)" + getDescriptor() );
	}

	public void compileDateFromExcel( MethodVisitor _mv )
	{
		compileRuntimeMethod( _mv, "dateFromExcel", "(" + getDescriptor() + ")Ljava/util/Date;" );
	}

	public void compileBooleanToExcel( MethodVisitor _mv )
	{
		compileRuntimeMethod( _mv, "booleanToExcel", "(Z)" + getDescriptor() );
	}

	public void compileBooleanFromExcel( MethodVisitor _mv )
	{
		compileRuntimeMethod( _mv, "booleanFromExcel", "(" + getDescriptor() + ")Z" );
	}

}
