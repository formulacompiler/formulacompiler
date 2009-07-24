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

package org.formulacompiler.runtime;


/**
 * Runtime exception thrown by compiled computations for conditions normally flagged as
 * {@code #VALUE!}, {@code #NUM!}, or {@code #DIV/0!}. The spreadsheet functions {@code ISERR()}
 * and {@code ISERROR()} both trap this error.
 * 
 * @author peo
 * 
 * @see NotAvailableException
 */
public class FormulaException extends ComputationException
{

	public FormulaException()
	{
		super();
	}

	public FormulaException( String _message, Throwable _cause )
	{
		super( _message, _cause );
	}

	public FormulaException( String _message )
	{
		super( _message );
	}

	public FormulaException( Throwable _cause )
	{
		super( _cause );
	}

}
