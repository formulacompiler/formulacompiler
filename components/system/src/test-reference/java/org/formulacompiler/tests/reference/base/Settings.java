/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
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

package org.formulacompiler.tests.reference.base;

public final class Settings
{
	public static boolean EMIT_DOCUMENTATION = isSysPropTrue( "emit_documentation" );
	public static boolean QUICK_RUN = isSysPropTrue( "quick_run" );
	public static boolean THREADED_RUN = isSysPropTrue( "threaded_run" );

	private static boolean isSysPropTrue( String _name )
	{
		return "true".equals( System.getProperty( "org.formulacompiler.tests.reference." + _name ) );
	}

}
