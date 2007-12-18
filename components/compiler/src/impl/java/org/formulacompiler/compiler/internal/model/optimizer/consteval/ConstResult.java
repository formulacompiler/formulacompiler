/*
 * Copyright (c) 2006 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are prohibited, unless you have been explicitly granted
 * more rights by Abacus Research AG.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.formulacompiler.compiler.internal.model.optimizer.consteval;

import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.TypedResult;

public final class ConstResult implements TypedResult
{
	public static final ConstResult TRUE = new ConstResult( true, DataType.NUMERIC );
	public static final ConstResult FALSE = new ConstResult( false, DataType.NUMERIC );

	private final Object value;
	private final DataType type;

	public static ConstResult valueOf( boolean _value )
	{
		return _value? TRUE : FALSE;
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
