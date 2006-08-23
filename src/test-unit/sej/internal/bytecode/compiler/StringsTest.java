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
package sej.internal.bytecode.compiler;

import sej.CallFrame;
import sej.EngineBuilder;
import sej.SEJ;
import sej.SaveableEngine;
import sej.Spreadsheet;
import sej.SpreadsheetBuilder;
import sej.SpreadsheetBinder.Section;
import junit.framework.TestCase;

public class StringsTest extends TestCase
{


	public void testStringOutput() throws Exception
	{
		SpreadsheetBuilder sb = SEJ.newSpreadsheetBuilder();
		sb.newCell( sb.cst( "This is a string." ) );
		sb.nameCell( "Result" );
		Spreadsheet s = sb.getSpreadsheet();

		EngineBuilder eb = SEJ.newEngineBuilder();
		eb.setSpreadsheet( s );
		eb.setFactoryClass( MyFactory.class );
		Section rb = eb.getRootBinder();
		rb.defineOutputCell( s.getCell( "Result" ), new CallFrame( MyOutputs.class.getMethod( "result" ) ) );
		SaveableEngine e = eb.compile();
		
		MyFactory f = (MyFactory) e.getComputationFactory();

		MyInputs i = new MyInputs();
		MyOutputs o = f.newComputation( i );
		String r = o.result();
		assertEquals( "This is a string.", r );
	}


	public static class MyInputs
	{
		public String value()
		{
			return "This is a dynamic string.";
		}
	}

	public static interface MyOutputs
	{
		String result();
	}

	public static interface MyFactory
	{
		MyOutputs newComputation( MyInputs _inputs );
	}


}
