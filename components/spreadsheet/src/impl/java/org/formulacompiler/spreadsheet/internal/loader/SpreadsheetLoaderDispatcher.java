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
package org.formulacompiler.spreadsheet.internal.loader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.formulacompiler.runtime.ImplementationLocator;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.SpreadsheetLoader;

/**
 * Central dispatcher for the loaders for the various spreadsheet file formats supported by AFC.
 */
public final class SpreadsheetLoaderDispatcher implements SpreadsheetLoader
{
	private static final Collection<Factory> FACTORIES = ImplementationLocator.getInstances( Factory.class );
	private final Config config;

	private  SpreadsheetLoaderDispatcher( Config _config )
	{
		this.config = _config;
	}


	public Spreadsheet loadFrom( String _originalFileName, InputStream _stream ) throws IOException,
			SpreadsheetException
	{
		for (Factory factory : FACTORIES) {
			if (factory.canHandle( _originalFileName )) {
				SpreadsheetLoader loader = factory.newInstance( this.config );
				return loader.loadFrom( _originalFileName, _stream );
			}
		}
		throw new SpreadsheetException.UnsupportedFormat( "No loader found for file " + _originalFileName );
	}


	/**
	 * Interface that must be implemented by spreadsheet file loader factories to be able to
	 * participate in the central dispatching by {@link SpreadsheetLoaderDispatcher}.
	 */
	public static interface Factory extends SpreadsheetLoader.Factory
	{
		public boolean canHandle( String _fileName );
	}


	public static final class FactoryImpl implements SpreadsheetLoader.Factory
	{
		public SpreadsheetLoader newInstance( Config _config )
		{
			_config.validate();
			return new SpreadsheetLoaderDispatcher( _config );
		}
	}

}
