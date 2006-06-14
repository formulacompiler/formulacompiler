package sej.internal.spreadsheet.loader;

import java.io.IOException;
import java.io.InputStream;

import sej.Spreadsheet;
import sej.SpreadsheetError;

public interface SpreadsheetLoader
{
	public Spreadsheet loadFrom( InputStream _stream ) throws IOException, SpreadsheetError;
}
