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

package org.formulacompiler.spreadsheet;


/**
 * Utility interface that supports the creation of cell names from other cells' values.
 * <p>
 * <em>This interface is an API only. Do not implement it yourself.</em>
 * 
 * @author peo
 */
public interface SpreadsheetNameCreator
{


	/**
	 * Configuration data for new instances of {@link SpreadsheetNameCreator}.
	 * 
	 * @author peo
	 */
	public static class Config
	{

		/**
		 * The sheet of the spreadsheet representation in which to name cells.
		 */
		public Spreadsheet.Sheet sheet;

		/**
		 * Validates the configuration.
		 * 
		 * @throws IllegalArgumentException
		 */
		public void validate()
		{
			if (this.sheet == null) throw new IllegalArgumentException( "sheet is null" );
		}

	}


	/**
	 * Creates cell names from row titles. A row title is the constant string value of the leftmost
	 * cell of a row. This value is given as a cell name to the cell just to the right of it. The
	 * method only processes rows which have two leftmost cells and the leftmost one of them holds a
	 * constant string value. Before setting the name, some characters that are illegal for cell
	 * names are filtered out. In particular, white space and some punctuation is filtered.
	 */
	public void createCellNamesFromRowTitles();


	/**
	 * Factory interface for
	 * {@link org.formulacompiler.runtime.ImplementationLocator#getInstance(Class)}.
	 */
	public static interface Factory
	{
		/**
		 * Factory method.
		 */
		SpreadsheetNameCreator newInstance( Config _config );
	}

}
