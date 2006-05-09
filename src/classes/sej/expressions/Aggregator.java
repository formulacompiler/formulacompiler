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
package sej.expressions;


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
		public Operator getReductor()
		{
			return Operator.MIN;
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
		public Operator getReductor()
		{
			return null;
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
	};


	public abstract String getName();
	public abstract boolean isOrderOfArgumentsIrrelevant();
	public abstract Operator getReductor();
	
	public boolean isPartialAggregationSupported()
	{
		return isOrderOfArgumentsIrrelevant();
	}

}
