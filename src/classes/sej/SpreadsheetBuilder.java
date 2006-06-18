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

import java.util.Date;


/**
 * This interface allows you to build a spreadsheet representation in memory from scratch. Used
 * instead of loading one from a file or other source.
 * 
 * <p>
 * See the <a href="../../tutorial/buildsheet.htm">tutorial</a> for details.
 * 
 * @author peo
 * 
 * @see SEJ#newSpreadsheetBuilder()
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
	 * @see sej.Spreadsheet.Sheet
	 */
	public void newSheet();


	/**
	 * Terminates the current row and starts a new, empty one in the current sheet.
	 * 
	 * @see sej.Spreadsheet.Row
	 */
	public void newRow();


	/**
	 * Creates a new cell in the current row with a constant value.
	 * 
	 * @param _const is the value of the new cell.
	 * 
	 * @see sej.Spreadsheet.Cell
	 */
	public void newCell( Constant _const );

	/**
	 * Creates a new, calculated cell in the current row, with the given expression tree defining its
	 * formula.
	 * 
	 * @param _expr is the expression tree defining the cell's formula.
	 * 
	 * @see sej.Spreadsheet.Cell
	 */
	public void newCell( ExprNode _expr );

	/**
	 * Defines a name for the cell created last.
	 * 
	 * @param _name is the name for the cell. Must be unique in the spreadsheet.
	 * 
	 * @see #newCell(sej.SpreadsheetBuilder.Constant)
	 * @see #newCell(sej.SpreadsheetBuilder.ExprNode)
	 * @see sej.Spreadsheet#getCell(String)
	 * @see sej.Spreadsheet#getDefinedName(String)
	 */
	public void nameCell( String _name );


	/**
	 * Returns a reference to the cell created last. Use it to reference the cell in formulas for
	 * other, calculated cells.
	 * 
	 * @see #ref(sej.SpreadsheetBuilder.CellRef)
	 */
	public CellRef currentCell();


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
	 * Returns an expression tree node which aggregates the values of its argument nodes into a
	 * single value.
	 * 
	 * @param _agg is the aggregator.
	 * @param _args is the list of arguments. The number of arguments depends on the aggregator. Most
	 *           aggregators accept an arbitrary number of arguments.
	 */
	public ExprNode agg( Aggregator _agg, ExprNode... _args );
	

	/**
	 * Opaque handle for a constant value supported by the spreadsheet builder.
	 * 
	 * @author peo
	 * 
	 * @see sej.SpreadsheetBuilder#cst(Number)
	 * @see sej.SpreadsheetBuilder#cst(String)
	 * @see sej.SpreadsheetBuilder#cst(Date)
	 * @see sej.SpreadsheetBuilder#cst(boolean)
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
	 * @see sej.SpreadsheetBuilder#ref(sej.SpreadsheetBuilder.Constant)
	 * @see sej.SpreadsheetBuilder#ref(sej.SpreadsheetBuilder.CellRef)
	 * @see sej.SpreadsheetBuilder#op(Operator, sej.SpreadsheetBuilder.ExprNode[])
	 * @see sej.SpreadsheetBuilder#fun(Function, sej.SpreadsheetBuilder.ExprNode[])
	 * @see sej.SpreadsheetBuilder#agg(Aggregator, sej.SpreadsheetBuilder.ExprNode[])
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
	 * @see sej.SpreadsheetBuilder#currentCell()
	 * @see sej.SpreadsheetBuilder#ref(sej.SpreadsheetBuilder.CellRef)
	 */
	public static interface CellRef
	{
		// opaque
	}

}
