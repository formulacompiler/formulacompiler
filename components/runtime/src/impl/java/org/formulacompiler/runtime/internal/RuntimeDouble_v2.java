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
package org.formulacompiler.runtime.internal;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.TimeZone;

import org.formulacompiler.runtime.internal.cern.jet.stat.Gamma;
import org.formulacompiler.runtime.internal.cern.jet.stat.Probability;


public final class RuntimeDouble_v2 extends Runtime_v2
{
	private static final double EXCEL_EPSILON = 0.0000001;
	private static final double[] POW10 = { 1E-10, 1E-9, 1E-8, 1E-7, 1E-6, 1E-5, 1E-4, 1E-3, 1E-2, 1E-1, 1, 1E+1, 1E+2,
			1E+3, 1E+4, 1E+5, 1E+6, 1E+7, 1E+8, 1E+9, 1E+10 };


	public static double max( final double a, final double b )
	{
		return a >= b? a : b;
	}

	public static double min( final double a, final double b )
	{
		return a <= b? a : b;
	}

	public static double fun_CEILING( final double _number, final double _significance )
	{
		final double a = _number / _significance;
		if (a < 0) {
			return 0.0; // Excel #NUM
		}
		return roundUp( a ) * _significance;
	}

	public static double fun_FLOOR( final double _number, final double _significance )
	{
		final double a = _number / _significance;
		if (a < 0) {
			return 0.0; // Excel #NUM
		}
		return roundDown( a ) * _significance;
	}

	public static double fun_RAND()
	{
		return generator.nextDouble();
	}

	// Leave this comment in. It is used to cite the code into the documentation.
	// ---- round
	public static double round( final double _val, final int _maxFrac )
	{
		final double shift = pow10( _maxFrac );
		if (0 > _val) {
			return Math.ceil( _val * shift - 0.5 ) / shift;
		}
		else {
			return Math.floor( _val * shift + 0.5 ) / shift;
		}
	}
	// ---- round

	public static double fun_ROUNDDOWN( final double _val, final int _maxFrac )
	{
		final double shift = pow10( _maxFrac );
		return roundDown( _val * shift ) / shift;
	}

	public static double fun_ROUNDUP( final double _val, final int _maxFrac )
	{
		final double shift = pow10( _maxFrac );
		return roundUp( _val * shift ) / shift;
	}

	// In case a>709 Excel returns error (too large value)
	public static double fun_SINH( final double a )
	{
		if (a > 709) {
			return 0;
		}
		else {
			return Math.sinh( a );
		}
	}

	public static double fun_ACOSH( final double a )
	{
		if (a < 1) {
			return 0;
		}
		else {
			return Math.log( a + Math.sqrt( a * a - 1 ) );
		}
	}

	public static double fun_ASINH( final double a )
	{
		return Math.log( a + Math.sqrt( a * a + 1 ) );
	}

	public static double fun_ATANH( final double a )
	{
		if (a <= -1 || a >= 1) {
			return 0;
		}
		else {
			return Math.log( (1 + a) / (1 - a) ) / 2;
		}
	}

	public static double trunc( final double _val, final int _maxFrac )
	{
		final double shift = pow10( _maxFrac );
		return roundDown( _val * shift ) / shift;
	}

	private static double roundDown( final double _val )
	{
		return 0 > _val? Math.ceil( _val ) : Math.floor( _val );
	}

	private static double roundUp( final double _val )
	{
		return 0 > _val? Math.floor( _val ) : Math.ceil( _val );
	}

	private static double pow10( final int _exp )
	{
		return (_exp >= -10 && _exp <= 10)? POW10[ _exp + 10 ] : Math.pow( 10, _exp );
	}

	public static boolean booleanFromNum( final double _val )
	{
		return (_val != 0);
	}

	public static double booleanToNum( final boolean _val )
	{
		return _val? 1.0 : 0.0;
	}

	public static double numberToNum( final Number _num )
	{
		return (_num == null)? 0.0 : _num.doubleValue();
	}


	public static double fromScaledLong( long _scaled, long _scalingFactor )
	{
		return ((double) _scaled) / ((double) _scalingFactor);
	}

	public static long toScaledLong( double _value, long _scalingFactor )
	{
		return (long) (_value * _scalingFactor);
	}


	public static String toExcelString( double _value, Environment _environment )
	{
		return stringFromBigDecimal( BigDecimal.valueOf( _value ), _environment );
	}


	// ---- Excel date conversion; copied from JExcelAPI (DateRecord.java)

	public static Date dateFromNum( final double _excel, TimeZone _timeZone )
	{
		return dateFromDouble( _excel, _timeZone );
	}

	public static long msSinceUTC1970FromNum( double _msSinceUTC1970, TimeZone _timeZone )
	{
		return msSinceUTC1970FromDouble( _msSinceUTC1970, _timeZone );
	}

	public static long msFromNum( double _msSinceUTC1970 )
	{
		return msFromDouble( _msSinceUTC1970 );
	}

