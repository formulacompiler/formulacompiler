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


import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.formulacompiler.runtime.Computation;
import org.formulacompiler.tests.reference.base.SheetSuiteSetup;

import junit.framework.Test;
import junit.framework.TestSuite;

public class Conversions extends SheetSuiteSetup
{

	public static Test suite() throws Exception
	{
		final TestSuite sheets = new TestSuite( "Files" );

		sheets.addTest( sheetSuite( "ConversionFunctions" ) );

		sheets.addTest( sheetSuiteWithLocale( "ConversionFunctions", "en", "US" ) );
		sheets.addTest( sheetSuiteWithLocale( "ConversionFunctions", "de", "CH", false ) );

		{
			final Locale locale = new Locale( "en", "US" );
			final DecimalFormatSymbols symbols = new DecimalFormatSymbols( locale );
			symbols.setDecimalSeparator( '\'' );
			symbols.setGroupingSeparator( ':' );
			final Computation.Config config = new Computation.Config( locale, symbols );
			config.decimalFormatSymbols = symbols;
			sheets.addTest( sheetSuite( "ConversionFunctions_en_US_CustomDecimalSymbols", config, false ) );
		}

		sheets.addTest( sheetSuiteWithLocale( "ConversionFunctions", "ru", "RU", false ) );

		return sheets;
	}

}
