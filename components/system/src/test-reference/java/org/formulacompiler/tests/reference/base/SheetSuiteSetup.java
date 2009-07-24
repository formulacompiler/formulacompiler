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

package org.formulacompiler.tests.reference.base;

import java.util.List;
import java.util.Locale;

import org.formulacompiler.runtime.Computation;
import org.formulacompiler.spreadsheet.internal.RowImpl;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Base class for all standard reference test definitions.
 *
 * @author peo
 */
public abstract class SheetSuiteSetup extends AbstractSuiteSetup
{

	/**
	 * Returns a suite providing full test sheet coverage for a single spreadsheet file.
	 *
	 * @param _fileName is the base name of the file without path or extension.
	 */
	public static Test sheetSuite( String _fileName ) throws Exception
	{
		return sheetSuite( _fileName, null );
	}

	/**
	 * Returns a suite providing full test sheet coverage for a single spreadsheet file with a given
	 * computation configuration. Documentation output is controlled by the system property
	 * {@code org.formulacompiler.tests.reference.emit_documentation}.
	 *
	 * @param _fileName is the base name of the file without path or extension.
	 * @param _config is how to configure the computation factory.
	 */
	public static Test sheetSuite( String _fileName, Computation.Config _config ) throws Exception
	{
		return sheetSuite( _fileName, _config, Settings.EMIT_DOCUMENTATION );
	}

	/**
	 * Returns a suite providing full test sheet coverage for a single spreadsheet file with a given
	 * computation configuration.
	 *
	 * @param _fileName is the base name of the file without path or extension.
	 * @param _config is how to configure the computation factory.
	 */
	public static Test sheetSuite( String _fileName, Computation.Config _config, boolean _documented ) throws Exception
	{
		final TestSuite testSuite = new TestSuite( _fileName );
		final Context xlsLoaderCx = newXlsSheetContext( _fileName );
		testSuite.addTest( setupContext( xlsLoaderCx, _config, _documented, new XlsRowVerificationsTestSetup() ) );
		if (odsSpreadsheetExists( _fileName )) {
			final Context odsLoaderCx = newOdsSheetContext( _fileName );
			testSuite.addTest( setupContext( odsLoaderCx, _config, _documented, new OdsRowVerificationsTestSetup() ) );
		}
		{
			final Context xlsxLoaderCx = newXlsxSheetContext( _fileName );
			testSuite.addTest( setupContext( xlsxLoaderCx, _config, _documented, new XlsxRowVerificationsTestSetup() ) );
		}
		return testSuite;
	}

	private static TestSuite setupContext( Context _loaderCx, Computation.Config _config, boolean _documented,
			RowTestSetup _setup ) throws Exception
	{
		if (null != _config) {
			_loaderCx.setComputationConfig( _config );
		}
		TestSuite loader = newLoader( _loaderCx );

		// This is inverse notation as each setup step wraps its inner setup:
		AbstractSetup setup = new SheetSetup( _setup );
		if (Settings.QUICK_RUN) {
			if (_documented) {
				_loaderCx.setDocumenter( new HtmlDocumenter() );
			}
			else {
				// The documenter is not thread-safe, so only enable threading here.
				if (Settings.THREADED_RUN) {
					setup = new ThreadedSetup( setup );
				}
			}
		}
		else {
			if (_documented) {
				setup = new AllNumberTypesSetup( setup, BindingType.DOUBLE );
			}
			else {
				setup = new AllNumberTypesSetup( setup );
			}
			// The documenter is instantiated per sheet for the DOUBLE type, so we can thread the
			// types.
			if (Settings.THREADED_RUN) {
				setup = new ThreadedSetup( setup );
			}
			setup = new AllCachingVariantsSetup( setup );
		}
		setup.setup( loader, _loaderCx );
		return loader;
	}

