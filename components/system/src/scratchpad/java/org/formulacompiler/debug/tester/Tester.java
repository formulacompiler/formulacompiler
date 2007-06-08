package org.formulacompiler.debug.tester;

import org.formulacompiler.spreadsheet.SpreadsheetCompiler;

public final class Tester extends AbstractTester
{

	public static void main( String[] args ) throws Exception
	{
		new Tester().run( args );
	}


	@Override
	protected String sourceFileName()
	{
		return "/downloads/min_enterprise_fv.xls";
	}


	@Override
	protected void define() throws Exception
	{
		setNumericType( SpreadsheetCompiler.BIGDECIMAL8 );

		defineSection( "POSITIONS" );
		defineInputsFromNames( "IN_.*" );
		defineOutputsFromNames( "OUT_.*" );
	}

	
}
