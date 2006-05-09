package sej.engine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

public class RuntimeBigDecimal_v1 extends Runtime_v1
{

	public static BigDecimal newBigDecimal( final String _value )
	{
		return new BigDecimal( _value );
	}

	public static BigDecimal newBigDecimal( final long _value )
	{
		return new BigDecimal( _value );
	}

	public static BigDecimal round( final BigDecimal _val, final int _maxFrac )
	{
		return _val.setScale( _maxFrac, RoundingMode.HALF_UP );
	}

	public static BigDecimal stdROUND( final BigDecimal _val, final BigDecimal _maxFrac )
	{
		return round( _val, _maxFrac.intValue() );
	}


	public static boolean booleanFromExcel( final BigDecimal _val )
	{
		return (_val.compareTo( BigDecimal.ZERO ) != 0);
	}

	public static BigDecimal booleanToExcel( final boolean _val )
	{
		return _val ? BigDecimal.ONE : BigDecimal.ZERO;
	}


	private static BigDecimal MSINADAY = new BigDecimal( msInADay );
	private static BigDecimal NONLEAPDAY = new BigDecimal( nonLeapDay );
	private static BigDecimal UTCOFFSETDAYS = new BigDecimal( utcOffsetDays );


	public static Date dateFromExcel( final BigDecimal _excel )
	{
		return RuntimeDouble_v1.dateFromExcel( _excel.doubleValue() );
	}

	public static BigDecimal dateToExcel( final Date _date )
	{
		final long utcValue = _date.getTime();
		final boolean time = (utcValue < msInADay);

		// Convert this to the number of days, plus fractions of a day since
		// 01 Jan 1970
		final BigDecimal utcDays = new BigDecimal( utcValue ).divide( MSINADAY, 8, RoundingMode.HALF_UP );

		// Add in the offset to get the number of days since 01 Jan 1900
		BigDecimal value = utcDays.add( UTCOFFSETDAYS );

		// Work round a bug in excel. Excel seems to think there is a date
		// called the 29th Feb, 1900 - but this was not a leap year.
		// Therefore for values less than 61, we must subtract 1. Only do
		// this for full dates, not times
		if (!time && value.compareTo( NONLEAPDAY ) < 0) {
			value = value.subtract( BigDecimal.ONE );
		}

		// If this refers to a time, then get rid of the integer part
		if (time) {
			value = value.subtract( new BigDecimal( value.toBigInteger() ) );
		}

		return value;
	}

}
