package temp;

public class Optimizations
{
	
	private static final String A = "a";
	private static final String B = "b";
	@SuppressWarnings("unused")
	private static final String C = A + B;


	public double a( double x )
	{
		return x - 1e10 + 1e-10;
	}

	public double b( double x )
	{
		return x - 1e2 + 1e-2;
	}

	public double c( double x )
	{
		return 1e10 + 1e-10 + x;
	}

	public int ai( int x )
	{
		return x - 99999 + 1;
	}

	public int bi( int x )
	{
		return x + 99999 + 1;
	}
	
	
	public String as( String x )
	{
		return "a" + "b" + x;
	}

}
