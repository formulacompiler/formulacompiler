package temp;

import java.math.BigDecimal;
import java.math.BigInteger;

import sej.runtime.Computation;

public final class GeneratedComputation implements Computation, MyComputation
{
	private final MyInputs inputs;

	public GeneratedComputation(MyInputs _inputs)
	{
		this.inputs = _inputs;
	}

	public double getResult()
	{
		return this.inputs.getOne() + this.inputs.getTwo();
	}

	public double inbyte()
	{
		return this.inputs.getbyte();
	}

	public double inByte()
	{
		return this.inputs.getByte();
	}

	public byte getByte()
	{
		return (byte) getResult();
	}

	public short getShort()
	{
		return (short) getResult();
	}

	public int getInt()
	{
		return (int) getResult();
	}

	public long getLong()
	{
		return (long) getResult();
	}

	public Integer getInteger()
	{
		return (int) getResult();
	}

	public BigInteger getBigInteger()
	{
		return BigInteger.valueOf( (long) getResult() );
	}


	public BigDecimal getBD()
	{
		return BigDecimal.valueOf( 1 );
	}

	public Float getFloat()
	{
		return Float.valueOf( getBD().floatValue() );
	}

	public BigDecimal useBD()
	{
		final BigDecimal bd = getBD();
		return bd == null ? BigDecimal.ZERO : bd;
	}

}
