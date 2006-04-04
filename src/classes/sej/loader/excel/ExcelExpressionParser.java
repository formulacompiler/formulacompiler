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

import sej.Settings;
import sej.ModelError.CellRangeNotUniDimensional;
import sej.engine.expressions.ExpressionNode;
import sej.model.CellInstance;
import sej.model.CellIndex;
import sej.model.CellRange;
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


	public ExcelExpressionParser(CellInstance _cell)
	{
		this.cell = _cell;
		this.sheet = this.cell.getRow().getSheet();
		this.workbook = this.sheet.getWorkbook();
	}


	public ExpressionNode parseText( String _text ) throws Exception
	{

		if (Settings.isDebugLogEnabled()) {
			System.out.print( "Parse: " );
			System.out.println( _text );
		}

		StringReader reader = new StringReader( _text );
		Yylex lexer = new Yylex( reader );
		parser p = new parser( lexer );
		p.excelParser = this;
		p.parse();
		return p.rootNode;
	}


	String stripFirstAndLastCharOf( String _of )
	{
		return _of.substring( 1, _of.length() - 1 );
	}


	Reference parseIdentIntoRef( String _ident )
	{
		Reference result = this.workbook.getNamedRef( _ident );
		if (null == result) {
			result = parseIdentIntoCellIndex( _ident );
		}
		return result;
	}


	public CellIndex parseIdentIntoCellRef( String _ident ) throws CellRangeNotUniDimensional
	{
		Reference ref = this.workbook.getNamedRef( _ident );
		if (null == ref) {
			return parseIdentIntoCellIndex( _ident );
		}
		else if (ref instanceof CellIndex) {
			return (CellIndex) ref;
		}
		else {
			return ((CellRange) ref).getCellIndexRelativeTo( this.cell.getCellIndex() );
		}
	}


	CellIndex parseIdentIntoCellIndex( String _ident )
	{
		return this.workbook.getCellIndex( this.sheet, _ident, this.cell.getCellIndex() );
	}


	CellIndex parseIdentIntoCellIndex( String _ir, Integer _r, String _ic, Integer _c )
	{
		int rowIndex = this.cell.getRow().getRowIndex();
		int columnIndex = this.cell.getColumnIndex();

		if (null != _r && null != _c) {
			rowIndex += _r;
			columnIndex += _c;
		}
		else if (null != _ic) {
			rowIndex += _r;
			columnIndex = Sheet.parseRCIndex( columnIndex + 1, _ic, 1 ) - 1;
		}
		else {
			rowIndex = Sheet.parseRCIndex( rowIndex + 1, _ir, 1 ) - 1;
			columnIndex += _c;
		}

		return new CellIndex( this.sheet.getSheetIndex(), columnIndex, rowIndex );
	}


	public CellIndex parseIdentIntoCellIndexColRow( String _col, Integer _row )
	{
		return parseIdentIntoCellIndex( _col + _row );
	}


	ExpressionNode newNodeForReference( Reference _ref )
	{
		if (_ref instanceof CellIndex) {
			return new ExpressionNodeForCell( (CellIndex) _ref );
		}
		else if (_ref instanceof CellRange) {
			return new ExpressionNodeForRange( (CellRange) _ref );
		}
		throw new ExcelExpressionError( "Reference not valid." );
	}


}
