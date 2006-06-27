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
package sej.tutorials;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import sej.EngineBuilder;
import sej.Operator;
import sej.SEJ;
import sej.Spreadsheet;
import sej.SpreadsheetBuilder;
import sej.runtime.Engine;
import sej.runtime.SEJException;
import junit.framework.TestCase;

public class Basics extends TestCase
{


	// ------------------------------------------------ Static computation


	public void testStatic() throws Exception
	{
		LineItem item = new StaticLineItem();
		double rebate = item.computeRebate();
		assertEquals( 0.1, rebate, 0.00001 );
	}


	static class StaticLineItem extends LineItem
	{
		@Override
		// ---- computeStatic
		double computeRebate()
		{
			Date orderDate = getOrder().getDate();
			double customerRebate = getOrder().getCustomer().getStandardRebate();
			double articleRebate = getArticle().getSpecialRebateValidOn( orderDate );
			return Math.max( customerRebate, articleRebate );
		}
		// ---- computeStatic
	}


	// ------------------------------------------------ Computation is a strategy


	public void testStrategy() throws Exception
	{
		LineItem item = new StrategyLineItem();

		RebateComputation.factory = new StandardRebateComputationFactory();
		assertEquals( 0.1, item.computeRebate(), 0.00001 );

		RebateComputation.factory = new AlternativeRebateComputationFactory();
		assertEquals( 0.15, item.computeRebate(), 0.00001 );
	}


	static class StrategyLineItem extends LineItem
	{
		@Override
		// ---- computeStrategy
		double computeRebate()
		{
			RebateInputs inputs = new RebateInputsAdaptor( this );
			RebateComputation comp = RebateComputation.newInstance( inputs );
			return comp.getRebate();
		}
		// ---- computeStrategy
	}


	// ---- RebateInputs
	public static interface RebateInputs
	{
		int getCustomerCategory(); 
		int getArticleCategory();
		double getCustomerRebate();
		double getArticleRebate();
		Date getOrderDate();
	}

	// ---- RebateInputs


	static class RebateInputsAdaptor implements RebateInputs
	{
		private final LineItem item;

		public RebateInputsAdaptor(LineItem _item)
		{
			super();
			this.item = _item;
		}

		public Date getOrderDate()
		{
			return this.item.getOrder().getDate();
		}

		public double getCustomerRebate()
		{
			return this.item.getOrder().getCustomer().getStandardRebate();
		}

		public int getCustomerCategory()
		{
			return this.item.getOrder().getCustomer().getCategory();
		}

		public double getArticleRebate()
		{
			return this.item.getArticle().getSpecialRebateValidOn( this.item.getOrder().getDate() );
		}

		public int getArticleCategory()
		{
			return this.item.getArticle().getCategory();
		}

	}


	// ---- Strategy
	public static abstract class RebateComputation
	{
		static RebateComputationFactory factory = new StandardRebateComputationFactory();

		static RebateComputation newInstance( RebateInputs _inputs )
		{
			return factory.newInstance( _inputs );
		}

		protected final RebateInputs inputs;

		public RebateComputation(RebateInputs _inputs)
		{
			super();
			this.inputs = _inputs;
		}

		public abstract double getRebate();
	}

	// ---- Factory
	public static abstract class RebateComputationFactory
	{
		public abstract RebateComputation newInstance( RebateInputs _inputs );
	}

	// ---- Factory
	// ---- Strategy


	// ---- StandardStrategy
	static class StandardRebateComputation extends RebateComputation
	{
		public StandardRebateComputation(RebateInputs _inputs)
		{
			super( _inputs );
		}

		@Override
		public double getRebate()
		{
			return Math.max( this.inputs.getCustomerRebate(), this.inputs.getArticleRebate() );
		}
	}

	// ---- StandardStrategy


	static class StandardRebateComputationFactory extends RebateComputationFactory
	{
		@Override
		public RebateComputation newInstance( RebateInputs _inputs )
		{
			return new StandardRebateComputation( _inputs );
		}
	}


	// ------------------------------------------------ Alternative strategy


	public void testAlternativeStrategy() throws Exception
	{
		LineItem item = new StrategyLineItem();
		RebateComputation.factory = new AlternativeRebateComputationFactory();
		assertEquals( 0.15, item.computeRebate(), 0.00001 );
	}


	static class AlternativeRebateComputation extends RebateComputation
	{
		public AlternativeRebateComputation(RebateInputs _inputs)
		{
			super( _inputs );
		}

