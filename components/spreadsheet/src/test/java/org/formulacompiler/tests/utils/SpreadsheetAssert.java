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

package org.formulacompiler.tests.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;

import org.formulacompiler.runtime.ImplementationLocator;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.SpreadsheetException;

import junit.framework.Assert;

public class SpreadsheetAssert extends Assert
{
	public static void assertEqualSpreadsheets( InputStream _expected, InputStream _actual, String _fileExtension ) throws Exception
	{
		assertEqualSpreadsheets( null, _expected, _actual, _fileExtension );
	}

	public static void assertEqualSpreadsheets( String _message, InputStream _expected, InputStream _actual, String _fileExtension ) throws Exception
	{
		final SpreadsheetComparator spreadsheetComparator = getSpreadsheetComparator( _fileExtension );
		spreadsheetComparator.assertEqualSpreadsheets( _message, _expected, _actual );
	}

	public static void assertEqualSpreadsheets( File _expectedFile, File _actualFile ) throws Exception
	{
		final String name = _expectedFile.getName();
		final String ext = name.substring( name.lastIndexOf( '.' ) );
		final InputStream exp = new BufferedInputStream( new FileInputStream( _expectedFile ) );
		final InputStream act = new BufferedInputStream( new FileInputStream( _actualFile ) );
		assertEqualSpreadsheets( "Comparing files " + _expectedFile + " and " + _actualFile, exp, act, ext );
	}

	private static SpreadsheetComparator getSpreadsheetComparator( String _fileExtension ) throws Exception
	{
		final Collection<SpreadsheetComparator.Factory> factories = ImplementationLocator.getInstances( SpreadsheetComparator.Factory.class );
		for (SpreadsheetComparator.Factory factory : factories) {
			if (factory.canHandle( _fileExtension )) {
				return factory.getInstance();
			}
		}
		throw new SpreadsheetException.UnsupportedFormat( "No comparator found for file " + _fileExtension );
	}

	public static void assertEqualSpreadsheets( Spreadsheet _expected, InputStream _actual, String _typeExtensionOrFileName )
			throws Exception
	{
		Spreadsheet actual = SpreadsheetCompiler.loadSpreadsheet( _typeExtensionOrFileName, _actual );
		touchExpressions( actual );
		assertEquals( _expected.describe(), actual.describe() );
	}

	private static void touchExpressions( Spreadsheet _ss ) throws Exception
	{
		for (Spreadsheet.Sheet s : _ss.getSheets()) {
			for (Spreadsheet.Row r : s.getRows()) {
				for (Spreadsheet.Cell c : r.getCells()) {
					c.getExpressionText();
				}
			}
		}
	}
}
