package temp;

import sej.Engine;
import sej.engine.RuntimeLong_v1;


public final class GeneratedScaledLongEngine implements Engine, ScaledLongOutputs
{
	private static final RuntimeLong_v1 runtime = new RuntimeLong_v1( 2 );
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
		return runtime.round( get0(), 1 );
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


	public static void main( String... _args )
	{
		final GeneratedScaledLongEngine engine = new GeneratedScaledLongEngine();
		final ScaledLongOutputs computation = (ScaledLongOutputs) engine.newComputation( new ScaledLongInputs() );
		System.out.print( "Result (should be 580) = " );
		System.out.print( computation.getResult() );
		System.out.println();
	}

}
