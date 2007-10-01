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
package org.formulacompiler.compiler.internal.templates;

import org.formulacompiler.runtime.internal.Environment;
import org.formulacompiler.runtime.internal.Runtime_v2;


public final class ExpressionTemplatesForStrings
{
	private final Environment environment;

	public ExpressionTemplatesForStrings( Environment _env )
	{
		this.environment = _env;
	}


	String util_fromString( String a )
	{
		return Runtime_v2.stringFromString( a );
	}

	String util_fromObject( Object a )
	{
		return Runtime_v2.stringFromObject( a );
	}

	String util_fromNull()
	{
		return Runtime_v2.emptyString();
	}


	// Use utilFun_ here so "a" is not chained. We need it after the call to "new" for "<init>".

	StringBuilder utilFun_newBuilder( String a )
	{
		return new StringBuilder( a );
	}

	// Use utilOp_ from here because the StringBuilder is chained.

	StringBuilder utilOp_appendBuilder( StringBuilder b, String s )
	{
		return b.append( s );
	}

	String utilOp_fromBuilder( StringBuilder b )
	{
		return b.toString();
	}


	public String fun_LOWER( String a )
	{
		return Runtime_v2.fun_LOWER( a );
	}

	public String fun_UPPER( String a )
	{
		return Runtime_v2.fun_UPPER( a );
	}

	public String fun_PROPER( String a )
	{
		return Runtime_v2.fun_PROPER( a );
	}

	public String fun_REPT( String a, int n )
	{
		return Runtime_v2.fun_REPT( a, n );
	}

	public String fun_TRIM( String a )
	{
		return Runtime_v2.fun_TRIM( a );
	}

	// Leave this comment in. It is used to cite the code into the documentation.
	// ---- fun_MID
	public String fun_MID( String s, int start, int len )
	{
		return Runtime_v2.fun_MID( s, start, len );
	}
	// ---- fun_MID

	public String fun_LEFT( String s )
	{
		return Runtime_v2.fun_LEFT( s, 1 );
	}

	public String fun_LEFT( String s, int len )
	{
		return Runtime_v2.fun_LEFT( s, len );
	}

	public String fun_RIGHT( String s )
	{
		return Runtime_v2.fun_RIGHT( s, 1 );
	}

	public String fun_RIGHT( String s, int len )
	{
		return Runtime_v2.fun_RIGHT( s, len );
	}

	public String fun_SUBSTITUTE( String s, String src, String tgt )
	{
		return Runtime_v2.fun_SUBSTITUTE( s, src, tgt );
	}

	public String fun_SUBSTITUTE( String s, String src, String tgt, int occurrence )
	{
		return Runtime_v2.fun_SUBSTITUTE( s, src, tgt, occurrence );
	}

	public String fun_REPLACE( String s, int at, int len, String repl )
	{
		return Runtime_v2.fun_REPLACE( s, at, len, repl );
	}

	public String fun_TEXT( Number _num, String _format )
	{
		return Runtime_v2.fun_TEXT( _num, _format, this.environment );
	}


}
