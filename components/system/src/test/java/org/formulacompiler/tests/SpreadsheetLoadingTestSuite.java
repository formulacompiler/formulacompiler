/*
 * Copyright (c) 2006 by Abacus Research AG, Switzerland.
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
package org.formulacompiler.tests;

import java.io.File;
import java.io.IOException;

import org.formulacompiler.compiler.internal.IOUtil;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.tests.utils.AbstractIOTestBase;

import junit.framework.Test;

public final class SpreadsheetLoadingTestSuite extends AbstractTestSuite
{

	public static Test suite()
	{
		return new SpreadsheetLoadingTestSuite();
	}

	@Override
	protected void addTests() throws Exception
	{
		addTestsFor( ".xls" );
	}

	private void addTestsFor( String _ext ) throws Exception
	{
		addTestsIn( "src/test/data", _ext );
	}

	private void addTestsIn( String _path, final String _ext ) throws Exception
	{
		File path = new File( _path );
		IOUtil.iterateFiles( path, "*" + _ext, path, true, new IOUtil.FileVisitor()
		{

			public void visit( final File _inputFile, final File _outputFile ) throws IOException
			{
				addTest( new AbstractIOTestBase( _inputFile.getPath() )
				{

					@Override
					protected void runTest() throws Throwable
					{
						final Spreadsheet sheet = SpreadsheetCompiler.loadSpreadsheet( _inputFile );
						final String fileName = _inputFile.getName();
						final String baseName = fileName.substring( 0, fileName.length() - _ext.length() );
						assertYaml( _inputFile.getParentFile(), baseName, sheet, fileName );
					}

				} );
			}

		} );
	}

}
