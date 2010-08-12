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

package org.formulacompiler.spreadsheet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TimeZone;


/**
 * This interface allows you to save a spreadsheet representation. Used of save spreadsheet models
 * constructed in memory.
 * <p>
 * See the <a target="_top" href="{@docRoot}/../tutorial/generatesheet.htm" target="_top">tutorial</a>
 * for details.
 * <p>
 * <em>This interface is an API only. Do not implement it yourself.</em>
 * 
 * @author peo
 * 
 * @see SpreadsheetCompiler#newSpreadsheetSaver(org.formulacompiler.spreadsheet.SpreadsheetSaver.Config)
 */
public interface SpreadsheetSaver
{

	/**
	 * Configuration data for new instances of
	 * {@link org.formulacompiler.spreadsheet.SpreadsheetSaver}.
	 * 
	 * @author peo
	 * 
	 * @see SpreadsheetCompiler#newSpreadsheetSaver(org.formulacompiler.spreadsheet.SpreadsheetSaver.Config)
	 */
	public static class Config
	{

		/**
		 * Mandatory internal spreadsheet model that should be written out as a real spreadsheet file.
		 * Normally constructed using a {@link org.formulacompiler.spreadsheet.SpreadsheetBuilder}.
		 */
		public Spreadsheet spreadsheet;

		/**
		 * Mandatory file name extension of the format in which the spreadsheet should be written
		 * (.xls, .xml, .xsd, etc.).
		 */
		public String typeExtension;

		/**
		 * Mandatory stream to which the spreadsheet will be written.
		 */
		public OutputStream outputStream;

		/**
		 * Optional stream from which to load the template spreadsheet, or {@code null}.
		 */
		public InputStream templateInputStream;

		/**
		 * Optional time zone to use for converting date constants, or {@code null} which signifies to
		 * use the JRE default time zone.
		 * <p>
		 * See the <a target="_top" href="{@docRoot}/../tutorial/locale.htm#save"
		 * target="_top">tutorial</a> for details.
		 */
		public TimeZone timeZone;

		/**
		 * Validates the configuration.
		 * 
		 * @throws IllegalArgumentException
		 */
		public void validate()
		{
			if (this.spreadsheet == null) throw new IllegalArgumentException( "spreadsheet is null" );
			if (this.outputStream == null) throw new IllegalArgumentException( "outputStream is null" );
			if (this.typeExtension == null) throw new IllegalArgumentException( "typeExtension is null" );
		}

	}


	/**
	 * Saves the configured model to the output stream.
	 */
	public void save() throws IOException, SpreadsheetException;


	/**
	 * Factory interface for
	 * {@link org.formulacompiler.runtime.ImplementationLocator#getInstance(Class)}.
	 */
	public static interface Factory
	{
		/**
		 * Factory method.
		 */
		SpreadsheetSaver newInstance( Config _config );
	}

}
