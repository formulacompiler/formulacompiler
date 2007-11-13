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
package org.formulacompiler.tests.reference;

import java.io.File;
import java.io.IOException;

import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.compiler.internal.Debug;
import org.formulacompiler.compiler.internal.Settings;


// ---- DebugTest
/**
 * Special debugging testcase for automated reference tests.
 * 
 * <p>
 * You configure it using one of the following constructor variants. At first, just run the suite
 * you are interested in, or, if you know it, the starting row to use. Then, when you know which
 * test fails (from the console output), configure it precisely. Note how
 * <code>Integer.valueOf( "x", 2 )</code> is used to encode the bound/unbound input variant to
 * use.
 * 
 * <pre><code> super( "StringComparisons" );
 * super( "StringComparisons", 74 );
 * super( "StringComparisons", 9, NumType.DOUBLE, 
 *     Integer.valueOf( "1", 2 ), false );
 * </code></pre>
 */
public abstract class AbstractDebugTest extends AbstractReferenceTest
{

	/**
	 * Options you might wish to enable.
	 */
	static {
		Settings.LOG_CONSTEVAL.setEnabled( true );
		Settings.LOG_LETVARS.setEnabled( false );
	}

	/**
	 * Runs only a given row in a given configuration.
	 * 
	 * @param _baseName is the base name of the spreadsheet file (no extension).
	 * @param _onlyRowNumbered is the row number of the test.
	 * @param _onlyType is the numeric type.
	 * @param _onlyInputVariant controls which cells to bind to input values; use a bit vector as in
	 *           <code>Integer.valueOf( "x", 2 )</code>.
	 * @param _caching controls whether to enable caching or not.
	 */
	protected AbstractDebugTest( String _baseName, int _onlyRowNumbered, NumType _onlyType, int _onlyInputVariant,
			boolean _caching )
	{
		super( _baseName, _onlyRowNumbered, _onlyType, _onlyInputVariant, _caching );
	}

	/**
	 * Runs the tests starting with the given row.
	 * 
	 * @param _baseName is the base name of the spreadsheet file (no extension).
	 * @param _startingRowNumber is the row number fo the first test to run.
	 */
	protected AbstractDebugTest( String _baseName, int _startingRowNumber )
	{
		super( _baseName, _startingRowNumber );
	}

	/**
	 * Runs all the tests in the sheet.
	 * 
	 * @param _baseName is the base name of the spreadsheet file (no extension).
	 */
	protected AbstractDebugTest( String _baseName )
	{
		super( _baseName );
	}

	/**
	 * Logs the generated engine .jar to "temp/debug/engine.jar" and decompiles it to
	 * "temp/debug/decompiled" in case of a test failure. You can use
	 * 
	 * <pre>
	 * javap -c -private -classpath ./engine.jar org/formulacompiler/gen/$Root
	 * </pre>
	 * 
	 * to disassemble its VM byte-code instructions.
	 */
	@Override
	protected void reportDefectiveEngine( SaveableEngine _engine, String _testName )
	{
		if (_engine != null) {
			try {
				new File( "temp/debug/decompiled" ).mkdirs();
				Debug.saveEngine( _engine, "temp/debug/engine.jar" );
				System.out.println( ".. dumped to temp/debug/engine.jar" );
				Debug.decompileEngine( _engine, "temp/debug/decompiled" );
				System.out.println( ".. decompiled to temp/debug/decompiled" );
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
// ---- DebugTest
