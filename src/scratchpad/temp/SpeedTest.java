/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
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
package temp;

import java.math.BigDecimal;


public class SpeedTest
{
	private static final int MAXROUNDS = 10000000;
	private static final boolean DEBUG = false;


	static public void main( String[] _args )
	{
		new SpeedTest().run( _args );
	}


	private void run( String[] _args )
	{
		if (true) test( new BigDecimalStaticTest() );
		if (true) test( new BigDecimalFromLongTest() );
		if (true) test( new BigDecimalFromStringTest() );
		
		if (false) test( new DoubleTest() );
		if (false) test( new PrefixDoubleTest() );
		
		if (false) test( new ScaledLongTest() );
		if (false) test( new ScaledIntTest() );
		if (false) test( new BigDecimalTest() );

		if (false) test( new AccumulatorTest() );
		if (false) test( new DirectSumTest() );
		if (false) test( new ValuePassingTest() );
		if (false) test( new ObjectPassingTest() );
		if (false) test( new ThisIsExternallyCheckedStackTest() );
		if (false) test( new ThisIsAutoCheckedStackTest() );
		if (false) test( new InnerStackTest() );
		if (false) test( new CheckedStackTest() );
	}


	private void test( Test _test )
	{
		System.gc();
		final long startTime = System.nanoTime();
		for (int iRound = 0; iRound < MAXROUNDS; iRound++) {
			_test.run();
		}
		final long gcStartTime = System.nanoTime();
		System.gc();
		final long endTime = System.nanoTime();
		final long gcTime = endTime - gcStartTime;
		final long runTime = endTime - startTime;
		final long ms = runTime / 1000000;
		final long gcms = gcTime / 1000000;
		System.out.printf( "%50s: %12d ms (%12d gc)\n", _test.getClass().getName(), ms, gcms );
	}


	private static abstract class Test
	{
		public abstract void run();
	}


	private static abstract class TestDoubleInput extends Test
	{
		public abstract double run( double _d1, double _d2, double _d3 );

		@Override
		public void run()
		{
			double result = run( 1.0, 2.0, 3.0 );
			if (10.0 != result) throw new RuntimeException( "Wrong result" );
		}
	}


	private static final class BigDecimalFromLongTest extends Test
	{

		@Override
		public void run()
		{
			BigDecimal x = BigDecimal.valueOf( 100 );
			BigDecimal y = BigDecimal.valueOf( 3141, 3 );
			BigDecimal z = x.multiply( y );
			z.setScale( 8, BigDecimal.ROUND_HALF_UP );
		}

	}
	
	
	private static final class BigDecimalFromStringTest extends Test
	{

		@Override
		public void run()
		{
			BigDecimal x = new BigDecimal( "100" );
			BigDecimal y = new BigDecimal( "3.141" );
			BigDecimal z = x.multiply( y );
			z.setScale( 8, BigDecimal.ROUND_HALF_UP );
		}

	}
	
	
	private static final class BigDecimalStaticTest extends Test
	{
		private static BigDecimal x = new BigDecimal( "100" );
		private static BigDecimal y = new BigDecimal( "3.141" );

		@Override
		public void run()
		{
			BigDecimal z = x.multiply( y );
			z.setScale( 8, BigDecimal.ROUND_HALF_UP );
		}

	}
	
	
	private static final class DoubleTest extends Test
	{

		@Override
		public void run()
		{
			double result = getA() + getA() * getB();
			// if (result != 132.8322) throw new RuntimeException( "Wrong double" );
		}

		private double getA()
		{
			return 123.45;
		}

		private double getB()
		{
			return 0.076;
		}
	}
	
	
	private static final class DoubleRuntime
	{

		public static double opTIMES( double _a, double _b )
		{
			return _a * _b;
		}

		public static double opPLUS( double _a, double _b )
		{
			return _a + _b;
		}
		
	}


	private static final class PrefixDoubleTest extends Test
	{

		@Override
		public void run()
		{
			double result = DoubleRuntime.opPLUS( getA(), DoubleRuntime.opTIMES( getA(), getB() ) );
			// if (result != 132.8322) throw new RuntimeException( "Wrong double" );
		}

		private double getA()
		{
			return 123.45;
		}

		private double getB()
		{
			return 0.076;
		}
	}


	private static final class ScaledLongTest extends Test
	{

		@Override
		public void run()
		{
			long result = getA() + getA() * getB() / 10000L;
			// if (result != 1328322L) throw new RuntimeException( "Wrong scaled long" );
		}

