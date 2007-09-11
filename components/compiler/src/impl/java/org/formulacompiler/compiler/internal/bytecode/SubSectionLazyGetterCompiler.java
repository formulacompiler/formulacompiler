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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.CompilerException;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;


final class SubSectionLazyGetterCompiler extends MethodCompiler
{
	private static final Type ITERATOR_INTF = Type.getType( Iterator.class );
	private static final Type ITERABLE_INTF = Type.getType( Iterable.class );
	private static final Type COLLECTION_INTF = Type.getType( Collection.class );
	private static final Type ARRAYLIST_CLASS = Type.getType( ArrayList.class );

	private final SubSectionCompiler sub;


	SubSectionLazyGetterCompiler(SectionCompiler _section, SubSectionCompiler _sub)
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
		compileInit( mv, sub );
		mv.arrayStore( sub.classType() );

		// }
		mv.iinc( l_i, 1 );
		mv.mark( next );
		mv.loadLocal( l_i );
		mv.loadLocal( l_dl );
		mv.ifCmp( Type.INT_TYPE, mv.LT, again );

		return l_di;
	}


	private int compileInitFromCollection( final SubSectionCompiler sub, final GeneratorAdapter mv,
			final int l_dc )
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
		compileInit( mv, sub );
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
		compileInit( mv, sub );
		mv.visitMethodInsn( Opcodes.INVOKEINTERFACE, n_coll, "add", "(Ljava/lang/Object;)Z" );
		mv.pop();

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


	private void compileInit( final GeneratorAdapter _mv, final SubSectionCompiler _sub )
	{
		_mv.loadThis();
		_mv.visitMethodInsn( Opcodes.INVOKESPECIAL, _sub.classInternalName(), "<init>", "("
				+ _sub.inputType().getDescriptor() + section().classDescriptor() + ")V" );
	}


}
