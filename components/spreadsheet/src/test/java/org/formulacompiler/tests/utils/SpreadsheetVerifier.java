package org.formulacompiler.tests.utils;

import java.io.InputStream;

public interface SpreadsheetVerifier
{
	public void verify( InputStream _odsInputStream ) throws Exception;

	public static interface Factory
	{
		SpreadsheetVerifier getInstance();

		boolean canHandle( String _fileExtension );
	}
}
