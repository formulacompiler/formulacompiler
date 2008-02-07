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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForOperator;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCount;
import org.objectweb.asm.Opcodes;


abstract class ExpressionCompilerForStrings_Base extends ExpressionCompilerForAll_Generated
{
	private static final String SNAME = TypeCompilerForStrings.SNAME;
	private static final String S = TypeCompilerForStrings.S;
	private static final String S2I = "(" + S + ")I";
	private static final String S2Z = "(" + S + ")Z";

	public ExpressionCompilerForStrings_Base( MethodCompiler _methodCompiler )
	{
		super( _methodCompiler );
	}


	@Override
	protected TypeCompiler typeCompiler()
	{
		return section().engineCompiler().stringCompiler();
	}


	@Override
	protected void compileConversionFrom( Class _class ) throws CompilerException
	{
		if (String.class == _class) {
			compile_util_fromString();
		}
		else if (Object.class.isAssignableFrom( _class )) {
			compile_util_fromObject();
		}
		else {
			compileConversionFromUnboxed( method().numericCompiler().compileUnboxing( _class ) );
		}
	}

	protected void compileConversionFromUnboxed( Class _class ) throws CompilerException
	{
		if (_class == Integer.TYPE || _class == Short.TYPE || _class == Byte.TYPE) {
			compile_util_fromInt();
		}

		else if (_class == Long.TYPE) {
			compile_util_fromLong();
		}

		else if (_class == Double.TYPE) {
			compile_util_fromDouble();
		}

		else if (_class == Float.TYPE) {
			compile_util_fromFloat();
		}

		else if (_class == Boolean.TYPE) {
			compile_util_fromBoolean();
		}

		else {
			super.compileConversionFrom( _class );
		}
	}

	protected abstract void compile_util_fromInt() throws CompilerException;
	protected abstract void compile_util_fromLong() throws CompilerException;
	protected abstract void compile_util_fromDouble() throws CompilerException;
	protected abstract void compile_util_fromFloat() throws CompilerException;
	protected abstract void compile_util_fromBoolean() throws CompilerException;


	@Override
	protected void innerCompileConversionFromResultOf( Method _method ) throws CompilerException
	{
		Class returnType = _method.getReturnType();
		compileConversionFrom( returnType );
	}


	@Override
	protected void compileConversionFrom( DataType _type ) throws CompilerException
	{
		switch (_type) {
			case NULL:
				compile_util_fromNull();
				return;
			case NUMERIC:
				method().numericCompiler().compileConversionToString();
				return;
		}
		super.compileConversionFrom( _type );
	}


	@Override
	protected void compileConversionTo( Class _class ) throws CompilerException
	{
		if (String.class == _class) {
			return;
		}
		else {
			super.compileConversionTo( _class );
		}
	}


	@Override
	protected void innerCompileConversionToResultOf( Method _method ) throws CompilerException
	{
		Class returnType = _method.getReturnType();
		compileConversionTo( returnType );
	}


	@Override
	protected void compileOperator( ExpressionNodeForOperator _node ) throws CompilerException
	{
		final List<ExpressionNode> args = _node.arguments();
		switch (_node.getOperator()) {

			case CONCAT:
				switch (args.size()) {
					case 0:
						throw new CompilerException.UnsupportedExpression( "CONCAT needs at least one argument." );
					case 1:
						compile( args.get( 0 ) );
						break;
					default:
						compileConcatenation( args );
				}
				return;

		}
		super.compileOperator( _node );
	}


	private final void compileConcatenation( Collection<ExpressionNode> _args ) throws CompilerException
	{
		boolean first = true;
		for (ExpressionNode arg : _args) {
			if (first) {
				compile_utilFun_newBuilder( arg );
				first = false;
			}
			else {
				compile_utilOp_appendBuilder( arg );
			}
		}
		compile_utilOp_fromBuilder();
	}


	@Override
	protected int compileComparison( int _ifOpcode, int _comparisonOpcode ) throws CompilerException
	{
		switch (_ifOpcode) {

			/*
			 * This may seem counter-intuitive here, but the contract is to return 0 for equality.
			 * Boolean true, however, is 1. So we invert the test for EQ and NE.
			 */

			case Opcodes.IFEQ:
				mv().visitMethodInsn( Opcodes.INVOKEVIRTUAL, SNAME, "equalsIgnoreCase", S2Z );
				return Opcodes.IFNE;

			case Opcodes.IFNE:
				mv().visitMethodInsn( Opcodes.INVOKEVIRTUAL, SNAME, "equalsIgnoreCase", S2Z );
				return Opcodes.IFEQ;

			default:
				mv().visitMethodInsn( Opcodes.INVOKEVIRTUAL, SNAME, "compareToIgnoreCase", S2I );
				return _ifOpcode;

		}
	}


	@Override
	protected void compileNewArray()
	{
		mv().visitTypeInsn( Opcodes.ANEWARRAY, SNAME );
	}

	@Override
	protected int arrayStoreOpcode()
	{
		return Opcodes.AASTORE;
	}


	@Override
	protected void compileCount( ExpressionNodeForCount _node ) throws CompilerException
	{
		throw new AbstractMethodError();
	}


	@Override
	public String toString()
	{
		return "string";
	}


	protected abstract void compile_util_fromString() throws CompilerException;
	protected abstract void compile_util_fromObject() throws CompilerException;
	protected abstract void compile_util_fromNull() throws CompilerException;

	protected abstract void compile_utilFun_newBuilder( ExpressionNode _arg ) throws CompilerException;
	protected abstract void compile_utilOp_appendBuilder( ExpressionNode _arg ) throws CompilerException;
	protected abstract void compile_utilOp_fromBuilder() throws CompilerException;


}