	public static double dateToNum( final Date _date, TimeZone _timeZone )
	{
		return dateToDouble( _date, _timeZone );
	}

	public static double msSinceUTC1970ToNum( long _msSinceUTC1970, TimeZone _timeZone )
	{
		return msSinceUTC1970ToDouble( _msSinceUTC1970, _timeZone );
	}

	public static double msToNum( long _msSinceUTC1970 )
	{
		return msToDouble( _msSinceUTC1970 );
	}

	private static double valueOrZero( final double _value )
	{
		if (Double.isNaN( _value ) || Double.isInfinite( _value )) {
			return 0.0; // Excel #NUM
		}
		return _value;
	}


	public static double fun_DATE( final int _year, final int _month, final int _day )
	{
		final int year = _year < 1899? _year + 1900 : _year;
		return dateToNum( year, _month, _day );
	}

	private static double dateToNum( final int _year, final int _month, final int _day )
	{
		final Calendar calendar = new GregorianCalendar( TimeZone.getTimeZone( "GMT" ) );
		calendar.clear();
		calendar.setLenient( true );
		calendar.set( Calendar.YEAR, _year );
		calendar.set( Calendar.MONTH, _month - 1 );
		calendar.set( Calendar.DAY_OF_MONTH, _day );
		final Date date = calendar.getTime();
		final TimeZone timeZone = calendar.getTimeZone();
		return dateToNum( date, timeZone );
	}

	public static int fun_WEEKDAY( final double _date, int _type )
	{
		final int dayOfWeek = getCalendarValueFromNum( _date, Calendar.DAY_OF_WEEK );
		switch (_type) {
			case 1:
				return dayOfWeek;
			case 2:
				return dayOfWeek > 1? dayOfWeek - 1 : 7;
			case 3:
				return dayOfWeek > 1? dayOfWeek - 2 : 6;
			default:
				return 0; // Excel #NUM
		}
	}

	static long getDaySecondsFromNum( final double _time )
	{
		final double time = _time % 1;
		return Math.round( time * SECS_PER_DAY );
	}

	public static int fun_DAY( final double _date )
	{
		return getCalendarValueFromNum( _date, Calendar.DAY_OF_MONTH );
	}

	public static int fun_MONTH( final double _date )
	{
		return getCalendarValueFromNum( _date, Calendar.MONTH ) + 1;
	}

	public static int fun_YEAR( final double _date )
	{
		return getCalendarValueFromNum( _date, Calendar.YEAR );
	}

	private static int getCalendarValueFromNum( double _date, int _field )
	{
		final Calendar calendar = new GregorianCalendar( TimeZone.getTimeZone( "GMT" ) );
		final TimeZone timeZone = calendar.getTimeZone();
		final Date date = dateFromNum( _date, timeZone );
		calendar.setTime( date );
		return calendar.get( _field );
	}

	public static double fun_NOW( final Environment _environment, final ComputationTime _computationTime )
	{
		return dateToNum( now( _computationTime ), _environment.timeZone() );
	}

	public static double fun_TODAY( final Environment _environment, final ComputationTime _computationTime )
	{
		final TimeZone timeZone = _environment.timeZone();
		return dateToNum( today( timeZone, _computationTime ), timeZone );
	}

	public static double fun_TIME( double _hour, double _minute, double _second )
	{
		final long seconds = ((long) _hour * SECS_PER_HOUR + (long) _minute * 60 + (long) _second) % SECS_PER_DAY;
		return (double) seconds / SECS_PER_DAY;
	}

	public static double fun_SECOND( double _date )
	{
		final long seconds = getDaySecondsFromNum( _date ) % 60;
		return seconds;
	}

	public static double fun_MINUTE( double _date )
	{
		final long minutes = getDaySecondsFromNum( _date ) / 60 % 60;
		return minutes;
	}

	public static double fun_HOUR( double _date )
	{
		final long hours = getDaySecondsFromNum( _date ) / SECS_PER_HOUR % 24;
		return hours;
	}

	private static double mulRange( int m, int n )
	{
		double res = 1;
		for (int i = m + 1; i <= n; i++) {
			res *= i;
		}
		return res;
	}

	private static class Segment
	{
		int start;
		int end;

		private Segment( final int _start, final int _end )
		{
			this.start = _start;
			this.end = _end;
		}
	}

