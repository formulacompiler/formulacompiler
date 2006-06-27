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
package sej.tests.utils;


import sej.Aggregator;
import sej.CallFrame;
import sej.CompilerException;
import sej.Operator;
import sej.Orientation;
import sej.SEJ;
import sej.SpreadsheetBinder;
import sej.CompilerException.SectionOverlap;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForAggregator;
import sej.internal.expressions.ExpressionNodeForOperator;
import sej.internal.spreadsheet.CellIndex;
import sej.internal.spreadsheet.CellInstance;
import sej.internal.spreadsheet.CellRange;
import sej.internal.spreadsheet.CellWithConstant;
import sej.internal.spreadsheet.CellWithLazilyParsedExpression;
import sej.internal.spreadsheet.ExpressionNodeForCell;
import sej.internal.spreadsheet.ExpressionNodeForRange;
import sej.internal.spreadsheet.RowImpl;
import sej.internal.spreadsheet.SheetImpl;

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
	final CellWithLazilyParsedExpression formula;
	public RowImpl r1, r2, r3, r4;
	public CellInstance r1c1, r1c2, r1c3, r1c4;
	public CellInstance r2c1, r2c2, r2c3, r2c4;
	public CellInstance r3c1, r3c2, r3c3, r3c4;
	public CellInstance r4c1, r4c2, r4c3, r4c4;
	public SpreadsheetBinder.Section details;


	@SuppressWarnings("unqualified-field-access")
	public WorksheetBuilderWithBands(SheetImpl _sheet)
	{
		this.sheet = _sheet;
		this.r0 = sheet.getRowList().get( 0 );
		this.formula = (CellWithLazilyParsedExpression) r0.getCellList().get( 0 );

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
		this.formula.setExpression( new ExpressionNodeForAggregator( Aggregator.SUM, new ExpressionNodeForRange( r1c4
				.getCellIndex(), r4c4.getCellIndex() ) ) );
	}


	/**
	 * Define a binder and set the 3x4 values cells as a dynamic input range.
	 * 
	 * @throws SectionOverlap
	 */
	@SuppressWarnings("unqualified-field-access")
	public SpreadsheetBinder newBinder() throws CompilerException
	{
		SpreadsheetBinder binder = SEJ.newSpreadsheetBinder( sheet.getSpreadsheet(), Inputs.class, Outputs.class );
		defineWorkbook( binder.getRoot() );
		return binder;
	}


	@SuppressWarnings("unqualified-field-access")
	public void defineWorkbook( SpreadsheetBinder.Section _root ) throws CompilerException
	{
		try {
			_root.defineOutputCell( formula.getCellIndex(), new CallFrame( Outputs.class.getMethod( "getResult" ) ) );
			defineRange( _root );
		}
		catch (SecurityException e) {
			e.printStackTrace();
		}
		catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}


	@SuppressWarnings("unqualified-field-access")
	public void defineRange( SpreadsheetBinder.Section _root ) throws CompilerException, SecurityException,
			NoSuchMethodException
	{
		CellRange rng = new CellRange( r1c1.getCellIndex(), r4c4.getCellIndex() );
		details = _root.defineRepeatingSection( rng, Orientation.VERTICAL, new CallFrame( Inputs.class
				.getMethod( "getDetails" ) ), Inputs.class, null, null );
		details.defineInputCell( r1c1.getCellIndex(), new CallFrame( Inputs.class.getMethod( "getOne" ) ) );
		details.defineInputCell( r1c2.getCellIndex(), new CallFrame( Inputs.class.getMethod( "getTwo" ) ) );
		details.defineInputCell( r1c3.getCellIndex(), new CallFrame( Inputs.class.getMethod( "getThree" ) ) );
	}


	CellInstance buildRowSum( CellInstance _r1c1, CellInstance _r1c2, CellInstance _factor )
	{
		CellWithLazilyParsedExpression sum = new CellWithLazilyParsedExpression( _r1c1.getRow() );
		final CellIndex factorRef = _factor.getCellIndex().getAbsoluteIndex( false, true );
		final ExpressionNode aggregation = new ExpressionNodeForAggregator( Aggregator.SUM, new ExpressionNodeForRange(
				_r1c1.getCellIndex(), _r1c2.getCellIndex() ) );
		final ExpressionNode multiplication = new ExpressionNodeForOperator( Operator.TIMES, aggregation,
				new ExpressionNodeForCell( factorRef ) );
		sum.setExpression( multiplication );
		return sum;
	}

}