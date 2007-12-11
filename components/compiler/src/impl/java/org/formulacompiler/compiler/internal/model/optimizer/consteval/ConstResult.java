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
