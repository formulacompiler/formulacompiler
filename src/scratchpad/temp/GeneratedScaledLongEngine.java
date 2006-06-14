package temp;

import sej.Computation;


public final class GeneratedScaledLongEngine implements Computation, ScaledLongOutputs
{
	private final ScaledLongInputs inputs;

	private GeneratedScaledLongEngine(ScaledLongInputs _inputs)
	{
		super();
		this.inputs = _inputs;
	}

	public GeneratedScaledLongEngine()
	{
		super();
		this.inputs = null;
	}

	public final Object newComputation( Object _inputs )
	{
		if (null == _inputs) throw new IllegalArgumentException();
		return new GeneratedScaledLongEngine( (ScaledLongInputs) _inputs );
	}

	public long getResult()
	{
		return get3() + get3();
	}

	private long get0()
	{
		return get1() + get2();
	}

	private long get1()
	{
		return this.inputs.getA();
	}

	private long get2()
	{
		return this.inputs.getB();
	}
	
	private boolean have3;
	private long cache3;
	
	private long get3()
	{
		if (!this.have3) {
			this.cache3 = get0() + get0();
			this.have3 = true;
		}
		return this.cache3;
	}


	public static void main( String... _args )
	{
		final GeneratedScaledLongEngine engine = new GeneratedScaledLongEngine();
		final ScaledLongOutputs computation = (ScaledLongOutputs) engine.newComputation( new ScaledLongInputs() );
		System.out.print( "Result (should be 580) = " );
		System.out.print( computation.getResult() );
		System.out.println();
	}

}
