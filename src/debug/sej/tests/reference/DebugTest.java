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
		super( "Strings", 203 );
		// super( "Strings", 7, NumType.DOUBLE, Integer.valueOf( "0", 2 ), false );
	}

	@Override
	protected void reportDefectiveEngine( SaveableEngine _engine, String _testName )
	{
		if (_engine != null) {
			try {
				Debug.saveEngine( _engine, "d:/temp/debug.jar" );
				System.out.println( ".. dumped to d:/temp/debug.jar" );
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
