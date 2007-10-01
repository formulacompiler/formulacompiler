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
package org.formulacompiler.compiler.internal.logging;

import java.io.PrintStream;

/**
 * Class to help with writing debug logs that have consistent indentation for nested scopes. Typical idiom is:
 * 
 * <pre>
 * if (LOG.e()) LOG.a( "Value is: ").a( value ).lf();
 * </pre>
 * 
 * and, for nested scopes,
 * 
 * <pre>
 * if (LOG.e()) LOG.a( "Entering" ).lf().i();
 * ...
 * if (LOG.e()) LOG.o().a( "Leaving" ).lf();
 * </pre>
 * 
 * @author peo
 */
public final class Log
{
	private static final Logger DEFAULT_LOGGER = new Logger();

	private final Logger logger;
	private boolean enabled = false;

	public Log(Logger _logger)
	{
		this.logger = _logger;
		this.enabled = true;
	}

	public Log()
	{
		this( DEFAULT_LOGGER );
	}

	public final Logger logger()
	{
		return this.logger;
	}


	public final boolean isEnabled()
	{
		return this.enabled;
	}

	public final void setEnabled( boolean _enabled )
	{
		this.enabled = _enabled;
	}


	public final boolean e()
	{
		return isEnabled();
	}


	public Log i()
	{
		if (isEnabled()) logger().indent();
		return this;
	}

	public Log o()
	{
		if (isEnabled()) logger().outdent();
		return this;
	}


	public Log lf()
	{
		if (isEnabled()) logger().newLine();
		return this;
	}


	private final PrintStream stream()
	{
		return logger().stream();
	}


	public Log a( boolean _b )
	{
		if (isEnabled()) stream().print( _b );
		return this;
	}

	public Log a( char _c )
	{
		if (isEnabled()) stream().print( _c );
		return this;
	}

	public Log a( char[] _str )
	{
		if (isEnabled()) stream().print( _str );
		return this;
	}

	public Log a( CharSequence _s )
	{
		if (isEnabled()) stream().print( _s );
		return this;
	}

	public Log a( double _d )
	{
		if (isEnabled()) stream().print( _d );
		return this;
	}

	public Log a( float _f )
	{
		if (isEnabled()) stream().print( _f );
		return this;
	}

	public Log a( int _i )
	{
		if (isEnabled()) stream().print( _i );
		return this;
	}

	public Log a( long _lng )
	{
		if (isEnabled()) stream().print( _lng );
		return this;
	}

	public Log a( Object _obj )
	{
		if (isEnabled()) stream().print( _obj );
		return this;
	}

	public Log a( String _str )
	{
		if (isEnabled()) stream().print( _str );
		return this;
	}

	public Log a( StringBuffer _sb )
	{
		if (isEnabled()) stream().print( _sb );
		return this;
	}


}
