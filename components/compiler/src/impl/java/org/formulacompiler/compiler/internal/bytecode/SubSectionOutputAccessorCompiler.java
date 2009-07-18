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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.CompilerException;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;


final class SubSectionOutputAccessorCompiler extends FinalMethodCompiler
{
	private static final Type ITERATOR_INTF = Type.getType( Iterator.class );
	private static final Type ARRAYLIST_CLASS = Type.getType( ArrayList.class );

	private final SubSectionCompiler sub;
	private final CallFrame callToImplement;


	SubSectionOutputAccessorCompiler( SectionCompiler _section, SubSectionCompiler _sub, CallFrame _callToImplement )
	{
		super( _section, Opcodes.ACC_PUBLIC, _callToImplement.getMethod().getName(), Type
				.getMethodDescriptor( _callToImplement.getMethod() ) );
		this.sub = _sub;
		this.callToImplement = _callToImplement;
	}


	@SuppressWarnings( "unchecked" )
	@Override
	protected void compileBody() throws CompilerException
	{
		final SubSectionCompiler sub = this.sub;
		final GeneratorAdapter mv = mv();

		final CallFrame outputCall = this.callToImplement;
		final Class outputContainerClass = outputCall.getMethod().getReturnType();

		// get$Sect0()
		mv.loadThis();
		mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, section().classInternalName(), sub.getterName(), sub
				.getterDescriptor() );

		if (outputContainerClass.isArray()) {
			mv.visitInsn( Opcodes.ARETURN );
		}
		else {
			// Detail[] arr = get$Sect0();
			final int l_arr = mv.newLocal( sub.arrayType() );
			mv.storeLocal( l_arr );

			final int l_len = mv.newLocal( Type.INT_TYPE );
			mv.loadLocal( l_arr );
			mv.arrayLength();
			mv.storeLocal( l_len );

			// List lst = new ArrayList( arr.length );
			final int l_lst = mv.newLocal( ARRAYLIST_CLASS );
			mv.newInstance( ARRAYLIST_CLASS );
			mv.dup();
			mv.loadLocal( l_len );
			mv.visitMethodInsn( Opcodes.INVOKESPECIAL, ARRAYLIST_CLASS.getInternalName(), "<init>", "(I)V" );
			mv.storeLocal( l_lst );

			// for (int i = 0; i < len; i++) {
			final int l_i = mv.newLocal( Type.INT_TYPE );
			mv.push( 0 );
			mv.storeLocal( l_i );
			final Label test = mv.newLabel();
			mv.goTo( test );
			final Label again = mv.mark();

			// lst.add( arr[ i ] );
			mv.loadLocal( l_lst );
			mv.loadLocal( l_arr );
			mv.loadLocal( l_i );
			mv.arrayLoad( sub.classType() );
			mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, ARRAYLIST_CLASS.getInternalName(), "add", "(Ljava/lang/Object;)Z" );
			mv.pop();

			// } // for
			mv.iinc( l_i, 1 );
			mv.mark( test );
			mv.loadLocal( l_i );
			mv.loadLocal( l_len );
			mv.ifCmp( Type.INT_TYPE, mv.LT, again );

			mv.loadLocal( l_lst );
			if (outputContainerClass.isAssignableFrom( List.class )) {
				// return lst;
				mv.visitInsn( Opcodes.ARETURN );
			}
			else if (outputContainerClass.isAssignableFrom( Iterator.class )) {
				// return lst.iterator();
				mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, ARRAYLIST_CLASS.getInternalName(), "iterator", "()"
						+ ITERATOR_INTF.getDescriptor() );
				mv.visitInsn( Opcodes.ARETURN );
			}
			else {
				throw new CompilerException.UnsupportedDataType( "The return type of '"
						+ outputCall.getMethod() + "' is not supported as input to a repeating section." );
			}
		}
	}

}
