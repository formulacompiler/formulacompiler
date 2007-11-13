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

import org.formulacompiler.decompiler.FormulaDecompiler;
import org.formulacompiler.runtime.Engine;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;

public class UsingUnlimitedBigDecimal extends AbstractUsingBigDecimalTest
{

	
	public void testUsingBigDecimalN() throws Exception
	{
		String path = PATH;
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( path );
		builder.setFactoryClass( Factory.class );
		// ---- buildCompilerN
		builder.setNumericType( /**/SpreadsheetCompiler.getNumericType( BigDecimal.class )/**/ );
		// ---- buildCompilerN
		builder.bindAllByName();
		Engine engine = builder.compile();
		Factory factory = (Factory) engine.getComputationFactory();

		{
			// ---- checkResultNa
			Output output = factory.newInstance( new Input( /**/1, 4/**/ ) );
			assertEquals( /**/"1.25"/**/, output.getResult().toPlainString() );
			// ---- checkResultNa
		}

		{
			// ---- checkResultNb
			try {
				Output output = factory.newInstance( new Input( 1, /**/3/**/ ) );
				output.getResult();
				fail( "ArithmeticException expected" );
			}
			catch (/**/ArithmeticException e/**/) {
				assertEquals( "Non-terminating decimal expansion; no exact representable decimal result.", e.getMessage() );
			}
			// ---- checkResultNb
		}

		FormulaDecompiler.decompile( engine ).saveTo( "temp/test/decompiled/numeric_type/bigdecimal0" );
	}


}
