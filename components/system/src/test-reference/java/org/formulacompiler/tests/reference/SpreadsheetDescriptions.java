/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
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

package org.formulacompiler.tests.reference;

import java.io.File;

import org.formulacompiler.tests.reference.base.SheetSuiteSetup;
import org.formulacompiler.tests.utils.AbstractSpreadsheetDescriptionsTestSuite;

import junit.framework.Test;

public final class SpreadsheetDescriptions extends AbstractSpreadsheetDescriptionsTestSuite
{

	public static Test suite()
	{
		return new SpreadsheetDescriptions().init();
	}

	@Override
	protected void addTestsFor( String _ext ) throws Exception
	{
		addTestsIn( "src/test-reference/data", _ext, true );
	}

	@Override
	protected void addImpliedTestsFor( File _path, String _baseName, String _ext )
	{
		if (SheetSuiteSetup.odsSpreadsheetExists( _baseName )) {
			addTestFor( new File( _path, _baseName + ".ods" ), _baseName );
		}
	}

}
