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
package org.formulacompiler.spreadsheet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TimeZone;


/**
 * This interface allows you to save a spreadsheet representation. Used of save spreadsheet models
 * constructed in memory.
 * 
 * <p>
 * See the <a target="_top" href="{@docRoot}/../tutorial/generatesheet.htm" target="_top">tutorial</a>
 * for details.
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
