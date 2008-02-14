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

package org.formulacompiler.compiler.internal.templates;

import org.formulacompiler.runtime.internal.Environment;
import org.formulacompiler.runtime.internal.Runtime_v2;


public final class ExpressionTemplatesForNumbers
{

	private final Environment environment;

	public ExpressionTemplatesForNumbers( Environment _env )
	{
		super();
		this.environment = _env;
	}


	/**
	 * The "String" argument is automatically compiled using the String expression compiler. The
	 * "int" return is automatically converted to the proper output type.
	 */
	public int fun_CODE( String _s )
	{
		return Runtime_v2.fun_CODE( _s, this.environment );
	}

	public int fun_LEN( String a )
	{
		return a.length();
	}


	public boolean fun_EXACT( String a, String b )
	{
		return Runtime_v2.fun_EXACT( a, b );
	}


	public int fun_SEARCH( String _what, String _within )
	{
		return Runtime_v2.fun_SEARCH( _what, _within, 1 );
	}

	public int fun_SEARCH( String _what, String _within, int _startingAt )
	{
		return Runtime_v2.fun_SEARCH( _what, _within, _startingAt );
	}


	public int fun_FIND( String _what, String _within )
	{
		return Runtime_v2.fun_FIND( _what, _within, 1 );
	}

	public int fun_FIND( String _what, String _within, int _startingAt )
	{
		return Runtime_v2.fun_FIND( _what, _within, _startingAt );
	}


	public int fun_ERROR( String _message )
	{
		Runtime_v2.fun_ERROR( _message );
		return -1;
	}

	public int fun_NA()
	{
		Runtime_v2.fun_NA();
		return -1;
	}

}
