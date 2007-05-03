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
package sej.internal.spreadsheet.builder;

import sej.compiler.Function;
import sej.spreadsheet.SEJ;
import sej.spreadsheet.Spreadsheet;
import sej.spreadsheet.SpreadsheetBuilder;
import sej.spreadsheet.Spreadsheet.Row;
import sej.spreadsheet.Spreadsheet.Sheet;
import junit.framework.TestCase;

public class SpreadsheetBuilderTest extends TestCase
{


	public void testBuilder() throws Exception
	{
		SpreadsheetBuilder b = SEJ.newSpreadsheetBuilder();
		SpreadsheetBuilder.CellRef cr, ar;

		b.newCell( b.cst( "CustomerRebate" ) );
		b.newCell( b.cst( 0.1 ) );
		b.nameCell( "CustomerRebate" );
		cr = b.currentCell();

		b.newRow();
		b.newCell( b.cst( "ArticleRebate" ) );
		b.newCell( b.cst( 0.05 ) );
		b.nameCell( "ArticleRebate" );
		ar = b.currentCell();

		SpreadsheetBuilder.RangeRef rng = b.range( ar, cr );
		b.nameRange( rng, "Rebates" );

		b.newRow();
		b.newRow();
		b.newCell( b.cst( "Rebates" ) );
		b.newCell( b.fun( Function.MAX, b.ref( cr ), b.ref( ar ) ) );
		b.nameCell( "RebateOp" );
		b.newCell( b.fun( Function.MAX, b.ref( rng ) ) );
		b.nameCell( "RebateAgg" );

		Spreadsheet s = b.getSpreadsheet();
		Sheet[] sheets = s.getSheets();

		assertEquals( 1, sheets.length );

		Sheet sheet = sheets[ 0 ];
		Row[] rows = sheet.getRows();

		assertEquals( 4, rows.length );

		assertEquals( "CustomerRebate", rows[ 0 ].getCells()[ 0 ].getConstantValue() );
		assertEquals( 0.1, (Double) rows[ 0 ].getCells()[ 1 ].getConstantValue(), 0.0001 );
		assertEquals( "ArticleRebate", rows[ 1 ].getCells()[ 0 ].getConstantValue() );
		assertEquals( 0.05, (Double) rows[ 1 ].getCells()[ 1 ].getConstantValue(), 0.0001 );

		assertEquals( "Rebates", rows[ 3 ].getCells()[ 0 ].getConstantValue() );
		assertEquals( "MAX( B1, B2 )", rows[ 3 ].getCells()[ 1 ].getExpressionText() );
		assertEquals( "MAX( B1:B2 )", rows[ 3 ].getCells()[ 2 ].getExpressionText() );

		assertEquals( "MAX( B1, B2 )", s.getCell( "RebateOp" ).getExpressionText() );
		assertEquals( "MAX( B1:B2 )", s.getCell( "RebateAgg" ).getExpressionText() );

		assertTrue( s.getRange( "Rebates" ).contains( rows[ 0 ].getCells()[ 1 ] ) );
		assertTrue( s.getRange( "Rebates" ).contains( rows[ 1 ].getCells()[ 1 ] ) );
		assertFalse( s.getRange( "Rebates" ).contains( rows[ 0 ].getCells()[ 0 ] ) );
		assertFalse( s.getRange( "Rebates" ).contains( rows[ 1 ].getCells()[ 0 ] ) );
		assertFalse( s.getRange( "Rebates" ).contains( rows[ 3 ].getCells()[ 1 ] ) );

	}


	public static class Input
	{
		public double getCustomerRebate()
		{
			return 0.1;
		}
		public double getArticleRebate()
		{
			return 0.05;
		}
	}

	public static interface Output
	{
		double getRebateOp();
		double getRebateAgg();
	}

}
