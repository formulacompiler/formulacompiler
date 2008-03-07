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

package org.formulacompiler.compiler.internal.build.bytecode;

import java.io.IOException;
import java.util.List;

import org.formulacompiler.compiler.internal.DescriptionBuilder;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;


@SuppressWarnings( "unqualified-field-access" )
abstract class AbstractGenerator
{
	static final String IF_CLAUSE = "__if_";
	static final int IF_CLAUSE_LEN = IF_CLAUSE.length();

	private static final int ACCEPT_FLAGS = ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG;

	final DescriptionBuilder classBuilder = new DescriptionBuilder();
	final DispatchBuilder unaryOperatorDispatchBuilder = new DispatchBuilder();
	final DispatchBuilder binaryOperatorDispatchBuilder = new DispatchBuilder();
	final DispatchBuilder functionDispatchBuilder = new DispatchBuilder();
	final String superName;
	final String typeName;
	final Class cls;
	final ClassNode clsNode;

	static boolean verbose = false;


	public AbstractGenerator( Class _template, String _typeName, String _superName ) throws IOException
	{
		super();
		this.cls = _template;
		this.typeName = _typeName;
		this.superName = _superName;
		this.clsNode = new ClassNode();
		new ClassReader( _template.getCanonicalName() ).accept( clsNode, ACCEPT_FLAGS );
		this.unaryOperatorDispatchBuilder.indent( 4 );
		this.binaryOperatorDispatchBuilder.indent( 4 );
		this.functionDispatchBuilder.indent( 3 );
	}


	protected void genMethods() throws IOException
	{
		if (this.verbose) System.out.println( "Processing " + clsNode.name );
		genMethods( clsNode.methods );
		if (!"java/lang/Object".equals( clsNode.superName )) {
			final ClassNode outer = new ClassNode();
			new ClassReader( clsNode.superName ).accept( outer, ACCEPT_FLAGS );
			if (this.verbose) System.out.println( "Processing " + outer.name );
			genMethods( outer.methods );
		}
	}

	protected final void genMethods( List _methods )
	{
		for (Object o : _methods) {
			MethodNode mtd = (MethodNode) o;
			if ((mtd.access & Opcodes.ACC_SYNTHETIC) == 0) {
				genMethod( mtd );
			}
		}
	}

	protected abstract void genMethod( MethodNode _method );


	protected abstract class AbstractMethodTemplateGenerator
	{
		final MethodNode mtdNode;
		final Type[] argTypes;
		final Type returnType;
		final int cardinality;
		final String enumName;
		final String ifCond;

		public AbstractMethodTemplateGenerator( MethodNode _mtdNode )
		{
			super();
			this.mtdNode = _mtdNode;
			this.argTypes = Type.getArgumentTypes( mtdNode.desc );
			this.returnType = Type.getReturnType( mtdNode.desc );
			this.cardinality = argTypes.length;
			// split name
			final String n = _mtdNode.name;
			final int p = n.indexOf( '_' );
			if (p < 0) {
				this.enumName = "";
				this.ifCond = "";
			}
			else {
				final String s = n.substring( p + 1 );
				final int pp = s.indexOf( IF_CLAUSE );
				if (pp < 0) {
					this.enumName = s;
					this.ifCond = null;
				}
				else {
					this.enumName = s.substring( 0, pp );
					this.ifCond = s.substring( pp + IF_CLAUSE_LEN );
				}
			}
		}

	}


}
