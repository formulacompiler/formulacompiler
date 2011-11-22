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

import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;

abstract class ArrayAccessorCompiler extends FinalMethodCompiler
{
	protected final ExpressionNodeForArrayReference arrayNode;
	private final String arrayDescriptor;
	private final String elementDescriptor;

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
