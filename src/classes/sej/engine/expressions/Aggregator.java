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
package sej.engine.expressions;

import java.io.IOException;

import sej.describable.AbstractDescribable;
import sej.describable.DescriptionBuilder;


public enum Aggregator {

	SUM {
		@Override
		public String getName()
		{
			return "SUM";
		}


		@Override
		public boolean isOrderOfArgumentsIrrelevant()
		{
			return true;
		}


		@Override
		public Operator getReductor()
		{
			return Operator.PLUS;
		}


		@Override
		public Aggregation newAggregation()
		{
			return new Summation();
		}


		class Summation extends DoubleValuedAggregation
		{


			/**
			 * This constructor works around what I suppose to be a bug in the Java 5 compiler. If
			 * omitted, the runtime throws a code verification exception that neither super() nor
			 * this() have been called.
			 */
			public Summation()
			{
				super();
			}


			@Override
			public void aggregate( Object _value )
			{
				if (_value instanceof Number) {
					this.result += ((Number) _value).doubleValue();
				}
			}
		}
	},

	PRODUCT {
		@Override
		public String getName()
		{
			return "PRODUCT";
		}


		@Override
		public boolean isOrderOfArgumentsIrrelevant()
		{
			return true;
		}


		@Override
		public Operator getReductor()
		{
			return Operator.TIMES;
		}


		@Override
		public Aggregation newAggregation()
		{
			return new Multiplication();
		}


		class Multiplication extends DoubleValuedAggregation
		{


			/**
			 * This constructor works around what I suppose to be a bug in the Java 5 compiler. If
			 * omitted, the runtime throws a code verification exception that neither super() nor
			 * this() have been called.
			 */
			public Multiplication()
			{
				super();
				this.result = 1.0;
			}


			@Override
			public void aggregate( Object _value )
			{
				if (_value instanceof Number) {
					this.result *= ((Number) _value).doubleValue();
				}
			}
		}

	},

	MIN {
		@Override
		public String getName()
		{
			return "MIN";
		}


		@Override
		public boolean isOrderOfArgumentsIrrelevant()
		{
			return true;
		}


		@Override
		public Aggregation newAggregation()
		{
			return new Minimization();
		}


		@Override
		public Operator getReductor()
		{
			return Operator.MIN;
		}


		class Minimization extends ComparableValuedAggregation
		{


			/**
			 * This constructor works around what I suppose to be a bug in the Java 5 compiler. If
			 * omitted, the runtime throws a code verification exception that neither super() nor
			 * this() have been called.
			 */
			public Minimization()
			{
				super();
			}


			@SuppressWarnings("unchecked")
			@Override
			public void aggregate( Object _value )
			{
				if (null == this.result) {
					this.result = (Comparable) _value;
				}
				else if (this.result.compareTo( _value ) > 0) {
					this.result = (Comparable) _value;
				}
			}
		}
	},

	MAX {
		@Override
		public String getName()
		{
			return "MAX";
		}


		@Override
		public boolean isOrderOfArgumentsIrrelevant()
		{
			return true;
		}


		@Override
		public Operator getReductor()
		{
			return Operator.MAX;
		}


		@Override
		public Aggregation newAggregation()
		{
			return new Maximization();
		}


		class Maximization extends ComparableValuedAggregation
		{


			/**
			 * This constructor works around what I suppose to be a bug in the Java 5 compiler. If
			 * omitted, the runtime throws a code verification exception that neither super() nor
			 * this() have been called.
			 */
			public Maximization()
			{
				super();
			}


			@SuppressWarnings("unchecked")
			@Override
			public void aggregate( Object _value )
			{
				if (null == this.result) {
					this.result = (Comparable) _value;
				}
				else if (this.result.compareTo( _value ) < 0) {
					this.result = (Comparable) _value;
				}
			}
		}
	},

	AVERAGE {
		@Override
		public String getName()
		{
			return "AVERAGE";
		}


		@Override
		public boolean isOrderOfArgumentsIrrelevant()
		{
			return true;
		}


		@Override
		public Aggregation newAggregation()
		{
			return new Averaging();
		}


		final class Averaging extends DoubleValuedCountingAggregation
		{

			/**
			 * This constructor works around what I suppose to be a bug in the Java 5 compiler. If
			 * omitted, the runtime throws a code verification exception that neither super() nor
			 * this() have been called.
			 */
			public Averaging()
			{
				super();
			}


			@Override
			public void aggregate( Object _value )
			{
				if (_value instanceof Number) {
					this.result += ((Number) _value).doubleValue();
				}
				super.aggregate( _value );
			}


			@Override
			public Object getResult()
			{
				if (this.count > 0) {
					return this.result / this.count;
				}
				else {
					return Double.NaN;
				}
			}


		}
	},

