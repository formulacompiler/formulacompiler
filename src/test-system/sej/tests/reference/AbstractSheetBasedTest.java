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
package sej.tests.reference;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import sej.CallFrame;
import sej.EngineBuilder;
import sej.NumericType;
import sej.SEJ;
import sej.SaveableEngine;
import sej.SpreadsheetBinder.Section;
import sej.describable.DescriptionBuilder;
import sej.internal.expressions.ExpressionNode;
import sej.internal.logging.Log;
import sej.internal.spreadsheet.CellInstance;
import sej.internal.spreadsheet.CellRefFormat;
import sej.internal.spreadsheet.CellWithConstant;
import sej.internal.spreadsheet.CellWithLazilyParsedExpression;
import sej.internal.spreadsheet.RowImpl;
import sej.internal.spreadsheet.SheetImpl;
import sej.internal.spreadsheet.SpreadsheetImpl;
import sej.internal.spreadsheet.loader.excel.ExcelExpressionParserAccessor;
import sej.internal.spreadsheet.saver.excel.xls.ExcelXLSExpressionFormatter;
import sej.runtime.ComputationFactory;
import sej.runtime.Resettable;
import sej.runtime.ScaledLong;
import junit.framework.TestCase;

public abstract class AbstractSheetBasedTest extends TestCase
{
	public static final Log LOG = new Log();

	private static final File SPREADSHEET_PATH = new File( "src/test-system/testdata/sej/tests/reference" );
	private static final File HTML_PATH = new File( "temp/reference" );

	private static final Double DOUBLE_DUMMY = 023974.239874;
	private static final BigDecimal BIGDECIMAL_DUMMY = BigDecimal.valueOf( 023974.239874 );
	private static final Long LONG_DUMMY = 923748L;
	private static final Date DATE_DUMMY = Calendar.getInstance().getTime();
	private static final String STRING_DUMMY = "-DUMMY-";

	protected static final int CACHING_VARIANTS = 2;
	protected static final int TYPE_VARIANTS = 3;
	private static final int UNDEFINED_STARTING_ROW = -1;

	private final String baseName;
	private final String spreadsheetName;

	private int givenStartingRow;
	private int runOnlyRowNumbered = -1;
	private NumType runOnlyType = null;
	private int runOnlyInputVariant = -1;
	private Boolean runOnlyCacheVariant = null;
	private int numberOfEnginesCompiled = 0;

	protected static enum NumType {
		DOUBLE, BIGDECIMAL, LONG;
	}


	static {
		HTML_PATH.mkdirs();
	}


	protected AbstractSheetBasedTest()
	{
		super();
		this.givenStartingRow = UNDEFINED_STARTING_ROW;
		this.baseName = extractBaseNameFrom( getClass().getSimpleName() );
		this.spreadsheetName = this.baseName + ".xls";
	}

	protected AbstractSheetBasedTest(String _baseName, int _startingRowNumber)
	{
		super();
		this.baseName = _baseName;
		this.spreadsheetName = this.baseName + ".xls";
		this.givenStartingRow = _startingRowNumber - 1;
	}

	protected AbstractSheetBasedTest(String _baseName, int _onlyRowNumbered, NumType _onlyType, int _onlyInputVariant,
			boolean _caching)
	{
		super();
		this.baseName = _baseName;
		this.spreadsheetName = this.baseName + ".xls";
		this.runOnlyRowNumbered = _onlyRowNumbered;
		this.runOnlyType = _onlyType;
		this.runOnlyInputVariant = _onlyInputVariant;
		this.runOnlyCacheVariant = _caching;
	}


	private static String extractBaseNameFrom( String _name )
	{
		if (_name.endsWith( "Test" )) {
			return _name.substring( 0, _name.length() - 4 );
		}
		else {
			return _name;
		}
	}

	private static void writeStringTo( String _value, File _target ) throws IOException
	{
		BufferedWriter writer = new BufferedWriter( new FileWriter( _target ) );
		try {
			if (null != _value) writer.write( _value );
		}
		finally {
			writer.close();
		}
	}


	public void testExpressions() throws Exception
	{
		final EngineBuilder eb = SEJ.newEngineBuilder();
		eb.loadSpreadsheet( new File( SPREADSHEET_PATH, this.spreadsheetName ) );
		newSheetRunner( (SpreadsheetImpl) eb.getSpreadsheet() ).run();
	}

	protected abstract AbstractSheetRunner newSheetRunner( SpreadsheetImpl _impl );