	public static double fun_HYPGEOMDIST( int _x, int _n, int _M, int _N )
	{
		if ((_x < 0) || (_n < _x) || (_M < _x) || (_N < _n) || (_N < _M) || (_x < _n - _N + _M)) {
			// Illegal Argument
			return 0;
		}
		if (_N < 100) {
			// simple method which works for small numbers
			return mulRange( _M - _x, _M )
					* mulRange( _n - _x, _n ) / mulRange( _N - _M, _N ) * mulRange( _N - _n - _M + _x, _N - _n )
					/ mulRange( 1, _x );
		}
		else {
			// algorithm for large numbers
			LinkedList<Segment> numerator = new LinkedList<Segment>();
			LinkedList<Segment> denominator = new LinkedList<Segment>();
			numerator.add( new Segment( _M - _x + 1, _M ) );
			numerator.add( new Segment( _n - _x + 1, _n ) );
			numerator.add( new Segment( _N - _n - _M + _x + 1, _N - _n ) );
			denominator.add( new Segment( 1, _x ) );
			denominator.add( new Segment( _N - _M + 1, _N ) );
			for (int i = 0; i < numerator.size(); i++) {
				for (int j = 0; j < denominator.size(); j++) {
					if (i < 0) i++;
					Segment numeratorElement = numerator.get( i );
					Segment denominatorElement = denominator.get( j );
					if (numeratorElement.start > denominatorElement.start) {
						if (numeratorElement.start <= denominatorElement.end) {
							if (numeratorElement.end > denominatorElement.end) {
								int tmp = numeratorElement.start;
								numeratorElement.start = denominatorElement.end + 1;
								denominatorElement.end = tmp - 1;
							}
							else {
								if (numeratorElement.end < denominatorElement.end) {
									denominator.add( new Segment( numeratorElement.end + 1, denominatorElement.end ) );
								}
								denominatorElement.end = numeratorElement.start - 1;
								numerator.remove( i );
								i--;
							}
						}
					}
					else {
						if (numeratorElement.start < denominatorElement.start) {
							if (numeratorElement.end >= denominatorElement.start) {
								if (numeratorElement.end < denominatorElement.end) {
									int tmp = denominatorElement.start;
									denominatorElement.start = numeratorElement.end + 1;
									numeratorElement.end = tmp - 1;
								}
								else {
									if (numeratorElement.end > denominatorElement.end) {
										numerator.add( new Segment( denominatorElement.end + 1, numeratorElement.end ) );
									}
									numeratorElement.end = denominatorElement.start - 1;
									denominator.remove( j );
									j--;
								}
							}
						}
						else {
							if (numeratorElement.end < denominatorElement.end) {
								denominatorElement.start = numeratorElement.end + 1;
								numerator.remove( i );
								i--;
							}
							else {
								if (numeratorElement.end > denominatorElement.end) {
									numeratorElement.start = denominatorElement.end + 1;
									denominator.remove( j );
									j--;
								}
								else {
									denominator.remove( j );
									j--;
									numerator.remove( i );
									i--;
								}
							}
						}
					}
				}
			}
			double res = 1;
			int numeratorIndex = 0;
			int numeratorCurrMult = 0;
			int denominatorIndex = 0;
			int denominatorCurrMult = 0;
			double upperLimit = 1E+250;
			double lowerLimit = 1E-250;
			while (numeratorIndex < numerator.size() || denominatorIndex < denominator.size()) {
				if ((res >= upperLimit & denominatorIndex >= denominator.size())
						|| (res <= lowerLimit & numeratorIndex >= numerator.size())) {
					res = 0;
					numeratorIndex = numerator.size();
					denominatorIndex = denominator.size();
				}
				while (numeratorIndex < numerator.size() & res < upperLimit) {
					Segment numeratorElement = numerator.get( numeratorIndex );
					if (numeratorCurrMult == 0) {
						numeratorCurrMult = numeratorElement.start;
					}
					while (numeratorCurrMult <= numeratorElement.end & res < upperLimit) {
						res *= numeratorCurrMult;
						numeratorCurrMult++;
					}
					if (numeratorCurrMult > numeratorElement.end) {
						numeratorIndex++;
						numeratorCurrMult = 0;
					}
				}
				while (denominatorIndex < denominator.size() & res > lowerLimit) {
					Segment denominatorElement = denominator.get( denominatorIndex );
					if (denominatorCurrMult == 0) {
						denominatorCurrMult = denominatorElement.start;
					}
					while (denominatorCurrMult <= denominatorElement.end & res > lowerLimit) {
						res /= denominatorCurrMult;
						denominatorCurrMult++;
					}
					if (denominatorCurrMult > denominatorElement.end) {
						denominatorIndex++;
						denominatorCurrMult = 0;
					}
				}
			}
			return res;
		}
	}

	public static double fun_ACOS( double _a )
	{
		if (_a < -1 || _a > 1) {
			return 0.0; // Excel #NUM!
		}
		else {
			return Math.acos( _a );
		}
	}

	public static double fun_ASIN( double _a )
	{
		if (_a < -1 || _a > 1) {
			return 0.0; // Excel #NUM!
		}
		else {
			return Math.asin( _a );
		}
	}

	public static double fun_TRUNC( final double _val )
	{
		return roundDown( _val );
	}

	public static double fun_EVEN( final double _val )
	{
		return roundUp( _val / 2 ) * 2;
	}

