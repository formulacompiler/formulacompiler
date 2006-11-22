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

import sej.EngineBuilder;
import sej.Function;
import sej.SEJ;
import sej.Spreadsheet;
import sej.SpreadsheetBuilder;
import sej.Spreadsheet.Row;
import sej.Spreadsheet.Sheet;
import sej.runtime.Engine;
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
		assertNotNull( s.getCell( "RebateOp" ) );
		assertNotNull( s.getCell( "RebateAgg" ) );
		assertNotNull( s.getRange( "Rebates" ) );

		EngineBuilder c = SEJ.newEngineBuilder();
		c.setSpreadsheet( s );
		c.setInputClass( Input.class );
		c.setOutputClass( Output.class );
		c.bindAllByName();
		Engine e = c.compile();

		Output o = (Output) e.getComputationFactory().newComputation( new Input() );
		assertEquals( 0.1, o.getRebateOp(), 0.00001 );
		assertEquals( 0.1, o.getRebateAgg(), 0.00001 );
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
