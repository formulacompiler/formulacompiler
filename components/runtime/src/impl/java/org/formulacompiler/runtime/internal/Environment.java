package org.formulacompiler.runtime.internal;

import java.util.Locale;
import java.util.TimeZone;

import org.formulacompiler.runtime.Computation;

public final class Environment
{
	public static final Environment DEFAULT = new Environment( new Computation.Config() );

	public final Locale locale;
	public final TimeZone timeZone;

	public Environment(Computation.Config _cfg)
	{
		this.locale = _cfg.locale;
		this.timeZone = (TimeZone) _cfg.timeZone.clone(); // defensive copy as TimeZone is mutable
	}

}
