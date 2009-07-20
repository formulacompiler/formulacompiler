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

package org.formulacompiler.tutorials;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.formulacompiler.decompiler.FormulaDecompiler;
import org.formulacompiler.runtime.Engine;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.tests.MultiFormatTestFactory;

import junit.framework.Test;

public class UsingPrecisionBigDecimal extends AbstractUsingBigDecimalTest
{


	public void testUsingBigDecimal34() throws Exception
	{
		String path = getPath();

		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( path );
		builder.setFactoryClass( Factory.class );
		// ---- buildCompiler34
		builder.setNumericType( /**/SpreadsheetCompiler.BIGDECIMAL128/**/ );
		// ---- buildCompiler34
		builder.bindAllByName();
		Engine engine = builder.compile();
		Factory factory = (Factory) engine.getComputationFactory();

		{
			// ---- checkResult34a
			Output output = factory.newInstance( new Input( 1, 6 ) );
			assertEquals( /**/"1.166666666666666666666666666666667"/**/, output.getResult().toPlainString() );
			// ---- checkResult34a
		}

		{
			// ---- checkResult34b
			Output output = factory.newInstance( new Input( /**/1000000/**/, 6 ) );
			assertEquals( /**/"1166666.666666666666666666666666667"/**/, output.getResult().toPlainString() );
			// ---- checkResult34b
		}

		{
			// ---- checkResult34c
			Output output = factory.newInstance( new Input( 1, /**/3/**/ ) );
			assertEquals( /**/"1.333333333333333333333333333333333"/**/, output.getResult().toPlainString() );
			// ---- checkResult34c
		}

		FormulaDecompiler.decompile( engine ).saveTo( "temp/test/decompiled/numeric_type/bigdecimal_prec34" );
	}


	public void testUsingBigDecimal4() throws Exception
	{
		String path = getPath();

		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( path );
		builder.setFactoryClass( Factory.class );
		// ---- buildCompiler4
		MathContext mathContext = /**/new MathContext( 4, RoundingMode.HALF_UP )/**/;
		builder.setNumericType( /**/SpreadsheetCompiler.getNumericType( BigDecimal.class, mathContext )/**/ );
		// ---- buildCompiler4
		builder.bindAllByName();
		Engine engine = builder.compile();
		Factory factory = (Factory) engine.getComputationFactory();

		{
			// ---- checkResult4a
			Output output = factory.newInstance( new Input( 1, 6 ) );
			assertEquals( /**/"1.167"/**/, output.getResult().toPlainString() );
			// ---- checkResult4a
		}

		{
			// ---- checkResult4b
			Output output = factory.newInstance( new Input( 1000000, 6 ) );
			assertEquals( /**/"1167000"/**/, output.getResult().toPlainString() );
			// ---- checkResult4b
		}

		{
			// ---- checkResult4c
			Output output = factory.newInstance( new Input( /**/12345678/**/, 1 ) );
			assertEquals( /**/"-12345678"/**/, output.getNegated().toPlainString() );
			// ---- checkResult4c
		}

		FormulaDecompiler.decompile( engine ).saveTo( "temp/test/decompiled/numeric_type/bigdecimal_prec4" );
	}


	public static Test suite()
	{
		return MultiFormatTestFactory.testSuite( UsingPrecisionBigDecimal.class );
	}


}
