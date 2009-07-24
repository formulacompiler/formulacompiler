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

package org.formulacompiler.tests.utils;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.FormulaCompiler;

public abstract class AbstractStandardInputsOutputsTestCase extends AbstractIOTestCase
{

	protected AbstractStandardInputsOutputsTestCase()
	{
		super();
	}

	protected AbstractStandardInputsOutputsTestCase( String _name )
	{
		super( _name );
	}


	protected CallFrame getInput( String _name ) throws SecurityException, NoSuchMethodException
	{
		return FormulaCompiler.newCallFrame( Inputs.class.getMethod( _name ) );
	}

	protected CallFrame getOutput( String _name ) throws SecurityException, NoSuchMethodException
	{
		return FormulaCompiler.newCallFrame( OutputsWithoutReset.class.getMethod( _name ) );
	}


}
