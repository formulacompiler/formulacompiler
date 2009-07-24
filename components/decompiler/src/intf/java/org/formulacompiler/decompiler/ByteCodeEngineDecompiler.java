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

package org.formulacompiler.decompiler;

import java.io.IOException;

import org.formulacompiler.runtime.Engine;


/**
 * Decompiles a JVM byte code engine back to Java source using the <a href="jode.sourceforge.net"
 * target="_top">JODE</a> library.
 * <p>
 * <em>This interface is an API only. Do not implement it yourself.</em>
 * 
 * @author peo
 */
public interface ByteCodeEngineDecompiler
{

	/**
	 * Configuration data for new instances of
	 * {@link org.formulacompiler.decompiler.ByteCodeEngineDecompiler}.
	 * 
	 * @author peo
	 * 
	 * @see FormulaDecompiler#decompile(Engine)
	 */
	public static class Config
	{

		/**
		 * The engine to decompile.
		 */
		public Engine engine;

		/**
		 * Validates the configuration for missing or improperly set values.
		 * 
		 * @throws IllegalArgumentException
		 */
		public void validate()
		{
			if (this.engine == null) throw new IllegalArgumentException( "engine is null" );
		}
	}


	/**
	 * Decompiles the engine and returns a source code description object.
	 * 
	 * @throws IOException
	 */
	public abstract ByteCodeEngineSource decompile() throws IOException;

	/**
	 * Factory interface for
	 * {@link org.formulacompiler.runtime.ImplementationLocator#getInstance(Class)}.
	 */
	public static interface Factory
	{
		/**
		 * Factory method.
		 */
		public ByteCodeEngineDecompiler newInstance( Config _config );
	}

}
