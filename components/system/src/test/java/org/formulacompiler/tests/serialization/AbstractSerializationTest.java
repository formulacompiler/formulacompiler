/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
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

package org.formulacompiler.tests.serialization;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.SpreadsheetBinder.Section;


public abstract class AbstractSerializationTest extends AbstractTestBase
{

	public void testSerialization() throws Exception
	{
		serializeAndTest();
		deserializeAndTest();
	}


	private void serializeAndTest() throws Exception
	{
		final Class<Inputs> inp = Inputs.class;
		final Class<Outputs> outp = Outputs.class;
		final EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( "src/test/data/org/formulacompiler/tests/serialization/SerializationTest.xls" );
		builder.setInputClass( inp );
		builder.setOutputClass( outp );
		builder.setNumericType( getNumericType() );

		final Section bnd = builder.getRootBinder();
		final Spreadsheet sheet = builder.getSpreadsheet();
		bnd.defineInputCell( sheet.getCell( 0, 1, 0 ), inp.getMethod( "getA" + getTypeSuffix() ) );
		bnd.defineInputCell( sheet.getCell( 0, 1, 1 ), inp.getMethod( "getB" + getTypeSuffix() ) );
		bnd.defineOutputCell( sheet.getCell( 0, 1, 2 ), outp.getMethod( "getResult" + getTypeSuffix() ) );

		final SaveableEngine engine = builder.compile();
		serialize( engine );
		computeAndTestResult( engine );
	}


	protected abstract NumericType getNumericType();


	private void serialize( SaveableEngine _engine ) throws Exception
	{
		OutputStream outStream = new BufferedOutputStream( new FileOutputStream( getEngineFile() ) );
		try {
			_engine.saveTo( outStream );
		}
		finally {
			outStream.close();
		}
	}


}
