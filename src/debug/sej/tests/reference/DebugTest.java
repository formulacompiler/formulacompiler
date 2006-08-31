package sej.tests.reference;

import java.io.IOException;

import sej.SaveableEngine;
import sej.internal.Debug;
import sej.internal.Settings;


public final class DebugTest extends AbstractReferenceTest
{

	static {
		Settings.setDebugLogEnabled( true );
	}

	public DebugTest()
	{
		//super( "Strings" );
		super( "Strings", 14, NumType.DOUBLE, 0, false );
	}
	
	@Override
	protected void reportTestRun( String _testName )
	{
		System.out.println( _testName );
	}

	@Override
	protected void reportDefectiveEngine( SaveableEngine _engine, String _testName )
	{
		try {
			Debug.saveEngine( _engine, "d:/temp/debug.jar" );
			System.out.println( ".. dumped to d:/temp/debug.jar" );
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
