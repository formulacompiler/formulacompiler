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

import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.LetDictionary.LetEntry;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForSubSectionModel;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


abstract class HelperCompiler extends ValueMethodCompiler
{
	private final ExpressionNode node;


	HelperCompiler( SectionCompiler _section, int _access, ExpressionNode _node, Iterable<LetEntry<Compilable>> _closure,
			Type... _params )
	{
		super( _section, _access, _section.newGetterName(), descriptorOf( _params ) + descriptorOf( _section, _closure ),
				_node.getDataType() );
		this.node = _node;
		addClosureToLetDict( _closure, sizeOf( _params ) );
	}

	HelperCompiler( SectionCompiler _section, ExpressionNode _node, Iterable<LetEntry<Compilable>> _closure )
	{
		this( _section, Opcodes.ACC_FINAL, _node, _closure );
	}

	private static String descriptorOf( Type[] _params )
	{
		final StringBuilder b = new StringBuilder();
		for (Type param : _params) {
			b.append( param.getDescriptor() );
		}
		return b.toString();
	}

	private int sizeOf( Type[] _params )
	{
		int size = 0;
		for (Type param : _params) {
			size += param.getSize();
		}
		return size;
	}


	protected final ExpressionNode node()
	{
		return this.node;
	}


	protected static final ExpressionNode firstLocalElementIn( Iterable<ExpressionNode> _elts )
	{
		for (ExpressionNode elt : _elts) {
			if (!(elt instanceof ExpressionNodeForSubSectionModel)) {
				return elt;
			}
		}
		return null;
	}

}
