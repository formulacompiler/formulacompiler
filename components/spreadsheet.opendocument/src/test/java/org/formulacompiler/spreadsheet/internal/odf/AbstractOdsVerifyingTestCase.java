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

package org.formulacompiler.spreadsheet.internal.odf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.SpreadsheetSaver;
import org.formulacompiler.tests.utils.SpreadsheetAssert;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public abstract class AbstractOdsVerifyingTestCase extends TestCase
{
	protected static final String FILE_EXTENSION = ".ods";
	protected static final String TEMPLATE_FILE_EXTENSION = ".ots";
	protected Spreadsheet spreadsheet;

	@Override
	protected void tearDown() throws Exception
	{
		saveAndVerify( this.spreadsheet );
	}

	private void saveAndVerify( final Spreadsheet _s ) throws Exception
	{
		InputStream templateInputStream = null;

		{
			final File templateFile = new File( getDataDirectory(), this.getName() + TEMPLATE_FILE_EXTENSION );
			if (templateFile.exists()) {
				templateInputStream = new FileInputStream( templateFile );
			}
		}

		{
			final File templateFile = new File( getDataDirectory(), this.getName() + "_template" + FILE_EXTENSION );
			if (templateFile.exists()) {
				templateInputStream = new FileInputStream( templateFile );
			}
		}

		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		saveTo( _s, byteArrayOutputStream, templateInputStream );
		byte[] generatedDocument = byteArrayOutputStream.toByteArray();

		final File savedFile = new File( getDataDirectory(), this.getName() + "_saved" + FILE_EXTENSION );
		if (!savedFile.exists()) {
			final OutputStream outputStream = new FileOutputStream( savedFile );
			outputStream.write( generatedDocument );
		}
		else {
			final InputStream expectedInputStream = new FileInputStream( savedFile );
			final InputStream actualInputStream = new ByteArrayInputStream( generatedDocument );
			try {
				SpreadsheetAssert.assertEqualSpreadsheets( expectedInputStream, actualInputStream, ".ods" );
			} catch (AssertionFailedError e) {
				final File actualFile = new File( getDataDirectory(), this.getName() + "_saved-actual" + FILE_EXTENSION );
				final OutputStream outputStream = new FileOutputStream( actualFile );
				outputStream.write( generatedDocument );
				throw e;
			}
		}

		final InputStream inputStream = new ByteArrayInputStream( generatedDocument );
		OpenDocumentFormatVerifier.verify( inputStream );
	}

	protected abstract File getDataDirectory();

	private static void saveTo( Spreadsheet _s, OutputStream _os, InputStream _template ) throws Exception
	{
		SpreadsheetSaver.Config cfg = new SpreadsheetSaver.Config();
		cfg.spreadsheet = _s;
		cfg.typeExtension = FILE_EXTENSION;
		cfg.outputStream = _os;
		cfg.templateInputStream = _template;
		SpreadsheetCompiler.newSpreadsheetSaver( cfg ).save();
		_os.close();
	}

}
