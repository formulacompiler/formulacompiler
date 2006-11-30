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
package sej.internal.spreadsheet.loader.excel;

import java.io.StringReader;

import sej.internal.Settings;
import sej.internal.expressions.ExpressionNode;

public final class RewriteLanguageParser
{

	public static final ExpressionNode parse( String _expr ) throws Exception
	{
		final GeneratedScannerInternal scanner = new GeneratedScannerInternal( new StringReader( _expr ) );
		scanner.setSource( _expr );

		final GeneratedParser parser = new GeneratedParser( scanner );
		parser.excelParser = new ExcelExpressionParser( null );

		try {
			if (Settings.isDebugParserEnabled()) {
				parser.debug_parse();
			}
			else {
				parser.parse();
			}
		}
		catch (ExcelExpressionParserError e) {
			throw e;
		}
		catch (Exception e) {
			throw new ExcelExpressionParserError( e, _expr, scanner.charsRead() );
		}
		
		return parser.rootNode;
	}

}
