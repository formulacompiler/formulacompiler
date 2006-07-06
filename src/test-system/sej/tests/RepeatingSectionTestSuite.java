package sej.tests;

import java.io.File;
import java.util.List;

import sej.CallFrame;
import sej.EngineBuilder;
import sej.Orientation;
import sej.SEJ;
import sej.SaveableEngine;
import sej.SpreadsheetBinder.Section;
import sej.internal.Debug;
import sej.internal.expressions.ExpressionNode;
import sej.internal.spreadsheet.CellIndex;
import sej.internal.spreadsheet.CellInstance;
import sej.internal.spreadsheet.CellRange;
import sej.internal.spreadsheet.Reference;
import sej.internal.spreadsheet.RowImpl;
import sej.internal.spreadsheet.SheetImpl;
import sej.internal.spreadsheet.SpreadsheetImpl;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RepeatingSectionTestSuite extends TestSuite
{
	private static final int FIRST_TEST_ROW = 5;
	private static final int NAME_COL = 0;
	private static final int FORMULA_COL = 1;
	private static final int RANGE_NAME_COL = 2;


	public RepeatingSectionTestSuite()
	{
		try {
			addTestsIn( "SectionTests.xls" );
			// LATER addTestsIn( "Tests.xml" );
		}
		catch (Exception e) {
			e.printStackTrace();
			addTest( warning( e.getMessage() ) );
		}
	}


	public void testAll()
	{
		// Needed for some strange reason to make tests run in Eclipse.
	}


	private SpreadsheetImpl workbook;
	private SheetImpl sheet;
	private List<RowImpl> rows;


	@SuppressWarnings("unqualified-field-access")
	private void addTestsIn( String _fileName ) throws Exception
	{
		final TestSuite fileSuite = new TestSuite( _fileName );
		this.addTest( fileSuite );

		final String filePath = "src/test-system/testdata/sej/tests/" + _fileName;
		workbook = (SpreadsheetImpl) SEJ.loadSpreadsheet( new File( filePath ) );
		sheet = workbook.getSheetList().get( 0 );
		rows = sheet.getRowList();

		for (int iRow = FIRST_TEST_ROW; iRow < rows.size(); iRow++) {
			final RowImpl row = rows.get( iRow );
			final List<CellInstance> cells = row.getCellList();
			if (cells.size() > RANGE_NAME_COL) {
				final CellInstance rangeNameCell = cells.get( RANGE_NAME_COL );
				if (rangeNameCell != null) {
					final Object rangeNameValue = rangeNameCell.getValue();
					if (rangeNameValue instanceof String) {
						final String rangeName = (String) rangeNameValue;
						final CellInstance formulaCell = cells.get( FORMULA_COL );
						final CellInstance nameCell = cells.get( NAME_COL );
						final String testName = nameCell.getValue().toString();

						final TestSuite rangeSuite = new RangeTestSuite( testName, formulaCell, rangeName );

						// if (rangeName.equals( "H_THREE" )) // FIX-ME
						fileSuite.addTest( rangeSuite );

					}
				}
			}
		}
	}


	private final class RangeTestSuite extends TestSuite
	{
		private final CellInstance formulaCell;
		private final CellRange sectionRange;
		private final CellInstance[][] sectionCells;
		private final CellInstance[][] expectedResultsCells;
		private final Orientation orientation;
		private final CellInstance[][] outerRefCells;


		@SuppressWarnings("unqualified-field-access")
		public RangeTestSuite(String _name, CellInstance _formulaCell, String _rangeName)
		{
			super( _name );

			final CellRange expectedResultsRange = namedRange( _rangeName + "_E" );
			final CellRange outerRefsRange = namedRange( _rangeName + "_R" );

			formulaCell = _formulaCell;
			sectionRange = (CellRange) workbook.getNamedRef( _rangeName );
			orientation = (_rangeName.charAt( 0 ) == 'H') ? Orientation.HORIZONTAL : Orientation.VERTICAL;
			sectionCells = extractRangeCells( sectionRange, orientation );
			expectedResultsCells = extractRangeCells( expectedResultsRange, orientation );
			outerRefCells = extractRangeCells( outerRefsRange, orientation );

			addTests( false );
			// FIXME addTests( true );
		}


		private void addTests( boolean _caching )
		{
			addTest( new DoubleTestCase( _caching ) );
			// FIXME _suite.addTest( new BigDecimalTestCase( _caching ) );
			// FIXME _suite.addTest( new ScaledLongTestCase( _caching ) );
		}


		private abstract class SectionTestCase extends TestCase
		{
			private final boolean caching;


			public SectionTestCase(boolean _caching)
			{
				super( _caching ? "caching" : "transient" ); // FIXME name
				this.caching = _caching;
			}


			@SuppressWarnings("unqualified-field-access")
			@Override
			protected void runTest() throws Throwable
			{
				final EngineBuilder eb = SEJ.newEngineBuilder();
				eb.setSpreadsheet( workbook );
				eb.setFactoryClass( OutputFactory.class );
				// FIXME numeric type
				// FIXME caching
				final Section rb = eb.getRootBinder();
				rb.defineOutputCell( formulaCell.getCellIndex(), new CallFrame( Output.class.getMethod( "result" ) ) );
				Section sb = rb.defineRepeatingSection( sectionRange, orientation, new CallFrame( Input.class
						.getMethod( "subs" ) ), Input.class, null, null );

				final CellInstance[] templateCells = sectionCells[ 0 ];
				int iCell = 0;
				for (CellInstance c : templateCells) {
					if (c.getExpression() == null) {
						sb.defineInputCell( c.getCellIndex(), new CallFrame( Input.class.getMethod( "value", Integer.TYPE ),
								iCell ) );
					}
					iCell++;
				}

				SaveableEngine eng = eb.compile();

				Debug.saveEngine( eng, "/temp/sect.jar" );

				OutputFactory fact = (OutputFactory) eng.getComputationFactory();

				for (int iRes = 0; iRes < sectionCells.length; iRes++) {
					final Input in = new Input( iRes + 1, sectionCells, outerRefCells );
					final Output out = fact.newOutput( in );
					final double actual = out.result();
					final double expected = (Double) expectedResultsCells[ iRes ][ 0 ].getValue();
					assertEquals( getName() + "@" + iRes, expected, actual, 0.00001 );
				}
			}

		}


		private final class DoubleTestCase extends SectionTestCase
		{

			public DoubleTestCase(boolean _caching)
			{
				super( _caching );
			}

		}

	}


	public static interface OutputFactory
	{
		Output newOutput( Input _i );
	}

	public static interface Output
	{
		double result();
	}


	public static class Input
	{
		private final double[] values;
		private final Input[] subs;

		public Input(int _len, CellInstance[][] _sectionCells, CellInstance[][] _outerRefCells)
		{
			super();

			this.values = new double[ _outerRefCells.length ];
			for (int i = 0; i < _outerRefCells.length; i++) {
				final CellInstance cell = _outerRefCells[ i ][ 0 ];
				if (cell.getExpression() == null) {
					this.values[ i ] = (Double) cell.getValue();
				}
			}

			this.subs = new Input[ _len ];
			for (int i = 0; i < _len; i++) {
				this.subs[ i ] = new Input( _sectionCells[ i ] );
			}
		}

		public Input(CellInstance[] _cells)
		{
			super();
			this.values = new double[ _cells.length ];
			this.subs = null;
			for (int i = 0; i < _cells.length; i++) {
				if (_cells[ i ].getExpression() == null) {
					this.values[ i ] = (Double) _cells[ i ].getValue();
				}
			}
		}

		public double value( int _i )
		{
			return this.values[ _i ];
		}

		public Input[] subs()
		{
			return this.subs;
		}

	}


	private static Test warning( final String message )
	{
		return new TestCase( "warning" )
		{
			@Override
			protected void runTest()
			{
				fail( message );
			}
		};
	}


	private CellRange namedRange( String _name )
	{
		Reference ref = this.workbook.getNamedRef( _name );
		if (ref instanceof CellRange) {
			return (CellRange) ref;
		}
		return null;
	}

	private CellInstance[][] extractRangeCells( CellRange _range, Orientation _orientation )
	{
		if (_range == null) return new CellInstance[ 0 ][];

		final CellIndex f = _range.getFrom();
		final CellIndex t = _range.getTo();
		final Orientation other = (_orientation == Orientation.HORIZONTAL) ? Orientation.VERTICAL
				: Orientation.HORIZONTAL;
		final int felt = f.getIndex( _orientation );
		final int telt = t.getIndex( _orientation );
		final int nelt = telt - felt + 1;
		final int fcell = f.getIndex( other );
		final int tcell = t.getIndex( other );
		final int ncell = tcell - fcell + 1;

		final CellInstance[][] r = new CellInstance[ nelt ][];

		for (int ielt = 0; ielt < nelt; ielt++) {
			final CellInstance[] cells = new CellInstance[ ncell ];
			for (int icell = 0; icell < ncell; icell++) {
				switch (_orientation) {
					case HORIZONTAL:
						cells[ icell ] = new CellIndex( this.workbook, 0, felt + ielt, fcell + icell ).getCell();
						break;
					case VERTICAL:
						cells[ icell ] = new CellIndex( this.workbook, 0, fcell + icell, felt + ielt ).getCell();
						break;
				}
			}
			r[ ielt ] = cells;
		}

		return r;
	}

}
