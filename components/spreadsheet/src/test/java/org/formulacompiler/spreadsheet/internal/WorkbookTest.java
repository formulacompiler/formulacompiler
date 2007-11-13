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
package org.formulacompiler.spreadsheet.internal;

import static org.formulacompiler.compiler.internal.expressions.ExpressionBuilder.*;

import java.util.Calendar;
import java.util.TimeZone;

import org.formulacompiler.compiler.Function;
import org.formulacompiler.tests.utils.AbstractTestBase;
import org.formulacompiler.tests.utils.WorksheetBuilderWithBands;

public class WorkbookTest extends AbstractTestBase
{

	public void testDescribe() throws Exception
	{
		SpreadsheetImpl w = new SpreadsheetImpl();

		final CellInstance onFirstSheet;
		final CellInstance named;
		{
			SheetImpl s = new SheetImpl( w );

			{
				RowImpl r = new RowImpl( s );
				{
					CellWithLazilyParsedExpression c = new CellWithLazilyParsedExpression( r, null );
					c.setValue( "13.5" );
					c.setMaxFractionalDigits( 2 );
					w.defineName( "Result", c.getCellIndex() );
					named = c;
				}
			}

			new WorksheetBuilderWithBands( s );

			new RowImpl( s );

			{
				RowImpl r = new RowImpl( s );
				CellInstance c1 = new CellWithLazilyParsedExpression( r, null );
				CellInstance c2 = new CellWithConstant( r, null );
				w.addToNameMap( "Range", new CellRange( c1.getCellIndex(), c2.getCellIndex() ) );

				Calendar cal = Calendar.getInstance( TimeZone.getTimeZone( "GMT+1" ) );
				cal.clear();
				cal.set( 1970, 3, 19, 12, 13 );
				cal.set( cal.SECOND, 14 );
				onFirstSheet = new CellWithConstant( r, cal.getTime() );
			}

		}
		{
			SheetImpl s = new SheetImpl( w );
			{
				RowImpl r = new RowImpl( s );
				{
					CellInstance local = new CellWithConstant( r, 2.0 );
					new CellWithLazilyParsedExpression( r, fun( Function.SUM, new ExpressionNodeForCell( named ),
							new ExpressionNodeForCell( onFirstSheet ), new ExpressionNodeForCell( local ) ) );
					new CellWithLazilyParsedExpression( r, new ExpressionNodeForCell( onFirstSheet ) );
				}
			}
		}

		String description = w.describe();

		assertEqualToFile( "src/test/data/org/formulacompiler/spreadsheet/internal/test-sheet-description.yaml",
				description );
	}

}
