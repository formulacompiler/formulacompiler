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

package org.formulacompiler.spreadsheet.internal.parser;

import java.util.Collection;
import java.util.ListIterator;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForOperator;
import org.formulacompiler.compiler.internal.expressions.parser.CellRefFormat;
import org.formulacompiler.compiler.internal.expressions.parser.ExpressionParser;
import org.formulacompiler.compiler.internal.expressions.parser.Token;
import org.formulacompiler.runtime.internal.spreadsheet.CellAddressImpl;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.internal.BaseSpreadsheet;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellRange;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForCell;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForRange;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForRangeIntersection;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForRangeShape;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForRangeUnion;

public abstract class SpreadsheetExpressionParser extends ExpressionParser
{
	private final BaseSpreadsheet workbook;
	protected final CellIndex cellIndex;

	protected SpreadsheetExpressionParser( String _exprText, CellIndex _parseRelativeTo, CellRefFormat _cellRefFormat )
	{
		super( _exprText, _cellRefFormat );
		this.cellIndex = _parseRelativeTo;
		this.workbook = this.cellIndex.spreadsheet;
	}

	protected SpreadsheetExpressionParser( String _exprText, BaseSpreadsheet _workbook, CellRefFormat _cellRefFormat )
	{
		super( _exprText, _cellRefFormat );
		this.workbook = _workbook;
		this.cellIndex = new CellIndex( _workbook, CellAddressImpl.BROKEN_REF, CellAddressImpl.BROKEN_REF, CellAddressImpl.BROKEN_REF );
	}

	public static SpreadsheetExpressionParser newParser( String _exprText, CellIndex _parseRelativeTo,
			CellRefFormat _format )
	{
		final SpreadsheetExpressionParser parser;
		switch (_format) {
			case A1:
				parser = new SpreadsheetExpressionParserA1( _exprText, _parseRelativeTo );
				break;
			case A1_ODF:
				parser = new SpreadsheetExpressionParserA1ODF( _exprText, _parseRelativeTo );
				break;
			case A1_OOXML:
				parser = new SpreadsheetExpressionParserA1OOXML( _exprText, _parseRelativeTo );
				break;
			case R1C1:
				parser = new SpreadsheetExpressionParserR1C1( _exprText, _parseRelativeTo );
				break;
			default:
				throw new IllegalArgumentException( _format + " format is not supported" );
		}
		return parser;
	}


	private final CellRange parseNamedRef( String _ident )
	{
		final CellRange ref = this.workbook.getModelRangeNames().get( _ident );
		if (null == ref) {
			throw new InnerParserException( new CompilerException.UnsupportedExpression( "The name '"
					+ _ident + "' is not defined in this spreadsheet." ) );
		}
		return ref;
	}

	@Override
	protected final boolean isRangeName( Token _name )
	{
		return this.workbook.getModelRangeNames().get( _name.image ) != null;
	}

	@Override
	protected final ExpressionNode makeNamedRangeRef( Token _name )
	{
		final String name = _name.image;
		final CellRange range = parseNamedRef( name );
		if (range instanceof CellIndex) {
			final CellIndex cell = (CellIndex) range;
			return new ExpressionNodeForCell( cell, name );
		}
		else {
			return new ExpressionNodeForRange( range, name );
		}
	}


	@Override
	protected final ExpressionNode makeNodeForReference( Object _reference )
	{
		if (_reference instanceof CellIndex) {
			final CellIndex cell = (CellIndex) _reference;
			return new ExpressionNodeForCell( cell );
		}
		else if (_reference instanceof CellRange) {
			final CellRange range = (CellRange) _reference;
			return new ExpressionNodeForRange( range );
		}
		throw new IllegalArgumentException( "Reference must be a cell or range" );
	}

	@Override
	protected Object makeCellRange( final Object _from, final Object _to )
	{
		return CellRange.getCellRange( (CellIndex) _from, (CellIndex) _to );
	}

	@Override
	protected void convertRangesToCells( final boolean _allowRanges )
	{
		final ExpressionNode node = peekNode();
		convertRangesToCells( new ExpressionNodeSource()
		{
			public ExpressionNode getExpressionNode()
			{
				return node;
			}

			public void setExpressionNode( final ExpressionNode _node )
			{
				popNode();
				pushNode( _node );
			}
		}, _allowRanges );
	}

	private void convertRangesToCells( final ExpressionNodeSource _nodeSource, final boolean _allowRanges )
	{
		final ExpressionNode node = _nodeSource.getExpressionNode();
		if (node instanceof ExpressionNodeForOperator) {
			final ListIterator<ExpressionNode> nodeListIterator = node.arguments().listIterator();
			while (nodeListIterator.hasNext()) {
				final ExpressionNode argNode = nodeListIterator.next();
				convertRangesToCells( new ExpressionNodeSource()
				{
					public ExpressionNode getExpressionNode()
					{
						return argNode;
					}

					public void setExpressionNode( final ExpressionNode _node )
					{
						nodeListIterator.set( _node );
					}
				}, false );
			}
		}
		else if (!_allowRanges && node instanceof ExpressionNodeForRange) {
			try {
				final ExpressionNodeForRange nodeForRange = (ExpressionNodeForRange) node;
				final CellRange range = nodeForRange.getRange();
				final CellIndex cell = range.getCellIndexRelativeTo( this.cellIndex );
				_nodeSource.setExpressionNode( new ExpressionNodeForCell( cell, nodeForRange.getName() ) );
			} catch (SpreadsheetException e) {
				throw new InnerParserException( e );
			}
		}
	}

	@Override
	protected final ExpressionNode makeRangeIntersection( Collection<ExpressionNode> _firstTwoElements )
	{
		return new ExpressionNodeForRangeIntersection( _firstTwoElements );
	}

	@Override
	protected final ExpressionNode makeRangeUnion( Collection<ExpressionNode> _firstTwoElements )
	{
		return new ExpressionNodeForRangeUnion( _firstTwoElements );
	}

	@Override
	protected final ExpressionNode makeShapedRange( ExpressionNode _range )
	{
		return new ExpressionNodeForRangeShape( _range );
	}

	private static interface ExpressionNodeSource
	{
		ExpressionNode getExpressionNode();

		void setExpressionNode( ExpressionNode _node );
	}

}
