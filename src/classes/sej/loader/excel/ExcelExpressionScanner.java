package sej.loader.excel;

import java_cup.runtime.Scanner;


public interface ExcelExpressionScanner extends Scanner
{
	int charsRead();
	void setSource( String _source );
}
