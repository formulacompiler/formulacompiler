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

import java.lang.reflect.Method;

import sej.runtime.EngineException;


/**
 * Lets you compile a bound spreadsheet to a Java byte-code computation engine. The compiled engine
 * can then be used immediately or saved to persistent storage for later use.
 * 
 * @author peo
 * 
 * @see EngineBuilder
 */
public interface SpreadsheetCompiler
{

	/**
	 * Configuration data for new instances of {@link sej.SpreadsheetCompiler}.
	 * 
	 * @author peo
	 * 
	 * @see SEJ#newSpreadsheetCompiler(sej.SpreadsheetCompiler.Config)
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
		public NumericType numericType = SEJ.DEFAULT_NUMERIC_TYPE;

		/**
		 * Specifies either a class from which to descend the generated computation factory, or an
		 * interface which the generated factory should implement. Can be left {@code null}. If set,
		 * must be {@code public}, and have at most a single abstract method, which must then be the
		 * {@link #factoryMethod}.
		 */
		public Class factoryClass = null;

		/**
		 * The method of the {@link #factoryClass} which SEJ should implement to return new
		 * computation instances. Must be specified if and only if a factory class is specified,
		 * otherwise leave it {@code null}. Must be {@code public}, and have a single parameter of
		 * the {@link sej.SpreadsheetBinder.Config#inputClass} of the spreadsheet binding, and return
		 * the {@link sej.SpreadsheetBinder.Config#outputClass} of the spreadsheet binding.
		 */
		public Method factoryMethod = null;

		/**
		 * Validates the configuration.
		 * 
		 * @throws IllegalArgumentException
		 */
		public void validate()
		{
			if (this.binding == null) throw new IllegalArgumentException( "binding is null" );
			if (this.numericType == null) throw new IllegalArgumentException( "numericType is null" );

			SEJ.validateFactory( this.factoryClass, this.factoryMethod, this.binding.getInputClass(), this.binding
					.getOutputClass() );
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


}
