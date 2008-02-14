/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * This file is part of the Abacus Formula Compiler (AFC).
 *
 * For commercial licensing, please contact sales(at)formulacompiler.com.
 *
 * AFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AFC.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.formulacompiler.compiler.internal.logging;

import java.io.PrintStream;

/**
 * Class to help with writing debug logs that have consistent indentation for nested scopes. Typical
 * idiom is:
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

	public Log( Logger _logger )
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
