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
package org.formulacompiler.tests.time;

import java.util.Date;

import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.runtime.Engine;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;

import junit.framework.TestCase;


public abstract class AbstractTimeTest extends TestCase
{
	private ComputationFactory computationFactory;
	private final NumericType numericType;
	private final Class outputClass;

	public AbstractTimeTest( String _name, NumericType _numericType, Class _outputClass )
	{
		super( _name );
		numericType = _numericType;
		outputClass = _outputClass;
	}

	protected void setUp() throws Exception
	{
		final EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( "src/test/data/org/formulacompiler/tests/time/TimeTest.xls" );
		builder.setNumericType( numericType );
		builder.setInputClass( Object.class );
		builder.setOutputClass( outputClass );
		builder.bindAllByName();
		final Engine engine = builder.compile();
		computationFactory = engine.getComputationFactory();
	}

	public void testSameTimeInSameCell() throws Exception
	{
		final Outputs output = getOutputs();
		final Date date1 = output.now1();
		Thread.sleep( 1000 );
		final Date date2 = output.now1();
		assertEquals( date1, date2 );
	}

	public void testSameTimeInDifferentCells() throws Exception
	{
		final Outputs output = getOutputs();
		final Date date1 = output.now1();
		Thread.sleep( 1000 );
		final Date date2 = output.now2();
		assertEquals( date1, date2 );
	}

	protected Outputs getOutputs()
	{
		return (Outputs) computationFactory.newComputation( null );
	}
}
