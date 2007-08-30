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
package org.formulacompiler.tutorials;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.formulacompiler.decompiler.FormulaDecompiler;
import org.formulacompiler.runtime.Engine;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;

public class UsingPrecisionBigDecimal extends AbstractUsingBigDecimalTest
{


	public void testUsingBigDecimal34() throws Exception
	{
		String path = PATH;

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
		String path = PATH;

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


}
