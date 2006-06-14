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


public final class RuntimeLong_v1 extends Runtime_v1
{
	public static long[] ONE_AT_SCALE = new long[] { 1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000,
			1000000000, 10000000000L, 100000000000L, 1000000000000L, 10000000000000L, 100000000000000L, 1000000000000000L,
			10000000000000000L, 100000000000000000L, 1000000000000000000L };
	public static long[] HALF_AT_SCALE = new long[ ONE_AT_SCALE.length ];

	static {
		for (int i = 0; i < ONE_AT_SCALE.length; i++) {
			HALF_AT_SCALE[ i ] = ONE_AT_SCALE[ i ] / 2;
		}
	}


	public static final class Context
	{
		final int scale;
		final long one;
		final double oneAsDouble;

		public Context(final int _scale)
		{
			super();
			this.scale = _scale;
			if (_scale < 0 || _scale >= ONE_AT_SCALE.length) {
				throw new IllegalArgumentException( "Scale is out of range" );
			}
			this.one = ONE_AT_SCALE[ _scale ];
			this.oneAsDouble = this.one;
		}

		double toDouble( long _value )
		{
			if (this.scale == 0) return _value;
			return _value / this.oneAsDouble;
		}

		long fromDouble( double _value )
		{
			if (this.scale == 0) return (long) _value;
			return (long) (_value * this.oneAsDouble);
		}
	}


	public static long max( final long a, final long b )
	{
		return a >= b ? a : b;
	}

	public static long min( final long a, final long b )
	{
		return a <= b ? a : b;
	}

	public static long pow( final long x, final long n, Context _cx )
	{
		return _cx.fromDouble( Math.pow( _cx.toDouble( x ), _cx.toDouble( n ) ) );
	}

	public static long round( final long _val, final int _maxFrac, Context _cx )
	{
		if (_val == 0 || _maxFrac >= _cx.scale) {
			return _val;
		}
		else {
			final int truncateAt = _cx.scale - _maxFrac;
			final long shiftFactor = ONE_AT_SCALE[ truncateAt ];
			final long roundingCorrection = HALF_AT_SCALE[ truncateAt ];
			if (_val >= 0) {
				// I have: 123456 (scale = 3)
				// I want: 123500 (_maxFrac = 1)
				// So: (v + 50) / 100 * 100
				return (_val + roundingCorrection) / shiftFactor * shiftFactor;
			}
			else {
				// I have: -123456 (scale = 3)
				// I want: -123500 (_maxFrac = 1)
				// So: (v - 50) / 100 * 100
				return (_val - roundingCorrection) / shiftFactor * shiftFactor;
			}
		}
	}

	public static long stdROUND( final long _val, final long _maxFrac, Context _cx )
	{
		if (_cx.scale == 0) return round( _val, (int) _maxFrac, _cx );
		return round( _val, (int) (_maxFrac / _cx.one), _cx );
	}


	public static boolean booleanFromExcel( final long _val )
	{
		return (_val != 0);
	}

	public static long booleanToExcel( final boolean _val, Context _cx )
	{
		return _val ? _cx.one : 0;
	}

}
