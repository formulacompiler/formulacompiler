package sej.internal.spreadsheet.loader.excel;

import java.io.StringReader;

import sej.internal.expressions.ExpressionNode;

public final class RewriteLanguageParser
{

	public static final ExpressionNode parse( String _expr ) throws Exception
	{
		final GeneratedScannerInternal scanner = new GeneratedScannerInternal( new StringReader( _expr ) );
		scanner.setSource( _expr );

		final GeneratedParser parser = new GeneratedParser( scanner );
		parser.excelParser = new ExcelExpressionParser( null );
		parser.parse();

		return parser.rootNode;
	}

}
