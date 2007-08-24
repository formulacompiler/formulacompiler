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

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.Validation;
import org.formulacompiler.runtime.Computation;

/**
 * Defines the bindings of spreadsheet cells and sections to Java elements.
 * 
 * @author peo
 * 
 * @see EngineBuilder#getRootBinder()
 * @see SpreadsheetCompiler#newSpreadsheetBinder(Spreadsheet, Class, Class)
 */
public interface SpreadsheetBinder
{


	/**
	 * Configuration data for new instances of
	 * {@link org.formulacompiler.spreadsheet.SpreadsheetBinder}.
	 * 
	 * @author peo
	 * 
	 * @see SpreadsheetCompiler#newSpreadsheetBinder(org.formulacompiler.spreadsheet.SpreadsheetBinder.Config)
	 */
	public static class Config
	{

		/**
		 * The spreadsheet whose elements you want to bind; must not be {@code null}.
		 */
		public Spreadsheet spreadsheet;

		/**
		 * The class of the input type to whose methods you want to bind input elements; must not be
		 * {@code null}.
		 */
		public Class inputClass;

		/**
		 * The class of the output type whose methods you want to bind to output elements; must not be
		 * {@code null}.
		 */
		public Class outputClass;

		/**
		 * The compile-time configuration to use; may be left {@code null}.
		 * <p>
		 * Please refer to the <a target="_top" href="{@docRoot}/../tutorial/locale.htm#compile"
		 * target="_top">tutorial</a> for details.
		 * </p>
		 */
		public Computation.Config compileTimeConfig;

		/**
		 * Validates the configuration for missing or improperly set values.
		 * 
		 * @throws IllegalArgumentException
		 */
		public void validate()
		{
			if (this.spreadsheet == null) throw new IllegalArgumentException( "spreadsheet is null" );
			if (this.inputClass == null) throw new IllegalArgumentException( "inputClass is null" );
			if (this.outputClass == null) throw new IllegalArgumentException( "outputClass is null" );

			Validation.SINGLETON.validateIsAccessible( this.inputClass, "inputClass" );
			Validation.SINGLETON.validateIsImplementable( this.outputClass, "outputClass" );
		}
	}


	/**
	 * Returns the spreadsheet model of which elements are bound.
	 * 
	 * @return The spreadsheet model.
	 */
	public Spreadsheet getSpreadsheet();


	/**
	 * Finalizes and returns the binding built by this class.
	 * 
	 * @return The finalized binding.
	 * @throws CompilerException
	 */
	public SpreadsheetBinding getBinding() throws CompilerException;


	/**
	 * Returns the root section, which represents the entire worksheet.
	 * 
	 * @return The root section. Is never <code>null</code>.
	 */
	public Section getRoot();


	/**
	 * Represents both the container for the definitions of global spreadsheet cells and instances of
	 * horizontal or vertical sections within a spreadsheet. See the tutorial for details on <a
	 * href="{@docRoot}/../tutorial/binding.htm#BindRepeatingSections">sections</a>.
	 * 
	 * @author peo
	 * 
	 * @see SpreadsheetBinder#getRoot()
	 * @see EngineBuilder#getRootBinder()
	 */
	public static interface Section
	{

		/**
		 * The class of the input type to whose methods you are binding input elements.
		 */
		public Class getInputClass();

		/**
		 * The class of the output type whose methods you are binding to output elements.
		 */
		public Class getOutputClass();


		/**
		 * Marks a given spreadsheet cell as a variable input to the constructed engine and binds it
		 * to the given method call chain.
		 * 
		 * @param _cell is a spreadsheet cell whose value, rather than being assumed constant, will be
		 *           defined later, when a specific engine computation is run. The cell is thus like a
		 *           parameter to a Java method.
		 * @param _callChainToCall will be called during computations to obtain the value of the input
		 *           cell. The type of the cell is inferred from the return type of the last method in
		 *           the chain. The head of the chain must be callable on the input class of this name
		 *           space.
		 * @throws CompilerException
		 * 
		 * @see #defineOutputCell(Spreadsheet.Cell, CallFrame)
		 * @see Spreadsheet#getCell(int, int, int)
		 * @see Spreadsheet#getCell(String)
		 */
		public void defineInputCell( Spreadsheet.Cell _cell, CallFrame _callChainToCall ) throws CompilerException;


		/**
		 * Marks a given spreadsheet cell as a computable output of the constructed engine and binds
		 * it to the given call. The call will be implemented by the constructed engine to compute the
		 * formula in the designated cell.
		 * 
		 * @param _cell is a spreadsheet cell whose valuewill be computed later, when a specific
		 *           engine computation is run. The cell is thus like a return value of a Java method.
		 * @param _callToImplement is what you call on a computation to obtain the actual value of the
		 *           output cell in a specific computation. The type of the cell is inferred from the
		 *           return type of the call. The call may not be a chain. The call must be applicable
		 *           to the input class of this section. If the call is parametrized, the compiler
		 *           generates internal lookup code that maps the given argument values to the given
		 *           output cell. Other argument values can be bound to other cells. Unbound values
		 *           will call the inherited implementation (which must not be abstract).
		 * 
		 * @see #defineInputCell(Spreadsheet.Cell, CallFrame)
		 * @see Spreadsheet#getCell(int, int, int)
		 * @see Spreadsheet#getCell(String)
		 */
		public void defineOutputCell( Spreadsheet.Cell _cell, CallFrame _callToImplement ) throws CompilerException;


		/**
		 * Defines a range in the spreadsheet as a section of similar, repeating rows (or columns). At
		 * computation time, the section's effective height (or width) is determined by calling an
		 * iterator on your input interface.
		 * 
		 * <p>
		 * The compiler uses the first row (or column) of the range as a template for all other rows
		 * (or columns). It does not currently check that all rows (or columns) are in fact similar in
		 * structure in the model spreadsheet. The compiler does not support auto-extending constant
		 * series, for example the series 1, 2, 3 ... n.
		 * 
		 * @param _range is the range whose rows (or columns) should be repeated.
		 * @param _orientation indicates the orientation of the variable extent. Horizontal means
		 *           repeating columns, vertical means repeating rows.
		 * @param _inputCallChainReturningIterable gets called on a computation's input to return an
		 *           iterable for the elements that the repeating section should effectively have.
		 * @param _inputClass is the input type of the returned section.
		 * @param _outputCallToImplementIterable is implemented on the output class to iterate the
		 *           elements of the repeating section and compute output values for them.
		 * @param _outputClass is the output type of the returned section.
		 * @return A new section for the repeated rows (or columns). The input and output interfaces
		 *         are automatically inferred from the arguments above.
		 * 
		 * @see Spreadsheet#getRange(String)
		 */
		public Section defineRepeatingSection( Spreadsheet.Range _range, Orientation _orientation,
				CallFrame _inputCallChainReturningIterable, Class _inputClass, CallFrame _outputCallToImplementIterable,
				Class _outputClass ) throws CompilerException;


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
		SpreadsheetBinder newInstance( Config _config );
	}

}
