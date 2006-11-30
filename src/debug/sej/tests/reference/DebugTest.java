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

import java.io.IOException;

import sej.SaveableEngine;
import sej.internal.Debug;
import sej.internal.Settings;


// ---- DebugTest
/**
 * Special debugging testcase for automated reference tests. See constructor for how to configure
 * it.
 */
public final class DebugTest extends AbstractReferenceTest
{

	/**
	 * Options you might wish to enable.
	 */
	static {
		Settings.LOG_CONSTEVAL.setEnabled( true );
		Settings.LOG_LETVARS.setEnabled( false );
	}


	/**
	 * Configure which test to run here. At first, just run the suite you are interested in, or, if
	 * you know it, the starting row to use. Then, when you know which test fails (from the console
	 * output), configure it precisely. Note how <code>Integer.valueOf( "x", 2 )</code> is used to
	 * encode the bound/unbound input variant to use.
	 */
	public DebugTest()
	{
		// super( "StringComparisons" );
		// super( "StringComparisons", 74 );
		// super( "StringComparisons", 9, NumType.DOUBLE, Integer.valueOf( "1", 2 ), false );

		super( "FinancialFunctions" );
		// super( "FinancialFunctions", 2, NumType.DOUBLE, Integer.valueOf( "1", 2 ), false );
	}


	/**
	 * Logs the generated engine .jar to "/temp/debug.jar" in case of a test failure. You can use
	 * 
	 * <pre>
	 * javap -c -private -classpath ./debug.jar sej/gen/$Root
	 * </pre>
	 * 
	 * to disassemble its VM byte-code instructions.
	 */
	@Override
	protected void reportDefectiveEngine( SaveableEngine _engine, String _testName )
	{
		if (_engine != null) {
			try {
				Debug.saveEngine( _engine, "/temp/debug.jar" );
				System.out.println( ".. dumped to /temp/debug.jar" );
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
// ---- DebugTest
