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

import java.util.Map;

import org.formulacompiler.compiler.CompilerException;


/**
 * Utility interface that supports simple cell binding using the cell names in the spreadsheet and
 * reflection on the input and output types.
 * 
 * @author peo
 * 
 * @see EngineBuilder#getByNameBinder()
 * @see SpreadsheetCompiler#newSpreadsheetByNameBinder(SpreadsheetBinder)
 */
public interface SpreadsheetByNameBinder
{

	/**
	 * Configuration data for new instances of
	 * {@link org.formulacompiler.spreadsheet.SpreadsheetByNameBinder}.
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
	 * 
	 * @see #outputs()
	 */
	public CellBinder inputs();


	/**
	 * Cell binder for output cells.
	 * 
	 * @see #inputs()
	 */
	public CellBinder outputs();


	/**
	 * Returns the cell name definition this by-name binder has not bound to an input or output
	 * method. Used to warn users about unbound cells.
	 * 
	 * @see #failIfCellNamesAreStillUnbound()
	 */
	public Map<String, Spreadsheet.Range> cellNamesLeftUnbound();


	/**
	 * Raises an exception if there are named cells that were not bound.
	 * 
	 * @throws SpreadsheetException.NameNotFound if there is an unbound name.
	 * 
	 * @see #cellNamesLeftUnbound()
	 * @see EngineBuilder#failIfByNameBindingLeftNamedCellsUnbound()
	 */
	public void failIfCellNamesAreStillUnbound() throws SpreadsheetException;


	/**
	 * Interface to a cell binder for either input or output cells.
	 * 
	 * @author peo
	 * 
	 * @see org.formulacompiler.spreadsheet.SpreadsheetByNameBinder#inputs()
	 * @see org.formulacompiler.spreadsheet.SpreadsheetByNameBinder#outputs()
	 */
	public static interface CellBinder
	{

		/**
		 * Binds all abstract methods to cells of the corresponding name (that is, methods called
		 * {@code xy()} need a cell called "XY", methods called {@code getXy()} need a cell called
		 * either "XY" or "GETXY"). It is an error if no cell of a suitable name is defined.
		 * Non-abstract methods are bound likewise, but it is no error if there is no suitable named
		 * cell. Typically invoked for output cells (see
		 * {@link org.formulacompiler.spreadsheet.SpreadsheetByNameBinder#outputs()}).
		 * 
		 * <p>
		 * All cells thus bound are removed from the internal list of named cells still to be bound.
		 * This is so {@link #bindAllNamedCellsToMethods()} does not attempt to bind them again.
		 * 
		 * <p>
		 * See the <a target="_top" href="{@docRoot}/../tutorial/basics.htm#Convention">tutorial</a>
		 * for details.
		 * 
		 * @throws CompilerException
		 * 
		 * @see #bindAllMethodsToPrefixedNamedCells(String)
		 * @see #bindAllNamedCellsToMethods()
		 */
		public void bindAllMethodsToNamedCells() throws CompilerException;

		/**
		 * Binds all abstract methods to cells of the corresponding name. It is an error if no cell of
		 * a suitable name is defined. Non-abstract methods are bound likewise, but it is no error if
		 * there is not suitable named cell. Typically invoked for output cells (see
		 * {@link org.formulacompiler.spreadsheet.SpreadsheetByNameBinder#outputs()}).
		 * 
		 * <p>
		 * All cells thus bound are removed from the internal list of named cells still to be bound.
		 * This is so {@link #bindAllNamedCellsToMethods()} does not attempt to bind them again.
		 * 
		 * <p>
		 * See the <a target="_top" href="{@docRoot}/../tutorial/basics.htm#Convention">tutorial</a>
		 * for details.
		 * 
		 * @param _prefix is what all cell names to be considered must start with. Methods called
		 *           {@code xy()} need a cell called "prefixXY", methods called {@code getXy()} need a
		 *           cell called either "prefixXY" or "prefixGETXY" (all case-insensitive).
		 * 
		 * @throws CompilerException
		 * 
		 * @see #bindAllMethodsToNamedCells()
		 */
		public void bindAllMethodsToPrefixedNamedCells( String _prefix ) throws CompilerException;

		/**
		 * Binds all still unbound named cells to methods of the corresponding name (that is, a cell
		 * named "XY" must have a method named either {@code xy()} or {@code getXy()}). The method
		 * name search is not case-sensitive. It is an error if no suitable method can be found.
		 * Typically invoked for input cells (see
		 * {@link org.formulacompiler.spreadsheet.SpreadsheetByNameBinder#inputs()}).
		 * 
		 * <p>
		 * See the <a target="_top" href="{@docRoot}/../tutorial/basics.htm#Convention">tutorial</a>
		 * for details.
		 * 
		 * @throws CompilerException
		 * 
		 * @see #bindAllMethodsToNamedCells()
		 * @see #bindAllPrefixedNamedCellsToMethods(String)
		 */
		public void bindAllNamedCellsToMethods() throws CompilerException;

		/**
		 * Binds all still unbound named cells with a given name prefix to methods of the
		 * corresponding name. It is an error if no suitable method can be found. Typically invoked
		 * for input cells (see
		 * {@link org.formulacompiler.spreadsheet.SpreadsheetByNameBinder#inputs()}).
		 * 
		 * @param _prefix is what all cell names to be considered must start with. A cell named
		 *           "prefixXY" must have a method named either {@code xy()} or {@code getXy()}. The
		 *           method name search is not case-sensitive.
		 * 
		 * <p>
		 * See the <a target="_top" href="{@docRoot}/../tutorial/basics.htm#Convention">tutorial</a>
		 * for details.
		 * 
		 * @throws CompilerException
		 * 
		 * @see #bindAllNamedCellsToMethods()
		 */
		public void bindAllPrefixedNamedCellsToMethods( String _prefix ) throws CompilerException;

		/**
		 * Raises an exception if there are named cells with the given prefix that were not bound to
		 * the type (input/output) of this cell binder.
		 * 
		 * @param _prefix selects the cell names to check.
		 * 
		 * @throws SpreadsheetException.NameNotFound if there is an unbound name.
		 */
		public void failIfCellNamesAreStillUnbound( String _prefix ) throws CompilerException;

	}


	/**
	 * Factory interface for
	 * {@link org.formulacompiler.runtime.ImplementationLocator#getInstance(Class)}.
	 */
	public static interface Factory
	{
		/**
		 * Factory method.
		 */
		SpreadsheetByNameBinder newInstance( Config _config );
	}

}
