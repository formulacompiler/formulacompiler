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
package sej.internal.build.rewriting;

import sej.Function;

public final class RewriteRulesCompiler extends AbstractRewriteRulesCompiler
{

	private RewriteRulesCompiler()
	{
		super();
	}


	public static void main( String[] args ) throws Exception
	{
		new RewriteRulesCompiler().run();
	}


	@Override
	protected void defineFunctions() throws Exception
	{
		def( Function.MIN, "xs*", "_FOLDL( acc: 0; xi: `acc _min_ `xi; `xs )" );
		def( Function.MAX, "xs*", "_FOLDL( acc: 0; xi: `acc _max_ `xi; `xs )" );

		def( Function.SUM, "xs*", "_FOLDL( acc: 0; xi: `acc + `xi; `xs )" );
		def( Function.AVERAGE, "xs*", "SUM( `xs ) / COUNT( `xs )" );

		begin( Function.VARP, "xs*" );
		{
			body( "_LET( c: COUNT(`xs);" );
			// Inlining AVERAGE here because COUNT is already known:
			body( "	_LET( m: SUM(`xs) / `c;" );
			body( "		_FOLDL( acc: 0; xi: _LET( ei: `xi - `m; `acc + `ei*`ei ); `xs )" );
			body( "	)" );
			body( "	/ `c" );
			body( ")" );
		}
		end();
	}


}
