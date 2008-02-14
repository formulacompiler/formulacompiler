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

import java.util.Map;

import org.formulacompiler.compiler.Describable;

/**
 * Represents a spreadsheet model in memory. It can be constructed from different spreadsheet file
 * formats, for example Excel .xls and .xml, and OpenOffice Calc (see
 * {@link SpreadsheetCompiler#loadSpreadsheet(java.io.File)}). It serves as input to the
 * {@link SpreadsheetToEngineCompiler} implementations and thus decouples engine compilation from
 * the different spreadsheet file formats supported by AFC.
 * <p>
 * The spreadsheet model is immutable except for one thing. As a convenience, you can add custom
 * range names to be used by {@link #getCell(String)}, {@link #getRange(String)}, and
 * {@link #getRangeNames()}. These are never used internally to resolve formula references and do
 * not affect the semantics of the spreadsheet model. We support this convenience so that code
 * already written against the {@link #getCell(String)} API can transparently profit from names
 * extracted from a spreadsheet's layout, for instance.
 * <p>
 * The handling of additional range names is not thread-safe.
 * 
 * @see SpreadsheetLoader
 * @see SpreadsheetBuilder
 * @see SpreadsheetToEngineCompiler
 * 
 * @author peo
 */
public interface Spreadsheet extends Describable
{

	/**
	 * Get a cell by its index.
	 * 
	 * @param _sheetIndex is 0-based index of the sheet in the workbook (typically 0).
	 * @param _columnIndex is the 0-based column index of the cell (so 0 is A, 1 is B, etc.).
	 * @param _rowIndex is the 0-based row index of the cell.
	 * @return The requested cell, or possibly {@code null} if the cell is not defined in the
	 *         spreadsheet.
	 */
	public Cell getCell( int _sheetIndex, int _columnIndex, int _rowIndex );


	/**
	 * Get a cell by its range name.
	 * 
	 * @param _rangeName is the name of a single-cell range (BasePrice, NumberSold, etc.). Range
	 *           names are case-insensitive.
	 * @return The requested cell. The returned reference may specify a cell that is not within the
	 *         actual bounds of the spreadsheet. In this case, it denotes an empty cell.
	 * 
	 * @throws SpreadsheetException.NameNotFound if the name is not defined in the spreadsheet.
	 * @throws IllegalArgumentException if the name identifies a range instead of a single cell.
	 * 
	 * @see #getRange(String)
	 */
	public Cell getCell( String _rangeName ) throws SpreadsheetException.NameNotFound, IllegalArgumentException;


	/**
	 * Get a cell by its A1-style name. Sheet references are not supported.
	 * 
	 * @param _a1Name is the cell's A1-style name (A1, B5, AA15, etc.).
	 * @return The requested cell. The returned reference may specify a cell that is not within the
	 *         actual bounds of the spreadsheet. In this case, it denotes an empty cell.
	 * 
	 * @throws SpreadsheetException.NameNotFound if the name is not parseable as an A1-style cell
	 *            reference.
	 */
	public Cell getCellA1( String _a1Name ) throws SpreadsheetException.NameNotFound;


	/**
	 * Get a range by its name.
	 * 
	 * @param _rangeName is a name in {@link #getRangeNames()} (Items, Employees, etc.). Range names
	 *           are case-insensitive.
	 * @return The requested range. Might be a single cell.
	 * 
	 * @throws SpreadsheetException.NameNotFound if the name is not defined in the spreadsheet.
	 * @throws IllegalArgumentException if the name identifies a range instead of a single cell.
	 * 
	 * @see #getRangeNames()
	 * @see #getCell(String)
	 */
	public Range getRange( String _rangeName ) throws SpreadsheetException.NameNotFound, IllegalArgumentException;


	/**
	 * Get all the range (and cell) names defined for this spreadsheet. The key of the map is the
	 * range's specific name defined in the spreadsheet (Items, Employees, etc.). The value of the
	 * map is the named range or cell.
	 * <p>
	 * This map is initialized from the range names defined in the loaded spreadsheet (or constructed
	 * using a {@link SpreadsheetBuilder}). It can be extended using
	 * {@link #defineAdditionalRangeName(String, Range)}.
	 * 
	 * @return The read-only map of case-insensitive names to corresponding ranges.
	 * 
	 * @see #defineAdditionalRangeName(String, Range)
	 * @see #getRange(String)
	 * @see #getCell(String)
	 */
	public Map<String, Range> getRangeNames();


