package sej.engine.expressions;

import java.math.BigDecimal;

import sej.ModelError;
import sej.NumericType;

public class InterpretedNumericType
{

	InterpretedNumericType()
	{
		super();
	}

	public static InterpretedNumericType typeFor( NumericType _type ) throws ModelError
	{
		if (Double.TYPE == _type.getValueType()) {
			return new DoubleType();
		}
		else if (BigDecimal.class == _type.getValueType()) {
			return new BigDecimalType( _type.getScale() );
		}
		else {
			throw new ModelError.UnsupportedDataType( "Unsupported numeric type for run-time interpretation." );
		}
	}
	
	
	private static class DoubleType extends InterpretedNumericType
	{
		// Nothing so far.
	}
	

	private static class BigDecimalType extends InterpretedNumericType
	{

		public BigDecimalType(int _scale)
		{
			// TODO Auto-generated constructor stub
		}
		
	}
	

}
