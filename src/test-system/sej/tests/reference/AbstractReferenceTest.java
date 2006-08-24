package sej.tests.reference;

import java.io.File;
import java.math.BigDecimal;
import java.util.BitSet;
import java.util.Date;
import java.util.List;

import sej.CallFrame;
import sej.EngineBuilder;
import sej.NumericType;
import sej.SEJ;
import sej.SaveableEngine;
import sej.SpreadsheetBinder.Section;
import sej.internal.Debug;
import sej.internal.spreadsheet.CellInstance;
import sej.internal.spreadsheet.RowImpl;
import sej.internal.spreadsheet.SheetImpl;
import sej.internal.spreadsheet.SpreadsheetImpl;
import sej.runtime.ComputationFactory;
import sej.runtime.Resettable;
import sej.runtime.ScaledLong;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public abstract class AbstractReferenceTest extends TestCase
{
	private static final File SPREADSHEET_PATH = new File( "src/test-system/testdata/sej/tests/reference" );
	private static final int STARTING_ROW = 5;
	private static final int NAME_COL = 0;
	private static final int EXPECTED_COL = 1;
	private static final int FORMULA_COL = 2;
	private static final int INPUTCOUNT_COL = 3;
	private static final int INPUTS_COL = 4;

	private final String baseName;
	private final String spreadsheetName;


	protected AbstractReferenceTest()
	{
		super();
		this.baseName = extractBaseNameFrom( getClass().getSimpleName() );
		this.spreadsheetName = this.baseName + ".xls";
	}

	private String extractBaseNameFrom( String _name )
	{
		if (_name.endsWith( "Test" )) {
			return _name.substring( 0, _name.length() - 4 );
		}
		else {
			return _name;
		}
	}


	public void testExpressions() throws Exception
	{
		final EngineBuilder eb = SEJ.newEngineBuilder();
		eb.loadSpreadsheet( new File( SPREADSHEET_PATH, this.spreadsheetName ) );
		runTestsIn( (SpreadsheetImpl) eb.getSpreadsheet() );
	}


	private final void runTestsIn( SpreadsheetImpl _book ) throws Exception
	{
		final SheetImpl sheet = _book.getSheetList().get( 0 );
		final List<RowImpl> rows = sheet.getRowList();
		String testName = "";
		int atRow = STARTING_ROW;
		while (atRow < rows.size()) {
			final RowImpl row = rows.get( atRow );

			if (null != row.getCellOrNull( FORMULA_COL )) {

				final CellInstance nameCell = row.getCellOrNull( NAME_COL );
				if (null != nameCell) {
					testName = nameCell.getValue().toString();
				}

				new RowRunner( row, row, testName, atRow ).run();

				final CellInstance inputCountCell = row.getCellOrNull( INPUTCOUNT_COL );
				if (null != inputCountCell) {
					while (atRow < rows.size()) {
						final RowImpl variantRow = rows.get( atRow++ );
						if (null == variantRow.getCellOrNull( INPUTCOUNT_COL )) break;

						new RowRunner( row, variantRow, testName, atRow ).run();

					}
				}
			}

			atRow++;
		}
	}


	private static final class RowRunner
	{
		private final String rowName;
		private final RowImpl formulaRow;
		private final RowImpl valueRow;
		private final Object expected;
		private final ValueType expectedType;
		private final CellInstance formula;
		private Object[] inputs;
		private ValueType[] inputTypes;

		public RowRunner(RowImpl _formulaRow, RowImpl _valueRow, String _testName, int _atRow)
		{
			super();
			this.rowName = "R" + (_atRow + 1) + ": " + _testName;
			this.formulaRow = _formulaRow;
			this.valueRow = _valueRow;
			this.expected = _valueRow.getCellOrNull( EXPECTED_COL ).getValue();
			this.expectedType = valueTypeOf( this.expected );
			this.formula = _formulaRow.getCellOrNull( FORMULA_COL );
			extractInputsFrom( _valueRow );
		}

		private void extractInputsFrom( RowImpl _valueRow )
		{
			final CellInstance inputCountCell = _valueRow.getCellOrNull( INPUTCOUNT_COL );
			if (null != inputCountCell) {
				final int inputCount = ((Number) inputCountCell.getValue()).intValue();
				this.inputs = new Object[ inputCount ];
				this.inputTypes = new ValueType[ inputCount ];
				for (int i = 0; i < inputCount; i++) {
					final CellInstance inputCell = _valueRow.getCellOrNull( INPUTS_COL + i );
					this.inputs[ i ] = inputCell.getValue();
					this.inputTypes[ i ] = valueTypeOf( this.inputs[ i ] );
				}
			}
		}

		private ValueType valueTypeOf( Object _value )
		{
			if (_value instanceof String) return ValueType.STRING;
			if (_value instanceof Date) return ValueType.DATE;
			if (_value instanceof Boolean) return ValueType.BOOL;
			return ValueType.NUMBER;
		}


		public final void run() throws Exception
		{
			if (null == this.inputs) {
				new TestRunner().run();
			}
			else {
				final int inputLength = this.inputs.length;
				final int inputVariations = (int) Math.pow( 2, inputLength );
				for (int activationBits = 0; activationBits < inputVariations; activationBits++) {
					new TestRunner( activationBits ).run();
				}
			}
		}

		private class TestRunner
		{
			private final String testName;
			private final BitSet inputActivationBits;

			public TestRunner()
			{
				super();
				this.inputActivationBits = null;
				this.testName = RowRunner.this.rowName;
			}

			public TestRunner(int _activationBits)
			{
				super();
				this.inputActivationBits = new BitSet( _activationBits );
				this.testName = RowRunner.this.rowName + " @ " + Integer.toBinaryString( _activationBits );
			}

			public final void run() throws Exception
			{
				run( false );
				run( true );
			}

			private final void run( boolean _caching ) throws Exception
			{
				new DoubleTestRunner( _caching ).run();
				new BigDecimalTestRunner( _caching ).run();
				new ScaledLongTestRunner( _caching ).run();
			}


			private abstract class TypedTestRunner
			{
				private final String typedTestName;
				private final NumericType numericType;
				private final boolean caching;

				protected TypedTestRunner(NumericType _type, boolean _caching)
				{
					super();
					this.typedTestName = TestRunner.this.testName + " using " + _type + (_caching ? " (caching)" : "");
					this.numericType = _type;
					this.caching = _caching;
				}


				public final void run() throws Exception
				{
					// System.out.println( this.typedTestName );

					final EngineBuilder eb = SEJ.newEngineBuilder();
					eb.setSpreadsheet( RowRunner.this.formulaRow.getSheet().getSpreadsheet() );
					eb.setNumericType( this.numericType );
					configureInterface( eb, this.caching );
					final Section b = eb.getRootBinder();
					b.defineOutputCell( RowRunner.this.formula.getCellIndex(), new CallFrame( b.getOutputClass().getMethod(
							"get" + RowRunner.this.expectedType.toString() ) ) );

					if (null != TestRunner.this.inputActivationBits) {
						final RowImpl valueRow = RowRunner.this.valueRow;
						final Object[] inputs = RowRunner.this.inputs;
						for (int i = 0; i < inputs.length; i++) {
							if (TestRunner.this.inputActivationBits.get( i )) {
								b.defineInputCell( valueRow.getCellIndex( INPUTS_COL + i ), new CallFrame( b.getInputClass()
										.getMethod( "get" + RowRunner.this.inputTypes[ i ].toString(), Integer.TYPE ), i ) );
							}
						}
					}

					final SaveableEngine e = eb.compile();
					try {
						final ComputationFactory f = e.getComputationFactory();

						final Outputs o = (Outputs) f.newComputation( newInputs() );

						switch (RowRunner.this.expectedType) {
							case NUMBER:
								assertNumber( this.typedTestName, o, ((Double) RowRunner.this.expected).doubleValue() );
								break;
							case STRING:
								assertEquals( this.typedTestName, (String) RowRunner.this.expected, o.getSTRING() );
								break;
							case DATE:
								assertEquals( this.typedTestName, RowRunner.this.expected.toString(), o.getDATE().toString() );
								break;
							case BOOL:
								assertEquals( this.typedTestName, ((Boolean) RowRunner.this.expected).booleanValue(), o
										.getBOOL() );
								break;
						}
					}
					catch (AssertionFailedError ex) {
						Debug.saveEngine( e, "d:/temp/ref.jar" );
						throw ex;
					}
				}


				protected abstract void configureInterface( EngineBuilder eb, boolean _caching );
				protected abstract Inputs newInputs();
				protected abstract void assertNumber( String _name, Outputs o, double _expected );

			}


			private final class DoubleTestRunner extends TypedTestRunner
			{

				public DoubleTestRunner(boolean _caching)
				{
					super( SEJ.DOUBLE, _caching );
				}

				@Override
				protected void configureInterface( EngineBuilder eb, boolean caching )
				{
					eb.setInputClass( DoubleInputs.class );
					eb.setOutputClass( DoubleOutputs.class );
				}

				@Override
				protected Inputs newInputs()
				{
					return new DoubleInputs();
				}

				@Override
				protected void assertNumber( String _name, Outputs o, double expected )
				{
					final double actual = ((DoubleOutputs) o).getNUMBER();
					assertEquals( _name, expected, actual, 0.0000001 );
				}

			}


			private final class BigDecimalTestRunner extends TypedTestRunner
			{

				public BigDecimalTestRunner(boolean _caching)
				{
					super( SEJ.BIGDECIMAL8, _caching );
				}

				@Override
				protected void configureInterface( EngineBuilder eb, boolean caching )
				{
					eb.setInputClass( BigDecimalInputs.class );
					eb.setOutputClass( BigDecimalOutputs.class );
				}

				@Override
				protected Inputs newInputs()
				{
					return new BigDecimalInputs();
				}

				@Override
				protected void assertNumber( String _name, Outputs o, double _expected )
				{
					// Use toString because using the double is imprecise on JRE 1.4!
					final BigDecimal expected = new BigDecimal( Double.valueOf( _expected ).toString() ).setScale( 8 );
					final BigDecimal actual = ((BigDecimalOutputs) o).getNUMBER();
					if (!actual.equals( expected )) {
						assertEquals( _name, expected.toPlainString(), actual.toPlainString() );
					}
				}

			}


			private final class ScaledLongTestRunner extends TypedTestRunner
			{

				public ScaledLongTestRunner(boolean _caching)
				{
					super( SEJ.SCALEDLONG4, _caching );
				}

				@Override
				protected void configureInterface( EngineBuilder eb, boolean caching )
				{
					eb.setInputClass( ScaledLongInputs.class );
					eb.setOutputClass( ScaledLongOutputs.class );
				}

				@Override
				protected Inputs newInputs()
				{
					return new ScaledLongInputs();
				}

				@Override
				protected void assertNumber( String _name, Outputs o, double _expected )
				{
					final long expected = ((Long) SEJ.SCALEDLONG4.valueOf( _expected )).longValue();
					final long actual = ((ScaledLongOutputs) o).getNUMBER();
					assertEquals( _name, expected, actual );
				}

			}

		}

		public abstract class Inputs
		{
			public String getSTRING( int i )
			{
				return (String) RowRunner.this.inputs[ i ];
			}

			public Date getDATE( int i )
			{
				return (Date) RowRunner.this.inputs[ i ];
			}

			public boolean getBOOL( int i )
			{
				return (Boolean) RowRunner.this.inputs[ i ];
			}
		}

		public static abstract class Outputs
		{
			public String getSTRING()
			{
				throw new AbstractMethodError( "getString" );
			}

			public Date getDATE()
			{
				throw new AbstractMethodError( "getDate" );
			}

			public boolean getBOOL()
			{
				throw new AbstractMethodError( "getBoolean" );
			}
		}


		public final class DoubleInputs extends Inputs
		{
			public double getNUMBER( int i )
			{
				return (Double) RowRunner.this.inputs[ i ];
			}
		}

		public static class DoubleOutputs extends Outputs
		{
			public double getNUMBER()
			{
				throw new AbstractMethodError( "getDouble" );
			}
		}

		public static abstract class DoubleOutputsWithCaching extends DoubleOutputs implements Resettable
		{
			// Nothing new here.
		}


		public final class BigDecimalInputs extends Inputs
		{
			public BigDecimal getNUMBER( int i )
			{
				return (BigDecimal) RowRunner.this.inputs[ i ];
			}
		}

		public static class BigDecimalOutputs extends Outputs
		{
			public BigDecimal getNUMBER()
			{
				throw new AbstractMethodError( "getBigDecimal" );
			}
		}

		public static abstract class BigDecimalOutputsWithCaching extends BigDecimalOutputs implements Resettable
		{
			// Nothing new here.
		}


		@ScaledLong(4)
		public final class ScaledLongInputs extends Inputs
		{
			public long getNUMBER( int i )
			{
				return (Long) RowRunner.this.inputs[ i ];
			}
		}

		@ScaledLong(4)
		public static class ScaledLongOutputs extends Outputs
		{
			public long getNUMBER()
			{
				throw new AbstractMethodError( "getScaledLong" );
			}
		}

		public static abstract class ScaledLongOutputsWithCaching extends ScaledLongOutputs implements Resettable
		{
			// Nothing new here.
		}


	}


	private static enum ValueType {
		NUMBER, STRING, DATE, BOOL;
	}

}
