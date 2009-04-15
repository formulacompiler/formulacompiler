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

package org.formulacompiler.runtime.event;

import java.util.EventObject;


/**
 * This event is emitted after a cell has been computed.
 *
 * @author Vladimir Korenev
 * @see CellComputationListener
 * @see org.formulacompiler.runtime.Computation.Config#cellComputationListener
 */
public class CellComputationEvent extends EventObject
{
	private final Object value;

	/**
	 * Creates a new event.
	 *
	 * @param _cell  the cell which contains the computation.
	 * @param _value the computed value.
	 */
	public CellComputationEvent( Object _cell, Object _value )
	{
		super( _cell );
		this.value = _value;
	}

	/**
	 * Returns the computed cell value.
	 *
	 * @return computed value.
	 */
	public Object getValue()
	{
		return this.value;
	}

	public String toString()
	{
		return getClass().getName() + "[cell=" + this.source + ",value=" + this.value + "]";
	}
}
