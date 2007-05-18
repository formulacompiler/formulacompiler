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
package sej.spreadsheet;

import java.io.IOException;
import java.io.InputStream;

import sej.spreadsheet.Spreadsheet;
import sej.spreadsheet.SpreadsheetException;

/**
 * Allows loading of spreadsheets from external sources (like Excel files).
 * 
 * @see SEJ#loadSpreadsheet(String, InputStream)
 * 
 * @author peo
 */
public interface SpreadsheetLoader
{

	
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
	public Spreadsheet loadFrom( String _originalFileName, InputStream _stream ) throws IOException,
			SpreadsheetException;


	/**
	 * Factory interface for {@link sej.runtime.ImplementationLocator#getInstance(Class)}.
	 */
	public static interface Factory
	{
		/**
		 * Factory method.
		 */
		SpreadsheetLoader newInstance();
	}

}
