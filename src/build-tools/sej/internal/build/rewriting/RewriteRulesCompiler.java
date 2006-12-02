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

	
	// FIXME Get rid of `
	

	// Leave this comment in. It is used to cite the code into the documentation.
	// ---- fun_COMBIN
	@Override
	protected void defineFunctions() throws Exception
	{
		// ---- fun_COMBIN

		/*
		 * PRODUCT must return 0 for empty sections, so I cannot use _FOLDL_1ST_OK as the initial
		 * value would then be 1.
		 */
		def( Function.PRODUCT, "xs*", "_FOLD_1ST( x0: `x0; r xi: `r * `xi; 0; `xs )" );

		/*
		 * The first argument to SUM can be used as the initial value to get rid of one addition. But
		 * the single addition is not worth the overhead of _FOLDL_1ST. So use _FOLDL_1ST_OK.
		 */
		// Leave this comment in. It is used to cite the code into the documentation.
		// ---- fun_SUM
		def( Function.SUM, "xs*", "_FOLD_1STOK( r: 0; xi: `r + `xi; `xs )" );
		// ---- fun_SUM

		/*
		 * It is clearer for MIN and MAX to always act on the first arg and not some arbitrary
		 * extremal initial value. So use _FOLDL_1ST.
		 */
		// Leave this comment in. It is used to cite the code into the documentation.
		// ---- fun_MINMAX
		def( Function.MIN, "xs*", "_FOLD_1ST( x0: `x0; r xi: `r _min_ `xi; 0; `xs )" );
		def( Function.MAX, "xs*", "_FOLD_1ST( x0: `x0; r xi: `r _max_ `xi; 0; `xs )" );
		// ---- fun_MINMAX

		/*
		 * This definition of AVERAGE is not really suitable for large, non-cached sections. It is
		 * also not good for when NULL is properly supported. Since both of these features are not yet
		 * done, I'll leave it for the moment.
		 */
		def( Function.AVERAGE, "xs*", "SUM( `xs ) / COUNT( `xs )" );

		/*
		 * An efficient implementation of VARP for large datasets would require a helper function
		 * returning _both_ the sum and the count in one pass. We don't do cursor-style aggregation
		 * yet, so the following is quite OK.
		 * 
		 * I am inlining AVERAGE here because COUNT is already known.
		 */
		begin( Function.VARP, "xs*" );
		{
			body( "_LET( n: COUNT(`xs);" );
			body( "	 _LET( m: SUM(`xs) / `n;" );
			body( "    _FOLD( r: 0; xi: _LET( ei: `xi - `m; `r + `ei*`ei ); `xs )" );
			body( "  )" );
			body( "  / `n" );
			body( ")" );
		}
		end();

		// Leave this comment in. It is used to cite the code into the documentation.
		// ---- fun_VAR
		begin( Function.VAR, "xs*" );
		{
			body( "_LET( n: COUNT(`xs);" );
			body( "  _LET( m: SUM(`xs) / `n;" );
			body( "    _FOLD( r: 0; xi: _LET( ei: `xi - `m; `r + `ei*`ei ); `xs )" );
			body( "  )" );
			body( "  / (`n - 1)" );
			body( ")" );
		}
		end();
		// ---- fun_VAR


		// Leave this comment in. It is used to cite the code into the documentation.
		// ---- fun_COMBIN
		// ...
		begin( Function.COMBIN, "n", "k" );
		{
			body( "IF( OR( `n <= 0, `k < 0, `n < `k ), 0," );
			body( "  IF( `n = `k, 1," );
			body( "    IF( `k = 1, `n," );
			body( "      FACT(`n) / FACT(`k) / FACT(`n - `k)" );
			body( "    )" );
			body( "  )" );
			body( ")" );
		}
		end();
		// ...
		// ---- fun_COMBIN


		begin( Function.NPV, "rate", "vs#" );
		{
			body( "_LET( rate1: `rate + 1;" );
			body( "  _FOLD_ARRAY( r: 0; vi i: `r + `vi / `rate1 ^ `i; `vs ))" );
		}
		end();

		/*
		begin( Function.MIRR, "vs#", "frate", "rrate" );
		{
			body( "_LET( n: COUNT( `vs );" );
			body( "  ((-NPV( `rrate, _MAP_ARRAY( vi: IF( `vi > 0, `vi, 0 ))) * (1 + `rrate) ^ `n)" );
			body( "   / (NPV( `frate, _MAP_ARRAY( vi: IF( `vi < 0, `vi, 0 ))) * (1 + `frate)))" );
			body( "  ^ (1 / (`n - 1))" );
			body( "  - 1 )" );
		}
		end();
		*/

		begin( Function.MIRR, "vs#", "frate", "rrate" );
		{
			body( "_LET( n: COUNT( `vs );" );
			body( "_LET( rrate1: `rrate + 1;" );
			body( "_LET( frate1: `frate + 1;" );
			body( "  ((-_FOLD_ARRAY( r: 0; vi i: `r + IF( `vi > 0, `vi, 0 ) * `rrate1 ^ (`n - `i); `vs ))" );
			body( "   / _FOLD_ARRAY( r: 0; vi i: `r + IF( `vi < 0, `vi, 0 ) / `frate1 ^ (`i - 1); `vs ))" );
			body( "  ^ (1 / (`n - 1))" );
			body( "  - 1 )))" );
		}
		end();


		// Leave this comment in. It is used to cite the code into the documentation.
		// ---- fun_COMBIN
	}
	// ---- fun_COMBIN


}
