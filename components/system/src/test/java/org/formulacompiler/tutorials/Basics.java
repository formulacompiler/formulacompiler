/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * This file is part of the Abacus Formula Compiler (AFC).
 *
 * For commercial licensing, please contact sales(at)formulacompiler.com.
 *
 * AFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AFC.  If not, see <http://www.gnu.org/licenses/>.
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
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.SpreadsheetSaver;
import org.formulacompiler.tests.MultiFormatTestFactory;
import org.formulacompiler.tests.utils.SpreadsheetAssert;

import junit.framework.Test;


public class Basics extends MultiFormatTestFactory.SpreadsheetFormatTestCase
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


	protected String getStdSheetFileName()
	{
		return "src/test/data/org/formulacompiler/tutorials/Basics" + getSpreadsheetExtension();
	}

	protected String getCustomSheetFileName()
	{
		return "src/test/data/org/formulacompiler/tutorials/BasicsCustom" + getSpreadsheetExtension();
	}


	public void testAFCStd() throws Exception
	{
		LineItem item = new StrategyLineItem();
		RebateComputation.factory = compileFactoryFromSpreadsheet( getStdSheetFileName() );
		double rebate = item.computeRebate();
		assertEquals( 0.1, rebate, 0.00001 );
	}


	// ---- UseCompiledFactory
	public void testAFC() throws Exception
	{
		LineItem item = new StrategyLineItem();
		/**/RebateComputation.factory = compileFactoryFromSpreadsheet( getCustomSheetFileName() );/**/
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
		builder./**/failIfByNameBindingLeftNamedCellsUnbound/**/();

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


	private static final File GENDIR = new File( "temp/test/data" );

	static {
		GENDIR.mkdirs();
	}

	protected File getOutputFile()
	{
		return new File( GENDIR, "GeneratedSheet" + getSpreadsheetExtension() );
	}

	protected File getTemplatedOutputFile()
	{
		return new File( GENDIR, "GeneratedTemplatedSheet" + getSpreadsheetExtension() );
	}

	protected File getTemplateFile()
	{
		return new File( "src/test/data/org/formulacompiler/tutorials/Template" + getSpreadsheetExtension() );
	}

	protected File getExpectedTemplatedOutputFile()
	{
		return new File( "src/test/data/org/formulacompiler/tutorials/ExpectedTemplatedSheet" + getSpreadsheetExtension() );
	}


	public void testGenerateFile() throws Exception
	{
		// ---- GenerateFile
		Spreadsheet s = buildSpreadsheet();
		SpreadsheetCompiler./**/saveSpreadsheet/**/( s, getOutputFile(), null );
		// ---- GenerateFile
		SpreadsheetAssert.assertEqualSpreadsheets( s, new BufferedInputStream( new FileInputStream( getOutputFile() ) ), getSpreadsheetExtension() );
	}


	public void testGenerateStream() throws Exception
	{
		// ---- GenerateStream
		Spreadsheet s = buildSpreadsheet();
		ByteArrayOutputStream /**/os/**/ = new ByteArrayOutputStream();

		SpreadsheetSaver.Config cfg = new SpreadsheetSaver.Config();
		cfg.spreadsheet = s;
		cfg./**/typeExtension/**/ = getSpreadsheetExtension(); // .xls or .ods
		cfg./**/outputStream/**/ = os;
		/**/SpreadsheetCompiler.newSpreadsheetSaver( cfg ).save();/**/
		// ---- GenerateStream
		SpreadsheetAssert.assertEqualSpreadsheets( s, new ByteArrayInputStream( os.toByteArray() ), getSpreadsheetExtension() );
	}


	public void testGenerateTemplatedFile() throws Exception
	{
		// ---- GenerateTemplatedFile
		Spreadsheet s = buildTemplatedSpreadsheet();
		SpreadsheetCompiler.saveSpreadsheet( s, getTemplatedOutputFile(), /**/getTemplateFile()/**/ );
		// ---- GenerateTemplatedFile
		SpreadsheetAssert.assertEqualSpreadsheets( getExpectedTemplatedOutputFile(), getTemplatedOutputFile() );
	}


	public void testGenerateTemplatedStream() throws Exception
	{
		// ---- GenerateTemplatedStream
		Spreadsheet s = buildTemplatedSpreadsheet();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		InputStream /**/ts/**/ = new BufferedInputStream( new FileInputStream( getTemplateFile() ) );

		SpreadsheetSaver.Config cfg = new SpreadsheetSaver.Config();
		cfg.spreadsheet = s;
		cfg.typeExtension = getSpreadsheetExtension(); // .xls or .ods
		cfg.outputStream = os;
		cfg./**/templateInputStream/**/ = ts;
		SpreadsheetCompiler.newSpreadsheetSaver( cfg ).save();
		// ---- GenerateTemplatedStream

		ts.close();
		os.close();
		final byte[] bytes = os.toByteArray();
		final InputStream exp = new BufferedInputStream( new FileInputStream( getExpectedTemplatedOutputFile() ) );
		final InputStream act = new ByteArrayInputStream( bytes );

		copy( new ByteArrayInputStream( bytes ), new FileOutputStream( getTemplatedOutputFile() ) );

		SpreadsheetAssert.assertEqualSpreadsheets( "Comparing generated templated sheets", exp, act, getSpreadsheetExtension() );
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

	public static Test suite()
	{
		return MultiFormatTestFactory.testSuite( Basics.class );
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
