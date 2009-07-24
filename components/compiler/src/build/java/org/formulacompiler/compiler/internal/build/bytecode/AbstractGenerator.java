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

package org.formulacompiler.compiler.internal.build.bytecode;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.formulacompiler.compiler.internal.DescriptionBuilder;
import org.formulacompiler.runtime.ComputationMode;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;


@SuppressWarnings( "unqualified-field-access" )
abstract class AbstractGenerator
{
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


	private static final Pattern METHOD_NAME_PATTERN = Pattern.compile( "([^_]*)(?:_(.*?)(?:__if_(.*?))?(?:__for_(.*?))?)?" );

	protected abstract class AbstractMethodTemplateGenerator
	{
		final MethodNode mtdNode;
		final Type[] argTypes;
		final Type returnType;
		final int cardinality;
		final String enumName;
		final String ifCond;
		final ComputationMode computationMode;

		public AbstractMethodTemplateGenerator( MethodNode _mtdNode )
		{
			super();
			this.mtdNode = _mtdNode;
			this.argTypes = Type.getArgumentTypes( mtdNode.desc );
			this.returnType = Type.getReturnType( mtdNode.desc );
			this.cardinality = argTypes.length;
			// split name
			final String n = _mtdNode.name;
			final Matcher matcher = METHOD_NAME_PATTERN.matcher( n );
			matcher.matches();
			this.enumName = matcher.group( 2 );
			this.ifCond = matcher.group( 3 );
			final String forMode = matcher.group( 4 );
			this.computationMode = (forMode != null) ? ComputationMode.valueOf( forMode ) : null;
		}

	}


}
