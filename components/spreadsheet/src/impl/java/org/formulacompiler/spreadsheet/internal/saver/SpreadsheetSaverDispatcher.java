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
