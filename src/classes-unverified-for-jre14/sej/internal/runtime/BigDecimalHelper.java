package sej.internal.runtime;

import java.math.BigDecimal;

public class BigDecimalHelper
{

	public static int precision( final BigDecimal x )
	{
		return x.precision();
	}

	public static BigDecimal stripTrailingZeros( BigDecimal _value )
	{
		return _value.stripTrailingZeros();
	}

}
