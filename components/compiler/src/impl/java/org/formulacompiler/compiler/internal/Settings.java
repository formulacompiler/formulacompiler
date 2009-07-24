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

package org.formulacompiler.compiler.internal;

import org.formulacompiler.compiler.internal.logging.Log;

/**
 * Holds global settings for AFC.
 * 
 * @author peo
 */
public class Settings
{
	public static final Log LOG_LETVARS = logFor( "letVars" );
	public static final Log LOG_CONSTEVAL = logFor( "constEval" );

	public static final Log logFor( String _name )
	{
		final Log log = new Log();
		log.setEnabled( "true".equals( System.getProperty( "org.formulacompiler.Settings.LOG." + _name + ".enabled" ) ) );
		return log;
	}


	private static boolean debugParserEnabled = false;

	public static boolean isDebugParserEnabled()
	{
		return debugParserEnabled;
	}

	public static void setDebugParserEnabled( boolean _debugParserEnabled )
	{
		debugParserEnabled = _debugParserEnabled;
	}


	private static boolean debugCompilationEnabled = false;

	public static boolean isDebugCompilationEnabled()
	{
		return debugCompilationEnabled;
	}

	public static void setDebugCompilationEnabled( boolean _debugCompilationEnabled )
	{
		debugCompilationEnabled = _debugCompilationEnabled;
	}


}
