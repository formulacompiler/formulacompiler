package sej.tests;

import java.io.IOException;

import sej.EngineBuilder;
import sej.ModelError;
import sej.NumericType;
import sej.Settings;
import sej.engine.standard.compiler.StandardCompiler;
import sej.loader.excel.xls.ExcelXLSLoader;
import sej.runtime.Engine;
import junit.framework.TestCase;


public class DebugTestLong extends TestCase
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
		
		assertEquals( 620000L, result );
	}


	public static final class Inputs
	{
		public long getIA()
		{
			return 100000L;
		}

		public long getIB()
		{
			return 100000L;
		}

		public long getIC()
		{
			return 100000L;
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
