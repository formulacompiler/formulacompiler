/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * This file is part of the Abacus Formula Compiler (AFC).
 *
 * For commercial licensing, please contact sales(at)formulacompiler.com.
 *
 * AFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AFC.  If not, see <http://www.gnu.org/licenses/>.
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
