/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
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
package sej.internal.build.bytecode;

import java.io.IOException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import sej.describable.DescriptionBuilder;

@SuppressWarnings("unqualified-field-access")
abstract class AbstractGenerator
{
	static final String IF_CLAUSE = "__if_";
	static final int IF_CLAUSE_LEN = IF_CLAUSE.length();
	
	final DescriptionBuilder classBuilder = new DescriptionBuilder();
	final DispatchBuilder unaryOperatorDispatchBuilder = new DispatchBuilder();
	final DispatchBuilder binaryOperatorDispatchBuilder = new DispatchBuilder();
	final DispatchBuilder functionDispatchBuilder = new DispatchBuilder();
	final String superName;
	final String typeName;
	final Class cls;
	final ClassNode clsNode;


	public AbstractGenerator(Class _template, String _typeName, String _superName)
			throws IOException
	{
		super();
		this.cls = _template;
		this.typeName = _typeName;
		this.superName = _superName;
		this.clsNode = new ClassNode();
		new ClassReader( _template.getCanonicalName() ).accept( clsNode, true );
		this.unaryOperatorDispatchBuilder.indent( 4 );
		this.binaryOperatorDispatchBuilder.indent( 4 );
		this.functionDispatchBuilder.indent( 3 );
	}


	protected abstract class AbstractMethodTemplateGenerator
	{
		final MethodNode mtdNode;
		final Type[] argTypes;
		final Type returnType;
		final int cardinality;
		final String enumName;
		final String ifCond;

		public AbstractMethodTemplateGenerator(MethodNode _mtdNode)
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
