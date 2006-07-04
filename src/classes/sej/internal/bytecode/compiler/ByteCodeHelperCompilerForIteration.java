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

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.CompilerException;
import sej.Operator;
import sej.internal.model.ExpressionNodeForSubSectionModel;


final class ByteCodeHelperCompilerForIteration extends ByteCodeHelperCompiler
{
	private final ExpressionNodeForSubSectionModel node;
	private final ByteCodeSubSectionCompiler sub;
	private final Operator reductor;


	ByteCodeHelperCompilerForIteration(ByteCodeSectionCompiler _section, Operator _reductor,
			ExpressionNodeForSubSectionModel _node)
	{
		super( _section );
		this.node = _node;
		this.sub = section().subSectionCompiler( this.node.getSectionModel() );
		this.reductor = _reductor;
	}


	@Override
	protected void compileBody() throws CompilerException
	{
		final ByteCodeSubSectionCompiler sub = this.sub;
		final ByteCodeSectionNumericMethodCompiler subExpr = sub.compileExpr( this.node.getArguments().get( 0 ) );

		final GeneratorAdapter mv = mv();

		// final DetailPrototype[] ds = arrayDets();
		final int l_ds = mv.newLocal( sub.classType() );
		mv.loadThis();
		section().compileCallToGetterFor( mv, sub );
		mv.storeLocal( l_ds );

		// final int dl = ds.length;
		final int l_dl = mv.newLocal( Type.INT_TYPE );
		mv.loadLocal( l_ds );
		mv.arrayLength();
		mv.storeLocal( l_dl );

		// if (dl > 0) {
		final Label noData = mv.newLabel();
		mv.loadLocal( l_dl );
		mv.ifZCmp( mv.LE, noData );

		// ~ long result = ds[ 0 ].detailValue();
		final int l_result = mv.newLocal( numericType().getType() );
		mv.loadLocal( l_ds );
		mv.push( 0 );
		mv.arrayLoad( sub.classType() );
		mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, sub.classInternalName(), subExpr.methodName(), subExpr
				.methodDescriptor() );
		mv.storeLocal( l_result );

		// ~ for (int i = 1; i < dl; i++) {
		final Label next = mv.newLabel();
		final int l_i = mv.newLocal( Type.INT_TYPE );
		mv.push( 1 );
		mv.storeLocal( l_i );
		mv.goTo( next );
		final Label again = mv.mark();

		// ~ ~ result += ds[ i ].detailValue();
		mv.loadLocal( l_result );
		mv.loadLocal( l_ds );
		mv.loadLocal( l_i );
		mv.arrayLoad( sub.classType() );
		mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, sub.classInternalName(), subExpr.methodName(), subExpr
				.methodDescriptor() );
		compileOperator( this.reductor, 2 );
		mv.storeLocal( l_result );

		// ~ }
		mv.iinc( l_i, 1 );
		mv.mark( next );
		mv.loadLocal( l_i );
		mv.loadLocal( l_dl );
		mv.ifCmp( Type.INT_TYPE, mv.LT, again );

		// ~ return result;
		mv.loadLocal( l_result );
		mv.visitInsn( numericType().getReturnOpcode() );

		// } else {
		mv.mark( noData );
		numericType().compileZero( mv );
		mv.visitInsn( numericType().getReturnOpcode() );
	}


}
