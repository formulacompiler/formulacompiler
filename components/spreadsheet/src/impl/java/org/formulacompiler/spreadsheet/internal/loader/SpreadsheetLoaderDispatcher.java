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

	private SpreadsheetLoaderDispatcher( Config _config )
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
