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

import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.TimeZone;

import org.formulacompiler.runtime.Computation;

public final class Environment
{
	public static final Environment DEFAULT = new Environment( new Computation.Config() );

	private final Locale locale;
	private final DecimalFormatSymbols decimalFormatSymbols;
	private final TimeZone timeZone;

	public Environment(Computation.Config _cfg)
	{
		this.locale = _cfg.locale;
		this.decimalFormatSymbols = _cfg.decimalFormatSymbols;

		// Defensive copy below as TimeZone is mutable.
		this.timeZone = (null == _cfg.timeZone) ? null : (TimeZone) _cfg.timeZone.clone();
	}

	public Locale locale()
	{
		return (null == this.locale)? Locale.getDefault() : this.locale;
	}

	public DecimalFormatSymbols decimalFormatSymbols()
	{
		return decimalFormatSymbols;
	}

	public TimeZone timeZone()
	{
		return (null == this.timeZone)? TimeZone.getDefault() : this.timeZone;
	}

}
