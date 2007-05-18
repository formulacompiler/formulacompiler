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
package sej.internal.spreadsheet;

import sej.tests.utils.WorksheetBuilderWithBands;
import junit.framework.TestCase;

public class WorkbookTest extends TestCase
{

	public void testDescribe()
	{
		SpreadsheetImpl workbook = new SpreadsheetImpl();
		SheetImpl sheet = new SheetImpl( workbook );
		RowImpl r1 = new RowImpl( sheet );
		new CellWithLazilyParsedExpression( r1, null );
		new WorksheetBuilderWithBands( sheet );

		String description = workbook.describe();

		assertEquals( "<workbook>\n"
				+ "	<sheet name=\"Sheet1\">\n"
				+ "		<row>\n"
				+ "			<cell id=\"A1\">\n"
				+ "				<expr>SUM( D2:D5 )</expr>\n"
				+ "			</cell>\n"
				+ "			<cell id=\"B1\">\n"
				+ "				<value>0.5</value>\n"
				+ "			</cell>\n"
				+ "		</row>\n"
				+ "		<row>\n"
				+ "			<cell id=\"A2\">\n"
				+ "				<value>1.0</value>\n"
				+ "			</cell>\n"
				+ "			<cell id=\"B2\">\n"
				+ "				<value>2.0</value>\n"
				+ "			</cell>\n"
				+ "			<cell id=\"C2\">\n"
				+ "				<value>3.0</value>\n"
				+ "			</cell>\n"
				+ "			<cell id=\"D2\">\n"
				+ "				<expr>(SUM( A2:C2 ) * B1)</expr>\n"
				+ "			</cell>\n"
				+ "		</row>\n"
				+ "		<row>\n"
				+ "			<cell id=\"A3\">\n"
				+ "				<value>4.0</value>\n"
				+ "			</cell>\n"
				+ "			<cell id=\"B3\">\n"
				+ "				<value>5.0</value>\n"
				+ "			</cell>\n"
				+ "			<cell id=\"C3\">\n"
				+ "				<value>6.0</value>\n"
				+ "			</cell>\n"
				+ "			<cell id=\"D3\">\n"
				+ "				<expr>(SUM( A3:C3 ) * B1)</expr>\n"
				+ "			</cell>\n"
				+ "		</row>\n"
				+ "		<row>\n"
				+ "			<cell id=\"A4\">\n"
				+ "				<value>7.0</value>\n"
				+ "			</cell>\n"
				+ "			<cell id=\"B4\">\n"
				+ "				<value>8.0</value>\n"
				+ "			</cell>\n"
				+ "			<cell id=\"C4\">\n"
				+ "				<value>9.0</value>\n"
				+ "			</cell>\n"
				+ "			<cell id=\"D4\">\n"
				+ "				<expr>(SUM( A4:C4 ) * B1)</expr>\n"
				+ "			</cell>\n"
				+ "		</row>\n"
				+ "		<row>\n"
				+ "			<cell id=\"A5\">\n"
				+ "				<value>10.0</value>\n"
				+ "			</cell>\n"
				+ "			<cell id=\"B5\">\n"
				+ "				<value>11.0</value>\n"
				+ "			</cell>\n"
				+ "			<cell id=\"C5\">\n"
				+ "				<value>12.0</value>\n"
				+ "			</cell>\n"
				+ "			<cell id=\"D5\">\n"
				+ "				<expr>(SUM( A5:C5 ) * B1)</expr>\n"
				+ "			</cell>\n"
				+ "		</row>\n"
				+ "	</sheet>\n"
				+ "</workbook>\n"
				+ "", description );
	}

}