	AND {
		@Override
		public String getName()
		{
			return "AND";
		}


		@Override
		public boolean isOrderOfArgumentsIrrelevant()
		{
			return true;
		}


		@Override
		public Operator getReductor()
		{
			return Operator.AND;
		}


		@Override
		public Aggregation newAggregation()
		{
			return new Anding();
		}


		class Anding extends BooleanValuedAggregation
		{


			/**
			 * This constructor works around what I suppose to be a bug in the Java 5 compiler. If
			 * omitted, the runtime throws a code verification exception that neither super() nor
			 * this() have been called.
			 */
			public Anding()
			{
				super();
				this.result = true;
			}


			@SuppressWarnings("unchecked")
			@Override
			public void aggregate( Object _value )
			{
				this.result = this.result && Util.valueToBoolean( _value, false );
			}
		}
	},

	OR {
		@Override
		public String getName()
		{
			return "OR";
		}


		@Override
		public boolean isOrderOfArgumentsIrrelevant()
		{
			return true;
		}


		@Override
		public Operator getReductor()
		{
			return Operator.OR;
		}


		@Override
		public Aggregation newAggregation()
		{
			return new Oring();
		}


		class Oring extends BooleanValuedAggregation
		{


			/**
			 * This constructor works around what I suppose to be a bug in the Java 5 compiler. If
			 * omitted, the runtime throws a code verification exception that neither super() nor
			 * this() have been called.
			 */
			public Oring()
			{
				super();
			}


			@SuppressWarnings("unchecked")
			@Override
			public void aggregate( Object _value )
			{
				this.result = this.result || Util.valueToBoolean( _value, false );
			}
		}
	};


	public abstract String getName();


	public abstract Aggregation newAggregation();


	public abstract class Aggregation extends AbstractDescribable
	{
		public abstract void aggregate( Object _value );


		public abstract Object getResult();


		public abstract void initializeFrom( Aggregation _aggregation );


		@Override
		public void describeTo( DescriptionBuilder _to ) throws IOException
		{
			_to.append( getResult() );
		}

	}


	public abstract class DoubleValuedAggregation extends Aggregation
	{
		protected double result = 0.0;


		public double getAccumulator()
		{
			return this.result;
		}


		@Override
		public Object getResult()
		{
			return this.result;
		}


		@Override
		public void initializeFrom( Aggregation _aggregation )
		{
			this.result = (Double) _aggregation.getResult();
		}
	}


	public abstract class DoubleValuedCountingAggregation extends DoubleValuedAggregation
	{
		protected int count = 0;


		public int getCount()
		{
			return this.count;
		}


		@Override
		public void initializeFrom( Aggregation _aggregation )
		{
			super.initializeFrom( _aggregation );
			this.count = ((DoubleValuedCountingAggregation) _aggregation).count;
		}


		@Override
		public void aggregate( Object _value )
		{
			this.count++;
		}


		@Override
		public void describeTo( DescriptionBuilder _to ) throws IOException
		{
			super.describeTo( _to );
			_to.append( ',' );
			_to.append( this.count );
		}

	}


	public abstract class ComparableValuedAggregation extends Aggregation
	{
		protected Comparable result;


		@Override
		public Object getResult()
		{
			return this.result;
		}


		@Override
		public void initializeFrom( Aggregation _aggregation )
		{
			this.result = (Comparable) _aggregation.getResult();
		}
	}


	public abstract class BooleanValuedAggregation extends Aggregation
	{
		protected boolean result;


		@Override
		public Object getResult()
		{
			return this.result;
		}


		@Override
		public void initializeFrom( Aggregation _aggregation )
		{
			this.result = (Boolean) _aggregation.getResult();
		}
	}


	public boolean isOrderOfArgumentsIrrelevant()
	{
		return false;
	}


	public boolean isPartialAggregationSupported()
	{
		return isOrderOfArgumentsIrrelevant();
	}


	public Operator getReductor()
	{
		return null;
	}


}