	/**
	 * Returns a suite providing full test sheet coverage for multiple spreadsheet files.
	 *
	 * @param _fileNames are the base name of the files without path or extension.
	 */
	public static Test sheetSuite( String... _fileNames ) throws Exception
	{
		if (_fileNames.length == 1) return sheetSuite( _fileNames[ 0 ] );
		final TestSuite sheets = new TestSuite( "Files" );
		for (String fileName : _fileNames)
			sheets.addTest( sheetSuite( fileName ) );
		return sheets;
	}

	public static Test sheetSuiteWithLocale( String _fileName, String _lang, String _region ) throws Exception
	{
		return sheetSuite( _fileName + "_" + _lang + "_" + _region, new Computation.Config( new Locale( _lang, _region ) ) );
	}

	public static Test sheetSuiteWithLocale( String _fileName, String _lang, String _region, boolean _documented )
			throws Exception
	{
		return sheetSuite( _fileName + "_" + _lang + "_" + _region,
				new Computation.Config( new Locale( _lang, _region ) ), _documented );
	}


	protected static final class SheetSetup extends AbstractSetup
	{
		private final RowTestSetup rowTestSetup;

		public SheetSetup( final RowTestSetup _setup )
		{
			this.rowTestSetup = _setup;
		}

		@Override
		protected void setup( TestSuite _parent, Context _parentCx ) throws Exception
		{
			addSheetRowSequenceTo( _parentCx, _parent, this.rowTestSetup );
		}

	}


	static void addSheetRowSequenceTo( Context _cx, TestSuite _suite, final RowTestSetup _setup ) throws Exception
	{
		final List<RowImpl> rows = _cx.getSheetRows();
		int iRow = _cx.getRowSetup().startingRow();
		while (iRow < rows.size()) {
			final Context cx = new Context( _cx );
			cx.setRow( iRow );
			if (cx.getRowSetup().isTestRow()) {

				final SameNameRowSequenceTestSuite seqTest = new SameNameRowSequenceTestSuite( cx, _setup );
				_suite.addTest( seqTest.init() );

				iRow = seqTest.getNextRowIndex();
			}
			else {
				iRow++;
			}
		}
	}


	public static Test newSameEngineRowSequence( final Context _cx, final RowTestSetup _rowTestSetup, final int... _nextRowIndex ) throws Exception
	{
		{ // Don't let the setup escape.
			final RowSetup rowSetup = _cx.getRowSetup();
			rowSetup.makeOutput();
			rowSetup.makeInput();
		}
		_cx.setInputBindingBits( -1 );

		final int nInputs = _cx.getInputCellCount();
		final int nBoundVariations = 1 << nInputs;
		final boolean hasExpr = (_cx.getOutputExpr() != null);

		final TestSuite testSuite = new SameExprRowSequenceTestSuite( _cx )
		{

			@Override
			protected void addTests() throws Exception
			{
				// Only verify this once, not again for every type.
				// LATER Might have to change when loaders use numeric type.
				if (_cx.getNumberBindingType() == BindingType.DOUBLE && !_cx.getExplicitCaching()) {
					final int checkingCol = _cx.getRowSetup().checkingCol();
					if (checkingCol >= 0) {
						if (_rowTestSetup != null) {
							_rowTestSetup.setup( this, cx() );
						}
					}
					for (Context variant : _cx.variants()) {
						addTest( variant.getRowVerificationTestCaseFactory().newInstance( _cx, variant ) );
					}
				}
				if (hasExpr) {
					addTest( new ExpressionFormattingTestCase( new Context( _cx ) ) );
				}

				final SameEngineRowSequenceTestSuite refTest = new SameEngineRowSequenceTestSuite( _cx, true );
				refTest.init();
				if (_nextRowIndex.length > 0) {
					_nextRowIndex[ 0 ] = refTest.getNextRowIndex();
				}
				addTest( refTest );

				if (Settings.QUICK_RUN && nBoundVariations > 1) {
					addBoundVariationTest( 0 );
				}
				else {
					for (int iBoundVariation = 0; iBoundVariation < nBoundVariations - 1; iBoundVariation++) {
						addBoundVariationTest( iBoundVariation );
					}
				}
			}

			private void addBoundVariationTest( final int _iBoundVariation )
			{
				final Context cx = new Context( _cx );
				cx.setInputBindingBits( _iBoundVariation );
				addTest( new SameEngineRowSequenceTestSuite( cx, false ).init() );
			}

		}.init();
		return testSuite;
	}


