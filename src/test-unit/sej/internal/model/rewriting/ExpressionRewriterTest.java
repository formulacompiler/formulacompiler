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
package sej.internal.model.rewriting;

import sej.internal.expressions.ExpressionNode;
import sej.internal.spreadsheet.CellRefFormat;
import sej.internal.spreadsheet.CellWithLazilyParsedExpression;
import sej.internal.spreadsheet.RowImpl;
import sej.internal.spreadsheet.SheetImpl;
import sej.internal.spreadsheet.SpreadsheetImpl;
import sej.internal.spreadsheet.loader.excel.ExcelExpressionParserAccessor;
import junit.framework.TestCase;


public class ExpressionRewriterTest extends TestCase
{


	public void testSUM() throws Exception
	{
		assertRewrite( "_FOLDL( acc: 0.0; xi: (`acc + `xi); ( A1:A2 ) )", "SUM( A1:A2 )" );
	}

	public void testAVERAGE() throws Exception
	{
		assertRewrite( "(_FOLDL( acc: 0.0; xi: (`acc + `xi); ( A1:A2 ) ) / COUNT( ( A1:A2 ) ))", "AVERAGE( A1:A2 )" );
	}

	public void testVARP() throws Exception
	{
		assertRewrite(
				"_LET( c: COUNT( ( A1:A2 ) ); (_LET( m: (_FOLDL( acc: 0.0; xi: (`acc + `xi); ( A1:A2 ) ) / `c); _FOLDL( acc: 0.0; xi: _LET( ei: (`xi - `m); (`acc + (`ei * `ei)) ); ( A1:A2 ) ) ) / `c) )",
				"VARP( A1:A2 )" );
	}


	private void assertRewrite( String _rewritten, String _original ) throws Exception
	{
		SpreadsheetImpl ss = new SpreadsheetImpl();
		SheetImpl s = new SheetImpl( ss );
		RowImpl r = new RowImpl( s );
		CellWithLazilyParsedExpression c = new CellWithLazilyParsedExpression( r );
		ExcelExpressionParserAccessor p = new ExcelExpressionParserAccessor( c );
		ExpressionNode e = p.parseText( _original, CellRefFormat.A1 );
		ExpressionRewriter rw = new ExpressionRewriter();
		ExpressionNode re = rw.rewrite( e );

		assertEquals( _rewritten, re.toString() );
	}


}
