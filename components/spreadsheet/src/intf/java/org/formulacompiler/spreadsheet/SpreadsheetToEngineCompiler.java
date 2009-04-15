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

import java.lang.reflect.Method;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.FormulaCompiler;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.compiler.Validation;
import org.formulacompiler.runtime.ComputationMode;
import org.formulacompiler.runtime.EngineException;


/**
 * Lets you compile a bound spreadsheet to a Java byte-code computation engine. The compiled engine
 * can then be used immediately or saved to persistent storage for later use.
 * <p>
 * <em>This interface is an API only. Do not implement it yourself.</em>
 * 
 * @author peo
 * 
 * @see EngineBuilder
 */
public interface SpreadsheetToEngineCompiler
{

	/**
	 * Configuration data for new instances of
	 * {@link org.formulacompiler.spreadsheet.SpreadsheetToEngineCompiler}.
	 * 
	 * @author peo
	 * 
	 * @see SpreadsheetCompiler#newSpreadsheetCompiler(org.formulacompiler.spreadsheet.SpreadsheetToEngineCompiler.Config)
	 */
	public static class Config
	{

		/**
		 * The spreadsheet binding to use as input, which also identifies the spreadsheet to use.
		 */
		public SpreadsheetBinding binding;

		/**
		 * The numeric type to use for all internal computations.
		 */
		public NumericType numericType = FormulaCompiler.DEFAULT_NUMERIC_TYPE;


		/**
		 * Tells AFC to calculate expressions as for example Excel or OpenOffice does.
		 */
		public ComputationMode computationMode = null;

		/**
		 * Specifies either a class from which to descend the generated computation factory, or an
		 * interface which the generated factory should implement. Can be left {@code null}. If set,
		 * must be {@code public}, and have at most a single abstract method, which must then be the
		 * {@link #factoryMethod}.
		 */
		public Class factoryClass = null;

		/**
		 * The method of the {@link #factoryClass} which AFC should implement to return new
		 * computation instances. Must be specified if and only if a factory class is specified,
		 * otherwise leave it {@code null}. Must be {@code public}, and have a single parameter of
		 * the {@link org.formulacompiler.spreadsheet.SpreadsheetBinder.Config#inputClass} of the
		 * spreadsheet binding, and return the
		 * {@link org.formulacompiler.spreadsheet.SpreadsheetBinder.Config#outputClass} of the
		 * spreadsheet binding.
		 */
		public Method factoryMethod = null;

		/**
		 * Controls whether AFC should compile the computation with full internal caching of values,
		 * or only (usually minimal) caching at its own discretion.
		 */
		public boolean fullCaching;

		/**
		 * The parent class loader to use for the compiled engine.
		 */
		public ClassLoader parentClassLoader = ClassLoader.getSystemClassLoader();

		/**
		 * Controls whether AFC should attempt to compile more readable code (when decompiled),
		 * possibly at the expense of engine size and performance.
		 */
		public boolean compileToReadableCode = false;

		/**
		 * Allows logging intermediate cell values.
		 */
		public boolean computationListenerEnabled = false;

		/**
		 * Allows to receive notifications about events during compilation process.
		 */
		public ConstantExpressionOptimizationListener constantExpressionOptimizationListener = null;

		/**
		 * Validates the configuration.
		 * 
		 * @throws IllegalArgumentException
		 */
		public void validate()
		{
			if (this.binding == null) throw new IllegalArgumentException( "binding is null" );
			if (this.numericType == null) throw new IllegalArgumentException( "numericType is null" );

			Validation.SINGLETON.validateFactory( this.factoryClass, this.factoryMethod, this.binding.getInputClass(),
					this.binding.getOutputClass() );
		}
	}


	/**
	 * Compiles the engine.
	 * 
	 * @return the compiled engine, ready to be used immediately, or saved to persistent storage for
	 *         later use.
	 * 
	 * @throws CompilerException
	 * @throws EngineException
	 */
	public SaveableEngine compile() throws CompilerException, EngineException;


	/**
	 * Factory interface for
	 * {@link org.formulacompiler.runtime.ImplementationLocator#getInstance(Class)}.
	 */
	public static interface Factory
	{
		/**
		 * Factory method.
		 */
		SpreadsheetToEngineCompiler newInstance( Config _config );
	}

}
