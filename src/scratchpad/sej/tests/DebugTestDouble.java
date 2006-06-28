package sej.tests;

import sej.EngineBuilder;
import sej.SEJ;
import sej.internal.Settings;
import sej.runtime.Engine;
import junit.framework.TestCase;


public class DebugTestDouble extends TestCase
{

	static {
		Settings.setDebugLogEnabled( true );
	}


	public void testDebugCase() throws Exception
	{
		final EngineBuilder builder = SEJ.newEngineBuilder();
		builder.setFactoryClass( OutputFactory.class );
		builder.loadSpreadsheet( "src/scratchpad/data/DebugCase.xls" );
		builder.bindAllByName();
		final Engine engine = builder.compile();
		final OutputFactory factory = (OutputFactory) engine.getComputationFactory();

		final Input input = new Input();
		final Output output = factory.newOutput( input );
		final double result = output.getResult();
		
		assertEquals( 0.0, result, 0.0001 );
	}


	public static final class Input
	{
		public double getIA()
		{
			return 10.0;
		}

		public double getIB()
		{
			return 20.0;
		}

		public double getIC()
		{
			return 10.0;
		}

		public double getID()
		{
			return 0;
		}
	}


	public static interface Output
	{
		double getResult();
	}
	
	public static interface OutputFactory
	{
		Output newOutput( Input _input );
	}

}
