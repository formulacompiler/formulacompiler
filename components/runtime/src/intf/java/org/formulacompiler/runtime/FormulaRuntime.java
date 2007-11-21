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
