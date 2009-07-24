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

package org.formulacompiler.spreadsheet.internal.binding;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.AbstractDescribable;
import org.formulacompiler.compiler.internal.DescriptionBuilder;
import org.formulacompiler.spreadsheet.internal.CellIndex;

public class InputCellBinding extends CellBinding
{
	private final CallFrame callChainToCall;


	public InputCellBinding( SectionBinding _space, CallFrame _callChainToCall, CellIndex _index )
			throws CompilerException
	{
		super( _space, _index );
		this.callChainToCall = _callChainToCall;
	}


	public CallFrame getCallChainToCall()
	{
		return this.callChainToCall;
	}


	@Override
	public CallFrame boundCall()
	{
		return this.callChainToCall;
	}


	@Override
	public void describeTo( DescriptionBuilder _to )
	{
		getIndex().describeTo( _to );
		if (null != getCallChainToCall()) {
			_to.append( " gets " );
			((AbstractDescribable) getCallChainToCall()).describeTo( _to );
		}
	}

}