	public static double fun_ODD( final double _val )
	{
		if (0 > _val) {
			return Math.floor( (_val - 1) / 2 ) * 2 + 1;
		}
		else {
			return Math.ceil( (_val + 1) / 2 ) * 2 - 1;
		}
	}

	public static double fun_POWER( final double _n, final double _p )
	{
		return valueOrZero( Math.pow( _n, _p ) );
	}

	public static double fun_LN( final double _p )
	{
		return valueOrZero( Math.log( _p ) );
	}

	public static double fun_LOG( final double _n, final double _x )
	{
		final double lnN = Math.log( _n );
		if (Double.isNaN( lnN ) || Double.isInfinite( lnN )) {
			return 0; // Excel #NUM!
		}
		final double lnX = Math.log( _x );
		if (Double.isNaN( lnX ) || Double.isInfinite( lnX )) {
			return 0; // Excel #NUM!
		}
		if (lnX == 0) {
			return 0; // Excel #DIV/0!
		}
		return lnN / lnX;
	}

	public static double fun_LOG10( final double _p )
	{
		return valueOrZero( Math.log10( _p ) );
	}

	public static double fun_ERF( double x )
	{
		return Probability.errorFunction( x );
	}

	public static double fun_ERFC( double a )
	{
		return Probability.errorFunctionComplemented( a );
	}

	public static double fun_BETADIST( double _x, double _alpha, double _beta )
	{
		if (_alpha <= 0 || _beta <= 0 || _x < 0 || _x > 1) {
			return 0; // Excel #NUM!
		}

		return Probability.beta( _alpha, _beta, _x );
	}

	public static double fun_BINOMDIST( int _successes, int _trials, double _probability, boolean _cumulative )
	{
		if (_successes < 0 || _successes > _trials || _probability < 0 || _probability > 1) {
			return 0; // Excel #NUM!
		}

		if (_cumulative) {
			return binomialCumulative( _successes, _trials, _probability );
		}
		else {
			return binomialDensity( _successes, _trials, _probability );
		}
	}

	private static double binomialCumulative( final int _successes, final int _trials, final double _probability )
	{
		return Probability.binomial( _successes, _trials, _probability );
	}

	private static double binomialDensity( final int _successes, final int _trials, final double _probability )
	{
		final double q = 1.0 - _probability;
		double factor = Math.pow( q, _trials );
		if (factor == 0.0) {
			factor = Math.pow( _probability, _trials );
			if (factor == 0.0) {
				return 0; // Excel #NUM!
			}
			else {
				final int max = _trials - _successes;
				for (int i = 0; i < max && factor > 0.0; i++) {
					factor *= ((double) (_trials - i)) / ((double) (i + 1)) * q / _probability;
				}
				return factor;

			}
		}
		else {
			for (int i = 0; i < _successes && factor > 0.0; i++) {
				factor *= ((double) (_trials - i)) / ((double) (i + 1)) * _probability / q;
			}
			return factor;
		}
	}

	interface StatisticDistFunc
	{
		public double GetValue( double x );
	}

	private static class BetaDistFunction implements StatisticDistFunc
	{
		double x0, alpha, beta;

		BetaDistFunction( double x0, double alpha, double beta )
		{
			this.x0 = x0;
			this.alpha = alpha;
			this.beta = beta;
		}

		public double GetValue( double x )
		{
			return this.x0 - fun_BETADIST( x, this.alpha, this.beta );
		}
	}

	private static double iterateInverse( StatisticDistFunc func, double _x0, double _x1 )
			throws IllegalArgumentException, ArithmeticException
	{
		double x0 = _x0;
		double x1 = _x1;
		if (x0 >= x1) {
			// IterateInverse: wrong interval
			throw new IllegalArgumentException();
		}
		double fEps = 1E-7;
		// find enclosing interval
		double f0 = func.GetValue( x0 );
		double f1 = func.GetValue( x1 );
		double xs;
		int i;
		for (i = 0; i < 1000 & f0 * f1 > 0; i++) {
			if (Math.abs( f0 ) <= Math.abs( f1 )) {
				xs = x0;
				x0 += 2 * (x0 - x1);
				if (x0 < 0) x0 = 0;
				x1 = xs;
				f1 = f0;
				f0 = func.GetValue( x0 );
			}
			else {
				xs = x1;
				x1 += 2 * (x1 - x0);
				x0 = xs;
				f0 = f1;
				f1 = func.GetValue( x1 );
			}
		}
		if (f0 == 0) return x0;
		if (f1 == 0) return x1;
		// simple iteration
		double x00 = x0;
		double x11 = x1;
		double fs = func.GetValue( 0.5 * (x0 + x1) );
		for (i = 0; i < 100; i++) {
			xs = 0.5 * (x0 + x1);
			if (Math.abs( f1 - f0 ) >= fEps) {
				fs = func.GetValue( xs );
				if (f0 * fs <= 0) {
					x1 = xs;
					f1 = fs;
				}
				else {
					x0 = xs;
					f0 = fs;
				}
			}
			else {
				// add one step of regula falsi to improve precision
				if (x0 != x1) {
					double regxs = (f1 - f0) / (x1 - x0);
					if (regxs != 0) {
						double regx = x1 - f1 / regxs;
						if (regx >= x00 && regx <= x11) {
							double regfs = func.GetValue( regx );
							if (Math.abs( regfs ) < Math.abs( fs )) xs = regx;
						}
					}
				}
				return xs;
			}
		}
		throw new ArithmeticException();
	}