	private void reportTestRun( String _testName )
	{
		LOG.a( _testName ).lf().i();
	}

	protected void reportDefectiveEngine( SaveableEngine _engine, String _testName )
	{
		// overridable
	}

	private void reportEndOfTestRun( String _testName )
	{
		LOG.o();
	}

	protected final String htmlize( String _text )
	{
		return _text.replace( "&", "&amp;" ).replace( "<", "&lt;" ).replace( ">", "&gt;" );
	}

	protected final int getNumberOfEnginesCompiled()
	{
		return this.numberOfEnginesCompiled;
	}


	protected abstract class AbstractSheetRunner
	{
		protected final SpreadsheetImpl book;
		protected final DescriptionBuilder html = new DescriptionBuilder();
		protected final DescriptionBuilder terms = new DescriptionBuilder();
		protected String sequenceName;
		protected String[] highlightTerms;
		protected int columnCount = 2;
		protected String lastNormalizedExprShown = "";

		public AbstractSheetRunner(SpreadsheetImpl _book)
		{
			super();
			this.book = _book;
		}


		protected abstract int expectedCol();
		protected abstract int formulaCol();
		protected abstract int firstInputCol();
		protected abstract int nameCol();
		protected abstract int highlightCol();
		protected abstract int skipForCol();
		protected abstract int excelSaysCol();
		protected abstract int inputColumnCountFor( RowImpl _row );


		public final void run() throws Exception
		{
			startHtml();
			startTerms();

			final SheetImpl sheet = this.book.getSheetList().get( 0 );
			final List<RowImpl> rows = sheet.getRowList();
			final int onlyRow = AbstractSheetBasedTest.this.runOnlyRowNumbered;
			int atRow = getStartingRow();
			while (atRow < rows.size()) {
				final RowImpl row = rows.get( atRow++ );
				if (onlyRow < 0 || atRow == onlyRow) {
					if (null != row.getCellOrNull( formulaCol() )) {

						final CellInstance nameCell = row.getCellOrNull( nameCol() );
						if (null != nameCell) {
							final String newName = nameCell.getValue().toString();
							if (!newName.equals( "..." )) {
								this.sequenceName = newName;
								final CellInstance highlightCell = row.getCellOrNull( highlightCol() );
								if (null != highlightCell) {
									final String highlights = highlightCell.getValue().toString();
									if (highlights.equals( "xx" )) {
										this.highlightTerms = null;
									}
									else {
										this.highlightTerms = htmlize( highlights ).split( " " );
										emitTerm( highlights.split( " " )[ 0 ] );
									}
								}
								this.columnCount = maxColumnCountInSequence( rows, row, atRow );
								emitNameToHtml();
							}
						}

						final SaveableEngine[] engines = newRowRunner( row, row, atRow, null ).run();

						while (atRow < rows.size()) {
							final RowImpl variantRow = rows.get( atRow );
							final CellInstance variantNameCell = variantRow.getCellOrNull( nameCol() );
							if (null == variantNameCell || !variantNameCell.getValue().toString().equals( "..." )) break;
							atRow++;

							newRowRunner( row, variantRow, atRow, engines ).run();

						}

					}
				}
			}

			endHtml();
			endTerms();
		}

		private final int getStartingRow()
		{
			final int given = AbstractSheetBasedTest.this.givenStartingRow;
			return given == UNDEFINED_STARTING_ROW ? getDefaultStartingRow() : given;
		}

		protected abstract int getDefaultStartingRow();

		protected abstract AbstractRowRunner newRowRunner( RowImpl _formulaRow, RowImpl _valueRow, int _rowNumber,
				SaveableEngine[] _engines );


		private int maxColumnCountInSequence( List<RowImpl> _rows, RowImpl _row, int _nextRow )
		{
			int result = inputColumnCountFor( _row );
			int atRow = _nextRow;
			while (atRow < _rows.size()) {
				final RowImpl nextRow = _rows.get( atRow );
				final CellInstance nameCell = nextRow.getCellOrNull( nameCol() );
				if (null != nameCell && !nameCell.getValue().toString().equals( "..." )) break;
				atRow++;
				result = Math.max( result, inputColumnCountFor( nextRow ) );
			}
			return result;
		}


		private void startHtml()
		{
			// No intro.
		}

