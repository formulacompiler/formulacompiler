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
package sej.tests.utils;

import java.io.InputStream;

import sej.spreadsheet.SEJ;
import sej.spreadsheet.Spreadsheet;

public abstract class AbstractSpreadsheetTestBase extends AbstractIOTestBase
{

	protected void checkSpreadsheetStream( Spreadsheet _expected, InputStream _stream, String _typeExtensionOrFileName )
			throws Exception
	{
		Spreadsheet actual = SEJ.loadSpreadsheet( _typeExtensionOrFileName, _stream );
		touchExpressions( actual );
		assertEquals( _expected.describe(), actual.describe() );
	}

	protected void touchExpressions( Spreadsheet _ss ) throws Exception
	{
		for (Spreadsheet.Sheet s : _ss.getSheets()) {
			for (Spreadsheet.Row r : s.getRows()) {
				for (Spreadsheet.Cell c : r.getCells()) {
					c.getExpressionText();
				}
			}
		}
	}

}