	public static double fun_BETAINV( double _x, double _alpha, double _beta )
	{
		if (_x < 0 || _x >= 1 || _alpha <= 0 || _beta <= 0) {
			// Error: Illegal Argument!
			return 0;
		}
		if (_x == 0) {
			// correct result: 0
			return 0;
		}
		else {
			BetaDistFunction func = new BetaDistFunction( _x, _alpha, _beta );
			try {
				double res = iterateInverse( func, 0, 1 );
				return res;
			}
			catch (IllegalArgumentException e) {
				// Error in func.GetValue() method, wrong parameters
				return 0;
			}
			catch (ArithmeticException e) {
				// Error in func.GetValue() method, calculation not finished successfully
				return 0;
			}
		}
	}

	public static double fun_CHIDIST( double _x, double _degFreedom )
	{
		if (_x < 0 || _degFreedom < 1) {
			return 0; // Excel #NUM!
		}

		return Probability.chiSquareComplemented( Math.floor( _degFreedom ), _x );
	}

	private static class ChiDistFunction implements StatisticDistFunc
	{
		double x0, degrees;

		ChiDistFunction( double x0, double degrees )
		{
			this.x0 = x0;
			this.degrees = degrees;
		}

		public double GetValue( double x )
		{
			return this.x0 - fun_CHIDIST( x, this.degrees );
		}
	}

	public static double fun_CHIINV( double _x, double _degFreedom )
	{
		if (_x <= 0 || _x > 1 || _degFreedom < 1 || _degFreedom > 10000000000.0) {
			return 0; // Excel #NUM!
		}
		ChiDistFunction func = new ChiDistFunction( _x, _degFreedom );
		try {
			double res = iterateInverse( func, _degFreedom / 2, _degFreedom );
			return res;
		}
		catch (IllegalArgumentException e) {
			// Error in func.GetValue() method, wrong parameters
			return 0;
		}
		catch (ArithmeticException e) {
			// Error in func.GetValue() method, calculation not finished successfully
			return 0;
		}
	}

	public static double fun_CRITBINOM( double _n, double _p, double _alpha )
	{
		if (_n < 0 || _p < 0 || _p >= 1 || _alpha <= 0 || _alpha >= 1) {
			return 0;
		}
		else {
			double n = Math.floor( _n );
			double q = 1 - _p;
			double factor = Math.pow( q, n );
			if (factor == 0) {
				factor = Math.pow( _p, n );
				if (factor == 0)
					return 0;
				else {
					double sum = 1 - factor;
					int i;
					for (i = 0; i < n && sum >= _alpha; i++) {
						factor *= (n - i) / (i + 1) * q / _p;
						sum -= factor;
					}
					return n - i;
				}
			}
			else {
				double sum = factor;
				int i;
				for (i = 0; i < n && sum < _alpha; i++) {
					factor *= (n - i) / (i + 1) * _p / q;
					sum += factor;
				}
				return i;
			}
		}
	}

	public static double getFDist( double x, double f1, double f2 )
	{
		double arg = f2 / (f2 + f1 * x);
		double alpha = f2 / 2.0;
		double beta = f1 / 2.0;
		return (fun_BETADIST( arg, alpha, beta ));
	}

	private static class FDistFunction implements StatisticDistFunc
	{
		double p, f1, f2;

		FDistFunction( double p, double f1, double f2 )
		{
			this.p = p;
			this.f1 = f1;
			this.f2 = f2;
		}

		public double GetValue( double x )
		{
			return this.p - getFDist( x, this.f1, this.f2 );
		}
	}

	public static double fun_FINV( double _p, double _f1, double _f2 )
	{
		if (_p < 0 || _f1 < 1 || _f2 < 1 || _f1 >= 1.0E10 || _f2 >= 1.0E10 || _p > 1) {
			// Error: Illegal Argument
			return 0;
		}
		if (_p == 0) {
			return 1000000000;
		}
		double f1 = Math.floor( _f1 );
		double f2 = Math.floor( _f2 );
		FDistFunction func = new FDistFunction( _p, f1, f2 );
		try {
			double res = iterateInverse( func, f1 / 2, f1 );
			return res;
		}
		catch (IllegalArgumentException e) {
			// Error in func.GetValue() method, wrong parameters
			return 0;
		}
		catch (ArithmeticException e) {
			// Error in func.GetValue() method, calculation not finished successfully
			return 0;
		}
	}

