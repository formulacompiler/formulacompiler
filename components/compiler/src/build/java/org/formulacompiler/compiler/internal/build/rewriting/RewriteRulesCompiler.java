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
package org.formulacompiler.compiler.internal.build.rewriting;

import org.formulacompiler.compiler.Function;


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


	// Leave these comments in. They are used to cite the code into the documentation.
	// ---- fun_COMBIN
	@Override
	protected void defineFunctions() throws Exception
	{
		// ---- fun_COMBIN

		defineAggregators();
		defineFinancial();
		defineStatistical();

		// Please leave this rule here. It is cited into the documentation.
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
	}
	// ---- fun_COMBIN


	private void defineAggregators() throws Exception
	{
		/*
		 * PRODUCT must return 0 for empty sections, so I cannot use _FOLD_OR_REDUCE as the initial
		 * value would then be 1.
		 */
		def( Function.PRODUCT, "xs*", "_REDUCE( r, xi: `r * `xi; 0; `xs )" );

		/*
		 * The first argument to SUM can be used as the initial value to get rid of one addition. But
		 * the single addition is not worth the overhead of _FOLDL_1ST. So use _FOLDL_1ST_OK.
		 */
		// Leave this comment in. It is used to cite the code into the documentation.
		// ---- fun_SUM
		def( Function.SUM, "xs*", "_FOLD_OR_REDUCE( r: 0; xi: `r + `xi; `xs )" );
		// ---- fun_SUM

		/*
		 * It is clearer for MIN and MAX to always act on the first arg and not some arbitrary
		 * extremal initial value. So use _FOLDL_1ST.
		 */
		// Leave this comment in. It is used to cite the code into the documentation.
		// ---- fun_MINMAX
		def( Function.MIN, "xs*", "_REDUCE( r, xi: `r _min_ `xi; 0; `xs )" );
		def( Function.MAX, "xs*", "_REDUCE( r, xi: `r _max_ `xi; 0; `xs )" );
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
		 * One might also consider turning this into an ARRAY function (xs# instead of xs*).
		 * Currently, AFC does not support converting arbitrary range unions with possibly dynamic
		 * sections into arrays.
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

		begin( Function.KURT, "xs*" );
		{
			body( "_LET( n: COUNT(`xs);" );
			body( "  _LET( a: `n - 1;" );
			body( "    _LET( b: (`n - 2) * (`n - 3);" );
			body( "      _LET( s: STDEV(`xs);" );
			body( "        _LET( m: SUM(`xs) / `n;" );
			body( "          _FOLD( r: 0; xi:" );
			body( "            _LET( ei2:" );
			body( "              _LET( ei: (`xi - `m) / `s; `ei*`ei );" );
			body( "            `r + `ei2*`ei2 );" );
			body( "          `xs )" );
			body( "        )" );
			body( "      ) * `n * (`n + 1) / (`a * `b) - 3 * `a * `a / `b" );
			body( "    )" );
			body( "  )" );
			body( ")" );
		}
		end();

		begin( Function.SKEW, "xs*" );
		{
			body( "_LET( n: COUNT(`xs);" );
			body( "  _LET( s3: _LET( s: STDEV(`xs); `s*`s*`s);" );
			body( "    _LET( m: SUM(`xs) / `n;" );
			body( "      _FOLD( r: 0; xi: _LET( ei: `xi - `m; `r + `ei*`ei*`ei ); `xs )" );
			body( "    )" );
			body( "    / `s3" );
			body( "  )" );
			body( "  * `n / ((`n - 1) * (`n - 2))" );
			body( ")" );
		}
		end();

		def( Function.STDEV, "xs*", "SQRT( VAR( `xs ) )" );
		def( Function.STDEVP, "xs*", "SQRT( VARP( `xs ) )" );

		begin( Function.AVEDEV, "xs*" );
		{
			body( "_LET( n: COUNT(`xs);" );
			body( "  _LET( m: SUM(`xs) / `n;" );
			body( "    _FOLD( r:0; xi: `r + ABS( `m - `xi ); `xs )" );
			body( "  ) / `n" );
			body( ")" );
		}
		end();

		begin( Function.DEVSQ, "xs*" );
		{
			body( "_LET( n: COUNT(`xs);" );
			body( "  _LET( m: SUM(`xs) / `n;" );
			body( "    _FOLD( r: 0; xi: _LET( ei: `xi - `m; `r + `ei*`ei ); `xs )" );
			body( "  )" );
			body( ")" );
		}
		end();

		def( Function.SUMSQ, "xs*", "_FOLD( r: 0; xi: `r + `xi*`xi; `xs )" );
	}


	private void defineFinancial() throws Exception
	{

		// Leave this comment in. It is used to cite the code into the documentation.
		// ---- fun_NPV
		// ...
		begin( Function.NPV, "rate", "vs#" );
		{
			body( "_LET( rate1: `rate + 1;" );
			body( "  _FOLD_ARRAY( r: 0; vi, i: `r + `vi / `rate1 ^ `i; `vs ))" );
		}
		end();
		// ...
		// ---- fun_NPV

		/*
		 * MIRR could use NPV internally (array passing is not yet supported by the compiler, though),
		 * but a little math shows that the following is equivalent but quicker.
		 */
		begin( Function.MIRR, "vs#", "frate", "rrate" );
		{
			body( "_LET( n: COUNT( `vs );" );
			body( "_LET( rrate1: `rrate + 1;" );
			body( "_LET( frate1: `frate + 1;" );
			body( "  ((-_FOLD_ARRAY( r: 0; vi, i: `r + IF( `vi > 0, `vi, 0 ) * `rrate1 ^ (`n - `i); `vs ))" );
			body( "   / _FOLD_ARRAY( r: 0; vi, i: `r + IF( `vi < 0, `vi, 0 ) / `frate1 ^ (`i - 1); `vs ))" );
			body( "  ^ (1 / (`n - 1))" );
			body( "  - 1 )))" );
		}
		end();

		def( Function.SLN, "cost", "salvage", "life", "(`cost - `salvage) / `life" );
		def( Function.SYD, "cost", "salvage", "life", "per",
				"(`cost - `salvage) * (`life - `per + 1) * 2 / (`life * (`life + 1))" );

		begin( Function.FV, "rate", "nper", "pmt", "pv", "type" );
		{
			body( "IF( `rate = 0," );
			body( "  -`pv - `pmt * `nper," );
			body( "  _LET( p: (`rate + 1) ^ `nper;" );
			body( "  _LET( k: IF (`type > 0, `rate + 1, 1);" );
			body( "  -`pv * `p - `pmt * (`p - 1) * `k / `rate" );
			body( "  ))" );
			body( ")" );
		}
		end();
		def( Function.FV, "rate", "nper", "pmt", "pv", "FV( `rate, `nper, `pmt, `pv, 0 )" );
		def( Function.FV, "rate", "nper", "pmt", "FV( `rate, `nper, `pmt, 0, 0 )" );

		begin( Function.NPER, "rate", "pmt", "pv", "fv", "type" );
		{
			body( "IF( `rate = 0," );
			body( "  -(`pv + `fv) / `pmt," );
			body( "  _LET( a: IF(`type > 0 , `pmt * (1 + `rate) , `pmt);" );
			body( "    LOG( -(`rate * `fv - `a) / (`rate * `pv + `a), 1 + `rate )" );
			body( "  )" );
			body( ")" );
		}
		end();
		def( Function.NPER, "rate", "pmt", "pv", "fv", "NPER( `rate, `pmt, `pv, `fv, 0 )" );
		def( Function.NPER, "rate", "pmt", "pv", "NPER( `rate, `pmt, `pv, 0, 0 )" );

		begin( Function.PV, "rate", "nper", "pmt", "fv", "type" );
		{
			body( "IF( `rate = 0," );
			body( "  -`fv - `pmt * `nper," );
			body( "  _LET( a: 1 + `rate;" );
			body( "  _LET( b: -`fv * ( `a ^ -`nper );" );
			body( "  IF( `type > 0," );
			body( "    `b + (`pmt * (( `a ^ ( -`nper + 1 )) - 1) / `rate) - `pmt," );
			body( "    `b + (`pmt * (( `a ^ -`nper ) - 1) / `rate)" );
			body( "  )))" );
			body( ")" );
		}
		end();
		def( Function.PV, "rate", "nper", "pmt", "fv", "PV (`rate, `nper, `pmt, `fv, 0 )" );
		def( Function.PV, "rate", "nper", "pmt", "PV (`rate, `nper, `pmt, 0, 0 )" );

		begin( Function.PMT, "rate", "nper", "pv", "fv", "type" );
		{
			body( "IF( `rate = 0," );
			body( "  -(`pv + `fv) / `nper," );
			body( "  _LET( a: (1 + `rate) ^ `nper;" );
			body( "  _LET( b: `pv / (1 - 1 / `a);" );
			body( "  _LET( c: `fv / (`a - 1);" );
			body( "  _LET( d: -(`b + `c) * `rate;" );
			body( "  IF( `type > 0 , `d / (1 + `rate) , `d)" );
			body( "  ))))" );
			body( ")" );
		}
		end();
		def( Function.PMT, "rate", "nper", "pv", "fv", "PMT (`rate, `nper, `pv, `fv, 0 )" );
		def( Function.PMT, "rate", "nper", "pv", "PMT (`rate, `nper, `pv, 0, 0 )" );
	}


	private void defineStatistical() throws Exception
	{
		begin( Function.RANK, "number", "ref#", "order" );
		{
			body( "_FOLD_ARRAY( r: 1; refi, i: " );
			body( "  `r + IF( IF( `order = 0, `number < `refi, `number > `refi ), 1, 0); " );
			body( "`ref )" );
		}
		end();
		def( Function.RANK, "number+", "ref+", "RANK (`number, `ref, 0 )" );
	}


}
