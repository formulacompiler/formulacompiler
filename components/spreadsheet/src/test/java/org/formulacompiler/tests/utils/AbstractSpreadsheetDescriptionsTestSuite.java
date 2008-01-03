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
package org.formulacompiler.tests.utils;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.formulacompiler.compiler.internal.IOUtil;
import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;

public abstract class AbstractSpreadsheetDescriptionsTestSuite extends AbstractInitializableTestSuite
{

	protected AbstractSpreadsheetDescriptionsTestSuite( String _name )
	{
		super( _name );
	}

	protected AbstractSpreadsheetDescriptionsTestSuite()
	{
		this( "Check loaded spreadsheets against their .yaml descriptions" );
	}


	private final Set<File> added = New.set();

	protected final boolean mustStillAdd( File _new )
	{
		return !this.added.contains( _new ) && allow( _new );
	}

	protected boolean allow( File _new )
	{
		return true;
	}

	protected final void haveAdded( File _new )
	{
		this.added.add( _new );
	}


	@Override
	protected void addTests() throws Exception
	{
		addTestsFor( ".xls" );
		addTestsFor( ".ods" );
	}

	protected abstract void addTestsFor( String _ext ) throws Exception;

	protected final void addTestsIn( String _path, final String _ext, boolean _recurse ) throws Exception
	{
		File path = new File( _path );
		IOUtil.iterateFiles( path, "*" + _ext, path, _recurse, new IOUtil.FileVisitor()
		{

			public void visit( final File _inputFile, final File _outputFile ) throws IOException
			{
				if (mustStillAdd( _inputFile )) {
					final String fileName = _inputFile.getName();
					final String baseName = fileName.substring( 0, fileName.length() - _ext.length() );
					addTestFor( _inputFile, baseName );
					addImpliedTestsFor( _inputFile.getParentFile(), baseName, _ext );
				}
			}

		} );
	}

	protected void addImpliedTestsFor( File _path, String _baseName, String _ext )
	{
		// Can be overridden.
	}

	protected final void addTestFor( final File _file, final String _baseName )
	{
		if (mustStillAdd( _file )) {
			addTest( new AbstractIOTestCase( _file.getPath() )
			{

				@Override
				protected void runTest() throws Throwable
				{
					final Spreadsheet sheet = SpreadsheetCompiler.loadSpreadsheet( _file );
					assertYaml( _file.getParentFile(), _baseName, sheet, _file.getName() );
				}

			} );
			haveAdded( _file );
		}
	}

}
