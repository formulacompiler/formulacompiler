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
import org.formulacompiler.compiler.internal.expressions.ArrayDescriptor;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

final class ArrayAccessorForConstDataCompiler extends ArrayAccessorCompiler
{

	public ArrayAccessorForConstDataCompiler( SectionCompiler _section, String _name,
			ExpressionNodeForArrayReference _node )
	{
		super( _section, "$constarr$" + _name, _node );
	}


	@Override
	protected void compileBody() throws CompilerException
	{
		final GeneratorAdapter mv = mv();
		final ArrayDescriptor dim = this.arrayNode.arrayDescriptor();
		final int n = dim.numberOfElements();
		final DataType eltDataType = this.arrayNode.getDataType();
		final TypeCompiler eltCompiler = section().engineCompiler().typeCompiler( eltDataType );
		final Type eltType = eltCompiler.type();

		// private double[] xy;
		final FieldVisitor fv = cw().visitField( Opcodes.ACC_PRIVATE, methodName(), arrayDescriptor(), null, null );
		fv.visitEnd();

		// if (this.xy == null) {
		final Label skipInit = mv.newLabel();
		mv.loadThis();
		mv.visitFieldInsn( Opcodes.GETFIELD, section().classInternalName(), methodName(), arrayDescriptor() );
		mv.ifNonNull( skipInit );

		// ... new double[ n ]
		mv.loadThis();
		mv.push( n );
		mv.newArray( eltType );

		// ... { c1, c2, ... }
		int i = 0;
		for (ExpressionNode elt : this.arrayNode.arguments()) {
			if (elt instanceof ExpressionNodeForConstantValue) {
				mv.visitInsn( Opcodes.DUP );
				mv.push( i );
				final ExpressionNodeForConstantValue constElt = (ExpressionNodeForConstantValue) elt;
				eltCompiler.compileConst( mv, constElt.value() );
				mv.arrayStore( eltType );
			}
			i++;
		}

		// this.xy *=* new double[] { ... }
		mv.visitFieldInsn( Opcodes.PUTFIELD, section().classInternalName(), methodName(), arrayDescriptor() );

		// }
		// return this.xy;
		mv.mark( skipInit );
		mv.loadThis();
		mv.visitFieldInsn( Opcodes.GETFIELD, section().classInternalName(), methodName(), arrayDescriptor() );
		mv.visitInsn( Opcodes.ARETURN );
	}


}
