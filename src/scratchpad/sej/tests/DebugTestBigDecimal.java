package sej.tests;

import java.math.BigDecimal;

import sej.EngineBuilder;
import sej.SEJ;
import sej.SaveableEngine;
import sej.internal.Debug;
import sej.internal.Settings;
import junit.framework.TestCase;


public class DebugTestBigDecimal extends TestCase
{

	static {
		Settings.setDebugLogEnabled( true );
	}


	public void testDebugCase() throws Exception
	{
		final EngineBuilder builder = SEJ.newEngineBuilder();
		builder.setNumericType( SEJ.BIGDECIMAL8 );
		builder.setFactoryClass( OutputFactory.class );
		builder.loadSpreadsheet( "src/scratchpad/data/DebugCase.xls" );
		builder.bindAllByName();
		final SaveableEngine engine = builder.compile();
		Debug.saveEngine( engine, "/temp/Debug.jar" );
		final OutputFactory factory = (OutputFactory) engine.getComputationFactory();

		final Input input = new Input();
		final Output output = factory.newOutput( input );
		final BigDecimal result = output.getResult();
		
		assertEquals( "2.00000000", result.toPlainString() );
	}


	public static final class Input
	{
		public BigDecimal getIA()
		{
			return BigDecimal.valueOf( 10 );
		}

		public BigDecimal getIB()
		{
			return BigDecimal.valueOf( 10 );
		}

		public BigDecimal getIC()
		{
			return BigDecimal.valueOf( 10 );
		}

		public BigDecimal getID()
		{
			return BigDecimal.ZERO;
		}
	}


	public static interface Output
	{
		BigDecimal getResult();
	}

	public static interface OutputFactory
	{
		Output newOutput( Input _input );
	}

}
