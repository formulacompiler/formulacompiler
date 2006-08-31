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
import java.util.Collection;
import java.util.List;

import org.objectweb.asm.Opcodes;

import sej.CompilerException;
import sej.internal.expressions.DataType;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForFunction;
import sej.internal.expressions.ExpressionNodeForOperator;

final class ExpressionCompilerForStrings extends ExpressionCompiler
{

	ExpressionCompilerForStrings(MethodCompiler _methodCompiler)
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
			compileRuntimeMethod( "stringFromString", "(Ljava/lang/String;)Ljava/lang/String;" );
		}
		else if (Object.class.isAssignableFrom( _class )) {
			compileRuntimeMethod( "stringFromObject", "(Ljava/lang/Object;)Ljava/lang/String;" );
		}
		else {
			super.compileConversionFrom( _class );
		}
	}


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
				compileRuntimeMethod( "emptyString", "()Ljava/lang/String;" );
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
						unsupported( "CONCAT needs at least one argument." );
						break;
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
				compile( arg );
				if (ByteCodeEngineCompiler.JRE14) {
					compileRuntimeMethod( "newStringBuffer", "(Ljava/lang/String;)Ljava/lang/StringBuffer;" );
				}
				else {
					compileRuntimeMethod( "newStringBuilder", "(Ljava/lang/String;)Ljava/lang/StringBuilder;" );
				}
				first = false;
			}
			else {
				compile( arg );
				if (ByteCodeEngineCompiler.JRE14) {
					mv().visitMethodInsn( Opcodes.INVOKEVIRTUAL, "java/lang/StringBuffer", "append",
							"(Ljava/lang/String;)Ljava/lang/StringBuffer;" );
				}
				else {
					mv().visitMethodInsn( Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
							"(Ljava/lang/String;)Ljava/lang/StringBuilder;" );
				}
			}
		}
		if (ByteCodeEngineCompiler.JRE14) {
			mv().visitMethodInsn( Opcodes.INVOKEVIRTUAL, "java/lang/StringBuffer", "toString", "()Ljava/lang/String;" );
		}
		else {
			mv().visitMethodInsn( Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;" );
		}
	}
	
	
	@Override
	protected void compileFunction( ExpressionNodeForFunction _node ) throws CompilerException
	{
		final List<ExpressionNode> args = _node.arguments();
		final ExpressionCompilerForNumbers num = method().numericCompiler();
		switch (_node.getFunction()) {
			
			case MID:
				switch (_node.cardinality()) {
					case 3:
						compile( args.get( 0 ) );
						num.compile( args.get( 1 ) );
						num.compileConversionToInt();
						num.compile( args.get( 2 ) );
						num.compileConversionToInt();
						compileRuntimeMethod( "stdMID", "(Ljava/lang/String;II)Ljava/lang/String;" );
						return;
				}
				break;
				
			case LEFT:
			case RIGHT:
				switch (_node.cardinality()) {
					case 1:
					case 2:
						compile( args.get( 0 ) );
						if (_node.cardinality() > 1) {
							num.compile( args.get( 1 ) );
							num.compileConversionToInt();
						}
						else {
							mv().visitInsn( Opcodes.ICONST_1 );
						}
						compileRuntimeMethod( "std" + _node.getFunction().getName(), "(Ljava/lang/String;I)Ljava/lang/String;" );
						return;
				}
				break;
				
			case SUBSTITUTE:
				switch (_node.cardinality()) {
					case 3:
						compile( args.get( 0 ) );
						compile( args.get( 1 ) );
						compile( args.get( 2 ) );
						compileRuntimeMethod( "stdSUBSTITUTE", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;" );
						return;
					case 4:
						compile( args.get( 0 ) );
						compile( args.get( 1 ) );
						compile( args.get( 2 ) );
						num.compile( args.get( 3 ) );
						num.compileConversionToInt();
						compileRuntimeMethod( "stdSUBSTITUTE", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)Ljava/lang/String;" );
						return;
				}
				break;
				
			case REPLACE:
				switch (_node.cardinality()) {
					case 4:
						compile( args.get( 0 ) );
						num.compile( args.get( 1 ) );
						num.compileConversionToInt();
						num.compile( args.get( 2 ) );
						num.compileConversionToInt();
						compile( args.get( 3 ) );
						compileRuntimeMethod( "stdREPLACE", "(Ljava/lang/String;IILjava/lang/String;)Ljava/lang/String;" );
						return;
				}
				break;
				
			case LOWER:
			case UPPER:
			// LATER case PROPER:
				switch (_node.cardinality()) {
					case 1:
						compile( args.get( 0 ) );
						compileRuntimeMethod( "std" + _node.getFunction().getName(), "(Ljava/lang/String;)Ljava/lang/String;" );
						return;
				}
				break;
				
		}
		super.compileFunction( _node );
	}


	@Override
	protected void compileComparison( int _comparisonOpcode ) throws CompilerException
	{
		// LATER compare strings
		unsupported( "String comparison is not implemented yet." );
		mv().visitMethodInsn( Opcodes.INVOKEVIRTUAL, "java/lang/String", "compareTo", "(Ljava/lang/String;)I" );
	}


	@Override
	public String toString()
	{
		return "string";
	}

}
