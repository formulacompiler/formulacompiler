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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.CompilerException;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;


final class SubSectionLazyGetterCompiler extends FinalMethodCompiler
{
	private static final Type ITERATOR_INTF = Type.getType( Iterator.class );
	private static final Type ITERABLE_INTF = Type.getType( Iterable.class );
	private static final Type COLLECTION_INTF = Type.getType( Collection.class );
	private static final Type ARRAYLIST_CLASS = Type.getType( ArrayList.class );

	private final SubSectionCompiler sub;


	SubSectionLazyGetterCompiler( SectionCompiler _section, SubSectionCompiler _sub )
	{
		super( _section, 0, _sub.getterName(), _sub.getterDescriptor() );
		this.sub = _sub;
	}


	@Override
	protected void compileBody() throws CompilerException
	{
		final SubSectionCompiler sub = this.sub;
		final GeneratorAdapter mv = mv();

		// if (this.field == null) {
		final Label alreadySet = mv.newLabel();
		mv.loadThis();
		mv.getField( section().classType(), sub.getterName(), sub.arrayType() );
		mv.ifNonNull( alreadySet );

		// ~ final DetailInput[] ds = this.inputs.getarray();
		final CallFrame inputCall = sub.model().getCallChainToCall();
		final Class inputContainerClass = inputCall.getMethod().getReturnType();
		final Type inputContainerType = Type.getType( inputContainerClass );
		final int l_ds = mv.newLocal( inputContainerType );
		compileInputGetterCall( inputCall );
		mv.storeLocal( l_ds );

		// ~ if (ds != null) {
		final Label isNull = mv.newLabel();
		mv.loadLocal( l_ds );
		mv.ifNull( isNull );

		int l_di;
		if (inputContainerClass.isArray()) {
			l_di = compileInitFromArray( sub, mv, l_ds );
		}
		else if (Collection.class.isAssignableFrom( inputContainerClass )) {
			l_di = compileInitFromCollection( sub, mv, l_ds );
		}
		else if (Iterable.class.isAssignableFrom( inputContainerClass )) {
			final int l_it = mv.newLocal( ITERATOR_INTF );
			mv.loadLocal( l_ds );
			mv.visitMethodInsn( Opcodes.INVOKEINTERFACE, ITERABLE_INTF.getInternalName(), "iterator", "()"
					+ ITERATOR_INTF.getDescriptor() );
			mv.storeLocal( l_it );
			l_di = compileInitFromIterator( sub, mv, l_it );
		}
		else if (Iterator.class.isAssignableFrom( inputContainerClass )) {
			l_di = compileInitFromIterator( sub, mv, l_ds );
		}
		else {
			throw new CompilerException.UnsupportedDataType( "The return type of '"
					+ inputCall.getMethod() + "' is not supported as input to a repeating section." );
		}

		// ~ ~ this.field = di;
		mv.loadThis();
		mv.loadLocal( l_di );
		mv.putField( section().classType(), sub.getterName(), sub.arrayType() );

		// ~ } else {
		mv.goTo( alreadySet );
		mv.mark( isNull );

		// ~ ~ this.field = new DetailPrototype[ 0 ];
		mv.loadThis();
		mv.push( 0 );
		mv.newArray( sub.classType() );
		mv.putField( section().classType(), sub.getterName(), sub.arrayType() );

		// ~ }
		// }
		mv.mark( alreadySet );

		// return this.field;
		mv.loadThis();
		mv.getField( section().classType(), sub.getterName(), sub.arrayType() );
		mv.visitInsn( Opcodes.ARETURN );
	}


	private int compileInitFromArray( final SubSectionCompiler sub, final GeneratorAdapter mv, final int l_ds )
	{
		// final int dl = ds.length;
		final int l_dl = mv.newLocal( Type.INT_TYPE );
		mv.loadLocal( l_ds );
		mv.arrayLength();
		mv.storeLocal( l_dl );

		// DetailImpl[] di = new DetailPrototype[ dl ];
		final int l_di = mv.newLocal( sub.arrayType() );
		mv.loadLocal( l_dl );
		mv.newArray( sub.classType() );
		mv.storeLocal( l_di );

		// for (int i = 0; i < dl; i++) {
		final int l_i = mv.newLocal( Type.INT_TYPE );
		final Label next = mv.newLabel();
		mv.push( 0 );
		mv.storeLocal( l_i );
		mv.goTo( next );
		final Label again = mv.mark();

		// ~ di[ i ] = new DetailPrototype( ds[ i ], this );
		mv.loadLocal( l_di );
		mv.loadLocal( l_i );
		mv.newInstance( sub.classType() );
		mv.dup();
		mv.loadLocal( l_ds );
		mv.loadLocal( l_i );
		mv.arrayLoad( sub.inputType() );
		final IndexCompiler ic = sub.isComputationListenerEnabled() ? new IndexCompiler( l_i ) : null;
		compileInit( mv, sub, ic );
		mv.arrayStore( sub.classType() );

		// }
		mv.iinc( l_i, 1 );
		mv.mark( next );
		mv.loadLocal( l_i );
		mv.loadLocal( l_dl );
		mv.ifCmp( Type.INT_TYPE, mv.LT, again );

		return l_di;
	}


