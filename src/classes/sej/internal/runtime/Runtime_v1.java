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
package sej.internal.runtime;

import java.util.Calendar;
import java.util.Date;

public abstract class Runtime_v1
{

	// ---- Excel date conversion; copied from JExcelAPI (DateRecord.java)

	static final long SECS_PER_DAY = 24 * 60 * 60;
	static final long MS_PER_SEC = 1000;
	static final long MS_PER_DAY = SECS_PER_DAY * MS_PER_SEC;
	static final int NON_LEAP_DAY = 61;
	static final int UTC_OFFSET_DAYS = 25569;
	static final int UTC_OFFSET_DAYS_1904 = 24107;
	static final boolean BASED_ON_1904 = false;


	public static boolean unboxBoolean( Boolean _boxed )
	{
		return (_boxed == null) ? false : _boxed;
	}

	public static long unboxLong( Long _boxed )
	{
		return (_boxed == null) ? 0L : _boxed;
	}
	
	
	public static Date today()
	{
		long now = Calendar.getInstance().getTime().getTime();
		long today = now / MS_PER_DAY * MS_PER_DAY;
		return new Date( today );
	}

	
	public static StringBuilder newStringBuilder( String _first )
	{
		return new StringBuilder( _first );
	}
	
	public static StringBuffer newStringBuffer( String _first )
	{
		return new StringBuffer( _first );
	}
	
	public static String stringFromObject( Object _obj )
	{
		return (_obj == null)? "" : _obj.toString();
	}
	
	public static String stringFromString( String _str )
	{
		return (_str == null)? "" : _str;
	}
	
}
