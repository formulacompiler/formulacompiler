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

import java.util.List;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;


final class IndexerCompiler extends MethodCompiler
{
	private final ExpressionNodeForArrayReference node;


	IndexerCompiler( SectionCompiler _section, String _name, ExpressionNodeForArrayReference _node )
	{
		super( _section, 0, "$idx$" + _name, "(I)"
				+ _section.engineCompiler().typeCompiler( _node.getDataType() ).typeDescriptor() );
		this.node = _node;
	}


	@Override
	protected void compileBody() throws CompilerException
	{
		final ExpressionNodeForArrayReference arrayNode = this.node;
		final ExpressionCompiler valCompiler = expressionCompiler( arrayNode.getDataType() );
		final int valReturn = valCompiler.typeCompiler().returnOpcode();
		final GeneratorAdapter mv = mv();

		final List<ExpressionNode> vals = arrayNode.arguments();
		final int valCnt = vals.size();
		if (valCnt > 0) {
			mv.visitVarInsn( Opcodes.ILOAD, 1 ); // index

			// gen switch
			final int[] valIdxs = new int[ valCnt ];
			for (int i = 0; i < valCnt; i++) {
				valIdxs[ i ] = i;
			}
			compileTableSwitch( valIdxs, new TableSwitchGenerator()
			{

				@Override
				protected void generateCase( int _key, Label _end ) throws CompilerException
				{
					final ExpressionNode val = vals.get( _key );
					valCompiler.compile( val );
					mv.visitInsn( valReturn );
				}

			} );
		}
		mv().throwException( ExpressionCompiler.FORMULA_ERROR_TYPE, "#VALUE/REF! because index is out of range in INDEX" );
	}


}
