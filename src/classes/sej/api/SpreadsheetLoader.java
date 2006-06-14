package sej.api;

import java.io.IOException;
import java.io.InputStream;

public interface SpreadsheetLoader
{
	public Spreadsheet loadFrom( InputStream _stream ) throws IOException, SpreadsheetError;
}
