/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * This file is part of the Abacus Formula Compiler (AFC).
 *
 * For commercial licensing, please contact sales(at)formulacompiler.com.
 *
 * AFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AFC.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.formulacompiler.tests.time;

import java.io.File;
import java.util.Date;

import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.runtime.Engine;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public abstract class AbstractTimeTest
{
	private ComputationFactory computationFactory;
	private final String spreadsheetExtension;
	private final NumericType numericType;
	private final Class outputClass;

	public AbstractTimeTest( String _spreadsheetExtension, NumericType _numericType, Class _outputClass )
	{
		this.spreadsheetExtension = _spreadsheetExtension;
		this.numericType = _numericType;
		this.outputClass = _outputClass;
	}

	private String getSpreadsheetExtension()
	{
		return this.spreadsheetExtension;
	}

	@Before
	public void setUp() throws Exception
	{
		final EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( new File( "src/test/data/org/formulacompiler/tests/time/TimeTest"
				+ getSpreadsheetExtension() ) );
		builder.setNumericType( this.numericType );
		builder.setInputClass( Object.class );
		builder.setOutputClass( this.outputClass );
		builder.bindAllByName();
		final Engine engine = builder.compile();
		this.computationFactory = engine.getComputationFactory();
	}

	@Test
	public void testSameTimeInSameCell() throws Exception
	{
		final Outputs output = getOutputs();
		final Date date1 = output.now1();
		Thread.sleep( 1000 );
		final Date date2 = output.now1();
		assertEquals( date1, date2 );
	}

	@Test
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
		return (Outputs) this.computationFactory.newComputation( null );
	}
}
