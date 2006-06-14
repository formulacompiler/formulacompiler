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
package sej.tests;

import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import sej.CallFrame;
import sej.NumericType;
import sej.SEJ;
import sej.SpreadsheetBinder;
import sej.expressions.ExpressionNode;
import sej.internal.Settings;
import sej.internal.spreadsheet.CellIndex;
import sej.internal.spreadsheet.CellInstance;
import sej.internal.spreadsheet.Reference;
import sej.internal.spreadsheet.RowImpl;
import sej.internal.spreadsheet.SheetImpl;
import sej.internal.spreadsheet.SpreadsheetImpl;
import sej.runtime.Engine;
import sej.runtime.Resettable;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class FormulaEvaluationTestSuite extends TestSuite
{
	private static final int FIRST_TEST_ROW = 4;
	private static final int NAME_COL = 0;
	private static final int EXPECTED_COL = 1;
	private static final int ACTUAL_COL = 2;
	private static final int INPUTCOUNT_COL = 3;
	private static final int FIRST_INPUT_COL = 4;


	public FormulaEvaluationTestSuite()
	{
		addTestsIn( "Tests.xls" );
		// addTestsIn( "Tests.xml" );
	}


	private void addTestsIn( String _fileName )
	{
		SpreadsheetImpl workbook;
		try {
			workbook = (SpreadsheetImpl) SEJ.loadSpreadsheet( new File( "src/test-system/testdata/sej/" + _fileName ) );
		}
		catch (Exception e) {
			e.fillInStackTrace();
			throw new RuntimeException( e );
		}

		TestSuite fileSuite = new TestSuite( _fileName );
		this.addTest( fileSuite );

		TestSuite suite = null;
		int formulaRow = -1;

		int debugRow = getNamedRow( workbook, "DEBUG" );
		if (debugRow >= 0) {
			int debugFormulaRow = getNamedRow( workbook, "DEBUGFORMULA" );
			if (debugFormulaRow < 0) debugFormulaRow = debugRow;
			fileSuite.addTest( new DoubleTestCase( workbook, debugFormulaRow, debugRow, true, false ) );
		}
		else {
			SheetImpl sheet = workbook.getSheetList().get( 0 );
			for (int iRow = FIRST_TEST_ROW; iRow < sheet.getRowList().size(); iRow++) {
				RowImpl row = sheet.getRowList().get( iRow );
				if (isRowEmpty( row )) {
					formulaRow = -1;
				}
				else {
					CellInstance nameCell = row.getCellOrNull( NAME_COL );
					if (null != nameCell) {
						suite = new TestSuite( (String) nameCell.getValue() );
						fileSuite.addTest( suite );
					}
					if (!isRowEmptyStartingWith( row, EXPECTED_COL )) {
						if (isRowEmptyStartingWith( row, INPUTCOUNT_COL )) {
							formulaRow = -1;
							addTests( suite, workbook, iRow, iRow, false );
						}
						else {
							if (formulaRow < 0) formulaRow = iRow;
							addTests( suite, workbook, formulaRow, iRow, false );
							addTests( suite, workbook, formulaRow, iRow, true );
						}
					}
				}
			}
		}
	}


	private void addTests( TestSuite _suite, SpreadsheetImpl _workbook, int _formulaRow, int _valueRow,
			boolean _useInputs )
	{
		addTests( _suite, _workbook, _formulaRow, _valueRow, _useInputs, false );
		addTests( _suite, _workbook, _formulaRow, _valueRow, _useInputs, true );
	}

	private void addTests( TestSuite _suite, SpreadsheetImpl _workbook, int _formulaRow, int _valueRow,
			boolean _useInputs, boolean _cachingEnabled )
	{
		_suite.addTest( new DoubleTestCase( _workbook, _formulaRow, _valueRow, _useInputs, _cachingEnabled ) );
		_suite.addTest( new BigDecimalTestCase( _workbook, _formulaRow, _valueRow, _useInputs, _cachingEnabled ) );
		_suite.addTest( new ScaledLongTestCase( _workbook, _formulaRow, _valueRow, _useInputs, _cachingEnabled ) );
	}


	private boolean isRowEmpty( RowImpl _row )
	{
		return isRowEmptyStartingWith( _row, 0 );
	}

	private boolean isRowEmptyStartingWith( RowImpl _row, int _col )
	{
		if (null == _row) return true;
		final List<CellInstance> cells = _row.getCellList();
		final int size = cells.size();
		if (size <= _col) return true;
		for (int i = _col; i < size; i++) {
			CellInstance cell = cells.get( i );
			if (null != cell) {
				if (null != cell.getExpression() || null != cell.getValue()) return false;
			}
		}
		return true;
	}


	private int getNamedRow( SpreadsheetImpl _workbook, String _name )
	{
		Reference namedRef = _workbook.getNameMap().get( _name );
		if (namedRef instanceof CellIndex) {
			CellIndex namedCell = (CellIndex) namedRef;
			return namedCell.rowIndex;
		}
		else return -1;
	}


	public void testAll()
	{
		// Needed for some strange reason to make tests run in Eclipse.
	}


	private static abstract class FormulaEvaluationTestCase extends TestCase
	{
		SpreadsheetImpl workbook;
		RowImpl formulaRow, inputRow;
		NumericType numericType;
		boolean useInputs;
		boolean cachingEnabled;
		Object expected;


		public FormulaEvaluationTestCase(SpreadsheetImpl _workbook, int _formulaRow, int _inputRow, boolean _useInputs,
				boolean _cachingEnabled, NumericType _numericType)
		{
			super( null );
			SheetImpl sheet = _workbook.getSheetList().get( 0 );
			this.workbook = _workbook;
			this.formulaRow = sheet.getRowList().get( _formulaRow );
			this.inputRow = sheet.getRowList().get( _inputRow );
			this.useInputs = _useInputs;
			this.cachingEnabled = _cachingEnabled;
			this.numericType = _numericType;

			CellInstance cell = this.formulaRow.getCellList().get( 2 );
			ExpressionNode expr = cell.getExpression();
			String name = "R" + (_inputRow + 1);
			if (null != expr) {
				name = name + ": " + expr.toString();
			}
			else {
				name = name + ": " + cell.getValue().toString();
			}
			if (_inputRow > _formulaRow) {
				name = name + " " + (_inputRow - _formulaRow);
			}
			if (_useInputs) name = name + " with inputs";
			if (_cachingEnabled) name = name + " with caching";
			name = name + " as " + _numericType.toString();
			name = name.replace( '(', '[' ).replace( ')', ']' );
			this.setName( name );

			if (!_useInputs) this.formulaRow = this.inputRow;
		}


		protected abstract Class getInputClass();
		protected abstract Class getOutputClass();
		protected abstract Inputs newInputs( int _input );
		protected abstract void assertNumber( double _expected, Object _computation );


		@Override
		protected void runTest() throws Throwable
		{
			final Class inputClass = getInputClass();
			final Class outputClass = getOutputClass();
			assertEquals( this.cachingEnabled, Resettable.class.isAssignableFrom( outputClass ) );

			final SpreadsheetBinder binder = SEJ.newSpreadsheetBinder( this.workbook, inputClass, outputClass );

			Inputs inputs = setupBinder( binder );

			runUsing( SEJ.compileEngine( binder.getBinding(), this.numericType ), inputs );
		}


		private Inputs setupBinder( SpreadsheetBinder _binder ) throws Exception
		{
			final SpreadsheetBinder.Section root = _binder.getRoot();
			final CellInstance outputCell = this.formulaRow.getCellOrNull( ACTUAL_COL );
			this.expected = this.inputRow.getCellOrNull( EXPECTED_COL ).getValue();

			if (null != outputCell.getExpression()) {
				if (Settings.isDebugLogEnabled()) {
					System.out.print( "Test: " );
					System.out.println( outputCell.getExpression().toString() );
				}
			}

			if (this.expected instanceof Double) root.defineOutputCell( outputCell.getCellIndex(), new CallFrame(
					getOutputClass().getMethod( "getNumber" ) ) );
			else if (this.expected instanceof Date) root.defineOutputCell( outputCell.getCellIndex(), new CallFrame(
					getOutputClass().getMethod( "getDate" ) ) );
			else fail( "Output cell type not supported" );

			Inputs inputs = null;
			if (this.useInputs) {
				final CellInstance nInputCell = this.inputRow.getCellOrNull( INPUTCOUNT_COL );
				final int nInput = (null == nInputCell) ? 0 : valueToIntOrZero( nInputCell.getValue() );
				inputs = newInputs( nInput );
				for (int iInput = 0; iInput < nInput; iInput++) {
					final int iCell = iInput + FIRST_INPUT_COL;
					final CellInstance inputValueCell = this.inputRow.getCellOrNull( iCell );
					if (null == inputValueCell || null == inputValueCell.getExpression()) {
						final CellIndex inputReferenceCellIndex = this.formulaRow.getCellIndex( iCell );
						final Object inputValue = (null == inputValueCell) ? null : inputValueCell.getValue();
						if (null == inputValue) {
							root.defineInputCell( inputReferenceCellIndex, new CallFrame( getInputClass().getMethod(
									"getNumber", new Class[] { Integer.TYPE } ), iInput ) );
						}
						else if (inputValue instanceof Double) {
							root.defineInputCell( inputReferenceCellIndex, new CallFrame( getInputClass().getMethod(
									"getNumber", new Class[] { Integer.TYPE } ), iInput ) );
						}
						else if (inputValue instanceof Date) {
							root.defineInputCell( inputReferenceCellIndex, new CallFrame( getInputClass().getMethod(
									"getDate", new Class[] { Integer.TYPE } ), iInput ) );
						}
						else if (inputValue instanceof Boolean) {
							root.defineInputCell( inputReferenceCellIndex, new CallFrame( getInputClass().getMethod(
									"getBoolean", new Class[] { Integer.TYPE } ), iInput ) );
						}
						else {
							fail( "Input cell type not supported" );
						}
						inputs.setValue( iInput, inputValue );
					}
				}
			}

			return inputs;
		}


		private void runUsing( Engine _engine, Inputs _inputs ) throws Exception
		{
			try {
				final Outputs computation = (Outputs) _engine.getComputationFactory().newInstance( _inputs );

				if (this.expected instanceof Double) {
					assertNumber( (Double) this.expected, computation );
				}
				else if (this.expected instanceof Date) {
					final Date actual = computation.getDate();
					assertEquals( this.expected, actual );
				}
				else fail( "Output comparison not implemented" );

			}
			catch (VerifyError e) {
				throw e;
			}
		}


		public static int valueToInt( Object _value, int _ifNull )
		{
			if (_value instanceof Number) return ((Number) _value).intValue();
			if (_value instanceof String) return Integer.valueOf( (String) _value );
			return _ifNull;
		}


		public static int valueToIntOrZero( Object _value )
		{
			return valueToInt( _value, 0 );
		}


		public static class Inputs
		{
			private final Object[] values;

			public Inputs(int _numberOfValues)
			{
				super();
				this.values = new Object[ _numberOfValues ];
			}

			public void setValue( int _index, Object _value )
			{
				this.values[ _index ] = _value;
			}

			public Object getValue( int _index )
			{
				return this.values[ _index ];
			}

			public double getDouble( int _index )
			{
				return (Double) this.values[ _index ];
			}

			public Date getDate( int _index )
			{
				return (Date) this.values[ _index ];
			}

			public boolean getBoolean( int _index )
			{
				return (Boolean) this.values[ _index ];
			}

		}

		public static class Outputs
		{
			Date getDate()
			{
				throw new AbstractMethodError( "getDate()" );
			}
		}

	}


	private static class DoubleTestCase extends FormulaEvaluationTestCase
	{

		public DoubleTestCase(SpreadsheetImpl _workbook, int _formulaRow, int _inputRow, boolean _useInputs,
				boolean _cachingEnabled)
		{
			super( _workbook, _formulaRow, _inputRow, _useInputs, _cachingEnabled, NumericType.DOUBLE );
		}

		@Override
		protected Class getInputClass()
		{
			return Inputs.class;
		}

		@Override
		protected Class getOutputClass()
		{
			if (this.cachingEnabled) return CachingOutputs.class;
			return Outputs.class;
		}

		@Override
		protected Inputs newInputs( int _numberOfValues )
		{
			return new Inputs( _numberOfValues );
		}

		@Override
		protected void assertNumber( double _expected, Object _computation )
		{
			Outputs computation = (Outputs) _computation;
			final double actual = computation.getNumber();
			assertEquals( _expected, actual, 0.00000001 );
		}


		public static class Inputs extends FormulaEvaluationTestCase.Inputs
		{
			public Inputs(int _numberOfValues)
			{
				super( _numberOfValues );
			}

			public double getNumber( int _index )
			{
				return getDouble( _index );
			}
		}


		public static class Outputs extends FormulaEvaluationTestCase.Outputs
		{
			public double getNumber()
			{
				throw new AbstractMethodError( "getNumber()" );
			}
		}

		public static abstract class CachingOutputs extends Outputs implements Resettable
		{
			// Nothing new here.
		}

	}


	private static class BigDecimalTestCase extends FormulaEvaluationTestCase
	{

		public BigDecimalTestCase(SpreadsheetImpl _workbook, int _formulaRow, int _inputRow, boolean _useInputs,
				boolean _cachingEnabled)
		{
			// Use BigDecimal9 here so it is compatible with Excel's double.
			super( _workbook, _formulaRow, _inputRow, _useInputs, _cachingEnabled, NumericType.BIGDECIMAL9 );
		}

		@Override
		protected Class getInputClass()
		{
			return Inputs.class;
		}

		@Override
		protected Class getOutputClass()
		{
			if (this.cachingEnabled) return CachingOutputs.class;
			return Outputs.class;
		}

		@Override
		protected Inputs newInputs( int _numberOfValues )
		{
			return new Inputs( _numberOfValues );
		}

		@Override
		protected void assertNumber( double _expected, Object _computation )
		{
			final Outputs computation = (Outputs) _computation;
			final BigDecimal actual = computation.getNumber();

			// BigDecimal.valueOf( _expected ) is not precise on JRE 1.4!
			final BigDecimal expected = new BigDecimal( Double.toString( _expected ) );

			final boolean isOK = (0 == expected.compareTo( actual ));
			assertTrue( "Expected: " + expected.toString() + ", but was: " + actual.toString(), isOK );
		}


		public static class Inputs extends FormulaEvaluationTestCase.Inputs
		{
			public Inputs(int _numberOfValues)
			{
				super( _numberOfValues );
			}

			public BigDecimal getNumber( int _index )
			{
				return BigDecimal.valueOf( getDouble( _index ) );
			}
		}


		public static class Outputs extends FormulaEvaluationTestCase.Outputs
		{
			public BigDecimal getNumber()
			{
				throw new AbstractMethodError( "getNumber()" );
			}
		}

		public static abstract class CachingOutputs extends Outputs implements Resettable
		{
			// Nothing new here.
		}

	}


	private static class ScaledLongTestCase extends FormulaEvaluationTestCase
	{

		public ScaledLongTestCase(SpreadsheetImpl _workbook, int _formulaRow, int _inputRow, boolean _useInputs,
				boolean _cachingEnabled)
		{
			super( _workbook, _formulaRow, _inputRow, _useInputs, _cachingEnabled, NumericType.LONG4 );
		}

		@Override
		protected Class getInputClass()
		{
			return Inputs.class;
		}

		@Override
		protected Class getOutputClass()
		{
			if (this.cachingEnabled) return CachingOutputs.class;
			return Outputs.class;
		}

		@Override
		protected Inputs newInputs( int _numberOfValues )
		{
			return new Inputs( _numberOfValues );
		}

		@Override
		protected void assertNumber( double _expected, Object _computation )
		{
			final Outputs computation = (Outputs) _computation;
			final long actual = computation.getNumber();
			final long expected = ((Long) NumericType.LONG4.valueOf( _expected )).longValue();
			assertEquals( expected, actual );
		}


		public static class Inputs extends FormulaEvaluationTestCase.Inputs
		{
			public Inputs(int _numberOfValues)
			{
				super( _numberOfValues );
			}

			public long getNumber( int _index )
			{
				return ((Long) NumericType.LONG4.valueOf( getDouble( _index ) )).longValue();
			}
		}


		public static class Outputs extends FormulaEvaluationTestCase.Outputs
		{
			public long getNumber()
			{
				throw new AbstractMethodError( "getNumber()" );
			}
		}

		public static abstract class CachingOutputs extends Outputs implements Resettable
		{
			// Nothing new here.
		}

	}

}
