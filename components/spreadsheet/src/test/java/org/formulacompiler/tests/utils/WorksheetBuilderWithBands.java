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

package org.formulacompiler.tests.utils;


import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForOperator;
import org.formulacompiler.spreadsheet.Orientation;
import org.formulacompiler.spreadsheet.SpreadsheetBinder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.CellRange;
import org.formulacompiler.spreadsheet.internal.CellWithConstant;
import org.formulacompiler.spreadsheet.internal.CellWithExpression;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForCell;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForRange;
import org.formulacompiler.spreadsheet.internal.RowImpl;
import org.formulacompiler.spreadsheet.internal.SheetImpl;

/**
 * Construct the following sheet:
 * 
 * <pre>
 * SUM(C2:C3)  0.5
 *  1.0  2.0  3.0  SUM(A2:C2)*B$1
 *  5.0  5.0  6.0  SUM(A3:C3)*B$1
 *  7.0  8.0  9.0  SUM(A4:C4)*B$1
 * 10.0 11.0 12.0  SUM(A5:C5)*B$1
 * </pre>
 */
public class WorksheetBuilderWithBands
{
	final SheetImpl sheet;
	final RowImpl r0;
	final CellWithExpression formula;
	public RowImpl r1, r2, r3, r4;
	public CellInstance r1c1, r1c2, r1c3, r1c4;
	public CellInstance r2c1, r2c2, r2c3, r2c4;
	public CellInstance r3c1, r3c2, r3c3, r3c4;
	public CellInstance r4c1, r4c2, r4c3, r4c4;
	public SpreadsheetBinder.Section details;


	@SuppressWarnings( "unqualified-field-access" )
	public WorksheetBuilderWithBands( SheetImpl _sheet )
	{
		this.sheet = _sheet;
		this.r0 = sheet.getRowList().get( 0 );
		this.formula = (CellWithExpression) r0.getCellList().get( 0 );

		CellInstance factor = new CellWithConstant( r0, 0.5 );

		r1 = new RowImpl( sheet );
		r1c1 = new CellWithConstant( r1, 1.0 );
		r1c2 = new CellWithConstant( r1, 2.0 );
		r1c3 = new CellWithConstant( r1, 3.0 );
		r1c4 = buildRowSum( r1c1, r1c3, factor );

		r2 = new RowImpl( sheet );
		r2c1 = new CellWithConstant( r2, 4.0 );
		r2c2 = new CellWithConstant( r2, 5.0 );
		r2c3 = new CellWithConstant( r2, 6.0 );
		r2c4 = buildRowSum( r2c1, r2c3, factor );

		r3 = new RowImpl( sheet );
		r3c1 = new CellWithConstant( r3, 7.0 );
		r3c2 = new CellWithConstant( r3, 8.0 );
		r3c3 = new CellWithConstant( r3, 9.0 );
		r3c4 = buildRowSum( r3c1, r3c3, factor );

		r4 = new RowImpl( sheet );
		r4c1 = new CellWithConstant( r4, 10.0 );
		r4c2 = new CellWithConstant( r4, 11.0 );
		r4c3 = new CellWithConstant( r4, 12.0 );
		r4c4 = buildRowSum( r4c1, r4c3, factor );

		// Setup the formula to sum the row sums defined above.
		this.formula.setExpression( new ExpressionNodeForFunction( Function.SUM, new ExpressionNodeForRange( CellRange
				.getCellRange( r1c4.getCellIndex(), r4c4.getCellIndex() ) ) ) );
	}


	/**
	 * Define a binder and set the 3x4 values cells as a dynamic input range.
	 * 
	 * @throws SpreadsheetException.SectionOverlap
	 */
	@SuppressWarnings( "unqualified-field-access" )
	public SpreadsheetBinder newBinder() throws CompilerException
	{
		SpreadsheetBinder binder = SpreadsheetCompiler.newSpreadsheetBinder( sheet.getSpreadsheet(), Inputs.class,
				Outputs.class );
		defineWorkbook( binder.getRoot() );
		return binder;
	}


	@SuppressWarnings( "unqualified-field-access" )
	public void defineWorkbook( SpreadsheetBinder.Section _root ) throws CompilerException
	{
		try {
			_root.defineOutputCell( formula.getCellIndex(), Outputs.class.getMethod( "getResult" ) );
			defineRange( _root );
		}
		catch (SecurityException e) {
			e.printStackTrace();
		}
		catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}


	@SuppressWarnings( "unqualified-field-access" )
	public void defineRange( SpreadsheetBinder.Section _root ) throws CompilerException, SecurityException,
			NoSuchMethodException
	{
		final CellRange rng = CellRange.getCellRange( r1c1.getCellIndex(), r4c4.getCellIndex() );
		details = _root.defineRepeatingSection( rng, Orientation.VERTICAL, Inputs.class.getMethod( "getDetails" ),
				Inputs.class, null, null );
		details.defineInputCell( r1c1.getCellIndex(), "getOne" );
		details.defineInputCell( r1c2.getCellIndex(), "getTwo" );
		details.defineInputCell( r1c3.getCellIndex(), "getThree" );
	}


	CellInstance buildRowSum( CellInstance _r1c1, CellInstance _r1c2, CellInstance _factor )
	{
		CellWithExpression sum = new CellWithExpression( (RowImpl) _r1c1.getRow() );
		final CellIndex factorRef = _factor.getCellIndex().getAbsoluteIndex( false, true );
		final ExpressionNode aggregation = new ExpressionNodeForFunction( Function.SUM, new ExpressionNodeForRange(
				CellRange.getCellRange( _r1c1.getCellIndex(), _r1c2.getCellIndex() ) ) );
		final ExpressionNode multiplication = new ExpressionNodeForOperator( Operator.TIMES, aggregation,
				new ExpressionNodeForCell( factorRef ) );
		sum.setExpression( multiplication );
		return sum;
	}

}