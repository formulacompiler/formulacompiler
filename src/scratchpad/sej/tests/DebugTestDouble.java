package sej.tests;

import sej.EngineBuilder;
import sej.SEJ;
import sej.SaveableEngine;
import sej.internal.Debug;
import sej.internal.Settings;
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
		final SaveableEngine engine = builder.compile();
		Debug.saveEngine( engine, "/temp/Debug.jar" );
		final OutputFactory factory = (OutputFactory) engine.getComputationFactory();

		final Input input = new Input();
		final Output output = factory.newOutput( input );
		final double result = output.getResult();
		
		assertEquals( 6.0, result, 0.0001 );
	}


	public static final class Input
	{
		public double getIA()
		{
			return 5.0;
		}

		public double getIB()
		{
			return 6.0;
		}

		public double getIC()
		{
			return 7.0;
		}

		public double getID()
		{
			return 2.0;
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
