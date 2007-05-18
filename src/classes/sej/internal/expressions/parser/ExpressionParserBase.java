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
package sej.internal.expressions.parser;

import java.util.Collection;
import java.util.Deque;
import java.util.List;

import sej.compiler.CompilerException;
import sej.internal.expressions.ExpressionNode;
import sej.runtime.New;


/**
 * Provides a node stack for the recursive descent expression parser generated by JavaCC. Also adds
 * override points for implementing spreadsheet-specific elements like cells and ranges. Those are
 * not implemented here so the basic parser does not depend on {@link sej.spreadsheet}.
 * Nevertheless, it does "parse" the cell constructs because it seems impossible to add them in
 * derivations (JavaCC does not support inheritance and it would be hard for it to do so, too).
 * 
 * @author peo
 */
abstract class ExpressionParserBase
{
	private static final String ERR_NORANGES = "This parser does not support ranges.";

	/**
	 * Use a List instead of a Deque here so {@link #popNodes(int)} does not have to reverse
	 * elements.
	 */
	private List<ExpressionNode> nodes = New.newList();

	protected final void pushNode( ExpressionNode _node )
	{
		this.nodes.add( _node );
	}

	protected final ExpressionNode popNode()
	{
		return this.nodes.remove( this.nodes.size() - 1 );
	}

	/**
	 * Returns the top <i>n</i> nodes <em>in the order they were pushed</em> (FIFO). Popping the
	 * nodes individually would reverse this order (LIFO).
	 */
	protected final List<ExpressionNode> popNodes( int _howMany )
	{
		final int have = this.nodes.size();
		if (_howMany > have) {
			throw new IllegalArgumentException( "_howMany > have" );
		}
		else {
			final List<ExpressionNode> result = New.newList( _howMany );
			for (int i = have - _howMany; i < have; i++) {
				result.add( this.nodes.get( i ) );
			}
			for (int i = 1; i <= _howMany; i++) {
				this.nodes.remove( have - i );
			}
			return result;
		}
	}


	private Deque<Integer> marks = New.newDeque();

	/**
	 * Remembers the current node stack pointer for {@code popMarkedNodes()}. Calls are nestable.
	 */
	protected final void mark()
	{
		this.marks.push( this.nodes.size() );
	}

	/**
	 * Returns the top <i>n</i> nodes since the last {@code mark()}
	 * <em>in the order they were pushed</em> (FIFO).
	 */
	protected final List<ExpressionNode> popMarkedNodes()
	{
		final int mark = this.marks.pop();
		return popNodes( this.nodes.size() - mark );
	}


	/**
	 * Pops the top node and adds it as the last child to the then top node.
	 */
	protected final void popNodeAndMergeIntoTopNode()
	{
		final ExpressionNode popped = popNode();
		final ExpressionNode top = peekNode();
		top.addArgument( popped );
	}

	private final ExpressionNode peekNode()
	{
		return this.nodes.get( this.nodes.size() - 1 );
	}


	protected final int parseInt( String _text )
	{
		return Integer.parseInt( _text );
	}

	protected final double parseDouble( String _text )
	{
		return Double.parseDouble( _text );
	}


	protected final void unsupportedFunction( Token _name )
	{
		throw new InnerParserException( new CompilerException.UnsupportedExpression( "Unsupported function "
				+ _name.image + " encountered" ) );
	}


	protected ExpressionNode makeCellA1( Token _cell )
	{
		return makeCellA1( _cell, null );
	}

	protected ExpressionNode makeCellA1( Token _cell, Token _sheet )
	{
		throw new UnsupportedOperationException( "This parser does not support parsing A1-style cells." );
	}

	protected ExpressionNode makeCellR1C1( Token _cell )
	{
		return makeCellR1C1( _cell, null );
	}

	protected ExpressionNode makeCellR1C1( Token _cell, Token _sheet )
	{
		throw new UnsupportedOperationException( "This parser does not support parsing R1C1-style cells." );
	}

	protected ExpressionNode makeNamedCellRef( Token _name )
	{
		throw new UnsupportedOperationException( "This parser does not support parsing named references." );
	}

	protected boolean isRangeName( Token _name )
	{
		return false;
	}

	protected ExpressionNode makeNamedRangeRef( Token _name )
	{
		throw new UnsupportedOperationException( "This parser does not support parsing named references." );
	}

	protected Object makeCellRange( Collection<ExpressionNode> _nodes )
	{
		throw new UnsupportedOperationException( ERR_NORANGES );
	}

	protected ExpressionNode makeNodeForReference( Object _reference )
	{
		throw new UnsupportedOperationException( ERR_NORANGES );
	}

	protected ExpressionNode makeRangeIntersection( Collection<ExpressionNode> _firstTwoElements )
	{
		throw new UnsupportedOperationException( ERR_NORANGES );
	}

	protected ExpressionNode makeRangeUnion( Collection<ExpressionNode> _firstTwoElements )
	{
		throw new UnsupportedOperationException( ERR_NORANGES );
	}

	protected ExpressionNode makeShapedRange( ExpressionNode _range )
	{
		throw new UnsupportedOperationException( ERR_NORANGES );
	}


	protected static final class InnerParserException extends RuntimeException
	{
		public InnerParserException(Throwable _cause)
		{
			super( _cause );
		}
	}

}
