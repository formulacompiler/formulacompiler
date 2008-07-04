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

package org.formulacompiler.runtime;


/**
 * Base class for all exceptions thrown by the run-time engine support of AFC, except for
 * computation errors. The latter are of type {@link FormulaException}.
 * 
 * @author peo
 * 
 * @see FormulaException
 */
public class EngineException extends FormulaCompilerException
{

	public EngineException( String _message, Throwable _cause )
	{
		super( _message, _cause );
	}

	public EngineException( String _message )
	{
		super( _message );
	}

	public EngineException( Throwable _cause )
	{
		super( _cause );
	}

}
