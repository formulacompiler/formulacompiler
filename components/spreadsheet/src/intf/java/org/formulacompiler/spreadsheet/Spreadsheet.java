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
package org.formulacompiler.spreadsheet;

import org.formulacompiler.describable.Describable;

/**
 * Represents a spreadsheet model in memory. It can be constructed from different spreadsheet file
 * formats, for example Excel .xls and .xml, and OpenOffice Calc (see
 * {@link SpreadsheetCompiler#loadSpreadsheet(java.io.File)}). It serves as input to the
 * {@link SpreadsheetToEngineCompiler} implementations and thus decouples engine compilation from the
 * different spreadsheet file formats supported by AFC.
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
	 * Get a cell by its defined name.
	 * 
	 * @param _cellName is the cell's specific name defined in the spreadsheet (BasePrice,
	 *           NumberSold, etc.).
	 * @return The requested cell. The name must not designate a range.
	 * 
	 * @throws SpreadsheetException.NameNotFound if the name is not defined in the spreadsheet.
	 * @throws IllegalArgumentException if the name identifies a range instead of a single cell.
	 */
	public Cell getCell( String _cellName ) throws SpreadsheetException.NameNotFound, IllegalArgumentException;


	/**
	 * Get a range by its defined name.
	 * 
	 * @param _rangeName is the range's specific name defined in the spreadsheet (Items, Employees,
	 *           etc.).
	 * @return The requested range. The name can designate a cell.
	 * 
	 * @throws SpreadsheetException.NameNotFound if the name is not defined in the spreadsheet.
	 * @throws IllegalArgumentException if the name identifies a range instead of a single cell.
	 */
	public Range getRange( String _rangeName ) throws SpreadsheetException.NameNotFound, IllegalArgumentException;


	/**
	 * Determine all the cell and range names defined in the spreadsheet model.
	 * 
	 * @return The list of name definitions. They are all instances of either
	 *         {@link CellNameDefinition} or {@link RangeNameDefinition}.
	 */
	public NameDefinition[] getDefinedNames();


	/**
	 * Returns the definition for the given name, if any.
	 * 
	 * @return The definition for the name, or {@code null} if not found.
	 */
	public NameDefinition getDefinedName( String _cellName );


	/**
	 * Adds a new cell name definition to the spreadsheet.
	 * 
	 * @param _name is the name to be defined.
	 * @param _cell is the cell to be named.
	 */
	public void defineName( String _name, Cell _cell );


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
		 * Returns the cells defined in the row.
		 */
		public Cell[] getCells();

	}


	/**
	 * Returns information about a spreadsheet cell.
	 * 
	 * @author peo
	 */
	public static interface Cell extends Describable
	{

		/**
		 * Returns the row this cell is part of.
		 */
		public Row getRow();

		/**
		 * Returns the constant value of the cell, as defined in the spreadsheet.
		 * 
		 * @return the value, or {@code null} if the cell is empty or computed by a formula.
		 * 
		 * @see #getValue()
		 */
		public Object getConstantValue();

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
	public static interface Range
	{

		/**
		 * Tests whether the other range is completely contained within this one.
		 * 
		 * @param _other is the other range.
		 * @return true iff the other range is completely contained.
		 */
		boolean contains( Range _other );

		/**
		 * Tests whether the cell is contained within this range.
		 * 
		 * @param _cell is the cell to test.
		 * @return true iff the cell is within the range.
		 */
		boolean contains( Cell _cell );

		/**
		 * Returns the top left corner of the range (on the leftmost sheet of the range).
		 */
		Cell getTopLeft();

		/**
		 * Returns the bottom right corner of the range (on the rightmost sheet of the range).
		 */
		Cell getBottomRight();

	}


	/**
	 * Describes a named reference defined in a spreadsheet model.
	 * 
	 * @author peo
	 */
	public static abstract interface NameDefinition
	{

		/**
		 * Returns the defined name.
		 * 
		 * @return The name. Never null.
		 */
		public String getName();

	}


	/**
	 * Describes a named cell in a spreadsheet.
	 * 
	 * @author peo
	 */
	public static interface CellNameDefinition extends NameDefinition
	{

		/**
		 * Returns the cell designated by the name.
		 * 
		 * @return The cell. Never null.
		 */
		public Cell getCell();

	}


	/**
	 * Describes a named range in an spreadsheet.
	 * 
	 * @author peo
	 */
	public static interface RangeNameDefinition extends NameDefinition
	{

		/**
		 * Returns the cell range designated by the name.
		 * 
		 * @return The cell range. Never null.
		 */
		public Range getRange();

	}


}