		private void emitNameToHtml()
		{
			final DescriptionBuilder h = this.html;
			if (h.length() > 0) {
				h.appendLine( "</tbody></table><p/>" );
			}
			h.append( "<h5 class=\"ref\">" ).append( htmlize( this.sequenceName ) ).appendLine( "</h5>" );
			h.append( "<table class=\"xl ref\"><thead><tr><td/>" );
			for (int col = 0; col < this.columnCount; col++) {
				h.append( "<td>" ).append( (char) ('A' + col) ).append( "</td>" );
			}
			h.appendLine( "</tr></thead><tbody>" );
			this.lastNormalizedExprShown = "";
		}

		private void endHtml() throws IOException
		{
			final DescriptionBuilder h = this.html;
			h.appendLine( "</tbody></table><p/>" );
			writeStringTo( h.toString(), new File( HTML_PATH, AbstractSheetBasedTest.this.baseName + ".htm" ) );
		}


		private void startTerms()
		{
			// No intro.
		}

		private void emitTerm( String _term )
		{
			final DescriptionBuilder h = this.terms;
			h.append( "terms << '" ).append( _term ).appendLine( "'" );
		}

		private void endTerms() throws IOException
		{
			final DescriptionBuilder h = this.terms;
			writeStringTo( h.toString(), new File( HTML_PATH, AbstractSheetBasedTest.this.baseName + "_terms.rb" ) );
		}


		protected abstract class AbstractRowRunner
		{
			private final int rowNumber;
			private final String rowName;
			private final RowImpl formulaRow;
			private final RowImpl valueRow;
			private final Object expected;
			private final ValueType expectedType;
			private final CellInstance formula;
			private final SaveableEngine[] fullyParametrizedTestEngines;
			private final String skipFor;
			protected Object[] inputs;
			protected ValueType[] inputTypes;

			public AbstractRowRunner(RowImpl _formulaRow, RowImpl _valueRow, int _rowNumber, SaveableEngine[] _engines)
			{
				super();
				this.rowNumber = _rowNumber;
				this.rowName = "R" + _rowNumber + ": " + AbstractSheetRunner.this.sequenceName;
				this.formulaRow = _formulaRow;
				this.valueRow = _valueRow;
				this.expected = expectedValueOf( valueOf( _valueRow.getCellOrNull( expectedCol() ) ) );
				this.expectedType = valueTypeOf( this.expected );
				this.formula = _formulaRow.getCellOrNull( formulaCol() );
				this.fullyParametrizedTestEngines = _engines;
				final CellInstance skipForCell = _valueRow.getCellOrNull( skipForCol() );
				this.skipFor = (skipForCell == null) ? "" : valueOf( skipForCell ).toString();
				extractInputsFrom( _valueRow );
			}

			protected final Object valueOf( final CellInstance _cell )
			{
				return (_cell == null) ? null : _cell.getValue();
			}

			protected abstract void extractInputsFrom( RowImpl _valueRow );

			private static final long SECS_PER_DAY = 24 * 60 * 60;
			private static final long MS_PER_SEC = 1000;
			private static final long MS_PER_DAY = SECS_PER_DAY * MS_PER_SEC;

			private Object expectedValueOf( Object _value )
			{
				if (_value instanceof String) {
					if (_value.toString().equals( "(days from 2006)" )) {
						final Calendar start = Calendar.getInstance();
						start.clear();
						start.set( 2006, Calendar.JANUARY, 1 );
						final Calendar time = Calendar.getInstance();
						time.set( Calendar.HOUR, 0 );
						time.set( Calendar.MINUTE, 0 );
						time.set( Calendar.SECOND, 0 );
						time.set( Calendar.MILLISECOND, 0 );
						final long diff = time.getTimeInMillis() - start.getTimeInMillis();
						final long days = diff / MS_PER_DAY;
						return Double.valueOf( days );
					}
					if (_value.toString().equals( "Infinity" )) {
						return 1.0 / 0.0;
					}
				}
				return _value;
			}

			protected ValueType valueTypeOf( Object _value )
			{
				if (_value instanceof String) return ValueType.STRING;
				if (_value instanceof Date) return ValueType.DATE;
				if (_value instanceof Boolean) return ValueType.BOOL;
				return ValueType.NUMBER;
			}

