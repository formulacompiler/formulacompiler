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
package sej.internal.model.templates;

import sej.internal.runtime.Runtime_v1;


@SuppressWarnings("unqualified-field-access")
public final class ExpressionTemplatesForNumbers
{

	/**
	 * The "String" argument is automatically compiled using the String expression compiler. The
	 * "int" return is automatically converted to the proper output type.
	 */
	public int fun_LEN( String a )
	{
		return a.length();
	}


	public boolean fun_EXACT( String a, String b )
	{
		return Runtime_v1.stdEXACT( a, b );
	}


	public int fun_SEARCH( String _what, String _within )
	{
		return Runtime_v1.stdSEARCH( _what, _within, 1 );
	}

	public int fun_SEARCH( String _what, String _within, int _startingAt )
	{
		return Runtime_v1.stdSEARCH( _what, _within, _startingAt );
	}


	public int fun_FIND( String _what, String _within )
	{
		return Runtime_v1.stdFIND( _what, _within, 1 );
	}

	public int fun_FIND( String _what, String _within, int _startingAt )
	{
		return Runtime_v1.stdFIND( _what, _within, _startingAt );
	}

}
