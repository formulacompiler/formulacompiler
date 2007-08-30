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
package org.formulacompiler.tutorials;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.formulacompiler.compiler.Operator;
import org.formulacompiler.decompiler.FormulaDecompiler;
import org.formulacompiler.runtime.Engine;
import org.formulacompiler.runtime.FormulaCompilerException;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetSaver;
import org.formulacompiler.tests.utils.AbstractSpreadsheetTestBase;


public class Basics extends AbstractSpreadsheetTestBase
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

		public RebateInputsAdaptor( LineItem _item )
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

		public RebateComputation( RebateInputs _inputs )
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
		public StandardRebateComputation( RebateInputs _inputs )
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
		public AlternativeRebateComputation( RebateInputs _inputs )
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


	// ------------------------------------------------ Using AFC


	private static final String STDSHEETNAME = "src/test/data/org/formulacompiler/tutorials/Basics.xls";
	private static final String CUSTOMSHEETNAME = "src/test/data/org/formulacompiler/tutorials/BasicsCustom.xls";


	public void testAFCStd() throws Exception
	{
		LineItem item = new StrategyLineItem();
		RebateComputation.factory = compileFactoryFromSpreadsheet( STDSHEETNAME );
		double rebate = item.computeRebate();
		assertEquals( 0.1, rebate, 0.00001 );
	}


	// ---- UseCompiledFactory
	public void testAFC() throws Exception
	{
		LineItem item = new StrategyLineItem();
		/**/RebateComputation.factory = compileFactoryFromSpreadsheet( CUSTOMSHEETNAME );/**/
		double rebate = item.computeRebate();
		assertEquals( 0.15, rebate, 0.00001 );
	}
	// ---- UseCompiledFactory


	// ---- CompileFactory
	private RebateComputationFactory compileFactoryFromSpreadsheet( String _sheetName ) throws Exception
	{
		EngineBuilder builder = /**/SpreadsheetCompiler.newEngineBuilder()/**/;
		builder./**/loadSpreadsheet/**/( _sheetName );
		builder./**/setFactoryClass/**/( RebateComputationFactory.class );
		builder./**/bindAllByName/**/();
		Engine engine = builder./**/compile/**/();
		// ---- CompileFactory
		FormulaDecompiler.decompile( engine ).saveTo( new File( "temp/test/decompiled/basics" ) );
		// ---- CompileFactory
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
	private RebateComputationFactory compileFactoryFromOwnUI() throws FormulaCompilerException
	{
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder./**/setSpreadsheet( buildSpreadsheet() )/**/; // instead of loadSpreadsheet()
		builder.setFactoryClass( RebateComputationFactory.class );
		builder.bindAllByName();
		Engine engine = builder.compile();
		return (RebateComputationFactory) engine.getComputationFactory();
	}

	// ---- OwnUIFactory

	// ---- OwnUISheet
	private Spreadsheet buildSpreadsheet()
	{
		SpreadsheetBuilder b = SpreadsheetCompiler./**/newSpreadsheetBuilder/**/();

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


	// ------------------------------------------------ Generate Initial Sheet


	private static final String GENDIR = "temp/test/data";
	private static final String GENFILE = GENDIR + "/GeneratedSheet.xls";
	private static final String GENTEMPLATEDFILE = GENDIR + "/GeneratedTemplatedSheet.xls";
	private static final String TEMPLATEFILE = "src/test/data/org/formulacompiler/tutorials/Template.xls";
	private static final String EXPECTEDGENTEMPLATEDFILE = "src/test/data/org/formulacompiler/tutorials/ExpectedTemplatedSheet.xls";

	static {
		new File( GENDIR ).mkdirs();
	}


	public void testGenerateFile() throws Exception
	{
		// ---- GenerateFile
		Spreadsheet s = buildSpreadsheet();
		SpreadsheetCompiler./**/saveSpreadsheet/**/( s, GENFILE, null );
		// ---- GenerateFile
		checkSpreadsheetStream( s, new BufferedInputStream( new FileInputStream( GENFILE ) ), GENFILE );
	}


	public void testGenerateStream() throws Exception
	{
		// ---- GenerateStream
		Spreadsheet s = buildSpreadsheet();
		ByteArrayOutputStream /**/os/**/ = new ByteArrayOutputStream();

		SpreadsheetSaver.Config cfg = new SpreadsheetSaver.Config();
		cfg.spreadsheet = s;
		cfg./**/typeExtension/**/ = ".xls";
		cfg./**/outputStream/**/ = os;
		/**/SpreadsheetCompiler.newSpreadsheetSaver( cfg ).save();/**/
		// ---- GenerateStream
		checkSpreadsheetStream( s, new ByteArrayInputStream( os.toByteArray() ), GENFILE );
	}


	public void testGenerateTemplatedFile() throws Exception
	{
		// ---- GenerateTemplatedFile
		Spreadsheet s = buildTemplatedSpreadsheet();
		SpreadsheetCompiler.saveSpreadsheet( s, GENTEMPLATEDFILE, /**/TEMPLATEFILE/**/ );
		// ---- GenerateTemplatedFile
		assertEqualFiles( EXPECTEDGENTEMPLATEDFILE, GENTEMPLATEDFILE );
	}


	public void testGenerateTemplatedStream() throws Exception
	{
		// ---- GenerateTemplatedStream
		Spreadsheet s = buildTemplatedSpreadsheet();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		InputStream /**/ts/**/ = new BufferedInputStream( new FileInputStream( TEMPLATEFILE ) );

		SpreadsheetSaver.Config cfg = new SpreadsheetSaver.Config();
		cfg.spreadsheet = s;
		cfg.typeExtension = ".xls";
		cfg.outputStream = os;
		cfg./**/templateInputStream/**/ = ts;
		SpreadsheetCompiler.newSpreadsheetSaver( cfg ).save();
		// ---- GenerateTemplatedStream

		ts.close();
		os.close();
		final byte[] bytes = os.toByteArray();
		final InputStream exp = new BufferedInputStream( new FileInputStream( EXPECTEDGENTEMPLATEDFILE ) );
		final InputStream act = new ByteArrayInputStream( bytes );

		copy( new ByteArrayInputStream( bytes ), new FileOutputStream( GENTEMPLATEDFILE ) );

		assertEqualStreams( "Comparing generated templated sheets", exp, act );
	}


	private void copy( InputStream _in, OutputStream _out ) throws Exception
	{
		final byte[] buf = new byte[ 1024 ];
		int l;
		while (0 < (l = _in.read( buf ))) {
			_out.write( buf, 0, l );
		}
		_out.close();
	}


	private Spreadsheet buildTemplatedSpreadsheet()
	{
		// DO NOT REFORMAT BELOW THIS LINE
		// ---- BuildTemplatedSheet
		final String CAPTION = "Caption";
		final String LBL = "Label";
		final String IN = "InputValue";
		final String IN_P = "PercentInputValue";
		final String IN_D = "DateInputValue";
		final String OUT_P = "PercentOutputValue";
		final String INTER = "IntermediateValue";

		SpreadsheetBuilder b = SpreadsheetCompiler.newSpreadsheetBuilder();

		b.newCell().newCell( b.cst( "Styled" ) ).newCell( b.cst( "Plain" ) );

		b.newRow()./**/styleRow( CAPTION )/**/.newCell( b.cst( "Inputs" ) )./**/styleCell( CAPTION )/**/.newRow();
		b.newCell( b.cst( "CustomerRebate" ) )./**/styleCell( LBL )/**/;
		b.newCell( b.cst( 0.1 ) )./**/styleCell( IN_P )/**/;
		b.newCell( b.cst( 0.1 ) );
		SpreadsheetBuilder.CellRef cr = b.currentCell();

		b.newRow();
		b.newCell( b.cst( "ArticleRebate" ) )./**/styleCell( LBL )/**/;
		b.newCell( b.cst( 0.05 ) )./**/styleCell( IN_P )/**/;
		b.newCell( b.cst( 0.05 ) );
		SpreadsheetBuilder.CellRef ar = b.currentCell();

		final Calendar cal = new GregorianCalendar();
		cal.clear();
		cal.set( 2006, Calendar.OCTOBER, 29 );
		Date orderDateSampleValue = cal.getTime();

		b.newRow();
		b.newCell( b.cst( "OrderDate" ) )./**/styleCell( LBL )/**/;
		b.newCell( b.cst( orderDateSampleValue ) )./**/styleCell( IN_D )/**/;
		b.newCell( b.cst( orderDateSampleValue ) );

		b.newRow();
		b.newCell( b.cst( "IsKeyAccount" ) )./**/styleCell( LBL )/**/;
		b.newCell( b.cst( true ) )./**/styleCell( IN )/**/;
		b.newCell( b.cst( true ) );

		b.newRow()./**/styleRow( CAPTION )/**/.newCell( b.cst( "Outputs" ) )./**/styleCell( CAPTION )/**/.newRow();
		b.newCell( b.cst( "Rebate" ) )./**/styleCell( LBL )/**/;
		b.newCell( b.op( Operator.PLUS, b.ref( cr ), b.ref( ar ) ) )./**/styleCell( OUT_P )/**/;

		b.newRow()./**/styleRow( CAPTION )/**/.newCell( b.cst( "Intermediate Values" ) )./**/styleCell( CAPTION )/**/.newRow();
		b.newCell( b.cst( "(sample only)" ) )./**/styleCell( LBL )/**/;
		b.newCell()./**/styleCell( INTER )/**/;
		b.newCell();

		return b.getSpreadsheet();
		// ---- BuildTemplatedSheet
		// DO NOT REFORMAT ABOVE THIS LINE
	}

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