		private long getA()
		{
			return 1234500;
		}

		private long getB()
		{
			return 760;
		}
	}


	private static final class ScaledIntTest extends Test
	{

		@Override
		public void run()
		{
			int result = getA() + getA() * getB() / 10000;
			// if (result != 1328322) throw new RuntimeException( "Wrong scaled int" );
		}

		private int getA()
		{
			return 1234500;
		}

		private int getB()
		{
			return 760;
		}
	}


	private static final class BigDecimalTest extends Test
	{
		private static final BigDecimal VALUE = BigDecimal.valueOf( 123.45 );
		private static final BigDecimal FACTOR = BigDecimal.valueOf( 0.076 );

		@Override
		public void run()
		{
			BigDecimal result = getA().add( getA().multiply( getB() ) );
		}

		private BigDecimal getA()
		{
			return VALUE;
		}

		private BigDecimal getB()
		{
			return FACTOR;
		}
	}


	private static final class AccumulatorTest extends TestDoubleInput
	{
		@Override
		public double run( double _d1, double _d2, double _d3 )
		{
			double sum = _d1;
			sum += _d2;
			sum += _d3;
			sum += _d1;
			sum += _d3;
			return sum;
		}
	}


	private static final class DirectSumTest extends TestDoubleInput
	{
		@Override
		public double run( double _d1, double _d2, double _d3 )
		{
			return _d1 + _d2 + _d3 + _d1 + _d3;
		}
	}


	private static class ValuePassingTest extends TestDoubleInput
	{

		@Override
		public double run( double _d1, double _d2, double _d3 )
		{
			return compute( _d1, _d2, _d3 );
		}


		private double compute( double _d1, double _d2, double _d3 )
		{
			double a = computeSum( _d1, _d2, _d3 );
			double b = computeAdd( _d1, _d3 );
			double result = a + b;
			return result;
		}


		private double computeSum( double _d1, double _d2, double _d3 )
		{
			return _d1 + _d2 + _d3;
		}


		private double computeAdd( double _d1, double _d2 )
		{
			return _d1 + _d2;
		}


	}


	private static class ObjectPassingTest extends TestDoubleInput
	{

		@Override
		public double run( double _d1, double _d2, double _d3 )
		{
			return (Double) compute( _d1, _d2, _d3 );
		}


		private Object compute( Object _d1, Object _d2, Object _d3 )
		{
			Object a = computeSum( _d1, _d2, _d3 );
			Object b = computeAdd( _d1, _d3 );
			Object result = (Double) a + (Double) b;
			return result;
		}


		private Object computeSum( Object _d1, Object _d2, Object _d3 )
		{
			return (Double) _d1 + (Double) _d2 + (Double) _d3;
		}


		private Object computeAdd( Object _d1, Object _d2 )
		{
			return (Double) _d1 + (Double) _d2;
		}

	}


	private static class ThisIsExternallyCheckedStackTest extends TestDoubleInput
	{
		private static final int MAX_STACK_SIZE = 50;
		private static final byte TYPE_DOUBLE = 0;

		final double[] ds = new double[ MAX_STACK_SIZE ];
		final byte[] ts = new byte[ MAX_STACK_SIZE ];
		int sp = -1;


		public void reset()
		{
			this.sp = -1;
		}


		public void push( double _v )
		{
			this.ds[ ++this.sp ] = _v;
		}


		public double pop()
		{
			return this.ds[ this.sp-- ];
		}


		public void assertHaveRoomFor( int _size )
		{
			if (!(MAX_STACK_SIZE > this.sp + _size)) throw new RuntimeException( "Stack overflow" );
		}


		public void assertTopIs( byte _type )
		{
			if (!(this.sp >= 0)) throw new RuntimeException( "Stack underflow" );
			if (!(_type == this.ts[ this.sp ]))
				throw new RuntimeException( "Stack type mismatch; expected " + _type + ", was " + this.ts[ this.sp ] );
		}


		public void assertTopIs( byte _type1, byte _type2 )
		{
			if (!(this.sp >= 1)) throw new RuntimeException( "Stack underflow" );
			if (!(_type1 == this.ts[ this.sp ]))
				throw new RuntimeException( "Stack type mismatch; expected " + _type1 + ", was " + this.ts[ this.sp ] );
			if (!(_type2 == this.ts[ this.sp - 1 ]))
				throw new RuntimeException( "Stack type mismatch; expected " + _type2 + ", was " + this.ts[ this.sp - 1 ] );
		}