	private static class GammaDistFunction implements StatisticDistFunc
	{
		double p, alpha, beta;

		GammaDistFunction( double p, double alpha, double beta )
		{
			this.p = p;
			this.alpha = alpha;
			this.beta = beta;
		}

		public double GetValue( double x )
		{
			return this.p - gammaCumulative( x, this.alpha, this.beta );
		}
	}

	public static double fun_GAMMAINV( double _p, double _alpha, double _beta )
	{
		if (_p < 0 || _p >= 1 || _alpha <= 0 || _beta <= 0) {
			// Error: Illegal Argument!
			return 0;
		}
		if (_p == 0) {
			// correct result: 0
			return 0;
		}
		else {
			GammaDistFunction func = new GammaDistFunction( _p, _alpha, _beta );
			double start = _alpha * _beta;
			try {
				double res = iterateInverse( func, start / 2, start );
				return res;
			}
			catch (IllegalArgumentException e) {
				// Error in func.GetValue() method, wrong parameters
				return 0;
			}
			catch (ArithmeticException e) {
				// Error in func.GetValue() method, calculation not finished successfully
				return 0;
			}
		}
	}


	public static double fun_GAMMALN( double _x )
	{
		double x = _x;
		if (x <= 0) {
			// ERROR
			return 0;
		}
		else {
			boolean bReflect;
			double c[] = { 76.18009173, -86.50532033, 24.01409822, -1.231739516, 0.120858003E-2, -0.536382E-5 };
			if (x >= 1) {
				bReflect = false;
				x -= 1;
			}
			else {
				bReflect = true;
				x = 1 - x;
			}
			double g, anum;
			g = 1.0;
			anum = x;
			for (int i = 0; i < 6; i++) {
				anum += 1.0;
				g += c[ i ] / anum;
			}
			g *= 2.506628275; // sqrt(2*PI)
			g = (x + 0.5) * Math.log( x + 5.5 ) + Math.log( g ) - (x + 5.5);
			if (bReflect) g = Math.log( Math.PI * x ) - g - Math.log( Math.sin( Math.PI * x ) );
			return g;
		}
	}

	public static double fun_GAMMADIST( double _x, double _alpha, double _beta, boolean _cumulative )
	{
		if (_x < 0 || _alpha <= 0 || _beta <= 0) {
			return 0; // Excel #NUM!
		}

		if (_cumulative) {
			return gammaCumulative( _x, _alpha, _beta );
		}
		else {
			return gammaDensity( _x, _alpha, _beta );
		}
	}

	private static double gammaCumulative( double _x, double _alpha, double _beta )
	{
		return Probability.gamma( 1 / _beta, _alpha, _x );
	}

	private static double gammaDensity( double _x, double _alpha, double _beta )
	{
		return Math.pow( _x, _alpha - 1 ) * Math.exp( -_x / _beta ) / (Math.pow( _beta, _alpha ) * Gamma.gamma( _alpha ));
	}

	public static double fun_POISSON( int _x, double _mean, boolean _cumulative )
	{
		if (_x < 0 || _mean < 0) {
			return 0; // Excel #NUM!
		}

		if (_cumulative) {
			return poissonCumulative( _x, _mean );
		}
		else {
			return poissonDensity( _x, _mean );
		}
	}

	private static double poissonCumulative( final int _x, final double _mean )
	{
		return Probability.poisson( _x, _mean );
	}

	private static double poissonDensity( final int _x, final double _mean )
	{
		return Math.exp( -_mean ) * Math.pow( _mean, _x ) / factorial( _x );
	}

	public static double fun_TDIST( double _x, double _degFreedom, int _tails, boolean no_floor )
	{
		if (_x < 0 || _degFreedom < 1 || (_tails != 1 && _tails != 2)) {
			return 0; // Excel #NUM!
		}

		if (no_floor) {
			return (1 - Probability.studentT( _degFreedom, _x )) * _tails;
		}
		else {
			return (1 - Probability.studentT( Math.floor( _degFreedom ), _x )) * _tails;
		}
	}

	private static double getTDist( double t, double f )
	{
		return 0.5 * fun_BETADIST( f / (f + t * t), f / 2, 0.5 );
	}

	private static class TDistFunction implements StatisticDistFunc
	{
		double p, degFreedom;

		TDistFunction( double p, double degFreedom )
		{
			this.p = p;
			this.degFreedom = degFreedom;
		}

		public double GetValue( double x )
		{
			return this.p - getTDist( x, this.degFreedom ) * 2;
		}
	}

	public static double fun_TINV( double _x, double _degFreedom )
	{
		if (_degFreedom < 1.0 || _degFreedom >= 1.0E5 || _x <= 0.0 || _x > 1.0) {
			// Wrong parameters
			return 0;
		}
		StatisticDistFunc func = new TDistFunction( _x, _degFreedom );

		try {
			double res = iterateInverse( func, _degFreedom / 2, _degFreedom );
			return res;
		}
		catch (IllegalArgumentException e) {
			// Error in func.GetValue() method, wrong parameters
			return 0;
		}
		catch (ArithmeticException e) {
			// Error in func.GetValue() method, calculation not finished successfully
			return 0;
		}
	}

