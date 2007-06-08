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
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForCell;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForRange;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForRangeIntersection;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForRangeShape;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForRangeUnion;
import org.formulacompiler.spreadsheet.internal.Reference;
import org.formulacompiler.spreadsheet.internal.SheetImpl;
import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;

public abstract class SpreadsheetExpressionParser extends ExpressionParser
{
	protected final SpreadsheetImpl workbook;
	protected final SheetImpl sheet;
	protected final CellInstance cell;
	protected final CellIndex cellIndex;

	public SpreadsheetExpressionParser(String _exprText, CellInstance _parseRelativeTo)
	{
		super( _exprText );
		this.cell = _parseRelativeTo;
		this.sheet = (this.cell == null) ? null : this.cell.getRow().getSheet();
		this.workbook = (this.sheet == null) ? null : this.sheet.getSpreadsheet();
		this.cellIndex = (this.cell == null) ? null : this.cell.getCellIndex();
	}

	public static SpreadsheetExpressionParser newParser( String _exprText, CellInstance _parseRelativeTo,
			CellRefFormat _format )
	{
		return (_format == CellRefFormat.A1) ? new SpreadsheetExpressionParserA1( _exprText, _parseRelativeTo )
				: new SpreadsheetExpressionParserR1C1( _exprText, _parseRelativeTo );
	}


	@Override
	protected final ExpressionNode makeCellA1( Token _cell )
	{
		return makeCellA1( _cell.image, this.sheet );
	}

	@Override
	protected final ExpressionNode makeCellA1( Token _cell, Token _sheet )
	{
		return makeCellA1( _cell.image, getSheetByName( _sheet.image ) );
	}

	protected final ExpressionNode makeCellA1( String _ref, SheetImpl _sheet )
	{
		return new ExpressionNodeForCell( _sheet.getCellIndexForCanonicalNameA1( _ref ) );
	}

	@Override
	protected ExpressionNode makeCellR1C1( Token _cell )
	{
		return makeCellR1C1( _cell.image, this.sheet );
	}

	@Override
	protected ExpressionNode makeCellR1C1( Token _cell, Token _sheet )
	{
		return makeCellR1C1( _cell.image, getSheetByName( _sheet.image ) );
	}

	protected final ExpressionNode makeCellR1C1( String _ref, SheetImpl _sheet )
	{
		return new ExpressionNodeForCell( _sheet.getCellIndexForCanonicalNameR1C1( _ref, this.cellIndex ) );
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


	private final Reference parseNamedRef( String _ident )
	{
		final Reference ref = this.workbook.getNamedRef( _ident );
		if (null == ref) {
			throw new InnerParserException( new CompilerException.UnsupportedExpression( "The name '"
					+ _ident + "' is not defined in this spreadsheet." ) );
		}
		return ref;
	}

	@Override
	protected final boolean isRangeName( Token _name )
	{
		return this.workbook.getNamedRef( _name.image ) instanceof CellRange;
	}

	@Override
	protected final ExpressionNode makeNamedCellRef( Token _name )
	{
		final Reference ref = parseNamedRef( _name.image );
		if (ref instanceof CellIndex) {
			return new ExpressionNodeForCell( (CellIndex) ref );
		}
		else if (ref instanceof CellRange) {
			final CellRange range = (CellRange) ref;
			CellIndex cell;
			try {
				cell = range.getCellIndexRelativeTo( this.cellIndex );
			}
			catch (SpreadsheetException e) {
				throw new InnerParserException( e );
			}
			return new ExpressionNodeForCell( cell );
		}
		throw new IllegalArgumentException( "Unsupported reference type " + ref.getClass().getName() );
	}

	@Override
	protected final ExpressionNode makeNamedRangeRef( Token _name )
	{
		final Reference ref = parseNamedRef( _name.image );
		if (ref instanceof CellIndex) {
			final CellIndex cell = (CellIndex) ref;
			return new ExpressionNodeForRange( cell, cell );
		}
		else if (ref instanceof CellRange) {
			return new ExpressionNodeForRange( (CellRange) ref );
		}
		throw new IllegalArgumentException( "Unsupported reference type " + ref.getClass().getName() );
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
		return new CellRange( a.getCellIndex(), b.getCellIndex() );
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
