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

package org.formulacompiler.runtime;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.formulacompiler.runtime.EngineLoader.Config;


/**
 * Static class defining factory methods for run-time-only elements of AFC. This class is extended
 * by {@link org.formulacompiler.spreadsheet.SpreadsheetCompiler} which provides factory methods for
 * compile-time elements.
 * 
 * @see org.formulacompiler.compiler.FormulaCompiler
 * @see org.formulacompiler.spreadsheet.SpreadsheetCompiler
 * 
 * @author peo
 */
public class FormulaRuntime
{

	/**
	 * For BigDecimal types, indicates that no explicit scaling should be performed by the engine.
	 */
	public static final int UNDEFINED_SCALE = Integer.MAX_VALUE;

	/**
	 * Not supposed to be instantiated!
	 */
	protected FormulaRuntime()
	{
		super();
	}

	/**
	 * Returns a new engine deserialized by a registered engine loader (see {@code register()}) - it
	 * must have been saved using
	 * {@link org.formulacompiler.compiler.SaveableEngine#saveTo(java.io.OutputStream)}.
	 * 
	 * @param _stream is an input stream. If it does not support the {@link InputStream#mark(int)}
	 *           operation, it is automatically wrapped within a {@link BufferedInputStream}.
	 * @return The loaded engine.
	 * 
	 * @throws IOException
	 * @throws EngineException
	 * 
	 * @see #loadEngine(org.formulacompiler.runtime.EngineLoader.Config, InputStream)
	 */
	public static Engine loadEngine( InputStream _stream ) throws IOException, EngineException
	{
		return loadEngine( new EngineLoader.Config(), _stream );
	}

	/**
	 * Like {@link #loadEngine(InputStream)}, but with full configuration options.
	 * 
	 * @param _config is the engine loader configuration block.
	 * @param _stream is an input stream. If it does not support the {@link InputStream#mark(int)}
	 *           operation, it is automatically wrapped within a {@link BufferedInputStream}.
	 * @return The loaded engine.
	 * 
	 * @throws IOException
	 * @throws EngineException
	 * 
	 * @see #loadEngine(InputStream)
	 */
	public static Engine loadEngine( EngineLoader.Config _config, InputStream _stream ) throws IOException,
			EngineException
	{
		InputStream input = _stream;
		if (!_stream.markSupported()) {
			input = new BufferedInputStream( input );
		}
		assert input.markSupported();

		return ENGINE_LOADER_FACTORY.newInstance( _config ).loadEngineData( _stream );
	}

	private static final EngineLoader.Factory ENGINE_LOADER_FACTORY = getLoaderFactory();

	private static final EngineLoader.Factory getLoaderFactory()
	{
		try {
			return ImplementationLocator.getInstance( EngineLoader.Factory.class );
		}
		catch (ImplementationLocator.ConfigurationMissingException e) {
			return new EngineLoader.Factory()
			{
				public EngineLoader newInstance( Config _config )
				{
					return new EngineLoader()
					{
						public Engine loadEngineData( InputStream _stream ) throws EngineException
						{
							throw new EngineException( "No engine loader configured." );
						}
					};
				}
			};
		}
	}

}