	public static double fun_WEIBULL( double _x, double _alpha, double _beta, boolean _cumulative )
	{
		if (_x < 0 || _alpha <= 0 || _beta <= 0) {
			return 0; // Excel #NUM!
		}

		if (_cumulative) {
			return 1.0 - Math.exp( -Math.pow( _x / _beta, _alpha ) );
		}
		else {
			return _alpha
					/ Math.pow( _beta, _alpha ) * Math.pow( _x, _alpha - 1 ) * Math.exp( -Math.pow( _x / _beta, _alpha ) );

		}
	}

	public static double fun_MOD( double _n, double _d )
	{
		if (_d == 0) {
			return 0; // Excel #DIV/0!
		}
		final double remainder = _n % _d;
		if (remainder != 0 && Math.signum( remainder ) != Math.signum( _d )) {
			return remainder + _d;
		}
		else {
			return remainder;
		}
	}

	public static double fun_SQRT( double _n )
	{
		if (_n < 0) {
			return 0; // Excel #NUM!
		}
		return Math.sqrt( _n );
	}

	public static double fun_FACT( double _a )
	{
		if (_a < 0.0) {
			return 0.0; // Excel #NUM!
		}
		else {
			int a = (int) _a;
			return factorial( a );
		}
	}

	private static double factorial( int _a )
	{
		if (_a < 0) {
			throw new IllegalArgumentException( "number < 0" );
		}

		if (_a < FACTORIALS.length) {
			return FACTORIALS[ _a ];
		}
		else {
			int i = FACTORIALS.length;
			double r = FACTORIALS[ i - 1 ];
			while (i <= _a) {
				r *= i++;
			}
			return r;
		}
	}


	/**
	 * Computes IRR using Newton's method, where x[i+1] = x[i] - f( x[i] ) / f'( x[i] )
	 */
	public static double fun_IRR( double[] _values, double _guess )
	{
		final int EXCEL_MAX_ITER = 20;

		double x = _guess;
		int iter = 0;
		while (iter++ < EXCEL_MAX_ITER) {

			final double x1 = 1.0 + x;
			double fx = 0.0;
			double dfx = 0.0;
			for (int i = 0; i < _values.length; i++) {
				final double v = _values[ i ];
				final double x1_i = Math.pow( x1, i );
				fx += v / x1_i;
				final double x1_i1 = x1_i * x1;
				dfx += -i * v / x1_i1;
			}
			final double new_x = x - fx / dfx;
			final double epsilon = Math.abs( new_x - x );

			if (epsilon <= EXCEL_EPSILON) {
				if (_guess == 0.0 && Math.abs( new_x ) <= EXCEL_EPSILON) {
					return 0.0; // OpenOffice calc does this
				}
				else {
					return new_x;
				}
			}
			x = new_x;

		}
		return Double.NaN;
	}

	public static double fun_DB( double _cost, double _salvage, double _life, double _period, double _month )
	{
		final double month = Math.floor( _month );
		final double rate = round( 1 - Math.pow( _salvage / _cost, 1 / _life ), 3 );
		final double depreciation1 = _cost * rate * month / 12;
		double depreciation = depreciation1;
		if ((int) _period > 1) {
			double totalDepreciation = depreciation1;
			final int maxPeriod = (int) (_life > _period? _period : _life);
			for (int i = 2; i <= maxPeriod; i++) {
				depreciation = (_cost - totalDepreciation) * rate;
				totalDepreciation += depreciation;
			}
			if (_period > _life) {
				depreciation = (_cost - totalDepreciation) * rate * (12 - month) / 12;
			}
		}
		return depreciation;
	}

	public static double fun_DDB( double _cost, double _salvage, double _life, double _period, double _factor )
	{
		final double remainingCost;
		final double newCost;
		final double k = 1 - _factor / _life;
		if (k <= 0) {
			remainingCost = _period == 1? _cost : 0;
			newCost = _period == 0? _cost : 0;
		}
		else {
			final double k_p1 = Math.pow( k, _period - 1 );
			final double k_p = k_p1 * k;
			remainingCost = _cost * k_p1;
			newCost = _cost * k_p;
		}

		double depreciation = remainingCost - (newCost < _salvage? _salvage : newCost);
		if (depreciation < 0) {
			depreciation = 0;
		}
		return depreciation;
	}

