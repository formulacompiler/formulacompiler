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

import java.util.Date;

import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.Operator;



/**
 * This interface allows you to build a spreadsheet representation in memory from scratch. Used
 * instead of loading one from a file or other source.
 * 
 * <p>
 * See the <a target="_top" href="{@docRoot}/../tutorial/buildsheet.htm">tutorial</a> for details.
 * 
 * @author peo
 * 
 * @see SpreadsheetCompiler#newSpreadsheetBuilder()
 */
public interface SpreadsheetBuilder
{

	/**
	 * Returns the constructed spreadsheet. Must always be called as the last method of instances of
	 * this class.
	 */
	public Spreadsheet getSpreadsheet();


	/**
	 * Terminates the current sheet and starts a new one, with a new, empty first row.
	 * 
	 * @return {@code this} (for call chaining).
	 * 
	 * @see org.formulacompiler.spreadsheet.Spreadsheet.Sheet
	 */
	public SpreadsheetBuilder newSheet();


	/**
	 * Terminates the current row and starts a new, empty one in the current sheet.
	 * 
	 * @return {@code this} (for call chaining).
	 * 
	 * @see org.formulacompiler.spreadsheet.Spreadsheet.Row
	 */
	public SpreadsheetBuilder newRow();

	/**
	 * Remembers a style name for this row for use by the {@link SpreadsheetSaver}.
	 * 
	 * @param _styleName is an arbitrary string used by the {@link SpreadsheetSaver}.
	 * @return {@code this} (for call chaining).
	 */
	public SpreadsheetBuilder styleRow( String _styleName );


	/**
	 * Creates a new cell in the current row with a constant value.
	 * 
	 * @param _const is the value of the new cell.
	 * @return {@code this} (for call chaining).
	 * 
	 * @see org.formulacompiler.spreadsheet.Spreadsheet.Cell
	 */
	public SpreadsheetBuilder newCell( Constant _const );

	/**
	 * Creates a new, calculated cell in the current row, with the given expression tree defining its
	 * formula.
	 * 
	 * @param _expr is the expression tree defining the cell's formula.
	 * @return {@code this} (for call chaining).
	 * 
	 * @see org.formulacompiler.spreadsheet.Spreadsheet.Cell
	 */
	public SpreadsheetBuilder newCell( ExprNode _expr );

	/**
	 * Creates a new, blank cell in the current row.
	 * 
	 * @return {@code this} (for call chaining).
	 * 
	 * @see org.formulacompiler.spreadsheet.Spreadsheet.Cell
	 */
	public SpreadsheetBuilder newCell();

	/**
	 * Defines a name for the cell created last.
	 * 
	 * @param _name is the name for the cell. Must be unique in the spreadsheet.
	 * @return {@code this} (for call chaining).
	 * 
	 * @see #newCell(org.formulacompiler.spreadsheet.SpreadsheetBuilder.Constant)
	 * @see #newCell(org.formulacompiler.spreadsheet.SpreadsheetBuilder.ExprNode)
	 * @see org.formulacompiler.spreadsheet.Spreadsheet#getCell(String)
	 * @see org.formulacompiler.spreadsheet.Spreadsheet#getRange(String)
	 */
	public SpreadsheetBuilder nameCell( String _name );

	/**
	 * Remembers a style name for this cell for use by the {@link SpreadsheetSaver}.
	 * 
	 * @param _styleName is an arbitrary string used by the {@link SpreadsheetSaver}.
	 * @return {@code this} (for call chaining).
	 */
	public SpreadsheetBuilder styleCell( String _styleName );

	/**
	 * Returns a reference to the cell created last. Use it to reference the cell in formulas for
	 * other, calculated cells.
	 * 
	 * @return {@code this} (for call chaining).
	 * 
	 * @see #ref(org.formulacompiler.spreadsheet.SpreadsheetBuilder.CellRef)
	 */
	public CellRef currentCell();


	/**
	 * Returns a range defined by two diagonally opposed corner cells.
	 * 
	 * @param _oneCorner is one of the two corners defining the range.
	 * @param _otherCorner is the other of the two corners defining the range.
	 */
	public RangeRef range( CellRef _oneCorner, CellRef _otherCorner );

	/**
	 * Defines a name for the given range.
	 * 
	 * @param _name is the name for the range. Must be unique in the spreadsheet.
	 * @return {@code this} (for call chaining).
	 * 
	 * @see org.formulacompiler.spreadsheet.Spreadsheet#getRange(String)
	 */
	public SpreadsheetBuilder nameRange( RangeRef _range, String _name );


	/**
	 * Returns a numeric constant value.
	 */
	public Constant cst( Number _const );

	/**
	 * Returns a string constant value.
	 */
	public Constant cst( String _const );

