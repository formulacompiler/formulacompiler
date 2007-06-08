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
package org.formulacompiler.tests.utils;

import java.math.BigDecimal;

import org.formulacompiler.runtime.ScaledLong;


/**
 * Used to be an interface, but since AFC checks that the output is fully implemented, I had to
 * revert to this class here.
 * 
 * @author peo
 */
@ScaledLong(4)
public class OutputsWithoutCaching implements OutputInterface
{
	public double getResult()
	{
		throw new AbstractMethodError( "" );
	}
	public double getA()
	{
		throw new AbstractMethodError( "" );
	}
	public double getB()
	{
		throw new AbstractMethodError( "" );
	}
	public double getC()
	{
		throw new AbstractMethodError( "" );
	}
	public Iterable<OutputsWithoutCaching> getDetails()
	{
		throw new AbstractMethodError( "" );
	}
	public Double getDoubleObj()
	{
		throw new AbstractMethodError( "" );
	}
	public System getUnsupported()
	{
		throw new AbstractMethodError( "" );
	}
	public BigDecimal getBigDecimalA()
	{
		throw new AbstractMethodError( "" );
	}
	public BigDecimal getBigDecimalB()
	{
		throw new AbstractMethodError( "" );
	}
	public BigDecimal getBigDecimalC()
	{
		throw new AbstractMethodError( "" );
	}
	public BigDecimal getBigResult()
	{
		throw new AbstractMethodError( "" );
	}
	public long getScaledLongA()
	{
		throw new AbstractMethodError( "" );
	}
	public long getScaledLongB()
	{
		throw new AbstractMethodError( "" );
	}
	public long getScaledLongC()
	{
		throw new AbstractMethodError( "" );
	}
	public long getScaledLongResult()
	{
		throw new AbstractMethodError( "" );
	}
}