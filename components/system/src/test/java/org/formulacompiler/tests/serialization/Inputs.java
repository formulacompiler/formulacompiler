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
package org.formulacompiler.tests.serialization;

import java.math.BigDecimal;

import org.formulacompiler.runtime.ScaledLong;
import org.formulacompiler.runtime.ScaledLongSupport;


// ---- Inputs
public final class Inputs
{
	final String a;
	final String b;

	public Inputs(String _a, String _b)
	{
		super();
		this.a = _a;
		this.b = _b;
	}

	public double getA_Double()
	{
		return Double.parseDouble( this.a );
	}

	public double getB_Double()
	{
		return Double.parseDouble( this.b );
	}

	@ScaledLong(4)
	public long getA_Long4()
	{
		return ScaledLongSupport.scale( (long) getA_Double(), 4 );
	}

	@ScaledLong(4)
	public long getB_Long4()
	{
		return ScaledLongSupport.scale( (long) getB_Double(), 4 );
	}

	public BigDecimal getA_BigDecimal()
	{
		return new BigDecimal( this.a );
	}

	public BigDecimal getB_BigDecimal()
	{
		return new BigDecimal( this.b );
	}

}
// ---- Inputs
