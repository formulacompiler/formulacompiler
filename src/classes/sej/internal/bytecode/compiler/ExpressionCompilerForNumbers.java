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
import java.math.BigDecimal;
import java.util.Date;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.CompilerException;
import sej.NumericType;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForFunction;
import sej.runtime.ScaledLong;

abstract class ExpressionCompilerForNumbers extends ExpressionCompiler
{
	protected final static Type NUMBER_CLASS = Type.getType( Number.class );
	protected final static String N = NUMBER_CLASS.getDescriptor();

	static ExpressionCompilerForNumbers compilerFor( MethodCompiler _methodCompiler, NumericType _numericType )
	{
		if (Double.TYPE == _numericType.getValueType()) {
			return new ExpressionCompilerForDoubles( _methodCompiler, _numericType );
		}
		else if (Long.TYPE == _numericType.getValueType()) {
			return new ExpressionCompilerForScaledLongs( _methodCompiler, _numericType );
		}
		else if (BigDecimal.class == _numericType.getValueType()) {
			return new ExpressionCompilerForBigDecimals( _methodCompiler, _numericType );
		}
		else {
			throw new IllegalArgumentException( "Unsupported data type for byte code compilation." );
		}
	}


	private final NumericType numericType;

	ExpressionCompilerForNumbers(MethodCompiler _methodCompiler, NumericType _numericType)
	{
		super( _methodCompiler );
		this.numericType = _numericType;
	}

	NumericType numericType()
	{
		return this.numericType;
	}

	@Override
	protected TypeCompiler typeCompiler()
	{
		return section().engineCompiler().numberCompiler();
	}


	protected abstract void compileConversionFromInt() throws CompilerException;
	protected abstract void compileConversionToInt() throws CompilerException;


	protected abstract boolean compileConversionFrom( ScaledLong _scale ) throws CompilerException;
	protected abstract boolean compileConversionTo( ScaledLong _scale ) throws CompilerException;


	@Override
	protected void compileConversionTo( Class _class ) throws CompilerException
	{
		if (_class == Boolean.TYPE) {
			compileRuntimeMethod( "booleanFromNum", "(" + typeDescriptor() + ")Z" );
		}
		else if (_class == Boolean.class) {
			compileRuntimeMethod( "booleanFromNum", "(" + typeDescriptor() + ")Z" );
			compileConversionToBoxed( _class, Boolean.TYPE, Boolean.class );
		}
		else if (_class == Date.class) {
			compileRuntimeMethod( "dateFromNum", "(" + typeDescriptor() + ")Ljava/util/Date;" );
		}
		else {
			super.compileConversionTo( _class );
		}
	}


	protected void compileConversionToString() throws CompilerException
	{
		throw new CompilerException.UnsupportedDataType( "Cannot convert from a " + this + " to a string." );
	}

	
	@Override
	protected void compileConversionFrom( Class _class ) throws CompilerException
	{
		if (_class == Boolean.TYPE) {
			compileRuntimeMethod( "booleanToNum", "(Z)" + typeDescriptor() );
		}
		else if (_class == Boolean.class) {
			compileRuntimeMethod( "unboxBoolean", BOOL2Z );
			compileRuntimeMethod( "booleanToNum", "(Z)" + typeDescriptor() );
		}
		else if (_class == Date.class) {
			compileRuntimeMethod( "dateToNum", "(Ljava/util/Date;)" + typeDescriptor() );
		}
		else {
			super.compileConversionFrom( _class );
		}
	}


	@Override
	protected void innerCompileConversionFromResultOf( Method _method ) throws CompilerException
	{
		final Class returnType = _method.getReturnType();
		if (returnType == Long.TYPE || returnType == Long.class) {
			final ScaledLong scale = scaleOf( _method );
			if (scale != null && scale.value() != 0) {
				if (returnType == Long.class) {
					compileRuntimeMethod( "unboxLong", LONG2J );
				}
				if (!compileConversionFrom( scale )) {
					throw new CompilerException.UnsupportedDataType( "Scaled long is not supported here." );
				}
				return;
			}
		}
		compileConversionFrom( returnType );
	}