	private int compileInitFromCollection( final SubSectionCompiler sub, final GeneratorAdapter mv, final int l_dc )
	{
		final String n_iter = ITERATOR_INTF.getInternalName();
		final String n_coll = COLLECTION_INTF.getInternalName();

		// final int dl = dc.size();
		final int l_dl = mv.newLocal( Type.INT_TYPE );
		mv.loadLocal( l_dc );
		mv.visitMethodInsn( Opcodes.INVOKEINTERFACE, n_coll, "size", "()I" );
		mv.storeLocal( l_dl );

		// final Iterator<DetailInput> ds = dc.iterator();
		final int l_ds = mv.newLocal( ITERATOR_INTF );
		mv.loadLocal( l_dc );
		mv.visitMethodInsn( Opcodes.INVOKEINTERFACE, n_coll, "iterator", "()" + ITERATOR_INTF.getDescriptor() );
		mv.storeLocal( l_ds );

		// DetailImpl[] di = new DetailPrototype[ dl ];
		final int l_di = mv.newLocal( sub.arrayType() );
		mv.loadLocal( l_dl );
		mv.newArray( sub.classType() );
		mv.storeLocal( l_di );

		// for (int i = 0; i < dl; i++) {
		final int l_i = mv.newLocal( Type.INT_TYPE );
		final Label next = mv.newLabel();
		mv.push( 0 );
		mv.storeLocal( l_i );
		mv.goTo( next );
		final Label again = mv.mark();

		// ~ di[ i ] = new DetailPrototype( ds.next(), this );
		mv.loadLocal( l_di );
		mv.loadLocal( l_i );
		mv.newInstance( sub.classType() );
		mv.dup();
		mv.loadLocal( l_ds );
		mv.visitMethodInsn( Opcodes.INVOKEINTERFACE, n_iter, "next", "()Ljava/lang/Object;" );
		mv.checkCast( sub.inputType() );
		final IndexCompiler ic = sub.isComputationListenerEnabled() ? new IndexCompiler( l_i ) : null;
		compileInit( mv, sub, ic );
		mv.arrayStore( sub.classType() );

		// }
		mv.iinc( l_i, 1 );
		mv.mark( next );
		mv.loadLocal( l_i );
		mv.loadLocal( l_dl );
		mv.ifCmp( Type.INT_TYPE, mv.LT, again );

		return l_di;
	}


	private int compileInitFromIterator( final SubSectionCompiler sub, final GeneratorAdapter mv, final int l_ds )
	{
		final String n_iter = ITERATOR_INTF.getInternalName();
		final String n_coll = COLLECTION_INTF.getInternalName();
		final String n_arraylist = ARRAYLIST_CLASS.getInternalName();

		// final Collection<DetailPrototype> coll = new ArrayList<DetailPrototype>();
		final int l_coll = mv.newLocal( ARRAYLIST_CLASS );
		mv.newInstance( ARRAYLIST_CLASS );
		mv.dup();
		mv.visitMethodInsn( Opcodes.INVOKESPECIAL, n_arraylist, "<init>", "()V" );
		mv.storeLocal( l_coll );

		final IndexCompiler ic;
		if (sub.isComputationListenerEnabled()) {
			int l_i = mv.newLocal( Type.INT_TYPE );
			mv.push( 0 );
			mv.storeLocal( l_i );
			ic = new IndexCompiler( l_i );
		}
		else ic = null;

		// while (ds.hasNext()) {
		final Label next = mv.newLabel();
		mv.goTo( next );
		final Label again = mv.mark();

		// ~ coll.add( new DetailPrototype( ds.next(), this ) );
		mv.loadLocal( l_coll );
		mv.newInstance( sub.classType() );
		mv.dup();
		mv.loadLocal( l_ds );
		mv.visitMethodInsn( Opcodes.INVOKEINTERFACE, n_iter, "next", "()Ljava/lang/Object;" );
		mv.checkCast( sub.inputType() );
		compileInit( mv, sub, ic );
		mv.visitMethodInsn( Opcodes.INVOKEINTERFACE, n_coll, "add", "(Ljava/lang/Object;)Z" );
		mv.pop();

		// index++;
		if (ic != null) ic.compileInc( mv );

		// }
		mv.mark( next );
		mv.loadLocal( l_ds );
		mv.visitMethodInsn( Opcodes.INVOKEINTERFACE, n_iter, "hasNext", "()Z" );
		mv.ifZCmp( mv.NE, again );

		// final DetailPrototype[] di = coll.toArray( new DetailPrototype[ coll.size() ] );
		final int l_di = mv.newLocal( sub.arrayType() );
		mv.loadLocal( l_coll );
		mv.loadLocal( l_coll );
		mv.visitMethodInsn( Opcodes.INVOKEINTERFACE, n_coll, "size", "()I" );
		mv.newArray( sub.classType() );
		mv.visitMethodInsn( Opcodes.INVOKEINTERFACE, n_coll, "toArray", "([Ljava/lang/Object;)[Ljava/lang/Object;" );
		mv.checkCast( sub.arrayType() );
		mv.storeLocal( l_di );

		return l_di;
	}

	private void compileInit( final GeneratorAdapter _mv, final SubSectionCompiler _sub, IndexCompiler _ic )
	{
		final StringBuilder descriptor = new StringBuilder( "(" );
		descriptor.append( _sub.inputType().getDescriptor() );
		descriptor.append( section().classDescriptor() );
		if (_ic != null) descriptor.append( ByteCodeEngineCompiler.INDEX_TYPE.getDescriptor() );
		descriptor.append( ")V" );

		_mv.loadThis();
		// Section index as 3rd parameter if needed
		if (_ic != null) {
			_ic.compileLoad( _mv );
		}

		_mv.visitMethodInsn( Opcodes.INVOKESPECIAL, _sub.classInternalName(), "<init>", descriptor.toString() );
	}


	private static class IndexCompiler
	{
		private final int indexLocal;

		public IndexCompiler( final int _indexLocal )
		{
			this.indexLocal = _indexLocal;
		}

		public void compileLoad( final GeneratorAdapter _mv )
		{
			_mv.loadLocal( this.indexLocal );
		}

		public void compileInc( final GeneratorAdapter _mv )
		{
			_mv.iinc( this.indexLocal, 1 );
		}

	}


}
