package sej;

import java.math.BigDecimal;

public final class NumericType
{
	public static final int UNDEFINED_SCALE = Integer.MAX_VALUE;
	public static final NumericType DOUBLE = newInstance( Double.TYPE );
	public static final NumericType BIGDECIMAL = newInstance( BigDecimal.class );
	public static final NumericType LONG = newInstance( Long.TYPE );
	public static final NumericType CURRENCY = newInstance( Long.TYPE, 4 );
	
	private final Class valueType;
	private final int scale; 

	
	private NumericType(Class _valueType, int _scale)
	{
		super();
		this.valueType = _valueType;
		this.scale = _scale;
	}

	
	public static NumericType newInstance( Class _valueType )
	{
		return newInstance( _valueType, UNDEFINED_SCALE );
	}
	
	public static NumericType newInstance( Class _valueType, int _scale )
	{
		return new NumericType( _valueType, _scale );
	}


	public Class getValueType()
	{
		return this.valueType;
	}

	public int getScale()
	{
		return this.scale;
	}

}
