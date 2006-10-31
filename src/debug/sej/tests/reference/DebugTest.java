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
		super( "DateFunctions", 2, NumType.DOUBLE, Integer.valueOf( "0", 2 ), false );
	}

	@Override
	protected void reportDefectiveEngine( SaveableEngine _engine, String _testName )
	{
		if (_engine != null) {
			try {
				Debug.saveEngine( _engine, "/temp/debug.jar" );
				System.out.println( ".. dumped to /temp/debug.jar" );
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
