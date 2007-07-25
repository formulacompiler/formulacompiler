package org.formulacompiler.tests.reference;

import java.util.Locale;
import java.text.DecimalFormatSymbols;

import org.formulacompiler.runtime.Computation;

public class ConversionFunctions_en_US_CustomDecimalSymbolsTest extends AbstractReferenceTest
{
	public ConversionFunctions_en_US_CustomDecimalSymbolsTest()
	{
		final Locale locale = new Locale( "en", "US" );
		final DecimalFormatSymbols symbols = new DecimalFormatSymbols( locale );
		symbols.setDecimalSeparator( '\'' );
		symbols.setGroupingSeparator( ':' );
		final Computation.Config config = new Computation.Config( locale, symbols );
		config.decimalFormatSymbols = symbols;
		setConfig( config );
	}
}
