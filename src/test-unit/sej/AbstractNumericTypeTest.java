package sej;

import junit.framework.TestCase;

public abstract class AbstractNumericTypeTest extends TestCase
{

	protected abstract NumericType getType();


	public void testConstantsToString() throws Exception
	{
		assertEquals( "", getType().valueToString( null ) );
		assertEquals( "", getType().valueToConciseString( null ) );
		assertEquals( "0", getType().valueToConciseString( getType().getZero() ) );
		assertEquals( "1", getType().valueToConciseString( getType().getOne() ) );
	}

	public void testConstantsFromString() throws Exception
	{
		assertEquals( getType().getZero(), getType().valueOf( null ) );
		assertEquals( getType().getZero(), getType().valueOf( "" ) );
		assertEquals( getType().getZero(), getType().valueOf( "0" ) );
		assertEquals( getType().getOne(), getType().valueOf( "1" ) );
	}


	@SuppressWarnings("unchecked")
	protected static void assertEquals( Number _a, Number _b )
	{
		Comparable a = (Comparable) _a;
		Comparable b = (Comparable) _b;
		assertEquals( 0, a.compareTo( b ) );
	}

}