		@Override
		public double getRebate()
		{
			return this.inputs.getCustomerRebate() + this.inputs.getArticleRebate();
		}
	}


	static class AlternativeRebateComputationFactory extends RebateComputationFactory
	{
		@Override
		public RebateComputation newInstance( RebateInputs _inputs )
		{
			return new AlternativeRebateComputation( _inputs );
		}
	}


	// ------------------------------------------------ Using SEJ


	private static final String SHEETNAME = "src/test-system/testdata/sej/tutorials/BasicsCustom.xls";


	// ---- UseCompiledFactory
	public void testSEJ() throws Exception
	{
		LineItem item = new StrategyLineItem();
		/**/RebateComputation.factory = compileFactoryFromSpreadsheet();/**/
		double rebate = item.computeRebate();
		assertEquals( 0.15, rebate, 0.00001 );
	}
	// ---- UseCompiledFactory


	// ---- CompileFactory
	private RebateComputationFactory compileFactoryFromSpreadsheet() throws FileNotFoundException, IOException, SEJException
	{
		EngineBuilder builder = /**/SEJ.newEngineBuilder()/**/;
		builder./**/loadSpreadsheet/**/( SHEETNAME );
		builder./**/setFactoryClass/**/( RebateComputationFactory.class );
		builder./**/bindAllByName/**/();
		Engine engine = builder./**/compile/**/();
		return (RebateComputationFactory) engine./**/getComputationFactory/**/();
	}

	// ---- CompileFactory


	// ------------------------------------------------ Build Own Sheet


	// ---- UseOwnUIFactory
	public void testOwnUI() throws Exception
	{
		LineItem item = new StrategyLineItem();
		/**/RebateComputation.factory = compileFactoryFromOwnUI();/**/
		double rebate = item.computeRebate();
		assertEquals( 0.15, rebate, 0.00001 );
	}
	// ---- UseOwnUIFactory


	// ---- OwnUIFactory
	private RebateComputationFactory compileFactoryFromOwnUI() throws SEJException
	{
		EngineBuilder builder = SEJ.newEngineBuilder();
		builder./**/setSpreadsheet( buildSpreadsheet() )/**/;  // instead of loadSpreadsheet()
		builder.setFactoryClass( RebateComputationFactory.class );
		builder.bindAllByName();
		Engine engine = builder.compile();
		return (RebateComputationFactory) engine.getComputationFactory();
	}

	// ---- OwnUIFactory

	// ---- OwnUISheet
	private Spreadsheet buildSpreadsheet()
	{
		SpreadsheetBuilder b = SEJ./**/newSpreadsheetBuilder/**/();

		b./**/newCell/**/( b./**/cst( "CustomerRebate" )/**/ );
		b.newCell( b./**/cst( 0.1 )/**/ );
		// -- defCalc
		/* -hlCalc- */SpreadsheetBuilder.CellRef cr = b.currentCell();/* -hlCalc- */
		// -- defCalc

		b./**/newRow/**/();
		b.newCell( b.cst( "ArticleRebate" ) );
		b.newCell( b.cst( 0.05 ) );
		// -- defCalc
		/* -hlCalc- */SpreadsheetBuilder.CellRef ar = b.currentCell();/* -hlCalc- */

		b.newRow();
		b.newRow();
		b.newCell( b.cst( "Rebate" ) );
		b.newCell( /* -hlCalc- */b.op( Operator.PLUS, b.ref( cr ), b.ref( ar ) )/* -hlCalc- */ );
		// -- defCalc

		return b./**/getSpreadsheet/**/();
	}

	// ---- OwnUISheet


	// ------------------------------------------------ Base classes


	static abstract class LineItem
	{
		Order order = new Order();
		Article article = new Article();

		Order getOrder()
		{
			return this.order;
		}

		public Article getArticle()
		{
			return this.article;
		}

		abstract double computeRebate();
	}


	static class Order
	{
		Customer customer = new Customer();

		Customer getCustomer()
		{
			return this.customer;
		}

		Date getDate()
		{
			return Calendar.getInstance().getTime();
		}
	}


	static class Customer
	{
		double getStandardRebate()
		{
			return 0.05;
		}

		public int getCategory()
		{
			return 1;
		}
	}


	static class Article
	{
		double getSpecialRebateValidOn( Date _validOn )
		{
			return 0.10;
		}

		public int getCategory()
		{
			return 2;
		}
	}

}
