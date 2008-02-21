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

package org.formulacompiler.spreadsheet;

import java.io.IOException;
import java.io.InputStream;

/**
 * Allows loading of spreadsheets from external sources (like Excel files).
 * <p>
 * <em>This interface is an API only. Do not implement it yourself.</em>
 * 
 * @see SpreadsheetCompiler#loadSpreadsheet(String, InputStream, SpreadsheetLoader.Config)
 * 
 * @author peo
 */
public interface SpreadsheetLoader
{

	/**
	 * Configuration data for new instances of {@link SpreadsheetLoader}.
	 * 
	 * @author peo
	 */
	public static class Config
	{

		/**
		 * The sheet of the spreadsheet representation in which to name cells.
		 */
		public boolean loadAllCellValues = false;

		/**
		 * Validates the configuration.
		 * 
		 * @throws IllegalArgumentException
		 */
		public void validate()
		{
			// Nothing to validate so far.
		}

	}


	/**
	 * Loads a spreadsheet stream into an AFC spreadsheet model. The loader to use is determined by
	 * giving each registered loader a look at the file name. The first one that signals it can
	 * handle it is used.
	 * 
	 * @param _originalFileName is the complete file name of the original spreadsheet file (for
	 *           example Test.xls or Test.xml).
	 * @return The spreadsheet model loaded from the file.
	 * @throws IOException when there is any proplem accessing the file. May also throw runtime
	 *            exceptions when there are problems in file.
	 */
	public Spreadsheet loadFrom( String _originalFileName, InputStream _stream ) throws IOException,
			SpreadsheetException;


	/**
	 * Factory interface for
	 * {@link org.formulacompiler.runtime.ImplementationLocator#getInstance(Class)}.
	 */
	public static interface Factory
	{
		/**
		 * Factory method.
		 */
		SpreadsheetLoader newInstance( Config __config );
	}

}
