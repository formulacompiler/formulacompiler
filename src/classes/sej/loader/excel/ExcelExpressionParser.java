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
package sej.loader.excel;

import java.io.StringReader;

import sej.ModelError;
import sej.Settings;
import sej.engine.expressions.ExpressionNode;
import sej.model.CellIndex;
import sej.model.CellInstance;
import sej.model.CellRange;
import sej.model.CellRefFormat;
import sej.model.ExpressionNodeForCell;
import sej.model.ExpressionNodeForRange;
import sej.model.Reference;
import sej.model.Sheet;
import sej.model.Workbook;


class ExcelExpressionParser
{
	Workbook workbook;
	Sheet sheet;
	CellInstance cell;

	private String source;
	private ExcelExpressionScanner scanner;


	public ExcelExpressionParser(CellInstance _cell)
	{
		this.cell = _cell;
		this.sheet = this.cell.getRow().getSheet();
		this.workbook = this.sheet.getWorkbook();
	}


	public ExpressionNode parseText( String _text, CellRefFormat _cellRefFormat )
	{
		if (Settings.isDebugLogEnabled()) {
			System.out.print( "Parse: " );
			System.out.println( _text );
		}

		this.source = _text;
		final StringReader reader = new StringReader( _text );

		switch (_cellRefFormat) {
		case A1:
			this.scanner = new GeneratedScannerA1( reader );
			break;
		case R1C1:
			this.scanner = new GeneratedScannerR1C1( reader );
		}
		this.scanner.setSource( _text );
		
		final GeneratedParser parser = new GeneratedParser( this.scanner );
		parser.excelParser = this;
		try {
			if (Settings.isDebugParserEnabled()) {
				parser.debug_parse();
			}
			else {
				parser.parse();
			}
		}
		catch (ExcelExpressionError e) {
			throw e;
		}
		catch (Exception e) {
			throw new ExcelExpressionError( e, this.source, this.scanner.charsRead() );
		}

		return parser.rootNode;
	}


	String stripFirstAndLastCharOf( String _of )
	{
		return _of.substring( 1, _of.length() - 1 );
	}


	Reference parseNamedRef( String _ident )
	{
		return this.workbook.getNamedRef( _ident );
	}


	CellIndex parseCellRefA1( String _ref )
	{
		return this.sheet.getCellIndexForCanonicalNameA1( _ref );
	}


	CellIndex parseCellRefR1C1( String _ref )
	{
		return this.sheet.getCellIndexForCanonicalNameR1C1( _ref, this.cell.getCellIndex() );
	}


	CellRange parseVectorRefCol( String _first, String _second ) throws ModelError
	{
		// TODO parseVectorRefCol
		throw new ModelError.UnsupportedExpression( "Vectors are not supported yet." );
	}


	CellRange parseVectorRefRow( String _first, String _second ) throws ModelError
	{
		// TODO parseVectorRefRow
		throw new ModelError.UnsupportedExpression( "Vectors are not supported yet." );
	}


	ExpressionNode newNodeForReference( Reference _ref )
	{
		if (_ref instanceof CellIndex) {
			return new ExpressionNodeForCell( (CellIndex) _ref );
		}
		else if (_ref instanceof CellRange) {
			return new ExpressionNodeForRange( (CellRange) _ref );
		}
		throw new ExcelExpressionError( "Reference not valid.", this.source, this.scanner.charsRead() );
	}


	ExpressionNode makeCellExpr( ExpressionNode _node ) throws ModelError
	{
		if (_node instanceof ExpressionNodeForRange) {
			final ExpressionNodeForRange rangeNode = (ExpressionNodeForRange) _node;
			final CellRange range = rangeNode.getRange();
			final CellIndex cell = range.getCellIndexRelativeTo( this.cell.getCellIndex() );
			return new ExpressionNodeForCell( cell );
		}
		return _node;
	}


	ExpressionNode makeRangeExpr( ExpressionNode _node )
	{
		return _node;
	}


}
