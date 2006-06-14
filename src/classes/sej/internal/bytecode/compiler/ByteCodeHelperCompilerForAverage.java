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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import sej.CompilerError;
import sej.expressions.ExpressionNode;
import sej.expressions.ExpressionNodeForAggregator;
import sej.expressions.Operator;
import sej.internal.model.ExpressionNodeForPartialAggregation;
import sej.internal.model.ExpressionNodeForSubSectionModel;
import sej.internal.model.Aggregation.NonNullCountingAggregation;


final class ByteCodeHelperCompilerForAverage extends ByteCodeHelperCompiler
{
	private final ExpressionNodeForAggregator node;


	ByteCodeHelperCompilerForAverage(ByteCodeSectionCompiler _section, ExpressionNodeForAggregator _node)
	{
		super( _section );
		this.node = _node;
	}


	@Override
	protected void compileBody() throws CompilerError
	{
		compileMapReduceAggregator( this.node, Operator.PLUS );
		int divisor = this.node.getArguments().size();
		if (this.node instanceof ExpressionNodeForPartialAggregation) {
			final ExpressionNodeForPartialAggregation partialAggNode = (ExpressionNodeForPartialAggregation) this.node;
			final NonNullCountingAggregation partialAverage = (NonNullCountingAggregation) partialAggNode.getPartialAggregation();
			divisor += partialAverage.numberOfNonNullArguments; 
		}
		compileConst( divisor );
		getNumericType().compile( mv(), Operator.DIV, 2 );
	}


	/**
	 * The following is a sketch of what we will need to support dynamic sections.
	 *  
	 * @throws CompilerError
	 */
	void compileBodyFuture() throws CompilerError
	{
		final int varN = mv().newLocal( Type.LONG_TYPE );
		//final int varSum = mv().newLocal( getNumericType().getType() );
		
		if (this.node instanceof ExpressionNodeForPartialAggregation) {
			final ExpressionNodeForPartialAggregation partialAggNode = (ExpressionNodeForPartialAggregation) this.node;
			final NonNullCountingAggregation partialAverage = (NonNullCountingAggregation) partialAggNode.getPartialAggregation();
			mv().push( partialAverage.numberOfNonNullArguments );
			mv().storeLocal( varN );
			compileConst( partialAverage.accumulator );
			//mv().storeLocal( varSum );
		}
		else {
			mv().visitInsn( Opcodes.LCONST_0 );
			mv().storeLocal( varN );
			getNumericType().compileZero( mv() );
			//mv().storeLocal( varSum );
		}

		for (ExpressionNode arg : this.node.getArguments()) {
			//mv().loadLocal( varSum );

			if (arg instanceof ExpressionNodeForSubSectionModel) {
				unsupported( arg );
			}
			else {
				compileExpr( arg );
			}
			
			getNumericType().compile( mv(), Operator.PLUS, 2 );

			//mv().storeLocal( varSum );
			mv().iinc( varN, 1 );
		}

		//mv().loadLocal( varSum );
		mv().loadLocal( varN );
		// getNumericType().compileDivByUnscaledLong( mv() );
	}


}
