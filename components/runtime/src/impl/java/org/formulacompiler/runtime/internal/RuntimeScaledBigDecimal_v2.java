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
package org.formulacompiler.runtime.internal;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class RuntimeScaledBigDecimal_v2 extends RuntimeBigDecimal_v2
{
	public static final MathContext HIGHPREC = MathContext.DECIMAL128;
	public static final MathContext UNLIMITED = MathContext.UNLIMITED;

	
	public static BigDecimal fun_DEGREES( BigDecimal _a )
	{
		final BigDecimal product = _a.multiply( BigDecimal.valueOf( 180 ) );
		return product.divide( PI, RoundingMode.HALF_UP );
	}

	public static BigDecimal fun_RADIANS( BigDecimal _a )
	{
		final BigDecimal product = _a.multiply( PI );
		return product.divide( BigDecimal.valueOf( 180 ), RoundingMode.HALF_UP );
	}


}