	private static class XlsRowVerificationsTestSetup implements RowTestSetup
	{
		public void setup( final TestSuite _suite, final Context _cx ) throws Exception
		{
			final int checkingCol = _cx.getRowSetup().checkingCol();
			if (checkingCol >= 0) {
				_suite.addTest( new ExpressionVerificationTestCase( _cx, checkingCol,
						"OR( ISBLANK( Bn ), IF( ISERROR( Bn ), (ERRORTYPE( Bn ) = IF( ISBLANK( Mn ), ERRORTYPE( An ), ERRORTYPE( Mn ) )), IF( ISBLANK( Mn ), AND( NOT( ISBLANK( An ) ), (An = Bn) ), (Bn = Mn) ) ) )" ) );
				_suite.addTest( new ExpressionVerificationTestCase( _cx, checkingCol + 1,
						"IF( ISBLANK( On ), IF( ISERROR( Pn ), false, Pn ), On )" ) );
				if (!_cx.getSpreadsheetFileBaseName().startsWith( "Bad" )) {
					_suite.addTest( new ValueVerificationTestCase( _cx, checkingCol + 1, Boolean.TRUE ) );
				}
			}
		}
	}


	private static class OdsRowVerificationsTestSetup implements RowTestSetup
	{
		public void setup( final TestSuite _suite, final Context _cx ) throws Exception
		{
			final int checkingCol = _cx.getRowSetup().checkingCol();
			if (checkingCol >= 0) {
				_suite.addTest( new ExpressionVerificationTestCase( _cx, checkingCol,
						"IF( ISBLANK( Mn ), An, Mn )" ) );
				_suite.addTest( new ExpressionVerificationTestCase( _cx, checkingCol + 1,
						"IF( ISBLANK( On ), IF( ISERROR( Bn ), ((\"Err:\" & ERRORTYPE( Bn )) = Mn), OR( (Pn = Bn), AND( ISNUMBER( Pn ), ISNUMBER( Bn ), OR( AND( (Pn = 0.0), (ABS( Bn ) < 1.0E-307) ), ((\"\" & Bn) = (\"\" & Pn)) ) ) ) ), On )" ) );
				if (!_cx.getSpreadsheetFileBaseName().startsWith( "Bad" )) {
					_suite.addTest( new ValueVerificationTestCase( _cx, checkingCol + 1, Boolean.TRUE ) );
				}
			}
		}
	}


	private static class XlsxRowVerificationsTestSetup implements RowTestSetup
	{
		public void setup( final TestSuite _suite, final Context _cx ) throws Exception
		{
			final int checkingCol = _cx.getRowSetup().checkingCol();
			if (checkingCol >= 0) {
				_suite.addTest( new ExpressionVerificationTestCase( _cx, checkingCol,
						"OR( ISBLANK( Bn ), IF( ISERROR( Bn ), (ERRORTYPE( Bn ) = IF( ISBLANK( Mn ), ERRORTYPE( An ), ERRORTYPE( Mn ) )), IF( ISBLANK( Mn ), AND( NOT( ISBLANK( An ) ), (An = Bn) ), (Bn = Mn) ) ) )" ) );
				_suite.addTest( new ExpressionVerificationTestCase( _cx, checkingCol + 1,
						"IF( ISBLANK( On ), IF( ISERROR( Pn ), false, Pn ), On )" ) );
				if (!_cx.getSpreadsheetFileBaseName().startsWith( "Bad" )) {
					_suite.addTest( new ValueVerificationTestCase( _cx, checkingCol + 1, Boolean.TRUE ) );
				}
			}
		}
	}


}
