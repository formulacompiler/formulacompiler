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
		test( new AccumulatorTest() );
		test( new DirectSumTest() );
		test( new ValuePassingTest() );
		test( new ObjectPassingTest() );
		test( new ThisIsExternallyCheckedStackTest() );
		test( new ThisIsAutoCheckedStackTest() );
		test( new InnerStackTest() );
		test( new CheckedStackTest() );
	}


	private void test( Test _test )
	{
		System.gc();
		final long startTime = System.nanoTime();
		for (int iRound = 0; iRound < MAXROUNDS; iRound++) {
			double result = _test.run( 1.0, 2.0, 3.0 );
			if (10.0 != result) throw new RuntimeException( "Wrong result" );
		}
		System.gc();
		final long runTime = System.nanoTime() - startTime;
		final long ms = runTime / 1000000;
		System.out.printf( "%50s: %12d ms\n", _test.getClass().getName(), ms );
	}


	private abstract class Test
	{
		public abstract double run( double _d1, double _d2, double _d3 );

	}


	private final class AccumulatorTest extends Test
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


	private final class DirectSumTest extends Test
	{
		@Override
		public double run( double _d1, double _d2, double _d3 )
		{
			return _d1 + _d2 + _d3 + _d1 + _d3;
		}
	}


	private class ValuePassingTest extends Test
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


	private class ObjectPassingTest extends Test
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


	private class ThisIsExternallyCheckedStackTest extends Test
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


	private class ThisIsAutoCheckedStackTest extends Test
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


	private class InnerStackTest extends Test
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


	private class CheckedStackTest extends Test
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
