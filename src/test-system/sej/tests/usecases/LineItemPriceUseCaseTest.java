package sej.tests.usecases;

import sej.CallFrame;
import sej.SaveableEngine;
import sej.Spreadsheet;
import sej.SpreadsheetBinder.Section;
import sej.runtime.Engine;

public class LineItemPriceUseCaseTest extends AbstractUseCaseTest
{


	public void testComputeLineItemPrice() throws Exception
	{
		runUseCase( "LineItemPrice", new LineItemPriceUseCase(), Inputs.class, Outputs.class );
	}


	private final class LineItemPriceUseCase implements UseCase
	{
		public void defineEngine( Spreadsheet _model, Section _root ) throws Exception
		{
			defineInput( _model, _root, "ArticlePrice" );
			defineInput( _model, _root, "NumberSold" );
			defineOutput( _model, _root, "Total" );
		}

		private void defineInput( Spreadsheet _model, Section _root, final String _cellName ) throws Exception
		{
			_root.defineInputCell( _model.getCell( _cellName ),
					new CallFrame( Inputs.class.getMethod( "get" + _cellName ) ) );
		}

		private void defineOutput( Spreadsheet _model, Section _root, final String _cellName ) throws Exception
		{
			_root.defineOutputCell( _model.getCell( _cellName ), new CallFrame( Outputs.class
					.getMethod( "get" + _cellName ) ) );
		}

		public void useEngine( SaveableEngine _engine )
		{
			assertPrice( 2000, _engine, 100, 20 );
			assertPrice( 6930, _engine, 500, 14 );
			assertPrice( 47000, _engine, 1000, 50 );
		}

		private void assertPrice( double _total, Engine _engine, double _article, double _number )
		{
			final Inputs inputs = new Inputs( _article, _number );
			final Outputs outputs = (Outputs) _engine.getComputationFactory().newComputation( inputs );
			assertEquals( _total, outputs.getTotal(), 0.000001 );
		}
	}


	public static final class Inputs
	{
		private final double articlePrice;
		private final double numberSold;

		public Inputs(double _articlePrice, double _numberSold)
		{
			super();
			this.articlePrice = _articlePrice;
			this.numberSold = _numberSold;
		}

		public double getArticlePrice()
		{
			return this.articlePrice;
		}

		public double getNumberSold()
		{
			return this.numberSold;
		}

	}


	public static interface Outputs
	{
		public abstract double getTotal();
	}


}
