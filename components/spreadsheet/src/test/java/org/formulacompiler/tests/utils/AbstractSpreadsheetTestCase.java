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

package org.formulacompiler.tests.utils;

import java.io.File;

import org.formulacompiler.compiler.internal.Yamlizable;
import org.formulacompiler.spreadsheet.Spreadsheet;


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