			public final SaveableEngine[] run() throws Exception
			{
				testExpressionConversion();

				if (null == this.inputs) {
					final TestRunner test = newTestRunner();
					test.emitTestToHtml();
					test.run();
					return null;
				}
				else {
					final int inputLength = this.inputs.length;
					final int inputVariations = (int) Math.pow( 2, inputLength );
					final int onlyVariant = AbstractSheetBasedTest.this.runOnlyInputVariant;
					TestRunner test;
					if (onlyVariant >= 0) {
						test = newTestRunner( onlyVariant );
					}
					else if (this.formulaRow == this.valueRow) {
						for (int activationBits = 0; activationBits < inputVariations - 1; activationBits++) {
							newTestRunner( activationBits ).run();
						}
						test = newTestRunner( inputVariations - 1 );
						test.emitTestToHtml();
					}
					else {
						test = newTestRunner( inputVariations - 1, this.fullyParametrizedTestEngines );
						test.emitTestToHtml();
					}

					return test.run();
				}
			}

			private final TestRunner newTestRunner()
			{
				return newTestRunner( 0 );
			}

			private final TestRunner newTestRunner( int _activationBits )
			{
				return newTestRunner( this.rowName, _activationBits, null );
			}

			private final TestRunner newTestRunner( int _activationBits, SaveableEngine[] _fullyParametrizedTestEngines )
			{
				return newTestRunner( this.rowName + " @ " + Integer.toBinaryString( _activationBits ), _activationBits,
						_fullyParametrizedTestEngines );
			}

			protected TestRunner newTestRunner( String _testName, int _activationBits,
					SaveableEngine[] _fullyParametrizedTestEngines )
			{
				return new TestRunner( _testName, _activationBits, _fullyParametrizedTestEngines );
			}


			private void testExpressionConversion() throws Exception
			{
				if (this.formula instanceof CellWithLazilyParsedExpression) {
					final CellWithLazilyParsedExpression exprCell = (CellWithLazilyParsedExpression) this.formula;
					final ExpressionNode expr = exprCell.getExpression();
					final ExcelXLSExpressionFormatter formatter = new ExcelXLSExpressionFormatter();
					final ExcelExpressionParserAccessor parser = new ExcelExpressionParserAccessor( this.formula );
					final String expected = formatter.format( expr );
					final ExpressionNode parsed = parser.parseText( expected, CellRefFormat.A1 );
					final String actual = formatter.format( parsed );
					assertEquals( expected, actual );
				}
			}


			protected class TestRunner
			{
				private final String testName;
				private final int inputActivationBits;
				private final SaveableEngine[] testEngines;

				public TestRunner(String _testName, int _activationBits, SaveableEngine[] _engines)
				{
					super();
					this.testName = _testName;
					this.inputActivationBits = _activationBits;
					this.testEngines = _engines;
				}

				public final void emitTestToHtml()
				{
					final DescriptionBuilder h = AbstractSheetRunner.this.html;

					String expr = "...";
					String exprPrec = "";
					final RowImpl valueRow = AbstractRowRunner.this.valueRow;
					if (valueRow == AbstractRowRunner.this.formulaRow) {
						final CellWithLazilyParsedExpression exprCell = (CellWithLazilyParsedExpression) AbstractRowRunner.this.formula;
						expr = "=" + highlightTermIn( htmlize( exprCell.getExpressionParser().getSource() ) );
						exprPrec = htmlPrecision( exprCell );
						final String exprNormalized = expr
								.replace( Integer.toString( AbstractRowRunner.this.rowNumber ), "?" );
						if (exprNormalized.equals( AbstractSheetRunner.this.lastNormalizedExprShown )) {
							expr = "...";
						}
						else {
							AbstractSheetRunner.this.lastNormalizedExprShown = exprNormalized;
						}
					}

					final Object expected = AbstractRowRunner.this.expected;
					final String expectedCls = htmlCellClass( expected );
					h.append( "<tr><td class=\"xl-row\">" ).append( AbstractRowRunner.this.rowNumber ).append( "</td>" );
					h.append( "<td" ).append( expectedCls ).append( ">" ).append( expected ).append( "</td>" );
					h.append( "<td class=\"xl-exp\">" ).append( expr ).append( exprPrec ).append( "</td>" );

					final int columnCount = AbstractSheetRunner.this.columnCount;
					for (int col = firstInputCol(); col < columnCount; col++) {
						final CellInstance inputCell = valueRow.getCellOrNull( col );
						if (inputCell != null) {
							final Object input = inputCell.getValue();
							if (null != input) {
								final String inputCls = htmlCellClass( input );
								h.append( "<td" ).append( inputCls ).append( ">" ).append( htmlValue( input ) ).append(
										htmlPrecision( inputCell ) ).append( "</td>" );
							}
							else {
								final String inputExpr = ((CellWithLazilyParsedExpression) inputCell).getExpressionParser()
										.getSource();
								h.append( "<td>" ).append( htmlValue( inputExpr ) ).append( htmlPrecision( inputCell ) )
										.append( "</td>" );
							}
						}
						else {
							h.append( "<td/>" );
						}
					}

					final CellInstance excelSaysCell = valueRow.getCellOrNull( excelSaysCol() );
					if (null != excelSaysCell) {
						final Object excelSays = excelSaysCell.getValue();
						h.append( "<td class=\"ref-bad\">Excel says: " ).append( htmlValue( excelSays ) ).append( "</td>" );
					}

					h.appendLine( "</tr>" );
				}

