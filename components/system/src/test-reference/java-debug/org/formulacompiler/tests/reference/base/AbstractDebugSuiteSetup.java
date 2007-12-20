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

import java.io.File;
import java.util.Locale;

import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.compiler.internal.Debug;
import org.formulacompiler.compiler.internal.Settings;
import org.formulacompiler.runtime.Computation;
import org.formulacompiler.runtime.Computation.Config;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Base class for all debugging reference test definitions.
 * 
 * @author peo
 */
public abstract class AbstractDebugSuiteSetup extends SheetSuiteSetup
{
	private static String showFailedEnginesUsing = null;


	/**
	 * Enables tracing of the constant folder's reduction steps.
	 */
	public static void dbgConstantFolding()
	{
		Settings.LOG_CONSTEVAL.setEnabled( true );
	}

	/**
	 * Enables tracing of assignment and usage of letvars.
	 */
	public static void dbgLetVars()
	{
		Settings.LOG_LETVARS.setEnabled( true );
	}

	/**
	 * Enables display of failed engines.
	 * 
	 * @param _editor is the name of the editor executable to run with the failed engine's source as
	 *           its argument, for example "notepad" or "gedit".
	 */
	public static void dbgShowDecompiledEnginesUsing( String _editor )
	{
		showFailedEnginesUsing = _editor;
	}


	// ---- examples
	/**
	 * Returns a suite that runs a single sheet for the number type double and with no caching.
	 * <p>
	 * See here how to build such a test runs: {@.jcite -- sheetImpl}.
	 * 
	 * @param _fileName is the base name of the file without path or extension.
	 */
	public static Test dbgSheetSuite( String _fileName ) throws Exception
	{
		// DO NOT REFORMAT BELOW THIS LINE
		// -- sheetImpl
		return dbgSuiteBuilder( _fileName )
				// ... You could configure aspects here.
				.suite();
		// -- sheetImpl
		// DO NOT REFORMAT ABOVE THIS LINE
	}

	/**
	 * Returns a suite that runs a single row's engine for the given number type and no caching.
	 * <p>
	 * See here how to build such a test runs: {@.jcite -- rowImpl}.
	 * 
	 * @param _fileName is the base name of the file without path or extension.
	 * @param _rowNumber is the 1-based row number for which to compile and run an engine.
	 * @param _numberType is the numeric type to use.
	 */
	public static Test dbgRowSuite( String _fileName, int _rowNumber, BindingType _numberType ) throws Exception
	{
		// DO NOT REFORMAT BELOW THIS LINE
		// -- rowImpl
		return dbgSuiteBuilder( _fileName )
				.row( _rowNumber )
				.numberType( _numberType )
				// ... You could go on configuring more aspects here.
				.suite();
		// -- rowImpl
		// DO NOT REFORMAT ABOVE THIS LINE
	}

	/**
	 * Returns a suite that runs a single row's engine for all number types and caching variants.
	 * <p>
	 * See here how to customize such complex test runs: {@.jcite -- fullRowImpl}.
	 * 
	 * @param _fileName is the base name of the file without path or extension.
	 * @param _rowNumber is the 1-based row number for which to compile and run an engine.
	 * 
	 * @see #dbgSuite(AbstractSetup)
	 * @see BuilderSetup
	 */
	public static Test dbgFullRowSuite( String _fileName, final int _rowNumber ) throws Exception
	{
		// -- fullRowImpl
		return dbgSuite( new AllNumberTypesSetup( new AllCachingVariantsSetup( new BuilderSetup( _fileName )
		{
			@Override
			protected void configure( SheetSuiteBuilder _builder )
			{
				_builder.row( _rowNumber );
				// ... You could go on configuring more aspects here.
			}
		} ) ) );
		// -- fullRowImpl
	}
	// ---- examples


	/**
	 * Returns a suite builder suitable for setting up debugging configurations. If not further
	 * configured, it's {@code suite()} runs the entire sheet with the default settings (no variation
	 * in number type, caching, etc.).
	 * 
	 * @param _fileName is the base name of the file without path or extension. If it contains
	 *           "Database", the tests use database aggregation style test rows, otherwise default
	 *           rows.
	 * @return a new builder; use its {@link SheetSuiteBuilder#suite() suite()} method to get the
	 *         final test suite.
	 * 
	 * @see SheetSuiteBuilder
	 */
	public static SheetSuiteBuilder dbgSuiteBuilder( String _fileName )
	{
		return new SheetSuiteBuilder( newSheetContext( _fileName ) );
	}


