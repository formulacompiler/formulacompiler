/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * This file is part of the Abacus Formula Compiler (AFC).
 *
 * For commercial licensing, please contact sales(at)formulacompiler.com.
 *
 * AFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AFC.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.formulacompiler.tests.serialization;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.formulacompiler.runtime.Engine;
import org.formulacompiler.runtime.FormulaRuntime;

import junit.framework.TestCase;

public abstract class AbstractTestBase extends TestCase
{
	private static final String ENGINE_PATH = "temp/test/data";
	private static final String ENGINE_NAME = "SerializedEngine";
	private static final String ENGINE_EXT = ".jar";


	protected void deserializeAndTest() throws Exception
	{
		InputStream inStream = new BufferedInputStream( new FileInputStream( getEngineFile() ) );
		Engine engine = FormulaRuntime.loadEngine( inStream );

		computeAndTestResult( engine );
	}


	protected File getEngineFile()
	{
		final File path = new File( ENGINE_PATH );
		path.mkdirs();
		return new File( path, ENGINE_NAME + getTypeSuffix() + ENGINE_EXT );
	}


	protected abstract String getTypeSuffix();


	protected void computeAndTestResult( Engine _engine )
	{
		Inputs inputs = new Inputs( "4", "40" );
		Outputs outputs = (Outputs) _engine.getComputationFactory().newComputation( inputs );
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


	protected String trimTrailingZeroes( String _result )
	{
		String result = _result;
		if (result.contains( "." )) {
			while (result.endsWith( "0" ))
				result = result.substring( 0, result.length() - 1 );
		}
		if (result.endsWith( "." )) result = result.substring( 0, result.length() - 1 );
		return result;
	}


}