		public void assertTopIs( byte _type1, byte _type2, byte _type3 )
		{
			if (!(this.sp >= 2)) throw new RuntimeException( "Stack underflow" );
			if (!(_type1 == this.ts[ this.sp ]))
				throw new RuntimeException( "Stack type mismatch; expected " + _type1 + ", was " + this.ts[ this.sp ] );
			if (!(_type2 == this.ts[ this.sp - 1 ]))
				throw new RuntimeException( "Stack type mismatch; expected " + _type2 + ", was " + this.ts[ this.sp - 1 ] );
			if (!(_type3 == this.ts[ this.sp - 2 ]))
				throw new RuntimeException( "Stack type mismatch; expected " + _type3 + ", was " + this.ts[ this.sp - 2 ] );
		}


		@Override
		public double run( double _d1, double _d2, double _d3 )
		{
			reset();
			if (DEBUG) assertHaveRoomFor( 3 );
			push( _d1 );
			push( _d2 );
			push( _d3 );
			compute();
			if (DEBUG) assertTopIs( TYPE_DOUBLE );
			return pop();
		}


		private void compute()
		{
			if (DEBUG) assertTopIs( TYPE_DOUBLE, TYPE_DOUBLE, TYPE_DOUBLE );
			double d3 = pop();
			double d2 = pop();
			double d1 = pop();
			push( d1 );
			push( d2 );
			push( d3 );
			computeSum();
			if (DEBUG) assertTopIs( TYPE_DOUBLE );
			double a = pop();
			push( d1 );
			push( d3 );
			computeAdd();
			if (DEBUG) assertTopIs( TYPE_DOUBLE );
			double b = pop();
			push( a + b );
		}


		private void computeSum()
		{
			if (DEBUG) assertTopIs( TYPE_DOUBLE, TYPE_DOUBLE, TYPE_DOUBLE );
			double d3 = pop();
			double d2 = pop();
			double d1 = pop();
			double r = d1 + d2 + d3;
			push( r );
		}


		private void computeAdd()
		{
			if (DEBUG) assertTopIs( TYPE_DOUBLE, TYPE_DOUBLE );
			double d2 = pop();
			double d1 = pop();
			double r = d1 + d2;
			push( r );
		}

	}


	private static class ThisIsAutoCheckedStackTest extends TestDoubleInput
	{
		private static final int MAX_STACK_SIZE = 50;
		private static final byte TYPE_DOUBLE = 0;

		final double[] ds = new double[ MAX_STACK_SIZE ];
		final byte[] ts = new byte[ MAX_STACK_SIZE ];
		int sp = -1;


		public void reset()
		{
			this.sp = -1;
		}


		public void push( double _v )
		{
			if (DEBUG) {
				if (!(this.sp < MAX_STACK_SIZE - 1)) throw new RuntimeException( "Stack overflow" );
				this.ts[ this.sp + 1 ] = TYPE_DOUBLE;
			}
			this.ds[ ++this.sp ] = _v;
		}


		public double pop()
		{
			if (DEBUG) {
				if (!(this.sp >= 0)) throw new RuntimeException( "Stack underflow" );
				if (!(TYPE_DOUBLE == this.ts[ this.sp ]))
					throw new RuntimeException( "Stack type mismatch; expected "
							+ TYPE_DOUBLE + ", was " + this.ts[ this.sp ] );
			}
			return this.ds[ this.sp-- ];
		}


		@Override
		public double run( double _d1, double _d2, double _d3 )
		{
			reset();
			push( _d1 );
			push( _d2 );
			push( _d3 );
			compute();
			return pop();
		}


		private void compute()
		{
			double d3 = pop();
			double d2 = pop();
			double d1 = pop();
			push( d1 );
			push( d2 );
			push( d3 );
			computeSum();
			double a = pop();
			push( d1 );
			push( d3 );
			computeAdd();
			double b = pop();
			push( a + b );
		}


		private void computeSum()
		{
			double d3 = pop();
			double d2 = pop();
			double d1 = pop();
			double r = d1 + d2 + d3;
			push( r );
		}


		private void computeAdd()
		{
			double d2 = pop();
			double d1 = pop();
			double r = d1 + d2;
			push( r );
		}

	}


	private static class InnerStackTest extends TestDoubleInput
	{
		private final Stack stack = new Stack();


		private class Stack
		{
			protected static final int MAX_STACK_SIZE = 50;
			private static final byte TYPE_DOUBLE = 0;
			private byte[] ts = new byte[ MAX_STACK_SIZE ];
			private double[] ds = new double[ MAX_STACK_SIZE ];
			protected int sp = -1;


