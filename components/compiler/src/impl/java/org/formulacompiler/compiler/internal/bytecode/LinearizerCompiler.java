/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
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

import org.formulacompiler.compiler.CompilerException;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;


final class LinearizerCompiler extends MethodCompiler
{
	private final int rows;
	private final int cols;


	LinearizerCompiler( SectionCompiler _section, int _rows, int _cols )
	{
		super( _section, Opcodes.ACC_FINAL, "$lin$" + _rows + "$" + _cols, "(II)I" );
		this.rows = _rows;
		this.cols = _cols;
	}


	@Override
	protected void compileBody() throws CompilerException
	{
		final GeneratorAdapter mv = mv();
		final Label outOfRange = mv.newLabel();

		// range check row
		mv.visitVarInsn( Opcodes.ILOAD, 1 ); // row
		mv.push( 1 );
		mv.ifICmp( mv.LT, outOfRange );
		mv.visitVarInsn( Opcodes.ILOAD, 1 ); // row
		mv.push( this.rows );
		mv.ifICmp( mv.GT, outOfRange );

		// range check col
		mv.visitVarInsn( Opcodes.ILOAD, 2 ); // col
		mv.push( 1 );
		mv.ifICmp( mv.LT, outOfRange );
		mv.visitVarInsn( Opcodes.ILOAD, 2 ); // col
		mv.push( this.cols );
		mv.ifICmp( mv.GT, outOfRange );

		// (<row> - 1) * <num_cols>) + (<col> - 1);
		mv.visitVarInsn( Opcodes.ILOAD, 1 ); // row
		mv.push( 1 );
		mv.visitInsn( Opcodes.ISUB );
		mv.push( this.cols );
		mv.visitInsn( Opcodes.IMUL );
		mv.visitVarInsn( Opcodes.ILOAD, 2 ); // col
		mv.push( 1 );
		mv.visitInsn( Opcodes.ISUB );
		mv.visitInsn( Opcodes.IADD );
		mv.visitInsn( Opcodes.IRETURN );

		mv.visitLabel( outOfRange );
		mv.throwException( ExpressionCompiler.FORMULA_ERROR_TYPE, "#VALUE/REF! because index is out of range in INDEX" );
	}


}
