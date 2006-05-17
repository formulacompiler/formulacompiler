package sej.tests;

import java.io.IOException;

import sej.Engine;
import sej.EngineBuilder;
import sej.ModelError;
import sej.NumericType;
import sej.Settings;
import sej.engine.standard.compiler.StandardCompiler;
import sej.loader.excel.xls.ExcelXLSLoader;
import sej.runtime.RuntimeDouble_v1;
import junit.framework.TestCase;


public class DebugTest extends TestCase
{

	static {
		ExcelXLSLoader.register();
		StandardCompiler.registerAsDefault();
		Settings.setDebugLogEnabled( true );
	}


	public void testDebugCase() throws IOException, ModelError
	{
		final EngineBuilder builder = new EngineBuilder( Inputs.class, Outputs.class, NumericType.LONG4 );
		builder.loadSpreadsheet( "src/scratchpad/data/DebugCase.xls" );
		builder.bindCellsByName();
		final Engine engine = builder.buildEngine();

		final Inputs inputs = new Inputs();
		final Outputs outputs = (Outputs) engine.newComputation( inputs );
		final long result = outputs.getResult();
		
		final long x = inputs.getIB() * 10000L;
		final long y = x / inputs.getIC();
		final long z = inputs.getIA() - y;
		
		final long exp = (long) (RuntimeDouble_v1.round( 9.666666667, 2 ) * 10000 );
		

		assertEquals( 96666L, result );
	}


	public static final class Inputs
	{
		public long getIA()
		{
			return 100000L;
		}

		public long getIB()
		{
			return 10000L;
		}

		public long getIC()
		{
			return 30000L;
		}

		public long getID()
		{
			return 0;
		}
	}


	public static interface Outputs
	{
		long getResult();
	}

}