	@Override
	protected void innerCompileConversionToResultOf( Method _method ) throws CompilerException
	{
		final Class returnType = _method.getReturnType();
		if (returnType == Long.TYPE || returnType == Long.class) {
			final ScaledLong scale = scaleOf( _method );
			if (scale != null && scale.value() != 0) {
				if (compileConversionTo( scale )) {
					if (returnType == Long.class) {
						ByteCodeEngineCompiler.compileValueOf( mv(), "java/lang/Long", J2LONG, Long.TYPE );
					}
				}
				else {
					throw new CompilerException.UnsupportedDataType( "Scaled long return type is not supported." );
				}
				return;
			}
		}
		compileConversionTo( returnType );
	}


	protected final ScaledLong scaleOf( Method _method )
	{
		final ScaledLong typeScale = _method.getDeclaringClass().getAnnotation( ScaledLong.class );
		final ScaledLong mtdScale = _method.getAnnotation( ScaledLong.class );
		final ScaledLong scale = (mtdScale != null) ? mtdScale : typeScale;
		return scale;
	}


	protected final boolean compileConversionToBoxed( Class _returnType, Class _unboxed, Class _boxed,
			int... _conversionOpcodes )
	{
		if (_returnType == _boxed) {
			compileInstructions( _conversionOpcodes );
			final Type unboxedType = Type.getType( _unboxed );
			final Type boxedType = Type.getType( _boxed );

			ByteCodeEngineCompiler.compileValueOf( mv(), boxedType.getInternalName(), "("
					+ unboxedType.getDescriptor() + ")" + boxedType.getDescriptor(), _unboxed );

			return true;
		}
		else {
			return false;
		}
	}


	protected final boolean compileConversionToBoxable( Class _returnType, Class _unboxed, Class _boxed,
			int... _conversionOpcodes )
	{
		if (_returnType == _unboxed) {
			compileInstructions( _conversionOpcodes );
			return true;
		}
		else {
			return compileConversionToBoxed( _returnType, _unboxed, _boxed, _conversionOpcodes );
		}
	}

	protected final void compileInstructions( int... _conversionOpcodes )
	{
		final GeneratorAdapter mv = mv();
		for (int conv : _conversionOpcodes) {
			mv.visitInsn( conv );
		}
	}


	@Override
	protected void compileFunction( ExpressionNodeForFunction _node ) throws CompilerException
	{
		switch (_node.getFunction()) {

			case ROUND:
				compileStdFunction( _node );
				break;

			case TODAY:
				compileStdFunction( _node );
				break;

			case MATCH:
				compileHelpedExpr( new HelperCompilerForMatch( section(), _node ) );
				break;

			default:
				super.compileFunction( _node );
		}
	}


	protected void compileStdFunction( ExpressionNodeForFunction _node ) throws CompilerException
	{
		final boolean needsContext = doesStdFunctionNeedContext( _node );
		final StringBuilder descriptorBuilder = new StringBuilder();
		final String typeDescriptor = typeDescriptor();
		descriptorBuilder.append( '(' );
		for (ExpressionNode arg : _node.arguments()) {
			compile( arg );
			descriptorBuilder.append( typeDescriptor );
		}
		if (needsContext) appendStdFunctionContext( descriptorBuilder );
		descriptorBuilder.append( ')' );
		descriptorBuilder.append( typeDescriptor );
		if (needsContext) {
			compileRuntimeMethodWithContext( "std" + _node.getFunction().getName(), descriptorBuilder.toString() );
		}
		else {
			compileRuntimeMethod( "std" + _node.getFunction().getName(), descriptorBuilder.toString() );
		}
	}

	protected boolean doesStdFunctionNeedContext( ExpressionNodeForFunction _node )
	{
		return false;
	}

	protected void appendStdFunctionContext( StringBuilder _descriptorBuilder )
	{
		throw new IllegalStateException( "appendStdFunctionContext() is not applicable for this type." );
	}

	protected void compileRuntimeMethodWithContext( String _string, String _string2 )
	{
		throw new IllegalStateException( "compileRuntimeMethodWithContext() is not applicable for this type." );
	}


	protected void compileRound()
	{
		compileRuntimeMethod( "round", roundMethodSignature() );
	}

	protected abstract String roundMethodSignature();


	@Override
	public String toString()
	{
		return numericType().toString();
	}


}