				private final Object htmlValue( Object _value )
				{
					if (_value == null) return "";
					if (_value.toString().equals( "" )) return "' (empty string)";
					return _value;
				}

				private final String htmlCellClass( Object _value )
				{
					String cls = "";
					if (_value instanceof Date) cls = " class=\"xl-date\"";
					else if (_value instanceof Number) cls = " class=\"xl-num\"";
					return cls;
				}

				private final String htmlPrecision( CellInstance _inputCell )
				{
					if (_inputCell != null && _inputCell.getNumberFormat() != null) {
						final int prec = _inputCell.getNumberFormat().getMaximumFractionDigits();
						final StringBuilder result = new StringBuilder();
						result.append( " <span class=\"ref-prec\">(#0." );
						for (int i = 0; i < prec; i++) {
							result.append( "0" );
						}
						result.append( ")</span>" );
						return result.toString();
					}
					return "";
				}

				private final String highlightTermIn( String _source )
				{
					final String[] terms = AbstractSheetRunner.this.highlightTerms;
					if (terms == null) return _source;
					String result = _source;
					for (final String term : terms) {
						result = result.replace( term, "<em>" + term + "</em>" );
					}
					return result;
				}


				public final SaveableEngine[] run() throws Exception
				{
					final SaveableEngine[] result = (this.testEngines != null) ? this.testEngines
							: new SaveableEngine[ CACHING_VARIANTS * TYPE_VARIANTS ];
					final Boolean onlyCache = AbstractSheetBasedTest.this.runOnlyCacheVariant;
					if (null != onlyCache) {
						run( result, onlyCache );
					}
					else {
						run( result, false );
						run( result, true );
					}
					return result;
				}

