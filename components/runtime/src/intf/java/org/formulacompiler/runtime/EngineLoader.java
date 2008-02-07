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
package org.formulacompiler.runtime;

import java.io.IOException;
import java.io.InputStream;


/**
 * Interface for deserializing instances of the {@link Engine} class.
 * 
 * @author peo
 */
public interface EngineLoader
{

	/**
	 * Configuration data for new instances of {@link EngineLoader}.
	 * 
	 * @author peo
	 * 
	 * @see FormulaRuntime#loadEngine(org.formulacompiler.runtime.EngineLoader.Config, InputStream)
	 */
	public static class Config
	{

		/**
		 * The parent class loader to use for the loaded engine.
		 */
		public ClassLoader parentClassLoader = ClassLoader.getSystemClassLoader();

		/**
		 * Validates the configuration for missing or improperly set values.
		 * 
		 * @throws IllegalArgumentException
		 */
		public void validate()
		{
			// no validation so far
		}
	}


	/**
	 * Loads an engine. It must have been saved using
	 * {@link org.formulacompiler.compiler.SaveableEngine#saveTo(java.io.OutputStream)}.
	 * 
	 * @param _stream is an input stream which must support the {@link InputStream#mark(int)}
	 *           operation.
	 * @return The loaded engine.
	 * 
	 * @throws IOException
	 * @throws EngineException
	 */
	public Engine loadEngineData( InputStream _stream ) throws IOException, EngineException;


	/**
	 * Factory interface for
	 * {@link org.formulacompiler.runtime.ImplementationLocator#getInstance(Class)}.
	 */
	public static interface Factory
	{
		/**
		 * Factory method.
		 */
		EngineLoader newInstance( Config _config );
	}

}
