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

import java.util.Collection;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ArrayDescriptor;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForLetVar;
import org.formulacompiler.compiler.internal.expressions.LetDictionary.LetEntry;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForSubSectionModel;
import org.objectweb.asm.Type;


abstract class HelperCompiler extends ValueMethodCompiler
{
	private final ExpressionNode node;


	HelperCompiler( SectionCompiler _section, int _access, ExpressionNode _node, Iterable<LetEntry> _closure,
			Type... _params )
	{
		super( _section, _access, _section.newGetterName(), descriptorOf( _params ) + descriptorOf( _section, _closure ),
				_node.getDataType() );
		this.node = _node;
		addClosureToLetDict( _closure, sizeOf( _params ) );
	}

	HelperCompiler( SectionCompiler _section, ExpressionNode _node, Iterable<LetEntry> _closure )
	{
		this( _section, 0, _node, _closure );
	}

	private static String descriptorOf( Type[] _params )
	{
		StringBuffer b = new StringBuffer();
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


	protected final ExpressionNode[] arrayRefElements( ExpressionNode _outerNode, ExpressionNode _arrayRefNode )
			throws CompilerException
	{
		if (_arrayRefNode instanceof ExpressionNodeForArrayReference) {
			final Collection<ExpressionNode> args = _arrayRefNode.arguments();
			return args.toArray( new ExpressionNode[ args.size() ] );
		}
		else if (_arrayRefNode instanceof ExpressionNodeForLetVar) {
			final ExpressionNodeForLetVar var = (ExpressionNodeForLetVar) _arrayRefNode;
			final Object res = letDict().lookup( var.varName() );
			final ExpressionNode val = (ExpressionNode) res;
			return arrayRefElements( _outerNode, val );
		}
		else {
			throw new CompilerException.UnsupportedExpression( "Array reference expected in "
					+ _outerNode.describe() + "." );
		}
	}


	protected final ArrayDescriptor arrayDescriptor( ExpressionNode _outerNode, ExpressionNode _rangeNode )
			throws CompilerException
	{
		if (_rangeNode instanceof ExpressionNodeForArrayReference) {
			return ((ExpressionNodeForArrayReference) _rangeNode).arrayDescriptor();
		}
		else {
			throw new CompilerException.UnsupportedExpression( "Array reference expected in "
					+ _outerNode.describe() + "." );
		}
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
