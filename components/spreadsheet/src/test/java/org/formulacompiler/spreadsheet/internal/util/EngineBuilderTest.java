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
package org.formulacompiler.spreadsheet.internal.util;

import java.util.Arrays;
import java.util.Map;

import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.internal.builder.SpreadsheetBuilderImpl;

import junit.framework.TestCase;

public class EngineBuilderTest extends TestCase
{
	private static final double EPS = 0.0001;


	// DO NOT REFORMAT BELOW THIS LINE
	public static class Inputs {
		public double input() { return 10.0; }	
		public double both() { return 11.0; }
	}
	
	public static class Outputs {
		public double output() { return 100.0; } 
		public double both() { return 101.0; }
		public double missing() { return 102.0; }
		public double mapped() { return 103.0; }
	}
	// DO NOT REFORMAT ABOVE THIS LINE


	public void testBindAllByName() throws Exception
	{
		SpreadsheetBuilderImpl b = new SpreadsheetBuilderImpl();
		b.newCell( b.cst( 1 ) ).nameCell( "INPUT" );
		b.newCell( b.ref( b.currentCell() ) ).nameCell( "MAPPED" );
		b.newCell( b.cst( 2 ) ).nameCell( "OUTPUT" );
		b.newCell( b.cst( 3 ) ).nameCell( "BOTH" );
		b.newCell( b.cst( 4 ) ).nameCell( "NONE" );

		EngineBuilder eb = SpreadsheetCompiler.newEngineBuilder();
		eb.setSpreadsheet( b.getSpreadsheet() );
		eb.setInputClass( Inputs.class );
		eb.setOutputClass( Outputs.class );
		eb.bindAllByName();
		Outputs out = (Outputs) eb.compile().getComputationFactory().newComputation( new Inputs() );

		assertEquals( 10.0, out.mapped(), EPS );
		assertEquals( 11.0, out.both(), EPS );
		assertEquals( 2.0, out.output(), EPS );
		assertEquals( 102.0, out.missing(), EPS );

		assertUnbound( eb, "NONE" );

		try {
			eb.failIfByNameBindingLeftNamedCellsUnbound();
		}
		catch (SpreadsheetException.NameNotFound e) {
			assertEquals(
					"There is no input or output method named NONE() or getNONE() to bind the cell NONE to (character case is irrelevant).",
					e.getMessage() );
		}
	}


	public void testBindAllByPrefixedName() throws Exception
	{
		SpreadsheetBuilderImpl b = new SpreadsheetBuilderImpl();
		b.newCell( b.cst( 1 ) ).nameCell( "I_INPUT" );
		b.newCell( b.ref( b.currentCell() ) ).nameCell( "O_MAPPED" );
		b.newCell( b.cst( 3 ) ).nameCell( "O_OUTPUT" );
		b.newCell( b.cst( 2 ) ).nameCell( "I_NONE" );
		b.newCell( b.cst( 4 ) ).nameCell( "O_NONE" );
		b.newCell( b.cst( 5 ) ).nameCell( "PLAIN" );

		EngineBuilder eb = SpreadsheetCompiler.newEngineBuilder();
		eb.setSpreadsheet( b.getSpreadsheet() );
		eb.setInputClass( Inputs.class );
		eb.setOutputClass( Outputs.class );
		eb.bindAllByName( "I_", "O_" );
		Outputs out = (Outputs) eb.compile().getComputationFactory().newComputation( new Inputs() );

		assertEquals( 10.0, out.mapped(), EPS );
		assertEquals( 3.0, out.output(), EPS );
		assertEquals( 102.0, out.missing(), EPS );

		assertUnbound( eb, "I_NONE", "O_NONE", "PLAIN" );

		try {
			eb.failIfByNameBindingLeftNamedCellsUnbound( "I_", "O_" );
		}
		catch (SpreadsheetException.NameNotFound e) {
			assertEquals(
					"There is no input method named I_NONE() or getI_NONE() to bind the cell I_NONE to (character case is irrelevant).",
					e.getMessage() );
		}
	}


	private void assertUnbound( EngineBuilder _eb, String... _want ) throws Exception
	{
		Arrays.sort( _want );

		final Map<String, Spreadsheet.Range> unbound = _eb.getByNameBinder().cellNamesLeftUnbound();
		final String[] have = unbound.keySet().toArray( new String[ unbound.size() ] );
		Arrays.sort( have );

		assertEquals( _want.length, have.length );
		for (int i = 0; i < _want.length; i++) {
			assertEquals( _want[ i ], have[ i ] );
		}
	}
}
