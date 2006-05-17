/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are prohibited, unless you have been explicitly granted 
 * more rights by Abacus Research AG.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS 
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND 
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR 
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package sej.tests.serialization;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import sej.Engine;
import sej.EngineFactory;
import junit.framework.TestCase;

public abstract class AbstractTestBase extends TestCase
{
	private static final String ENGINE_PATH = "build/Engine";
	private static final String ENGINE_EXT = ".sej";


	protected void deserializeAndTest() throws Exception
	{
		InputStream inStream = new FileInputStream( getEngineFile() );
		Engine engine = EngineFactory.loadFrom( inStream );

		computeAndTestResult( engine );
	}


	protected File getEngineFile()
	{
		return new File( ENGINE_PATH + getTypeSuffix() + ENGINE_EXT );
	}


	protected abstract String getTypeSuffix();


	protected void computeAndTestResult( Engine _engine )
	{
		Inputs inputs = new Inputs( "4", "40" );
		Outputs outputs = (Outputs) _engine.newComputation( inputs );
		String result = numberToString( getResult( outputs ) );

		assertEquals( "160", result );
	}


	protected abstract Number getResult( Outputs _outputs );


	protected String numberToString( Number _arg )
	{
		String result = _arg.toString();
		result = trimTrailingZeroes( result );
		return result;
	}


	protected String trimTrailingZeroes( String result )
	{
		if (result.contains( "." )) {
			while (result.endsWith( "0" ))
				result = result.substring( 0, result.length() - 1 );
		}
		if (result.endsWith( "." )) result = result.substring( 0, result.length() - 1 );
		return result;
	}


}
