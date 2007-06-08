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
package org.formulacompiler.runtime;

/**
 * Support class for handling scaled longs.
 * <p>
 * See the <a href="../../tutorial/numeric_type.htm#long" target="_top">tutorial</a> for details.
 * 
 * @author peo
 * 
 * @see ScaledLong
 */
public final class ScaledLongSupport
{

	/**
	 * The number 1 for the scaled {@code long} type at the different supported scales. Use it to
	 * scale unscaled values by multiplying them with the appropriate {@code ONE}.
	 * 
	 * @see #scale(long, int)
	 */
	public static final long[] ONE = new long[] { 1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000,
			1000000000, 10000000000L, 100000000000L, 1000000000000L, 10000000000000L, 100000000000000L, 1000000000000000L,
			10000000000000000L, 100000000000000000L, 1000000000000000000L };


	/**
	 * Returns a scaled version of an unscaled long value.
	 * 
	 * @param _unscaled is the unscaled value.
	 * @param _scale is the desired number of decimal places to scale by.
	 * @return the scaled number.
	 */
	public static long scale( long _unscaled, int _scale )
	{
		return _unscaled * ONE[ _scale ];
	}


	private ScaledLongSupport()
	{
		// never instantiate
	}
}
