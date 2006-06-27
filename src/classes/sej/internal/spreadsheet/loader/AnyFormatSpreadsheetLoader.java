/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
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
package sej.internal.spreadsheet.loader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import sej.Spreadsheet;
import sej.SpreadsheetException;
import sej.internal.spreadsheet.loader.excel.xls.ExcelXLSLoader;

/**
 * Central dispatcher for the loaders for the various spreadsheet file formats supported by SEJ. You
 * must first register the loaders you want to be active using their {@code register()} method.
 * 
 * @author peo
 */
public final class AnyFormatSpreadsheetLoader
{
	private static Collection<Factory> factories = new ArrayList<Factory>();
	
	static {
		ExcelXLSLoader.register();
	}


	/**
	 * Registers a loader for a particular spreadsheet file format. You should not use this method
	 * directly. Rather, use the {@code register()} method of the loaders themselves (which will then
	 * call this method).
	 * 
	 * @param _factory is a factory for a spreadsheet file loader.
	 */
	public static void registerLoader( Factory _factory )
	{
		if (!factories.contains( _factory )) {
			factories.add( _factory );
		}
	}


	/**
	 * Loads a spreadsheet stream into an SEJ spreadsheet model. The loader to use is determined by
	 * giving each registered loader a look at the file name. The first one that signals it can
	 * handle it is used.
	 * 
	 * @param _originalFileName is the complete file name of the original spreadsheet file (for
	 *           example Test.xls or Test.xml).
	 * @return The spreadsheet model loaded from the file.
	 * @throws IOException when there is any proplem accessing the file. May also throw runtime
	 *            exceptions when there are problems in file.
	 */
	public static Spreadsheet loadSpreadsheet( String _originalFileName, InputStream _stream ) throws IOException,
			SpreadsheetException
	{
		for (Factory factory : factories) {
			if (factory.canHandle( _originalFileName )) {
				SpreadsheetLoader loader = factory.newWorkbookLoader();
				return loader.loadFrom( _stream );
			}
		}
		throw new IllegalStateException( "No loader found for file " + _originalFileName );
	}


	/**
	 * Interface that must be implemented by spreadsheet file loader factories to be able to
	 * participate in the central dispatching by {@link SpreadsheetLoader}.
	 * 
	 * @author peo
	 * 
	 */
	public static interface Factory
	{

		public SpreadsheetLoader newWorkbookLoader();

		public boolean canHandle( String _fileName );

	}


}
