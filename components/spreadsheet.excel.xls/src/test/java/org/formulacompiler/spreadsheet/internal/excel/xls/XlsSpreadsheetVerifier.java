package org.formulacompiler.spreadsheet.internal.excel.xls;

import java.io.InputStream;

import org.formulacompiler.tests.utils.SpreadsheetVerifier;

public class XlsSpreadsheetVerifier implements SpreadsheetVerifier
{
	public void verify( final InputStream _odsInputStream ) throws Exception
	{
		//LATER add verification code
	}

	public static final class Factory implements SpreadsheetVerifier.Factory
	{
		public SpreadsheetVerifier getInstance()
		{
			return new XlsSpreadsheetVerifier();
		}

		public boolean canHandle( String _fileExtension )
		{
			return _fileExtension.equalsIgnoreCase( ".xls" );
		}
	}
}
