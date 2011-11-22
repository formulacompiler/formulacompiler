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
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

public class ArrayAccessorForFullDataCompiler extends ArrayAccessorCompiler
{

	public ArrayAccessorForFullDataCompiler( SectionCompiler _section, String _name,
			ExpressionNodeForArrayReference _node )
	{
		super( _section, "$arr$" + _name, _node );
	}


	@Override
	protected void compileBody() throws CompilerException
	{
		final GeneratorAdapter mv = mv();
		final DataType eltDataType = this.arrayNode.getDataType();
		final ExpressionCompiler eltCompiler = expressionCompiler( eltDataType );
		final Type eltType = eltCompiler.type();
		final String initName = methodName() + "$init";
		final String initDesc = "Z";

		// private boolean xy$init;
		final FieldVisitor fv = cw().visitField( Opcodes.ACC_PRIVATE, initName, initDesc, null, null );
		fv.visitEnd();

		// if (!this.xy$init) {
		final Label skipInit = mv.newLabel();
		mv.loadThis();
		mv.visitFieldInsn( Opcodes.GETFIELD, section().classInternalName(), initName, initDesc );
		mv.visitJumpInsn( Opcodes.IFNE, skipInit );

		// this.xy$init = true;
		mv.loadThis();
		mv.push( true );
		mv.visitFieldInsn( Opcodes.PUTFIELD, section().classInternalName(), initName, initDesc );

		// this.xy = { ?, c1, c2, ... }
		mv.loadThis();
		section().getArrayAccessorForConstDataOnly( this.arrayNode ).compileCall( mv );
		int i = 0;
		for (ExpressionNode elt : this.arrayNode.arguments()) {
			if (!(elt instanceof ExpressionNodeForConstantValue)) {
				mv.visitInsn( Opcodes.DUP );
				mv.visitIntInsn( Opcodes.BIPUSH, i );
				eltCompiler.compile( elt );
				mv.arrayStore( eltType );
			}
			i++;
		}
		// return this.xy;
		mv.visitInsn( Opcodes.ARETURN );

		// } else
		// return this.xy;
		mv.mark( skipInit );
		mv.loadThis();
		section().getArrayAccessorForConstDataOnly( this.arrayNode ).compileCall( mv );
		mv.visitInsn( Opcodes.ARETURN );

		if (section().hasReset()) {
			final GeneratorAdapter reset = section().resetter();
			// this.xy$init = false;
			reset.loadThis();
			reset.push( false );
			reset.visitFieldInsn( Opcodes.PUTFIELD, section().classInternalName(), initName, initDesc );
		}

	}


}
