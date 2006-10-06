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
package sej.internal.build.bytecode;

import java.io.File;
import java.io.IOException;

import sej.internal.templates.ExpressionTemplatesForAll;
import sej.internal.templates.ExpressionTemplatesForBigDecimals;
import sej.internal.templates.ExpressionTemplatesForDoubles;
import sej.internal.templates.ExpressionTemplatesForNumbers;
import sej.internal.templates.ExpressionTemplatesForScaledLongs;

public final class PatternCompiler
{

	public static void main( String[] args ) throws Exception
	{
		new PatternCompiler().run();
	}


	private void run() throws IOException
	{
		final File p = new File( "src/classes-gen/sej/internal/bytecode/compiler" );
		p.mkdirs();

		new ByteCodeCompilerGenerator( this, ExpressionTemplatesForAll.class, "All", false, false ).generate( p );
		new ByteCodeCompilerGenerator( this, ExpressionTemplatesForNumbers.class, "Numbers" ).generate( p );
		new ByteCodeCompilerGenerator( this, ExpressionTemplatesForDoubles.class, "Doubles" ).generate( p );
		new ByteCodeCompilerGenerator( this, ExpressionTemplatesForScaledLongs.class, "ScaledLongs" ).generate( p );
		new ByteCodeCompilerGenerator( this, ExpressionTemplatesForBigDecimals.class, "BigDecimals", true, true ).generate( p );
	}
}