				private final void run( SaveableEngine[] _engines, boolean _caching ) throws Exception
				{
					final NumType onlyType = AbstractSheetBasedTest.this.runOnlyType;
					final String skipFor = AbstractRowRunner.this.skipFor;
					final int offsEngine = _caching ? TYPE_VARIANTS : 0;
					int iEngine;
					if ((null == onlyType || NumType.DOUBLE == onlyType) && !skipFor.contains( "double" )) {
						iEngine = offsEngine + 0;
						_engines[ iEngine ] = new DoubleTestRunner( _caching ).run( _engines[ iEngine ] );
					}
					if ((null == onlyType || NumType.BIGDECIMAL == onlyType) && !skipFor.contains( "big" )) {
						iEngine = offsEngine + 1;
						_engines[ iEngine ] = new BigDecimalTestRunner( _caching ).run( _engines[ iEngine ] );
					}
					if ((null == onlyType || NumType.LONG == onlyType) && !skipFor.contains( "long" )) {
						iEngine = offsEngine + 2;
						_engines[ iEngine ] = new ScaledLongTestRunner( _caching ).run( _engines[ iEngine ] );
					}
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


					public final SaveableEngine run( SaveableEngine _engine ) throws Exception
					{
						reportTestRun( this.typedTestName );

						SaveableEngine e = null;
						try {
							e = (_engine == null) ? compileEngine() : _engine;

							final ComputationFactory f = e.getComputationFactory();

							final Outputs o = (Outputs) f.newComputation( newInputs() );

							switch (AbstractRowRunner.this.expectedType) {
								case NUMBER:
									assertNumber( this.typedTestName, o, ((Double) AbstractRowRunner.this.expected)
											.doubleValue() );
									break;
								case STRING:
									assertEquals( this.typedTestName, (String) AbstractRowRunner.this.expected, o.getSTRING() );
									break;
								case DATE:
									assertEquals( this.typedTestName, AbstractRowRunner.this.expected.toString(), o.getDATE()
											.toString() );
									break;
								case BOOL:
									assertEquals( this.typedTestName,
											((Boolean) AbstractRowRunner.this.expected).booleanValue(), o.getBOOL() );
									break;
							}
						}
						catch (Error ex) {
							reportDefectiveEngine( e, this.typedTestName );
							throw ex;
						}

						reportEndOfTestRun( this.typedTestName );
						return e;
					}


					private final SaveableEngine compileEngine() throws Exception
					{
						final EngineBuilder eb = SEJ.newEngineBuilder();
						eb.setSpreadsheet( AbstractRowRunner.this.formulaRow.getSheet().getSpreadsheet() );
						eb.setNumericType( this.numericType );
						configureInterface( eb, this.caching );
						final Section b = eb.getRootBinder();
						b.defineOutputCell( AbstractRowRunner.this.formula.getCellIndex(), new CallFrame( b.getOutputClass()
								.getMethod( "get" + AbstractRowRunner.this.expectedType.toString() ) ) );

						if (TestRunner.this.inputActivationBits == 0) {
							AbstractSheetBasedTest.this.numberOfEnginesCompiled++;
							return eb.compile();
						}
						else {
							final RowImpl formulaRow = AbstractRowRunner.this.formulaRow;
							final Object[] inputs = AbstractRowRunner.this.inputs;
							final CellWithConstant[] originalCells = new CellWithConstant[ inputs.length ];
							final Object[] originalCellValues = new Object[ inputs.length ];
							try {
								for (int i = 0; i < inputs.length; i++) {
									if (((1 << i) & TestRunner.this.inputActivationBits) != 0) {
										final ValueType inputType = AbstractRowRunner.this.inputTypes[ i ];
										b.defineInputCell( formulaRow.getCellIndex( firstInputCol() + i ), new CallFrame( b
												.getInputClass().getMethod( "get" + inputType.toString(), Integer.TYPE ), i ) );
										final CellInstance valueCell = formulaRow.getCellOrNull( firstInputCol() + i );
										if (valueCell instanceof CellWithConstant) {
											final CellWithConstant constCell = (CellWithConstant) valueCell;
											final Object originalValue = constCell.getValue();
											originalCells[ i ] = constCell;
											originalCellValues[ i ] = originalValue;
											constCell.setValue( modifiedValue( originalValue ) );
										}
									}
								}
								AbstractSheetBasedTest.this.numberOfEnginesCompiled++;
								return eb.compile();
							}
							finally {
								for (int i = 0; i < originalCells.length; i++) {
									final CellWithConstant constCell = originalCells[ i ];
									if (null != constCell) {
										constCell.setValue( originalCellValues[ i ] );
									}
								}
							}
						}
					}

					private final Object modifiedValue( Object _value )
					{
						if (_value instanceof Double) {
							return DOUBLE_DUMMY;
						}
						if (_value instanceof BigDecimal) {
							return BIGDECIMAL_DUMMY;
						}
						if (_value instanceof Long) {
							return LONG_DUMMY;
						}
						if (_value instanceof Date) {
							return DATE_DUMMY;
						}
						if (_value instanceof Boolean) {
							boolean v = (Boolean) _value;
							return !v;
						}
						if (_value instanceof String) {
							return STRING_DUMMY;
						}
						return _value;
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
						final BigDecimal expected = new BigDecimal( Double.valueOf( _expected ).toString() ).setScale( 8,
								BigDecimal.ROUND_HALF_UP );
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
					return (String) AbstractRowRunner.this.inputs[ i ];
				}

				public Date getDATE( int i )
				{
					return (Date) AbstractRowRunner.this.inputs[ i ];
				}

				public boolean getBOOL( int i )
				{
					return (Boolean) AbstractRowRunner.this.inputs[ i ];
				}
			}

			public final class DoubleInputs extends Inputs
			{
				public Double getNUMBER( int i )
				{
					return (Double) AbstractRowRunner.this.inputs[ i ];
				}
			}

			public final class BigDecimalInputs extends Inputs
			{
				public BigDecimal getNUMBER( int i )
				{
					final Object val = AbstractRowRunner.this.inputs[ i ];
					return (val == null) ? null : BigDecimal.valueOf( (Double) val );
				}
			}

			@ScaledLong(4)
			public final class ScaledLongInputs extends Inputs
			{
				public Long getNUMBER( int i )
				{
					final Object val = AbstractRowRunner.this.inputs[ i ];
					return (val == null) ? null : (Long) SEJ.SCALEDLONG4.valueOf( (Double) val );
				}
			}

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


	protected static enum ValueType {
		NUMBER, STRING, DATE, BOOL;
	}

}
