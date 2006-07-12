package temp;

import java.math.BigDecimal;
import java.util.Date;

import sej.internal.runtime.RuntimeDouble_v1;
import sej.runtime.Computation;
import sej.tests.utils.InputInterface;
import sej.tests.utils.Inputs;
import sej.tests.utils.Outputs;


public class GeneratedEngine extends Outputs implements Computation
{
	private final Inputs inputs;
	
	private GeneratedEngine(Inputs _inputs)
	{
		super();
		this.inputs = _inputs;
	}

	public GeneratedEngine()
	{
		super();
		this.inputs = null;
	}

	public final Object newComputation( Object _inputs )
	{
		if (null == _inputs) throw new IllegalArgumentException();
		return new GeneratedEngine( (Inputs) _inputs );
	}

	public final double getResult()
	{
		return getIntermediate();
	}

	public final double getA()
	{
		return this.inputs.getOne();
	}

	public final double getB()
	{
		return this.inputs.getPlusOne( 100.0 );
	}

	public final double getC()
	{
		return getA() + getB();
	}

	public final boolean getBool()
	{
		return RuntimeDouble_v1.booleanFromNum( getA() );
	}

	public final Iterable<Outputs> getDetails()
	{
		return null;
	}

	final double getIntermediate()
	{
		return 123.0;
	}

	final double getMin()
	{
		return RuntimeDouble_v1.min( getA(), getB() );
	}

	final double getSum()
	{
		return getA() + getIteration();
	}

	final double getIteration()
	{
		GeneratedEngine[] c = null; 
		if (0 == c.length) return 0.0;
		double result = c[ 0 ].getIntermediate();
		for (int i = 1; i < c.length; i++)
			result += c[ i ].getIntermediate();
		return result;
	}

	final double getAverage()
	{
		double sum = 0;
		int n = 0;
		sum += getA();
		n++;
		sum += getB();
		n++;
		return sum / n;
	}

	final double getExp()
	{
		return Math.pow( getA(), getB() );
	}

	final double getIf()
	{
		return getA() > getB() ? 1.0 : 0.0;
	}


	final double getAndIf()
	{
		boolean a = false, b = false, c = false;
		return (a && b) ? 1.0 : 0.0;
	}


	final double getBoolIf()
	{
		double a = 0;
		return (a != 0) ? 1 : 0;
	}


	final double getRewrittenAndIf()
	{
		if (getA() > getB()) {
			if (!(getA() < getB())) {
				return 1.0;
			}
		}
		return 0.0;
	}
	
	
	final BigDecimal getBigAndIf()
	{
		return (getBigDecimalA().equals( getBigDecimalB() )) ? getBigDecimalA() : getBigDecimalB();
	}


	final BigDecimal getBigAndIf2()
	{
		return (getBigDecimalA().compareTo( getBigDecimalB() ) > 0) ? getBigDecimalA() : getBigDecimalB();
	}


	private InputInterface ii;

	final double getIntf()
	{
		return this.ii.getOne();
	}


	final double getDate()
	{
		Date date = null;
		return RuntimeDouble_v1.dateToNum( date );
	}


	final double getWithPrec()
	{
		return RuntimeDouble_v1.round( getSum(), 2 );
	}

	public Double getDoubleObj()
	{
		return 123.45;
	}

	public System getUnsupported()
	{
		return null;
	}

	final double getWithEnum()
	{
		return getEnumd( TestEnum.TWO );
	}


	private double getEnumd( TestEnum _two )
	{
		return 0;
	}


	final BigDecimal getBigAdd()
	{
		return BigDecimal.ONE.add( BigDecimal.TEN );
	}

	final BigDecimal getBigPercent()
	{
		return BigDecimal.ONE.movePointLeft( 2 );
	}

	final BigDecimal getBigExp()
	{
		return BigDecimal.ONE.pow( BigDecimal.TEN.intValue() );
	}

	final BigDecimal getBigZero()
	{
		return BigDecimal.ZERO;
	}

	final BigDecimal getBigConst()
	{
		return new BigDecimal( "123" );
	}
	

	private final static double[] getStaticIndex_Consts = new double[] { 0, 1, 2, 3, 4, 0, 6 };

	public double getStaticIndex()
	{
		final int i = (int) getA() - 1;
		switch (i) {
		case 5:
			return getB();
		case 6:
			return getA();
		case 9:
			return getC();
		case 100:
			return 0;
		default:
			return (i >= 0 && i < getStaticIndex_Consts.length) ? getStaticIndex_Consts[ i ] : 0;
		}

	}
	
	public BigDecimal getIndex()
	{
		final int i = getBigDecimalA().intValue() - 1;
		return BigDecimal.ZERO;
	}

	public BigDecimal getBigDecimalA()
	{
		return null;
	}

	public BigDecimal getBigDecimalB()
	{
		return null;
	}

	public BigDecimal getBigDecimalC()
	{
		return null;
	}

	public BigDecimal getBigResult()
	{
		return null;
	}
}
