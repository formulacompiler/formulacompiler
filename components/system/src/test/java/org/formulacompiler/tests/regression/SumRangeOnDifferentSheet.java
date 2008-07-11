package org.formulacompiler.tests.regression;

import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.tests.MultiFormatTestFactory;

import junit.framework.Test;

public class SumRangeOnDifferentSheet extends MultiFormatTestFactory.SpreadsheetFormatTestCase
{
	private static final String DATA_FILE_PATH = "src/test/data/org/formulacompiler/tests/regression/SumRangeOnDifferentSheet";

	public void testSheet1() throws Exception
	{
		EngineBuilder b = SpreadsheetCompiler.newEngineBuilder();
		b.loadSpreadsheet( DATA_FILE_PATH + getSpreadsheetExtension() );
		b.setInputClass( Inputs.class );
		b.setOutputClass( Outputs.class );
		b.getRootBinder().defineOutputCell( b.getSpreadsheet().getCellA1( "A1" ),
				b.newCallFrame( Outputs.class.getMethod( "result" ) ) );
		SaveableEngine e = b.compile();
		ComputationFactory f = e.getComputationFactory();
		Outputs c = (Outputs) f.newComputation( new Inputs() );
		assertEquals( 6.0, c.result(), 0.0001 );
	}

	public void testSheet2() throws Exception
	{
		EngineBuilder b = SpreadsheetCompiler.newEngineBuilder();
		b.loadSpreadsheet( DATA_FILE_PATH + getSpreadsheetExtension() );
		b.setInputClass( Inputs.class );
		b.setOutputClass( Outputs.class );
		b.getRootBinder().defineOutputCell( b.getSpreadsheet().getCellA1( "Sheet2!A1" ),
				b.newCallFrame( Outputs.class.getMethod( "result" ) ) );
		SaveableEngine e = b.compile();
		ComputationFactory f = e.getComputationFactory();
		Outputs c = (Outputs) f.newComputation( new Inputs() );
		assertEquals( 15.0, c.result(), 0.0001 );
	}

	public static Test suite()
	{
		return MultiFormatTestFactory.testSuite( SumRangeOnDifferentSheet.class );
	}

	public static class Inputs
	{
		// Dummy
	}

	public static interface Outputs
	{
		double result();
	}

}
