/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
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
package sej;

/**
 * Represents a spreadsheet model in memory. It can be constructed from different spreadsheet file
 * formats, for example Excel .xls and .xml, and OpenOffice Calc (see
 * {@code SpreadsheetLoader.loadFromFile()}). It serves as input the {@link Compiler}
 * implementations and thus decouples engine compilation from the different spreadsheet file formats
 * supported by SEJ.
 * 
 * @see SpreadsheetLoader
 * @see Compiler
 * 
 * @author peo
 */
public interface Spreadsheet
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
	 * @throws ModelError.NameNotFound if the name is not defined in the spreadsheet.
	 * @throws IllegalArgumentException if the name identifies a range instead of a single cell.
	 */
	public Cell getCell( String _cellName ) throws ModelError.NameNotFound, IllegalArgumentException;


	/**
	 * Get a range by its defined name.
	 * 
	 * @param _rangeName is the range's specific name defined in the spreadsheet (Items, Employees,
	 *           etc.).
	 * @return The requested range. The name can designate a cell.
	 * 
	 * @throws ModelError.NameNotFound if the name is not defined in the spreadsheet.
	 * @throws IllegalArgumentException if the name identifies a range instead of a single cell.
	 */
	public Range getRange( String _rangeName ) throws ModelError.NameNotFound, IllegalArgumentException;


	/**
	 * Determine all the cell and range names defined in the spreadsheet model.
	 * 
	 * @return The list of name definitions. They are all instances of either
	 *         {@link CellNameDefinition} or {@link RangeNameDefinition}.
	 */
	public NameDefinition[] getDefinedNames();


	/**
	 * Marker interface for a spreadsheet cell in the spreadsheet model.
	 * 
	 * @see Spreadsheet
	 * @author peo
	 */
	public static interface Cell
	{
		/* Marker interface only */
	}


	/**
	 * Marker interface for a spreadsheet range in the spreadsheet model.
	 * 
	 * @see Spreadsheet
	 * @author peo
	 */
	public static interface Range
	{
		/* Marker interface only */
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
		String getName();

	}


	public static interface CellNameDefinition extends NameDefinition
	{

		/**
		 * Returns the cell designated by the name.
		 * 
		 * @return The cell. Never null.
		 */
		Cell getCell();

	}


	public static interface RangeNameDefinition extends NameDefinition
	{

		/**
		 * Returns the cell range designated by the name.
		 * 
		 * @return The cell range. Never null.
		 */
		Range getRange();

	}
}
