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

package org.formulacompiler.spreadsheet.internal.parser;

import java.util.Collection;
import java.util.Iterator;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.parser.ExpressionParser;
import org.formulacompiler.compiler.internal.expressions.parser.Token;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.CellRange;
import org.formulacompiler.spreadsheet.internal.CellRefFormat;
import org.formulacompiler.spreadsheet.internal.CellRefParser;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForCell;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForRange;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForRangeIntersection;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForRangeShape;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForRangeUnion;
import org.formulacompiler.spreadsheet.internal.MultiCellRange;
import org.formulacompiler.spreadsheet.internal.SheetImpl;
import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;

public abstract class SpreadsheetExpressionParser extends ExpressionParser
{
	protected final SpreadsheetImpl workbook;
	protected final CellIndex cellIndex;

	protected SpreadsheetExpressionParser( String _exprText, CellInstance _parseRelativeTo )
	{
		super( _exprText );
		this.cellIndex = _parseRelativeTo.getCellIndex();
		this.workbook = this.cellIndex.spreadsheet;
	}

	protected SpreadsheetExpressionParser( String _exprText, SpreadsheetImpl _workbook )
	{
		super( _exprText );
		this.workbook = _workbook;
		this.cellIndex = new CellIndex( _workbook, CellIndex.BROKEN_REF, CellIndex.BROKEN_REF, CellIndex.BROKEN_REF );
	}

	public static SpreadsheetExpressionParser newParser( String _exprText, CellInstance _parseRelativeTo,
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
			case R1C1:
				parser = new SpreadsheetExpressionParserR1C1( _exprText, _parseRelativeTo );
				break;
			default:
				throw new IllegalArgumentException( _format + " format is not supported" );
		}
		return parser;
	}


	@Override
	protected final ExpressionNode makeCellA1( Token _cell, ExpressionNode _node )
	{
		final CellIndex cellIndex = getCellIndex( _node );
		return makeCell( _cell.image, cellIndex, CellRefFormat.A1 );
	}

	@Override
	protected final ExpressionNode makeCellA1ODF( Token _cell, ExpressionNode _node )
	{
		final CellIndex cellIndex = getCellIndex( _node );
		return makeCell( _cell.image, cellIndex, CellRefFormat.A1_ODF );
	}

	private CellIndex getCellIndex( final ExpressionNode _node )
	{
		final CellIndex cellIndex;
		if (_node != null) {
			final ExpressionNodeForCell nodeForCell = (ExpressionNodeForCell) _node;
			cellIndex = nodeForCell.getCellIndex();
		}
		else {
			cellIndex = this.cellIndex;
		}
		return cellIndex;
	}

	@Override
	protected ExpressionNode makeCellR1C1( Token _cell, ExpressionNode _node )
	{
		return makeCell( _cell.image, this.cellIndex, CellRefFormat.R1C1 );
	}

	private ExpressionNode makeCell( String _ref, CellIndex _cellIndex, CellRefFormat _cellRefFormat )
	{
		final CellRefParser parser = CellRefParser.getInstance( _cellRefFormat );
		return new ExpressionNodeForCell( parser.getCellIndexForCanonicalName( _ref, _cellIndex ) );
	}

	protected final SheetImpl getSheetByName( String _sheet )
	{
		final String sheetName = stripSheetNameDecorationFrom( _sheet );
		final SheetImpl namedSheet = this.workbook.getSheet( sheetName );
		if (null == namedSheet) {
			throw new CompilerException.NameNotFound( "Sheet '" + sheetName + "' is not defined." );
		}
		return namedSheet;
	}

	private final String stripSheetNameDecorationFrom( String _sheet )
	{
		int startPos = 0;
		int endPos = _sheet.length();
		if ('\'' == _sheet.charAt( 0 )) startPos++;
		if ('!' == _sheet.charAt( endPos - 1 )) endPos--;
		if ('\'' == _sheet.charAt( endPos - 1 )) endPos--;
		return _sheet.substring( startPos, endPos );
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
		return this.workbook.getModelRangeNames().get( _name.image ) instanceof MultiCellRange;
	}

	@Override
	protected final ExpressionNode makeNamedCellRef( Token _name )
	{
		final String name = _name.image;
		final CellRange range = parseNamedRef( name );
		try {
			final CellIndex cell = range.getCellIndexRelativeTo( this.cellIndex );
			return new ExpressionNodeForCell( cell, name );
		}
		catch (SpreadsheetException e) {
			throw new InnerParserException( e );
		}
	}

	@Override
	protected final ExpressionNode makeNamedRangeRef( Token _name )
	{
		final String name = _name.image;
		final CellRange range = parseNamedRef( name );
		return new ExpressionNodeForRange( range, name );
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
	protected final Object makeCellRange( Collection<ExpressionNode> _nodes )
	{
		final Iterator<ExpressionNode> nodes = _nodes.iterator();
		final ExpressionNodeForCell a = (ExpressionNodeForCell) nodes.next();
		final ExpressionNodeForCell b = (ExpressionNodeForCell) nodes.next();
		return CellRange.getCellRange( a.getCellIndex(), b.getCellIndex() );
	}

	@Override
	protected final Object makeCellIndex( ExpressionNode _node )
	{
		final ExpressionNodeForCell nodeForCell = (ExpressionNodeForCell) _node;
		return nodeForCell.getCellIndex();
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

}
