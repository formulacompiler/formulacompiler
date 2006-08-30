package sej.internal.runtime;

import java.math.BigDecimal;

import junit.framework.TestCase;

public class Runtime_v1Test extends TestCase
{


	public void testStringFromBigDecimal() throws Exception
	{
		assertEquals( "1.2", Runtime_v1.stringFromBigDecimal( BigDecimal.valueOf( 1.2 ) ) );
		assertEquals( "12", Runtime_v1.stringFromBigDecimal( BigDecimal.valueOf( 12 ) ) );
		assertEquals( "120", Runtime_v1.stringFromBigDecimal( BigDecimal.valueOf( 120 ) ) );
		assertEquals( "12000000000000000000", Runtime_v1.stringFromBigDecimal( BigDecimal.valueOf( 1.2e19 ) ) );
		assertEquals( "1.2E+20", Runtime_v1.stringFromBigDecimal( BigDecimal.valueOf( 1.2e20 ) ) );
		assertEquals( "12340000000000000000", Runtime_v1.stringFromBigDecimal( BigDecimal.valueOf( 12.34e18 ) ) );
		assertEquals( "1.234E+20", Runtime_v1.stringFromBigDecimal( BigDecimal.valueOf( 12.34e19 ) ) );
		assertEquals( "-12340000000000000000", Runtime_v1.stringFromBigDecimal( BigDecimal.valueOf( -12.34e18 ) ) );
		assertEquals( "-1.234E+20", Runtime_v1.stringFromBigDecimal( BigDecimal.valueOf( -12.34e19 ) ) );
	}


}
