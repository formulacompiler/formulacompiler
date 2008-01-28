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
import java.io.InputStream;

import org.formulacompiler.compiler.internal.Yamlizable;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;


public abstract class AbstractSpreadsheetTestCase extends AbstractStandardInputsOutputsTestCase
{
	private static final boolean UPDATE_YAML_IN_PLACE = Util.isBuildPropTrue( "test-ref-update-yaml" );


	protected AbstractSpreadsheetTestCase()
	{
		super();
	}

	protected AbstractSpreadsheetTestCase( String _name )
	{
		super( _name );
	}


	protected void checkSpreadsheetStream( Spreadsheet _expected, InputStream _stream, String _typeExtensionOrFileName )
			throws Exception
	{
		Spreadsheet actual = SpreadsheetCompiler.loadSpreadsheet( _typeExtensionOrFileName, _stream );
		touchExpressions( actual );
		assertEquals( _expected.describe(), actual.describe() );
	}

	protected void touchExpressions( Spreadsheet _ss ) throws Exception
	{
		for (Spreadsheet.Sheet s : _ss.getSheets()) {
			for (Spreadsheet.Row r : s.getRows()) {
				for (Spreadsheet.Cell c : r.getCells()) {
					c.getExpressionText();
				}
			}
		}
	}


	protected void assertYaml( File _path, String _expectedFileBaseName, Yamlizable _actual, String _actualFileName )
			throws Exception
	{
		final String have = _actual.toYaml();
		final File specificFile = new File( _path, _actualFileName + ".yaml" );
		final File genericFile = new File( _path, _expectedFileBaseName + ".yaml" );
		final File expectedFile = (specificFile.exists()) ? specificFile : genericFile;
		if (expectedFile.exists()) {
			String want = Util.readStringFrom( expectedFile );
			if (_actualFileName.endsWith( ".ods" )) {
				want = want.replaceAll( "- err: #DIV/0\\!", "- const: \"#DIV/0!\"" );
				want = want.replaceAll( "- err: #N/A", "- const: \"#N/A\"" );
				want = want.replaceAll( "- err: #VALUE\\!", "- const: \"#VALUE!\"" );
				want = want.replaceAll( "- err: #REF\\!", "- const: \"#REF!\"" );
				want = want.replaceAll( "- err: #NUM\\!", "- const: \"#NUM!\"" );
			}
			if (!want.equals( have )) {
				if (UPDATE_YAML_IN_PLACE) {
					final File actualFile = (_actualFileName.toLowerCase().endsWith( ".xls" ) && !specificFile.exists())
							? genericFile : specificFile;
					Util.writeStringTo( have, actualFile );
				}
				else {
					final File actualFile = new File( _path, _actualFileName + "-actual.yaml" );
					Util.writeStringTo( have, actualFile );
					assertEquals( "YAML bad for " + _actualFileName + "; actual YAML written to ...-actual.yaml", want, have );
				}
			}
		}
		else {
			Util.writeStringTo( have, expectedFile );
		}
	}


	protected void assertYaml( File _path, String _expectedFileBaseName, Spreadsheet _actual, String _actualFileName )
			throws Exception
	{
		assertYaml( _path, _expectedFileBaseName, (Yamlizable) _actual, _actualFileName );
	}


}
