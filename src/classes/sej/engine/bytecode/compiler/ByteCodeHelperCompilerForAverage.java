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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import sej.ModelError;
import sej.engine.compiler.ValueType;
import sej.engine.compiler.model.ExpressionNodeForSubSectionModel;
import sej.engine.expressions.Aggregator;
import sej.engine.expressions.ExpressionNode;
import sej.engine.expressions.ExpressionNodeForAggregator;


final class ByteCodeHelperCompilerForAverage extends ByteCodeHelperCompiler
{
	private final ExpressionNodeForAggregator node;


	public ByteCodeHelperCompilerForAverage(ByteCodeSectionCompiler _section, ExpressionNodeForAggregator _node) throws ModelError
	{
		super( _section, ValueType.DOUBLE ); // TODO 
		this.node = _node;
	}


	@Override
	protected void compileBody() throws ModelError
	{
		final int varN = mv().newLocal( Type.INT_TYPE );
		final int varSum = mv().newLocal( Type.DOUBLE_TYPE );

		if (this.node.getPartialAggregation() instanceof Aggregator.DoubleValuedCountingAggregation) {
			Aggregator.DoubleValuedCountingAggregation partialAverage = (Aggregator.DoubleValuedCountingAggregation) this.node
					.getPartialAggregation();
			compileConst( partialAverage.getAccumulator() );
			mv().visitVarInsn( Opcodes.DSTORE, varSum );
			mv().push( partialAverage.getCount() );
			mv().visitVarInsn( Opcodes.ISTORE, varN );
		}
		else {
			mv().visitInsn( Opcodes.DCONST_0 );
			mv().visitVarInsn( Opcodes.DSTORE, varSum );
			mv().visitInsn( Opcodes.ICONST_0 );
			mv().visitVarInsn( Opcodes.ISTORE, varN );
		}

		for (ExpressionNode arg : this.node.getArguments()) {
			mv().visitVarInsn( Opcodes.DLOAD, varSum );

			if (arg instanceof ExpressionNodeForSubSectionModel) {
				unsupported( arg );
			}
			else {
				compileExpr( arg );
			}

			mv().visitInsn( Opcodes.DADD );
			mv().visitVarInsn( Opcodes.DSTORE, varSum );
			mv().visitIincInsn( varN, 1 );
		}

		mv().visitVarInsn( Opcodes.DLOAD, varSum );
		mv().visitVarInsn( Opcodes.ILOAD, varN );
		mv().visitInsn( Opcodes.I2D );
		mv().visitInsn( Opcodes.DDIV );
	}


}
