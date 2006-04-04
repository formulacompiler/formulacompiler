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

import sej.engine.expressions.ExpressionNode;
import sej.model.CellIndex;
import sej.model.CellInstance;
import sej.model.CellWithConstant;
import sej.model.ExpressionNodeForCell;
import sej.model.Row;
import sej.model.Sheet;
import sej.model.Workbook;
import junit.framework.TestCase;

public class ExcelExpressionParserTest extends TestCase
{
	Workbook workbook = new Workbook();
	Sheet sheet = new Sheet( this.workbook );
	Row row1 = new Row( this.sheet );
	CellInstance cell11 = new CellWithConstant( this.row1, 123 );
	CellInstance cell21 = new CellWithConstant( this.row1, 123 );
	Row row2 = new Row( this.sheet );
	CellInstance cell12 = new CellWithConstant( this.row2, 123 );
	CellInstance cell22 = new CellWithConstant( this.row2, 123 );
	ExcelExpressionParser parser = new ExcelExpressionParser( this.cell22 );


	public void testRC() throws Exception
	{
		assertRef( "B2", "RC" );
		assertRef( "A1", "R1C1" );
		assertRef( "A2", "RC1" );
		assertRef( "B1", "R1C" );
		assertRef( "C2", "RC[1]" );
		assertRef( "A2", "RC[-1]" );
		assertRef( "A1", "R[-1]C[-1]" );
		assertRef( "B1", "R[-1]C" );
	}


	public void testAbs() throws Exception
	{
		assertRef( "B2", "B2" );
		assertRef( "D4", "D4" );
		assertRef( "D4", "$D$4" );
		assertRef( "D4", "$D4" );
		assertRef( "D4", "D$4" );
	}


	private void assertRef( String _canonicalName, String _ref ) throws Exception
	{
		ExpressionNode parsed = this.parser.parseText( _ref );
		ExpressionNodeForCell node = (ExpressionNodeForCell) parsed;
		CellIndex ref = node.getCellIndex();
		String actual = Sheet.getCanonicalNameForCellIndex( ref.columnIndex, ref.rowIndex );
		assertEquals( _canonicalName, actual );
	}

}
