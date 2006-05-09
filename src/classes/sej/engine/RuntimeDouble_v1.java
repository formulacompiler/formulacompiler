package sej.engine;

import java.util.Date;

public final class RuntimeDouble_v1 extends Runtime_v1
{

	public static void logDouble( final double _value, final String _message )
	{
		System.out.print( _message );
		System.out.print( _value );
		System.out.println();
	}

	public static double max( final double a, final double b )
	{
		return a >= b ? a : b;
	}

	public static double min( final double a, final double b )
	{
		return a <= b ? a : b;
	}

	public static double round( final double _val, final int _maxFrac )
	{
		final double shift = Math.pow( 10, _maxFrac );
		if (0 > _val) {
			return Math.ceil( _val * shift - 0.5 ) / shift;
		}
		else {
			return Math.floor( _val * shift + 0.5 ) / shift;
		}
	}

	public static double stdROUND( final double _val, final double _maxFrac )
	{
		return round( _val, (int) _maxFrac );
	}

	
	public static boolean booleanFromExcel( final double _val )
	{
		return (_val != 0);
	}

	public static double booleanToExcel( final boolean _val )
	{
		return _val ? 1.0 : 0.0;
	}

	
	// ---- Excel date conversion; copied from JExcelAPI (DateRecord.java)

	public static Date dateFromExcel( final double _excel )
	{
		final boolean time = (Math.abs( _excel ) < 1);
		double numValue = _excel;
	
		// Work round a bug in excel. Excel seems to think there is a date
		// called the 29th Feb, 1900 - but in actual fact this was not a leap year.
		// Therefore for values less than 61 in the 1900 date system,
		// add one to the numeric value
		if (!basedOn1904 && !time && numValue < nonLeapDay) {
			numValue += 1;
		}
	
		// Convert this to the number of days since 01 Jan 1970
		int offsetDays = basedOn1904 ? utcOffsetDays1904 : utcOffsetDays;
		double utcDays = numValue - offsetDays;
	
		// Convert this into utc by multiplying by the number of milliseconds
		// in a day. Use the round function prior to ms conversion due
		// to a rounding feature of Excel (contributed by Jurgen
		long utcValue = Math.round( utcDays * secondsInADay ) * msInASecond;
	
		return new Date( utcValue );
	}

	public static double dateToExcel( final Date _date )
	{
		final long utcValue = _date.getTime();
		final boolean time = (utcValue < msInADay);
	
		// Convert this to the number of days, plus fractions of a day since
		// 01 Jan 1970
		final double utcDays = (double) utcValue / (double) msInADay;
	
		// Add in the offset to get the number of days since 01 Jan 1900
		double value = utcDays + utcOffsetDays;
	
		// Work round a bug in excel. Excel seems to think there is a date
		// called the 29th Feb, 1900 - but this was not a leap year.
		// Therefore for values less than 61, we must subtract 1. Only do
		// this for full dates, not times
		if (!time && value < nonLeapDay) {
			value -= 1;
		}
	
		// If this refers to a time, then get rid of the integer part
		if (time) {
			value = value - (int) value;
		}
	
		return value;
	}

}