	public static double fun_RATE( double _nper, double _pmt, double _pv, double _fv, double _type, double _guess )
	{
		final int MAX_ITER = 50;
		final boolean type = _type != 0;
		double eps = 1;
		double rate0 = _guess;
		for (int count = 0; eps > EXCEL_EPSILON && count < MAX_ITER; count++) {
			final double rate1;
			if (rate0 == 0) {
				final double a = _pmt * _nper;
				final double b = a + (type? _pmt : -_pmt);
				rate1 = rate0 - (_pv + _fv + a) / (_nper * (_pv + b / 2));
			}
			else {
				final double a = 1 + rate0;
				final double b = Math.pow( a, _nper - 1 );
				final double c = b * a;
				final double d = _pmt * (1 + (type? rate0 : 0));
				final double e = rate0 * _nper * b;
				final double f = c - 1;
				final double g = rate0 * _pv;
				rate1 = rate0 * (1 - (g * c + d * f + rate0 * _fv) / (g * e - _pmt * f + d * e));
			}
			eps = Math.abs( rate1 - rate0 );
			rate0 = rate1;
		}
		if (eps >= EXCEL_EPSILON) {
			return 0; // Excel #NUM!
		}
		return rate0;
	}

	public static double fun_VDB( double _cost, double _salvage, double _life, double _start_period, double _end_period,
			double _factor, boolean _no_switch )
	{
		double valVDB = 0;
		if (_start_period < 0.0
				|| _end_period < _start_period || _end_period > _life || _cost < 0 || _salvage > _cost || _factor <= 0) {
			return 0;
		}
		else {
			int loopStart = (int) Math.floor( _start_period );
			int loopEnd = (int) Math.ceil( _end_period );
			if (_no_switch) {
				for (int i = loopStart + 1; i <= loopEnd; i++) {
					double valDDB = fun_DDB( _cost, _salvage, _life, i, _factor );
					if (i == loopStart + 1) {
						valDDB *= (_end_period < loopStart + 1? _end_period : loopStart + 1) - _start_period;
					}
					else if (i == loopEnd) {
						valDDB *= _end_period + 1 - loopEnd;
					}
					valVDB += valDDB;
				}
			}
			else {
				double _life2 = _life;
				double start = _start_period;
				double end = _end_period;
				double part;
				if (start != Math.floor( start )) {
					if (_factor > 1) {
						if (start >= _life / 2) {
							// this part works like in Open Office
							part = start - _life / 2;
							start = _life / 2;
							end -= part;
							_life2 += 1;
						}
					}
				}
				final double cost = _cost - interVDB( _cost, _salvage, _life, _life2, start, _factor );
				valVDB = interVDB( cost, _salvage, _life, _life - start, end - start, _factor );
			}
		}
		return valVDB;
	}

	private static double interVDB( double _cost, double _salvage, double _life, double _life2, double _period,
			double _factor )
	{
		double valVDB = 0;
		int loopEnd = (int) Math.ceil( _period );
		double salvageCost = _cost - _salvage;
		boolean flagSLN = false;
		double valDDB, valTmpRes;
		double valSLN = 0;
		for (int i = 1; i <= loopEnd; i++) {
			if (!flagSLN) {
				valDDB = fun_DDB( _cost, _salvage, _life, i, _factor );
				valSLN = salvageCost / (_life2 - i + 1);
				if (valSLN > valDDB) {
					valTmpRes = valSLN;
					flagSLN = true;
				}
				else {
					valTmpRes = valDDB;
					salvageCost -= valDDB;
				}
			}
			else {
				valTmpRes = valSLN;
			}
			if (i == loopEnd) valTmpRes *= (_period + 1 - loopEnd);

			valVDB += valTmpRes;
		}
		return valVDB;
	}

	public static double fun_VALUE( String _text, final Environment _environment )
	{
		final String text = _text.trim();
		final Number number = parseNumber( text, false, _environment );
		if (number != null) {
			return number.doubleValue();
		}
		return 0.0; // Excel #NUM!
	}


	public static int fun_MATCH_Exact( double _x, double[] _xs )
	{
		for (int i = 0; i < _xs.length; i++) {
			if (_x == _xs[ i ]) return i + 1; // Excel is 1-based
		}
		return 0;
	}

	public static int fun_MATCH_Ascending( double _x, double[] _xs )
	{
		final int iLast = _xs.length - 1;
		int iLeft = 0;
		int iRight = iLast;
		while (iLeft < iRight) {
			final int iMid = iLeft + ((iRight - iLeft) >> 1);
			if (_x > _xs[ iMid ]) iLeft = iMid + 1;
			else iRight = iMid;
		}
		if (iLeft > iLast || _x < _xs[ iLeft ]) iLeft--;
		return iLeft + 1; // Excel is 1-based
	}

	public static int fun_MATCH_Descending( double _x, double[] _xs )
	{
		final int iLast = _xs.length - 1;
		int iLeft = 0;
		int iRight = iLast;
		while (iLeft < iRight) {
			final int iMid = iLeft + ((iRight - iLeft) >> 1);
			if (_x < _xs[ iMid ]) iLeft = iMid + 1;
			else iRight = iMid;
		}
		if (iLeft > iLast || _x > _xs[ iLeft ]) iLeft--;
		return iLeft + 1; // Excel is 1-based
	}


}