			public void reset()
			{
				this.sp = -1;
			}


			public void push( double _v )
			{
				if (DEBUG) {
					if (!(this.sp < MAX_STACK_SIZE - 1)) throw new RuntimeException( "Stack overflow" );
					this.ts[ this.sp + 1 ] = TYPE_DOUBLE;
				}
				this.ds[ ++this.sp ] = _v;
			}


			public double pop()
			{
				if (DEBUG) {
					if (!(this.sp >= 0)) throw new RuntimeException( "Stack underflow" );
					if (!(TYPE_DOUBLE == this.ts[ this.sp ]))
						throw new RuntimeException( "Stack type mismatch; expected "
								+ TYPE_DOUBLE + ", was " + this.ts[ this.sp ] );
				}
				return this.ds[ this.sp-- ];
			}


		}


		@Override
		public double run( double _d1, double _d2, double _d3 )
		{
			Stack s = this.stack;
			s.reset();
			s.push( _d1 );
			s.push( _d2 );
			s.push( _d3 );
			compute( s );
			return s.pop();
		}


		private void compute( Stack s )
		{
			double d3 = s.pop();
			double d2 = s.pop();
			double d1 = s.pop();
			s.push( d1 );
			s.push( d2 );
			s.push( d3 );
			computeSum( s );
			double a = s.pop();
			s.push( d1 );
			s.push( d3 );
			computeAdd( s );
			double b = s.pop();
			s.push( a + b );
		}


		private void computeSum( Stack s )
		{
			double d3 = s.pop();
			double d2 = s.pop();
			double d1 = s.pop();
			double r = d1 + d2 + d3;
			s.push( r );
		}


		private void computeAdd( Stack s )
		{
			double d2 = s.pop();
			double d1 = s.pop();
			double r = d1 + d2;
			s.push( r );
		}

	}


	private static class CheckedStackTest extends TestDoubleInput
	{
		private final Stack stack = newStack();


		private Stack newStack()
		{
			Stack result = null;
			assert (result = new CheckedStack()) != null;
			if (null == result) result = new Stack();
			return result;
		}


		private class Stack
		{
			protected static final int MAX_STACK_SIZE = 50;
			private double[] ds = new double[ MAX_STACK_SIZE ];
			protected int sp = -1;


			public void reset()
			{
				this.sp = -1;
			}


			public void push( double _v )
			{
				this.ds[ ++this.sp ] = _v;
			}


			public double pop()
			{
				return this.ds[ this.sp-- ];
			}


			public boolean assertHaveRoomFor( int _size )
			{
				assert MAX_STACK_SIZE > this.sp + _size : "Stack overflow";
				return true;
			}

		}


		private class CheckedStack extends Stack
		{
			private static final byte TYPE_DOUBLE = 0;
			private byte[] ts = new byte[ MAX_STACK_SIZE ];


			@Override
			public void push( double _v )
			{
				assert this.sp < MAX_STACK_SIZE - 1 : "Stack overflow";
				super.push( _v );
				this.ts[ this.sp ] = TYPE_DOUBLE;
			}


			@Override
			public double pop()
			{
				assert this.sp >= 0 : "Stack underflow";
				assert TYPE_DOUBLE == this.ts[ this.sp ] : "Stack type mismatch; expected "
						+ TYPE_DOUBLE + ", was " + this.ts[ this.sp ];
				return super.pop();
			}
		}


		@Override
		public double run( double _d1, double _d2, double _d3 )
		{
			Stack s = this.stack;
			s.reset();
			s.push( _d1 );
			s.push( _d2 );
			s.push( _d3 );
			compute( s );
			return s.pop();
		}


		private void compute( Stack s )
		{
			double d3 = s.pop();
			double d2 = s.pop();
			double d1 = s.pop();
			s.push( d1 );
			s.push( d2 );
			s.push( d3 );
			computeSum( s );
			double a = s.pop();
			s.push( d1 );
			s.push( d3 );
			computeAdd( s );
			double b = s.pop();
			s.push( a + b );
		}


		private void computeSum( Stack s )
		{
			double d3 = s.pop();
			double d2 = s.pop();
			double d1 = s.pop();
			double r = d1 + d2 + d3;
			s.push( r );
		}


		private void computeAdd( Stack s )
		{
			double d2 = s.pop();
			double d1 = s.pop();
			double r = d1 + d2;
			s.push( r );
		}

	}


}
