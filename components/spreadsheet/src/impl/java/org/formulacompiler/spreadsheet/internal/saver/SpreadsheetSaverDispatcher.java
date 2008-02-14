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

package org.formulacompiler.spreadsheet.internal.saver;

import java.io.IOException;
import java.util.Collection;

import org.formulacompiler.runtime.ImplementationLocator;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.SpreadsheetSaver;

/**
 * Central dispatcher for the savers for the various spreadsheet file formats supported by AFC.
 */
public final class SpreadsheetSaverDispatcher implements SpreadsheetSaver
{
	private static final Collection<Factory> FACTORIES = ImplementationLocator.getInstances( Factory.class );
	private final Config config;


	public SpreadsheetSaverDispatcher( Config _config )
	{
		super();
		this.config = _config;
	}


	public void save() throws IOException, SpreadsheetException
	{
		for (Factory factory : FACTORIES) {
			if (factory.canHandle( this.config.typeExtension )) {
				final SpreadsheetSaver saver = factory.newInstance( this.config );
				saver.save();
				return;
			}
		}
		throw new SpreadsheetException.UnsupportedFormat( "No saver found for extension " + this.config.typeExtension );
	}


	/**
	 * Interface that must be implemented by spreadsheet file loader factories to be able to
	 * participate in the central dispatching by {@link SpreadsheetSaverDispatcher}.
	 */
	public static interface Factory extends SpreadsheetSaver.Factory
	{
		public boolean canHandle( String _fileName );
	}


	public static final class FactoryImpl implements SpreadsheetSaver.Factory
	{
		public SpreadsheetSaver newInstance( Config _config )
		{
			return new SpreadsheetSaverDispatcher( _config );
		}
	}

}
