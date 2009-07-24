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
