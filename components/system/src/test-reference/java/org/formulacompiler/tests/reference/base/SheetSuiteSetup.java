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
package org.formulacompiler.tests.reference.base;

import java.util.Locale;

import org.formulacompiler.runtime.Computation;
import org.formulacompiler.spreadsheet.Spreadsheet.Row;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Base class for all standard reference test definitions.
 *
 * @author peo
 */
public abstract class SheetSuiteSetup extends AbstractSuiteSetup
{
	public static boolean EMIT_DOCUMENTATION = isSysPropTrue( "emit_documentation" );
	public static boolean QUICK_RUN = isSysPropTrue( "quick_run" );
	public static boolean THREADED_RUN = isSysPropTrue( "threaded_run" );

	private static boolean isSysPropTrue( String _name )
	{
		return "true".equals( System.getProperty( "org.formulacompiler.tests.reference." + _name ) );
	}


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
		return sheetSuite( _fileName, _config, EMIT_DOCUMENTATION );
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
		Context loaderCx = newSheetContext( _fileName );
		if (null != _config) {
			loaderCx.setComputationConfig( _config );
		}
		TestSuite loader = newLoader( loaderCx );

		// This is inverse notation as each setup step wraps its inner setup:
		AbstractSetup setup = new SheetSetup();
		if (QUICK_RUN) {
			if (_documented) {
				loaderCx.setDocumenter( new HtmlDocumenter() );
				loader.setName( loader.getName() + " [documented]" );
			}
			else {
				// The documenter is not thread-safe, so only enable threading here.
				if (THREADED_RUN) {
					setup = new ThreadedSetup( setup );
				}
			}
		}
		else {
			setup = new AllCachingVariantsSetup( setup );
			if (_documented) {
				setup = new AllNumberTypesSetup( setup, BindingType.DOUBLE );
			}
			else {
				setup = new AllNumberTypesSetup( setup );
			}
			// The documenter is instantiated per sheet for the DOUBLE type, so we can thread the
			// types.
			if (THREADED_RUN) {
				setup = new ThreadedSetup( setup );
			}
		}
		setup.setup( loader, loaderCx );

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

		@Override
		protected void setup( TestSuite _parent, Context _parentCx ) throws Exception
		{
			addSheetRowSequenceTo( _parentCx, _parent );
		}

	}


	static void addSheetRowSequenceTo( Context _cx, TestSuite _suite ) throws Exception
	{
		final Row[] rows = _cx.getSheetRows();
		int iRow = _cx.getRowSetup().startingRow();
		while (iRow < rows.length) {
			final Context cx = _cx.newChild();
			cx.setRow( iRow );
			if (cx.getRowSetup().isTestRow()) {

				final SameNameRowSequenceTestSuite seqTest = new SameNameRowSequenceTestSuite( cx );
				_suite.addTest( seqTest.init() );

				iRow = seqTest.getNextRowIndex();
			}
			else {
				iRow++;
			}
		}
	}


	public static Test newSameEngineRowSequence( Context _cx, int... _nextRowIndex )
	{
		{ // Don't let the setup escape.
			final RowSetup rowSetup = _cx.getRowSetup();
			rowSetup.makeOutput();
			rowSetup.makeInput();
		}
		_cx.setInputIsBound( -1 );

		final int nInputs = _cx.getInputCells().length;
		final int nBoundVariations = QUICK_RUN? 1 : 1 << nInputs;
		final boolean hasExpr = (_cx.getOutputExpr() != null);

		final TestSuite suite;
		if (nBoundVariations == 1 && !hasExpr) {
			suite = null;
		}
		else {
			suite = new TestSuite( "Row " + (_cx.getRowIndex() + 1) );
			if (hasExpr) {
				suite.addTest( new ExpressionFormattingTestCase( _cx ) );
			}
			for (int iBoundVariation = 0; iBoundVariation < nBoundVariations - 1; iBoundVariation++) {
				final Context cx = _cx.newChild();
				cx.setInputIsBound( iBoundVariation );
				suite.addTest( new SameEngineRowSequenceTestSuite( cx, false ).init() );
			}
		}

		final SameEngineRowSequenceTestSuite refTest = new SameEngineRowSequenceTestSuite( _cx, true );
		refTest.init();
		if (_nextRowIndex.length > 0) {
			_nextRowIndex[ 0 ] = refTest.getNextRowIndex();
		}
		if (null == suite) return refTest;
		suite.addTest( refTest );
		return suite;
	}


}