	/**
	 * Reference test suite builder that allows convenient configuration of a debugging test run. Use
	 * the {@link #suite()} method to get the final test suite at the end.
	 * 
	 * @author peo
	 */
	public static final class SheetSuiteBuilder
	{
		private final Context sheetCx;
		private final TestSuite loader;
		private final Context rowCx;

		private SheetSuiteBuilder( Context _parentCx, TestSuite _loader )
		{
			super();
			this.sheetCx = _parentCx;
			this.loader = _loader;
			this.rowCx = new Context( this.sheetCx );
			this.rowCx.setFailedEngineReporter( new Context.FailedEngineReporter()
			{
				private int failureNo = 1;

				public void reportFailedEngine( Test _test, SaveableEngine _engine, Throwable _failure ) throws Throwable
				{
					System.err.println( "ENGINE FAILED: " + _test );
					File path = new File( "temp/debug/failure-" + (this.failureNo++) );
					path.mkdirs();

					String enginePath = new File( path, "engine.jar" ).getPath();
					Debug.saveEngine( _engine, enginePath );
					System.err.println( ".. dumped to " + enginePath );

					String decompPath = path.getPath();
					Debug.decompileEngine( _engine, decompPath );
					System.err.println( ".. decompiled to " + decompPath );

					if (null != showFailedEnginesUsing) {
						Runtime.getRuntime().exec(
								showFailedEnginesUsing + " " + path.getAbsolutePath() + "/org/formulacompiler/gen/$Root.java" );
					}
				}

			} );
		}

		public SheetSuiteBuilder( Context _sheetCx )
		{
			this( _sheetCx, newLoader( _sheetCx ) );
		}

		/**
		 * When set, restricts the tests run to just the given row (and its successor rows with "..."
		 * in their names). Runs all input binding variants.
		 * 
		 * @param _rowNumber the 1-based spreadsheet row number.
		 * @return this (fluent API).
		 */
		public SheetSuiteBuilder row( int _rowNumber )
		{
			this.rowCx.setRow( _rowNumber - 1 );
			this.rowCx.getRowSetup().makeInput().makeOutput();
			return this;
		}

		/**
		 * When set, configures exactly which input binding to use. Inputs where the flag is true are
		 * bound, others aren't. Restricts the test run to just the specified row without "..."-style
		 * successors.
		 * 
		 * @param _flags tells which input columns to bind.
		 * @return this (fluent API).
		 */
		public SheetSuiteBuilder bind( boolean... _flags )
		{
			int bits = 0;
			for (int i = 0; i < _flags.length; i++) {
				bits = bits << 1;
				if (_flags[ i ]) bits |= 1;
			}
			this.rowCx.setInputBindingBits( bits );
			return this;
		}

		/**
		 * Like {@link #bind(boolean...)}, but accepts the flags as a binary bit string. This is what
		 * the test runs report, too.
		 * 
		 * @param _bitstring binary bit string, e.g. "10011".
		 * @return this (fluent API).
		 */
		public SheetSuiteBuilder bind( String _bitstring )
		{
			this.rowCx.setInputBindingBits( _bitstring );
			return this;
		}

		/**
		 * Like {@link #bind(boolean...)}, but accepts the flags as a integer bitset. Mostly used to
		 * enable full binding by passing {@code -1} here.
		 * 
		 * @param _bitset integer bit set, typically {@code -1} or {@code 0}.
		 * @return this (fluent API).
		 */
		public SheetSuiteBuilder bind( int _bitset )
		{
			this.rowCx.setInputBindingBits( _bitset );
			return this;
		}

		/**
		 * Configures the caching variant to run. Default is no caching.
		 * 
		 * @param _caching if set, then compiles a caching engine.
		 * @return this (fluent API).
		 */
		public SheetSuiteBuilder cache( boolean _caching )
		{
			this.sheetCx.setExplicitCaching( _caching );
			return this;
		}

