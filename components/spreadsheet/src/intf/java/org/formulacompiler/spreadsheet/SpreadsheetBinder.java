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

import java.lang.reflect.Method;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.Validation;
import org.formulacompiler.runtime.Computation;

/**
 * Defines the bindings of spreadsheet cells and sections to Java elements.
 * <p>
 * <em>This interface is an API only. Do not implement it yourself.</em>
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
	 * Checks if a given cell already is bound to an input method. Cells may not be bound to multiple
	 * inputs.
	 * 
	 * @param _cell is the cell to check.
	 * @return {@code true} if the cell is already bound to an input method, {@code false} otherwise.
	 * 
	 * @see #isOutputCell(Spreadsheet.Cell)
	 */
	public boolean isInputCell( Spreadsheet.Cell _cell );

	/**
	 * Checks if a given cell already is bound to an output method.
	 * 
	 * @param _cell is the cell to check.
	 * @return {@code true} if the cell is already bound to an output method, {@code false}
	 *         otherwise.
	 * 
	 * @see #isInputCell(Spreadsheet.Cell)
	 */
	public boolean isOutputCell( Spreadsheet.Cell _cell );


	/**
	 * Represents both the container for the definitions of global spreadsheet cells and instances of
	 * horizontal or vertical sections within a spreadsheet. See the tutorial for details on <a
	 * href="{@docRoot}/../tutorial/index.htm#RepeatingSections">sections</a>.
	 * <p>
	 * <em>This interface is an API only. Do not implement it yourself.</em>
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
		 * Like {@link #defineInputCell(org.formulacompiler.spreadsheet.Spreadsheet.Cell, CallFrame)},
		 * but constructs the {@link CallFrame} directly.
		 * 
		 * @see #defineInputCell(org.formulacompiler.spreadsheet.Spreadsheet.Cell, CallFrame)
		 * @see SpreadsheetCompiler#newCallFrame(Method, Object...)
		 */
		public void defineInputCell( Spreadsheet.Cell _cell, Method _methodToCall, Object... _args )
				throws CompilerException;

		/**
		 * Like {@link #defineInputCell(org.formulacompiler.spreadsheet.Spreadsheet.Cell, CallFrame)},
		 * but constructs the {@link CallFrame} directly from a method looked up by name on
		 * {@link #getInputClass()}.
		 * 
		 * @see #defineInputCell(org.formulacompiler.spreadsheet.Spreadsheet.Cell, CallFrame)
		 * @see #getInputClass()
		 * @see SpreadsheetCompiler#newCallFrame(Method, Object...)
		 */
		public void defineInputCell( Spreadsheet.Cell _cell, String _nameOfMethodToCall ) throws CompilerException,
				NoSuchMethodException;


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
		 *           to the output class of this section. If the call is parametrized, the compiler
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
		 * Like {@link #defineOutputCell(org.formulacompiler.spreadsheet.Spreadsheet.Cell, CallFrame)},
		 * but constructs the {@link CallFrame} directly.
		 * 
		 * @see #defineOutputCell(org.formulacompiler.spreadsheet.Spreadsheet.Cell, CallFrame)
		 * @see SpreadsheetCompiler#newCallFrame(Method, Object...)
		 */
		public void defineOutputCell( Spreadsheet.Cell _cell, Method _methodToImplement, Object... _args )
				throws CompilerException;

		/**
		 * Like {@link #defineOutputCell(org.formulacompiler.spreadsheet.Spreadsheet.Cell, CallFrame)},
		 * but constructs the {@link CallFrame} directly from a method looked up by name on
		 * {@link #getOutputClass()}.
		 * 
		 * @see #defineOutputCell(org.formulacompiler.spreadsheet.Spreadsheet.Cell, CallFrame)
		 * @see #getOutputClass()
		 * @see SpreadsheetCompiler#newCallFrame(Method, Object...)
		 */
		public void defineOutputCell( Spreadsheet.Cell _cell, String _nameOfMethodToImplement ) throws CompilerException,
				NoSuchMethodException;


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
		 * @param _outputCallReturningIterableToImplement is implemented on the output class to
		 *           iterate the elements of the repeating section and compute output values for them.
		 *           Can be {@code null}.
		 * @param _outputClass is the output type of the returned section.
		 * @return A new section for the repeated rows (or columns). The input and output interfaces
		 *         are automatically inferred from the arguments above.
		 * 
		 * @see Spreadsheet#getRange(String)
		 */
		public Section defineRepeatingSection( Spreadsheet.Range _range, Orientation _orientation,
				CallFrame _inputCallChainReturningIterable, Class _inputClass,
				CallFrame _outputCallReturningIterableToImplement, Class _outputClass ) throws CompilerException;

		/**
		 * Like
		 * {@link #defineRepeatingSection(org.formulacompiler.spreadsheet.Spreadsheet.Range, Orientation, CallFrame, Class, CallFrame, Class)},
		 * but constructs the {@link CallFrame} instances directly.
		 */
		public Section defineRepeatingSection( Spreadsheet.Range _range, Orientation _orientation,
				Method _inputMethodReturningIterable, Class _inputClass, Method _outputMethodReturningIterableToImplement,
				Class _outputClass ) throws CompilerException;

		/**
		 * Like
		 * {@link #defineRepeatingSection(org.formulacompiler.spreadsheet.Spreadsheet.Range, Orientation, CallFrame, Class, CallFrame, Class)},
		 * but constructs the {@link CallFrame} instances directly.
		 */
		public Section defineRepeatingSection( Spreadsheet.Range _range, Orientation _orientation,
				String _nameOfInputMethodReturningIterable, Class _inputClass,
				String _nameOfOutputMethodReturningIterableToImplement, Class _outputClass ) throws CompilerException,
				NoSuchMethodException;

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
