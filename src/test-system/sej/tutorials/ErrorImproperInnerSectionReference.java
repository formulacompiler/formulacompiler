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
package sej.tutorials;

import sej.compiler.CallFrame;
import sej.runtime.Engine;
import sej.runtime.Resettable;
import sej.spreadsheet.EngineBuilder;
import sej.spreadsheet.Orientation;
import sej.spreadsheet.SEJ;
import sej.spreadsheet.Spreadsheet;
import sej.spreadsheet.SpreadsheetException;
import sej.spreadsheet.Spreadsheet.Range;
import sej.spreadsheet.SpreadsheetBinder.Section;
import junit.framework.TestCase;

public class ErrorImproperInnerSectionReference extends TestCase
{

	public void testGoodRef() throws Exception
	{
		// ---- goodRef
		EngineBuilder builder = builderForComputationOfCellNamed( /**/"GoodRef"/**/);
		Engine engine = builder.compile();
		MyFactory factory = (MyFactory) engine.getComputationFactory();
		MyComputation computation = factory.newComputation( new MyInputs() );
		assertEquals( 4 + 5 + 6 + 7, computation.result() );
		// ---- goodRef
	}

	public void testBadRef() throws Exception
	{
		// ---- badRef
		EngineBuilder builder = builderForComputationOfCellNamed( /**/"BadRef"/**/);
		try {
			builder.compile();
			fail();
		}
		catch (/**/SpreadsheetException.SectionExtentNotCovered e/**/) {
			String err = /**/"Range C2:C2 does not fully cover the height of its parent section B2:C4 (which iterates section()).\n"
					+ "Referenced by cell B10."/**/;
			assertEquals( err, e.getMessage() );
		}
		// ---- badRef
	}

	public void testBadRange1() throws Exception
	{
		// ---- badRange1
		EngineBuilder builder = builderForComputationOfCellNamed( /**/"BadRange1"/**/);
		try {
			builder.compile();
			fail();
		}
		catch (/**/SpreadsheetException.SectionExtentNotCovered e/**/) {
			String err = /**/"Range C2:C3 does not fully cover the height of its parent section B2:C4 (which iterates section()).\n"
					+ "Referenced by cell B11."/**/;
			assertEquals( err, e.getMessage() );
		}
		// ---- badRange1
	}

	public void testBadRange2() throws Exception
	{
		// ---- badRange2
		EngineBuilder builder = builderForComputationOfCellNamed( /**/"BadRange2"/**/);
		try {
			builder.compile();
			fail();
		}
		catch (/**/SpreadsheetException.SectionExtentNotCovered e/**/) {
			String err = /**/"Range C3:C4 does not fully cover the height of its parent section B2:C4 (which iterates section()).\n"
					+ "Referenced by cell B12."/**/;
			assertEquals( err, e.getMessage() );
		}
		// ---- badRange2
	}

	public void testBadRange3() throws Exception
	{
		// ---- badRange3
		EngineBuilder builder = builderForComputationOfCellNamed( /**/"BadRange3"/**/);
		try {
			builder.compile();
			fail();
		}
		catch (/**/SpreadsheetException.SectionExtentNotCovered e/**/) {
			String err = /**/"Range C3:C3 does not fully cover the height of its parent section B2:C4 (which iterates section()).\n"
					+ "Referenced by cell B13."/**/;
			assertEquals( err, e.getMessage() );
		}
		// ---- badRange3
	}

	public void testBadRange4() throws Exception
	{
		// ---- badRange4
		EngineBuilder builder = builderForComputationOfCellNamed( /**/"BadRange4"/**/);
		try {
			builder.compile();
			fail();
		}
		catch (/**/SpreadsheetException.SectionExtentNotCovered e/**/) {
			String err = /**/"Range C3:C3 does not fully cover the height of its parent section B2:C4 (which iterates section()).\n"
					+ "Referenced by cell B14."/**/;
			assertEquals( err, e.getMessage() );
		}
		// ---- badRange4
	}


	@SuppressWarnings("unchecked")
	private EngineBuilder builderForComputationOfCellNamed( String _cellName ) throws Exception
	{
		EngineBuilder builder = SEJ.newEngineBuilder();
		builder.loadSpreadsheet( "src/test-system/testdata/sej/tutorials/ErrorImproperInnerSectionReference.xls" );
		builder.setFactoryClass( MyFactory.class );
		Spreadsheet sheet = builder.getSpreadsheet();
		Section root = builder.getRootBinder();

		root.defineOutputCell( sheet.getCell( _cellName ), new CallFrame( MyComputation.class.getMethod( "result" ) ) );

		// ---- bindSection
		Range range = sheet.getRange( "Section" );
		CallFrame call = new CallFrame( MyInputs.class.getMethod( "section" ) );
		Class target = MyElement.class;
		Section section = root.defineRepeatingSection( range, Orientation.REPEAT_ROWS, call, target, null, null );
		section.defineInputCell( sheet.getRange( "Name" ).getTopLeft(), new CallFrame( target.getMethod( "name" ) ) );
		section.defineInputCell( sheet.getRange( "Value" ).getTopLeft(), new CallFrame( target.getMethod( "value" ) ) );
		// ---- bindSection

		return builder;
	}

	public static interface MyFactory
	{
		public MyComputation newComputation( MyInputs _inputs );
	}

	public static class MyInputs
	{
		public MyElement[] section()
		{
			return new MyElement[] { new MyElement( 4 ), new MyElement( 5 ), new MyElement( 6 ), new MyElement( 7 ) };
		}
	}

	public static class MyElement
	{
		private final int value;

		public MyElement(int _value)
		{
			super();
			this.value = _value;
		}

		public String name()
		{
			return toString();
		}

		public int value()
		{
			return this.value;
		}
	}

	public static interface MyComputation extends Resettable
	{
		public int result();
	}

}
