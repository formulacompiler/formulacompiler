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
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import junit.framework.AssertionFailedError;

public class SpreadsheetVerificationRule implements MethodRule
{
	private Spreadsheet spreadsheet;
	private String fileName;
	private final String spreadsheetExtension;
	private final String templateExtension;
	private final File dataDirectory;

	public SpreadsheetVerificationRule( String _spreadsheetExtension, String _templateExtension, File _dataDirectory )
	{
		this.spreadsheetExtension = _spreadsheetExtension;
		this.templateExtension = _templateExtension;
		this.dataDirectory = _dataDirectory;
	}

	public String getFileName()
	{
		return this.fileName;
	}

	public void setSpreadsheet( final Spreadsheet _spreadsheet )
	{
		this.spreadsheet = _spreadsheet;
	}

	private void verify() throws Exception
	{
		InputStream templateInputStream = null;

		{
			final File templateFile = new File( this.dataDirectory, this.fileName + this.templateExtension );
			if (templateFile.exists()) {
				templateInputStream = new FileInputStream( templateFile );
			}
		}

		{
			final File templateFile = new File( this.dataDirectory, this.fileName + "_template" + this.spreadsheetExtension );
			if (templateFile.exists()) {
				templateInputStream = new FileInputStream( templateFile );
			}
		}

		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		saveTo( this.spreadsheet, byteArrayOutputStream, templateInputStream );
		byte[] generatedDocument = byteArrayOutputStream.toByteArray();

		final File savedFile = new File( this.dataDirectory, this.fileName + "_saved" + this.spreadsheetExtension );
		if (!savedFile.exists()) {
			final OutputStream outputStream = new FileOutputStream( savedFile );
			outputStream.write( generatedDocument );
		}
		else {
			final InputStream expectedInputStream = new FileInputStream( savedFile );
			final InputStream actualInputStream = new ByteArrayInputStream( generatedDocument );
			try {
				SpreadsheetAssert.assertEqualSpreadsheets( expectedInputStream, actualInputStream, this.spreadsheetExtension );
			} catch (AssertionFailedError e) {
				final File actualFile = new File( this.dataDirectory, this.fileName + "_saved-actual" + this.spreadsheetExtension );
				final OutputStream outputStream = new FileOutputStream( actualFile );
				outputStream.write( generatedDocument );
				throw e;
			}
		}

		final InputStream inputStream = new ByteArrayInputStream( generatedDocument );
		SpreadsheetAssert.verify( inputStream, this.spreadsheetExtension );
	}

	private void saveTo( Spreadsheet _s, OutputStream _os, InputStream _template ) throws Exception
	{
		SpreadsheetSaver.Config cfg = new SpreadsheetSaver.Config();
		cfg.spreadsheet = _s;
		cfg.typeExtension = this.spreadsheetExtension;
		cfg.outputStream = _os;
		cfg.templateInputStream = _template;
		SpreadsheetCompiler.newSpreadsheetSaver( cfg ).save();
		_os.close();
	}

	public Statement apply( final Statement base, final FrameworkMethod method, final Object target )
	{
		this.fileName = method.getName();
		return new Statement()
		{
			@Override
			public void evaluate() throws Throwable
			{
				base.evaluate();
				verify();
			}
		};
	}
}
