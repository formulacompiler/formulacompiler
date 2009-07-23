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

package org.formulacompiler.runtime;

import java.io.IOException;
import java.io.InputStream;


/**
 * Interface for deserializing instances of the {@link Engine} class.
 * <p>
 * <em>This interface is an API only. Do not implement it yourself.</em>
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
		public ClassLoader parentClassLoader = Thread.currentThread().getContextClassLoader();

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
