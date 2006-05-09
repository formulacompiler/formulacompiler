package sej.tests;

import java.io.IOException;
import java.math.BigDecimal;

import sej.Engine;
import sej.EngineBuilder;
import sej.ModelError;
import sej.NumericType;
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
		final EngineBuilder builder = new EngineBuilder( Inputs.class, Outputs.class, NumericType.BIGDECIMAL8 );
		builder.loadSpreadsheet( "src/scratchpad/data/DebugCase.xls" );
		builder.bindCellsByName();
		final Engine engine = builder.buildEngine();

		final Inputs inputs = new Inputs();
		final Outputs outputs = (Outputs) engine.newComputation( inputs );
		final BigDecimal result = outputs.getResult();

		assertEquals( "10.5", result.toPlainString() );
	}


	public static final class Inputs
	{
		public BigDecimal getIA()
		{
			return new BigDecimal( "1" );
		}

		public BigDecimal getIB()
		{
			return new BigDecimal( "20" );
		}

		public BigDecimal getIC()
		{
			return new BigDecimal( "3" );
		}

		public BigDecimal getID()
		{
			return new BigDecimal( "4" );
		}
	}


	public static interface Outputs
	{
		BigDecimal getResult();
	}

}