	/**
	 * Defines an additional range name to be returned by {@code getRangeNames} et al. Such
	 * additional names are never used internally to resolve formula references and do not affect the
	 * semantics of the spreadsheet model.
	 * <p>
	 * We support this convenience method so that code written against the {@link #getCell(String)}
	 * API can transparently profit from additional names extracted, for instance, from a
	 * spreadsheet's layout.
	 * 
	 * @param _name is the name to be defined. It must not collide with a range name defined by the
	 *           underlying spreadsheet model (as loaded from a file or constructed by a builder).
	 *           Range names are case-insensitive.
	 * @param _ref is the range or single cell to be named.
	 * 
	 * @throws IllegalArgumentException if either argument is {@code null}, or if the name collides
	 *            with a model-defined name.
	 */
	public void defineAdditionalRangeName( String _name, Range _ref ) throws IllegalArgumentException;


	/**
	 * Returns the worksheets defined in the spreadsheet.
	 */
	public Sheet[] getSheets();


	/**
	 * Returns information about a worksheet.
	 * 
	 * @author peo
	 */
	public static interface Sheet extends Describable
	{

		/**
		 * Returns the spreadsheet this worksheet is part of.
		 */
		public Spreadsheet getSpreadsheet();

		/**
		 * Returns the sheet index (0 based) within the spreadsheet.
		 */
		public int getSheetIndex();

		/**
		 * Returns the name.
		 */
		public String getName();

		/**
		 * Returns the rows defined in the worksheet.
		 */
		public Row[] getRows();

	}


	/**
	 * Returns information about a row.
	 * 
	 * @author peo
	 */
	public static interface Row extends Describable
	{

		/**
		 * Returns the sheet this row is part of.
		 */
		public Sheet getSheet();

		/**
		 * Returns the row index (0 based) within the sheet.
		 */
		public int getRowIndex();

		/**
		 * Returns the cells defined in the row.
		 */
		public Cell[] getCells();

	}


	/**
	 * Returns information about a spreadsheet cell.
	 * 
	 * @author peo
	 */
	public static interface Cell extends Range
	{

		/**
		 * Returns the row this cell is part of.
		 */
		public Row getRow();

		/**
		 * Returns the cell index (0 based) within the row.
		 */
		public int getColumnIndex();

		/**
		 * Returns the constant value of the cell, as defined in the spreadsheet.
		 * 
		 * @return the value, or {@code null} if the cell is empty, an error value, or computed by a
		 *         formula.
		 * 
		 * @see #getValue()
		 */
		public Object getConstantValue();

		/**
		 * Returns the error text of the cell, as defined in the spreadsheet.
		 * 
		 * @return the text, or {@code null} if the cell is not an error cell, or computed by a
		 *         formula.
		 * 
		 * @see #getValue()
		 */
		public String getErrorText();

		/**
		 * Returns the value of the cell, as saved in the spreadsheet.
		 * 
		 * @return the value, or {@code null} if the cell is empty.
		 * 
		 * @see #getConstantValue()
		 */
		public Object getValue();

		/**
		 * Returns the expression text of the cell, as parsed from the spreadsheet by AFC.
		 * 
		 * @return the text of the parsed expression, or {@code null} if the cell is empty or
		 *         constant.
		 * @throws SpreadsheetException
		 */
		public String getExpressionText() throws SpreadsheetException;

	}


	/**
	 * Marker interface for a spreadsheet range in the spreadsheet model.
	 * 
	 * @see Spreadsheet
	 * @author peo
	 */
	public static interface Range extends Describable
	{

		/**
		 * Tests whether the other range is completely contained within this one.
		 * 
		 * @param _other is the other range.
		 * @return true iff the other range is completely contained.
		 */
		boolean contains( Range _other );

		/**
		 * Returns the top left corner of the range (on the leftmost sheet of the range).
		 */
		Cell getTopLeft();

		/**
		 * Returns the bottom right corner of the range (on the rightmost sheet of the range).
		 */
		Cell getBottomRight();

		/**
		 * Allows iteration over the cells contained in the range. The direction is first left, then
		 * down, from the top left.
		 */
		Iterable<Cell> cells();

	}


}