		/**
		 * Configures the numeric type to use. Default is double.
		 * 
		 * @param _type must be one of {@code DOUBLE, BIGDEC_PREC, BIGDEC_SCALE, LONG}.
		 * @return this (fluent API).
		 */
		public SheetSuiteBuilder numberType( BindingType _type )
		{
			this.sheetCx.setNumberBindingType( _type );
			return this;
		}

		/**
		 * Ensures the a computation config is in effect and returns it. You can customize the
		 * returned config.
		 * 
		 * @return the config in effect.
		 * 
		 * @see #locale(Locale)
		 */
		public Computation.Config config()
		{
			if (null == this.config) {
				this.config = new Computation.Config();
				this.sheetCx.setComputationConfig( this.config );
			}
			return this.config;
		}

		private Config config = null;

		/**
		 * Applies a computation config.
		 * 
		 * @param _config the config to use.
		 * @return this (fluent API).
		 */
		public SheetSuiteBuilder config( Computation.Config _config )
		{
			this.config = _config;
			this.sheetCx.setComputationConfig( this.config );
			return this;
		}

		/**
		 * Configures the locale to use.
		 * 
		 * @param _locale is passed to computations.
		 * @return this (fluent API).
		 */
		public SheetSuiteBuilder locale( Locale _locale )
		{
			config().locale = _locale;
			return this;
		}

		/**
		 * Configures the locale to use using {@link Locale#Locale(String, String)}.
		 * 
		 * @return this (fluent API).
		 */
		public SheetSuiteBuilder locale( String _language, String _country )
		{
			return locale( new Locale( _language, _country ) );
		}

		public SheetSuiteBuilder document()
		{
			this.sheetCx.setDocumenter( new HtmlDocumenter() );
			return this;
		}


		/**
		 * Builds and returns the desired test suite.
		 */
		public Test suite() throws Exception
		{
			setupInto( this.loader );
			return this.loader;
		}

		void setupInto( TestSuite _parent ) throws Exception
		{
			if (null == this.rowCx.getRow()) {
				addSheetRowSequenceTo( this.sheetCx, _parent );
			}
			else if (null != this.rowCx.getInputBindingBits()) {
				_parent.addTest( new SameExprRowSequenceTestSuite( this.rowCx )
				{
					@Override
					protected void addTests() throws Exception
					{
						addTest( new SameEngineRowSequenceTestSuite( cx(), false ).init() );
					}
				}.init() );
			}
			else {
				_parent.addTest( newSameEngineRowSequence( this.rowCx ) );
			}
		}

	}


	/**
	 * Builds a final test suite from the suite setup chain passed in. The innermost setup must be a
	 * {@link BuilderSetup}.
	 * 
	 * @param _setup is the setup chain.
	 * @return the final test suite.
	 * 
	 * @see #dbgFullRowSuite
	 */
	public static Test dbgSuite( AbstractSetup _setup ) throws Exception
	{
		Context loaderCx = newSheetContext( builderSetupFileName );
		TestSuite loader = newLoader( loaderCx );
		_setup.setup( loader, loaderCx );
		return loader;
	}

	/**
	 * Allows the use of a {@link SheetSuiteBuilder} within a suite setup chain. Use with
	 * {@link AbstractDebugSuiteSetup#dbgSuite(AbstractSuiteSetup.AbstractSetup)}.
	 * 
	 * @see #configure(AbstractDebugSuiteSetup.SheetSuiteBuilder)
	 * 
	 * @author peo
	 */
	public static class BuilderSetup extends AbstractSetup
	{

		/**
		 * Must be the innermost constructor in a setup chain; to be used with
		 * {@link AbstractDebugSuiteSetup#dbgSuite(AbstractSetup)}.
		 * 
		 * @param _fileName is the base name of the file without path or extension.
		 */
		public BuilderSetup( String _fileName )
		{
			AbstractDebugSuiteSetup.builderSetupFileName = _fileName;
		}

		/**
		 * Override this method to configure the inner suite builder.
		 * 
		 * @param _builder can be configured here.
		 */
		protected void configure( SheetSuiteBuilder _builder )
		{
			// To be overridden.
		}

		@Override
		protected void setup( TestSuite _parent, Context _parentCx ) throws Exception
		{
			final SheetSuiteBuilder builder = new SheetSuiteBuilder( _parentCx, null );
			configure( builder );
			builder.setupInto( _parent );
		}

	}

	// Ugly hack!
	private static String builderSetupFileName;
}