	/**
	 * Returns a date constant value.
	 */
	public Constant cst( Date _const );

	/**
	 * Returns a boolean constant value.
	 */
	public Constant cst( boolean _const );


	/**
	 * Returns an expression tree node which evaluates to the value of another cell, defined earlier.
	 * 
	 * @param _cell is the reference to the other cell.
	 * 
	 * @see #currentCell()
	 */
	public ExprNode ref( CellRef _cell );

	/**
	 * Returns an expression tree node which specifies a range of cells, defined earlier.
	 * 
	 * @param _rng is the reference to the range.
	 * 
	 * @see #range(org.formulacompiler.spreadsheet.SpreadsheetBuilder.CellRef, org.formulacompiler.spreadsheet.SpreadsheetBuilder.CellRef)
	 */
	public ExprNode ref( RangeRef _rng );

	/**
	 * Returns an expression tree node which always evaluates to the given, constant value.
	 * 
	 * @param _const is the constant.
	 */
	public ExprNode ref( Constant _const );

	/**
	 * Returns an expression tree node which applies an operator to the values of its argument nodes.
	 * 
	 * @param _op is the operator.
	 * @param _args is the list of arguments. The number of arguments depends on the operator. Most
	 *           operators need two arguments.
	 */
	public ExprNode op( Operator _op, ExprNode... _args );

	/**
	 * Returns an expression tree node which applies a function to the values of its argument nodes.
	 * 
	 * @param _fun is the function.
	 * @param _args is the list of arguments. The number of arguments depends on the function.
	 */
	public ExprNode fun( Function _fun, ExprNode... _args );


	/**
	 * Opaque handle for a constant value supported by the spreadsheet builder.
	 * 
	 * @author peo
	 * 
	 * @see org.formulacompiler.spreadsheet.SpreadsheetBuilder#cst(Number)
	 * @see org.formulacompiler.spreadsheet.SpreadsheetBuilder#cst(String)
	 * @see org.formulacompiler.spreadsheet.SpreadsheetBuilder#cst(Date)
	 * @see org.formulacompiler.spreadsheet.SpreadsheetBuilder#cst(boolean)
	 * @see org.formulacompiler.spreadsheet.SpreadsheetBuilder#ref(org.formulacompiler.spreadsheet.SpreadsheetBuilder.Constant)
	 */
	public static interface Constant
	{
		// opaque
	}


	/**
	 * Opaque handle for an node of an expression tree used to define calculated cells.
	 * 
	 * @author peo
	 * 
	 * @see org.formulacompiler.spreadsheet.SpreadsheetBuilder#ref(org.formulacompiler.spreadsheet.SpreadsheetBuilder.Constant)
	 * @see org.formulacompiler.spreadsheet.SpreadsheetBuilder#ref(org.formulacompiler.spreadsheet.SpreadsheetBuilder.CellRef)
	 * @see org.formulacompiler.spreadsheet.SpreadsheetBuilder#op(Operator, org.formulacompiler.spreadsheet.SpreadsheetBuilder.ExprNode[])
	 * @see org.formulacompiler.spreadsheet.SpreadsheetBuilder#fun(Function, org.formulacompiler.spreadsheet.SpreadsheetBuilder.ExprNode[])
	 */
	public static interface ExprNode
	{
		// opaque
	}


	/**
	 * Opaque handle for a constructed cell in a spreadsheet, used to reference the cell again in the
	 * expression of other, calculated cells.
	 * 
	 * @author peo
	 * 
	 * @see org.formulacompiler.spreadsheet.SpreadsheetBuilder#currentCell()
	 * @see org.formulacompiler.spreadsheet.SpreadsheetBuilder#ref(org.formulacompiler.spreadsheet.SpreadsheetBuilder.CellRef)
	 */
	public static interface CellRef
	{
		// opaque
	}


	/**
	 * Opaque handle for a range in a spreadsheet, used to reference the range again in the
	 * expression of other, calculated cells.
	 * 
	 * @author peo
	 * 
	 * @see org.formulacompiler.spreadsheet.SpreadsheetBuilder#range(org.formulacompiler.spreadsheet.SpreadsheetBuilder.CellRef,
	 *      org.formulacompiler.spreadsheet.SpreadsheetBuilder.CellRef)
	 * @see org.formulacompiler.spreadsheet.SpreadsheetBuilder#ref(org.formulacompiler.spreadsheet.SpreadsheetBuilder.RangeRef)
	 */
	public static interface RangeRef
	{
		// opaque
	}


	/**
	 * Factory interface for {@link org.formulacompiler.runtime.ImplementationLocator#getInstance(Class)}.
	 */
	public static interface Factory
	{
		/**
		 * Factory method.
		 */
		SpreadsheetBuilder newInstance();
	}

}
