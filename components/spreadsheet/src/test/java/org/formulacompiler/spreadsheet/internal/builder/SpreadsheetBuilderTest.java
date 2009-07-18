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

package org.formulacompiler.spreadsheet.internal.builder;

import org.formulacompiler.compiler.Function;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBuilder;
import org.formulacompiler.spreadsheet.Spreadsheet.Row;
import org.formulacompiler.spreadsheet.Spreadsheet.Sheet;

import junit.framework.TestCase;

public class SpreadsheetBuilderTest extends TestCase
{


	public void testBuilder() throws Exception
	{
		SpreadsheetBuilder b = SpreadsheetCompiler.newSpreadsheetBuilder();
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
