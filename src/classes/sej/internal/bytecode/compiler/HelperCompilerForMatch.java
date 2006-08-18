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

import sej.CompilerException;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.expressions.ExpressionNodeForFunction;

final class HelperCompilerForMatch extends HelperCompiler
{
	private final ExpressionNodeForFunction node;


	HelperCompilerForMatch(SectionCompiler _section, ExpressionNodeForFunction _node)
	{
		super( _section );
		this.node = _node;
	}


	@Override
	protected void compileBody() throws CompilerException
	{
		switch (this.node.cardinality()) {

			case 2: {
				compileWithConstType( 1 );
				break;
			}

			case 3: {
				final ExpressionNode typeArg = this.node.arguments().get( 2 );
				if (typeArg instanceof ExpressionNodeForConstantValue) {
					final ExpressionNodeForConstantValue constTypeArg = (ExpressionNodeForConstantValue) typeArg;
					final Object typeVal = constTypeArg.getValue();
					if (typeVal instanceof Number) {
						compileWithConstType( ((Number) typeVal).intValue() );
					}
					else {
						compileWithConstType( 1 );
					}
				}
				else {
					unsupported( this.node );
				}
				break;
			}

			default:
				unsupported( this.node );
		}
	}


	private void compileWithConstType( int _type ) throws CompilerException
	{
		final NumericTypeCompiler num = section().numericType();
		final Type numType = num.type();
		final ExpressionNode[] candidates = rangeElements( this.node, this.node.arguments().get( 1 ) );

		// final double val = <arg_1>;
		final int l_val = mv().newLocal( numType );
		compileExpr( this.node.arguments().get( 0 ) );

		mv().storeLocal( l_val );

		// int result = 0;
		final int l_result = mv().newLocal( Type.INT_TYPE );
		mv().push( 0 );
		mv().storeLocal( l_result );

		final Label done = mv().newLabel();

		for (ExpressionNode candidate : candidates) {
			mv().loadLocal( l_val );
			compileExpr( candidate );
			if (_type == 0) {
				// result++;
				mv().iinc( l_result, 1 );
				// else if (val == <expr_i>) return result;
				num.compileComparison( mv(), Opcodes.DCMPL );
				mv().visitJumpInsn( Opcodes.IFEQ, done );
			}
			else if (_type < 0) {
				// else if (val > <expr_i>) return result;
				num.compileComparison( mv(), Opcodes.DCMPG );
				mv().visitJumpInsn( Opcodes.IFGT, done );
				// result++;
				mv().iinc( l_result, 1 );
			}
			else {
				// else if (val < <expr_i>) return result;
				num.compileComparison( mv(), Opcodes.DCMPL );
				mv().visitJumpInsn( Opcodes.IFLT, done );
				// result++;
				mv().iinc( l_result, 1 );
			}
		}
		if (_type == 0) {
			// result = 0;
			mv().push( 0 );
			mv().storeLocal( l_result );
		}

		mv().mark( done );
		// return result;
		mv().loadLocal( l_result );
		num.compileToNum( mv(), Integer.TYPE );
	}


}
