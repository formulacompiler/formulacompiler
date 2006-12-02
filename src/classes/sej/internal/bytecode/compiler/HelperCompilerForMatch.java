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
import sej.internal.expressions.LetDictionary.LetEntry;

final class HelperCompilerForMatch extends HelperCompiler
{

	HelperCompilerForMatch(SectionCompiler _section, ExpressionNodeForFunction _node, Iterable<LetEntry> _closure)
	{
		super( _section, _node, _closure );
	}


	@Override
	protected void compileBody() throws CompilerException
	{
		switch (node().cardinality()) {

			case 2: {
				compileWithConstType( 1 );
				return;
			}

			case 3: {
				final ExpressionNode typeArg = node().arguments().get( 2 );
				if (typeArg instanceof ExpressionNodeForConstantValue) {
					final ExpressionNodeForConstantValue constTypeArg = (ExpressionNodeForConstantValue) typeArg;
					final Object typeVal = constTypeArg.value();
					if (typeVal instanceof Number) {
						compileWithConstType( ((Number) typeVal).intValue() );
					}
					else {
						compileWithConstType( 1 );
					}
					return;
				}
				throw new InnerExpressionException( typeArg, new CompilerException.UnsupportedExpression(
						"The last argument to MATCH, the match type, must be constant, but is " + typeArg.describe() + "." ) );
			}

		}
		throw new CompilerException.UnsupportedExpression( "MATCH must have two or three arguments." );
	}


	private void compileWithConstType( int _type ) throws CompilerException
	{
		final ExpressionNode valNode = node().arguments().get( 0 );
		final ExpressionNode candidateNode = node().arguments().get( 1 );

		if (valNode.getDataType() != candidateNode.getDataType()) {
			throw new CompilerException.UnsupportedExpression(
					"MATCH must have the same type of argument in the first and second slot." );
		}

		final ExpressionNode[] candidates = arrayRefElements( node(), candidateNode );
		final ExpressionCompilerForNumbers numCompiler = numericCompiler();
		final ExpressionCompiler valCompiler = expressionCompiler( valNode.getDataType() );
		final Type valType = valCompiler.type();

		// final double val = <arg_1>;
		final int l_val = mv().newLocal( valType );
		valCompiler.compile( valNode );
		mv().storeLocal( l_val );

		// int result = 0;
		final int l_result = mv().newLocal( Type.INT_TYPE );
		mv().push( 0 );
		mv().storeLocal( l_result );

		final Label done = mv().newLabel();

		for (ExpressionNode candidate : candidates) {
			mv().loadLocal( l_val );
			valCompiler.compile( candidate );
			if (_type == 0) {
				// result++;
				mv().iinc( l_result, 1 );
				// else if (val == <expr_i>) return result;
				mv().visitJumpInsn( valCompiler.compileComparison( Opcodes.IFEQ, Opcodes.DCMPL ), done );
			}
			else if (_type < 0) {
				// else if (val > <expr_i>) return result;
				mv().visitJumpInsn( valCompiler.compileComparison( Opcodes.IFGT, Opcodes.DCMPL ), done );
				// result++;
				mv().iinc( l_result, 1 );
			}
			else {
				// else if (val < <expr_i>) return result;
				mv().visitJumpInsn( valCompiler.compileComparison( Opcodes.IFLT, Opcodes.DCMPG ), done );
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
		numCompiler.compileConversionFromInt();
	}


}
