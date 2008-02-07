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
package org.formulacompiler.compiler.internal;

public abstract class AbstractLongType extends NumericTypeImpl
{
	public static final Long ZERO = Long.valueOf( 0L );

	protected AbstractLongType( int _scale, int _roundingMode )
	{
		super( Long.TYPE, _scale, _roundingMode );
	}

	@Override
	public final Number getZero()
	{
		return Long.valueOf( zero() );
	}

	@Override
	public Number getOne()
	{
		return Long.valueOf( one() );
	}

	@Override
	public final Number getMinValue()
	{
		return MIN;
	}

	private static final Long MIN = Long.valueOf( Long.MIN_VALUE );

	@Override
	public final Number getMaxValue()
	{
		return MAX;
	}

	private static final Long MAX = Long.valueOf( Long.MAX_VALUE );

	@Override
	protected final Long assertProperNumberType( Number _value )
	{
		return (Long) _value;
	}

	public final long zero()
	{
		return 0L;
	}

	public abstract long one();

}