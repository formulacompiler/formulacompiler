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
