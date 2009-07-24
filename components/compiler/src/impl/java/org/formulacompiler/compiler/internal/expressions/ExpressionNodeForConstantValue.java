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

package org.formulacompiler.compiler.internal.expressions;

import java.util.Collection;

import org.formulacompiler.compiler.internal.DescriptionBuilder;


public final class ExpressionNodeForConstantValue extends ExpressionNode
{
	private final Object value;


	public ExpressionNodeForConstantValue( Object _value )
	{
		assert !(_value instanceof ExpressionNode);
		this.value = _value;
	}


	public ExpressionNodeForConstantValue( Object _value, DataType _type )
	{
		assert !(_value instanceof ExpressionNode);
		this.value = _value;
		setDataType( _type );
	}


	public Object value()
	{
		return this.value;
	}


	@Override
	public boolean hasConstantValue()
	{
		return DataType.isValueConstant( getConstantValue() );
	}

	@Override
	public Object getConstantValue()
	{
		return value();
	}


	@Override
	public ExpressionNode innerCloneWithoutArguments()
	{
		return new ExpressionNodeForConstantValue( this.value );
	}


	@Override
	protected int countValuesCore( Collection<ExpressionNode> _uncountables )
	{
		return 1;
	}


	@Override
	public void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg )
	{
		if (this.value instanceof String) {
			_to.append( '"' );
			_to.append( this.value );
			_to.append( '"' );
		}
		else {
			_to.append( this.value );
		}
	}

}
