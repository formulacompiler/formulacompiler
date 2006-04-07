package sej.tests;

import java.io.IOException;

import sej.Engine;
import sej.EngineBuilder;
import sej.ModelError;
import sej.Settings;
import sej.engine.standard.compiler.StandardCompiler;
import sej.loader.excel.xls.ExcelXLSLoader;
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
		final EngineBuilder builder = new EngineBuilder( Inputs.class, Outputs.class );
		builder.loadSpreadsheet( "src/scratchpad/data/DebugCase.xls" );
		builder.bindCellsByName();
		final Engine engine = builder.buildEngine();

		final Inputs inputs = new Inputs();
		final Outputs outputs = (Outputs) engine.newComputation( inputs );

		assertEquals( 10.0, outputs.getResult(), 0.00001 );
	}


	public static final class Inputs
	{
		public boolean getIA()
		{
			return true;
		}

		public boolean getIB()
		{
			return false;
		}

		public boolean getIC()
		{
			return false;
		}

		public double getID()
		{
			return 20;
		}
	}


	public static interface Outputs
	{
		double getResult();
	}

}
