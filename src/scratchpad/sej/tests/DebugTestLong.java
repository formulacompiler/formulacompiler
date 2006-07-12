package sej.tests;

import sej.EngineBuilder;
import sej.SEJ;
import sej.SaveableEngine;
import sej.internal.Debug;
import sej.internal.Settings;
import sej.runtime.ScaledLong;
import junit.framework.TestCase;


public class DebugTestLong extends TestCase
{

	static {
		Settings.setDebugLogEnabled( true );
	}


	public void testDebugCase() throws Exception
	{
		final EngineBuilder builder = SEJ.newEngineBuilder();
		builder.setNumericType( SEJ.LONG4 );
		builder.setFactoryClass( OutputFactory.class );
		builder.loadSpreadsheet( "src/scratchpad/data/DebugCase.xls" );
		builder.bindAllByName();
		final SaveableEngine engine = builder.compile();
		Debug.saveEngine( engine, "/temp/Debug.jar" );
		final OutputFactory factory = (OutputFactory) engine.getComputationFactory();

		final Input input = new Input();
		final Output output = factory.newOutput( input );
		final long result = output.getResult();
		
		assertEquals( 60000L, result );
	}


	@ScaledLong(4)
	public static final class Input
	{		
		public long getIA()
		{
			return 50000L;
		}

		public long getIB()
		{
			return 60000L;
		}

		public long getIC()
		{
			return 70000L;
		}

		public long getID()
		{
			return 20000L;
		}
	}

	@ScaledLong(4)
	public static interface Output
	{
		long getResult();
	}

	public static interface OutputFactory
	{
		Output newOutput( Input _input );
	}

}
