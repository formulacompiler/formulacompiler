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

package org.formulacompiler.compiler.internal.model.optimizer.consteval;

import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.TypedResult;

public final class ConstResult implements TypedResult
{
	public static final ConstResult TRUE = new ConstResult( true, DataType.NUMERIC );
	public static final ConstResult FALSE = new ConstResult( false, DataType.NUMERIC );
	public static final ConstResult NULL = new ConstResult( null, DataType.NULL );

	private final Object value;
	private final DataType type;

	public static ConstResult valueOf( boolean _value )
	{
		return _value ? TRUE : FALSE;
	}

	protected ConstResult( Object _value, DataType _type )
	{
		super();
		this.value = _value;
		this.type = _type;
	}

	public boolean isConstant()
	{
		return hasConstantValue();
	}

	public boolean hasConstantValue()
	{
		return DataType.isValueConstant( getConstantValue() );
	}

	public Object getConstantValue()
	{
		return this.value;
	}

	public DataType getDataType()
	{
		return this.type;
	}

	@Override
	public String toString()
	{
		if (this.value == null) {
			return "#NULL";
		}
		if (this.type == null) {
			return this.value.toString() + " [?]";
		}
		return this.value.toString() + " [" + this.type.name() + "]";
	}

}
