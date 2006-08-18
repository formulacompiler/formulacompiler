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
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.model.ExpressionNodeForRangeValue;
import sej.internal.model.RangeValue;


abstract class HelperCompiler extends NumericMethodCompiler
{

	HelperCompiler(SectionCompiler _section) 
	{
		super( _section, _section.newGetterName() );
	}

	HelperCompiler(SectionCompiler _section, int _access) 
	{
		super( _section, _access, _section.newGetterName() );
	}

	
	protected ExpressionNode[] rangeElements( ExpressionNode _outerNode, ExpressionNode _rangeNode ) throws CompilerException 
	{
		if (_rangeNode instanceof ExpressionNodeForRangeValue) {
			final Collection<ExpressionNode> args = _rangeNode.arguments();
			return args.toArray( new ExpressionNode[ args.size() ] );
		}
		else if (_rangeNode instanceof ExpressionNodeForConstantValue) {
			final RangeValue rangeVal = (RangeValue) ((ExpressionNodeForConstantValue) _rangeNode).getValue();
			final int rangeSize = rangeVal.getNumberOfSheets() * rangeVal.getNumberOfRows() * rangeVal.getNumberOfColumns();
			ExpressionNode[] result = new ExpressionNode[ rangeSize ];
			final Iterator<Object> rangeIter = rangeVal.iterator();
			int i = 0;
			while (rangeIter.hasNext()) {
				result[ i++ ] = new ExpressionNodeForConstantValue( rangeIter.next() );
			}
			return result;
		}
		else {
			unsupported( _outerNode );
			return null; 
		}
	}
	

	protected RangeValue range( ExpressionNode _outerNode, ExpressionNode _rangeNode ) throws CompilerException 
	{
		if (_rangeNode instanceof ExpressionNodeForRangeValue) {
			return ((ExpressionNodeForRangeValue) _rangeNode).getRangeValue();
		}
		else if (_rangeNode instanceof ExpressionNodeForConstantValue) {
			return (RangeValue) ((ExpressionNodeForConstantValue) _rangeNode).getValue();
		}
		else {
			unsupported( _outerNode );
			return null; 
		}
	}
	
}
