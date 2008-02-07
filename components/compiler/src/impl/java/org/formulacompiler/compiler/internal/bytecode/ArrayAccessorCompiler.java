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

import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;

abstract class ArrayAccessorCompiler extends MethodCompiler
{
	protected final ExpressionNodeForArrayReference arrayNode;
	protected final String arrayDescriptor;
	protected final String elementDescriptor;

	protected ArrayAccessorCompiler( SectionCompiler _section, String _name, ExpressionNodeForArrayReference _node )
	{
		this( _section, _name, _node, _section.engineCompiler().typeCompiler( _node.getDataType() ).typeDescriptor() );
	}

	private ArrayAccessorCompiler( SectionCompiler _section, String _name, ExpressionNodeForArrayReference _node,
			String _elementDescriptor )
	{
		super( _section, 0, _name, "()[" + _elementDescriptor );
		this.arrayNode = _node;
		this.arrayDescriptor = "[" + _elementDescriptor;
		this.elementDescriptor = _elementDescriptor;
	}


	public String arrayDescriptor()
	{
		return this.arrayDescriptor;
	}

	public String elementDescriptor()
	{
		return this.elementDescriptor;
	}

}
