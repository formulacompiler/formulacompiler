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
 * Utility interface that supports simple cell binding using the cell names in the spreadsheet and
 * reflection on the input and output types.
 * 
 * @author peo
 * 
 * @see EngineBuilder#getByNameBinder()
 * @see SEJ#newSpreadsheetByNameBinder(SpreadsheetBinder)
 */
public interface SpreadsheetByNameBinder
{

	/**
	 * Configuration data for new instances of {@link sej.SpreadsheetByNameBinder}.
	 * 
	 * @author peo
	 */
	public static class Config
	{

		/**
		 * The binder to use for binding specific cells to specific methods.
		 */
		public SpreadsheetBinder binder;

		/**
		 * Validates the configuration.
		 * 
		 * @throws IllegalArgumentException
		 */
		public void validate()
		{
			if (this.binder == null) throw new IllegalArgumentException( "binder is null" );
		}

	}


	/**
	 * Cell binder for input cells.
	 */
	public CellBinder inputs();


	/**
	 * Cell binder for output cells.
	 */
	public CellBinder outputs();


	/**
	 * Interface to a cell binder for either input or output cells.
	 * 
	 * @author peo
	 * 
	 * @see sej.SpreadsheetByNameBinder#inputs()
	 * @see sej.SpreadsheetByNameBinder#outputs()
	 */
	public static interface CellBinder
	{

		/**
		 * Binds all abstract methods to cells of the corresponding name (that is, methods called
		 * {@code xy()} need a cell called "XY", methods called {@code getXy()} need a cell called
		 * either "XY" or "GETXY"). It is an error if no cell of a suitable name is defined.
		 * Non-abstract methods are bound likewise, but it is no error if there is not suitable named
		 * cell. Typically invoked for output cells (see {@link sej.SpreadsheetByNameBinder#outputs()}).
		 * 
		 * <p>
		 * All cells thus bound are removed from the internal list of named cells still to be bound.
		 * This is so {@link #bindAllNamedCellsToMethods()} does not attempt to bind them again.
		 * 
		 * <p>
		 * See the <a href="../../tutorial/basics.htm#Convention">tutorial</a> for details.
		 * 
		 * @throws CompilerError
		 */
		public void bindAllMethodsToNamedCells() throws CompilerError;

		/**
		 * Binds all still unbound named cells to methods of the corresponding name (that is, a cell
		 * named "XY" must have a method named either {@code xy()} or {@code getXy()}). It is an
		 * error if no suitable method can be found. Typically invoked for input cells (see
		 * {@link sej.SpreadsheetByNameBinder#inputs()}).
		 * 
		 * <p>
		 * See the <a href="../../tutorial/basics.htm#Convention">tutorial</a> for details.
		 * 
		 * @throws CompilerError
		 * 
		 * @see #bindAllMethodsToNamedCells()
		 */
		public void bindAllNamedCellsToMethods() throws CompilerError;

	}

}
