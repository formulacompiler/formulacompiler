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
package sej.internal.bytecode.compiler;

import java.util.Collection;
import java.util.Iterator;

import sej.CompilerException;
import sej.internal.expressions.ArrayDescriptor;
import sej.internal.expressions.ArrayValue;
import sej.internal.expressions.DataType;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForArrayReference;
import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.expressions.LetDictionary.LetEntry;


abstract class HelperCompiler extends ValueMethodCompiler
{
	private final ExpressionNode node;


	HelperCompiler(SectionCompiler _section, int _access, ExpressionNode _node, Iterable<LetEntry> _closure)
	{
		super( _section, _access, _section.newGetterName(), descriptorOf( _section, _closure ), _node.getDataType() );
		this.node = _node;
		addClosureToLetDict( _closure );
	}

	HelperCompiler(SectionCompiler _section, ExpressionNode _node, Iterable<LetEntry> _closure)
	{
		this( _section, 0, _node, _closure );
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
		else if (_arrayRefNode instanceof ExpressionNodeForConstantValue) {
			final ArrayValue arrVal = (ArrayValue) ((ExpressionNodeForConstantValue) _arrayRefNode).value();
			final DataType arrType = _arrayRefNode.getDataType();
			final int arrSize = arrVal.getNumberOfElements();
			final ExpressionNode[] result = new ExpressionNode[ arrSize ];
			final Iterator<Object> arrIter = arrVal.iterator();
			int i = 0;
			while (arrIter.hasNext()) {
				result[ i++ ] = new ExpressionNodeForConstantValue( arrIter.next(), arrType );
			}
			return result;
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
		else if (_rangeNode instanceof ExpressionNodeForConstantValue) {
			return (ArrayValue) ((ExpressionNodeForConstantValue) _rangeNode).value();
		}
		else {
			throw new CompilerException.UnsupportedExpression( "Array reference expected in "
					+ _outerNode.describe() + "." );
		}
	}

}
