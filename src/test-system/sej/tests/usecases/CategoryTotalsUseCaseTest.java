package sej.tests.usecases;

import sej.CallFrame;
import sej.Orientation;
import sej.SaveableEngine;
import sej.Spreadsheet;
import sej.SpreadsheetBinder;
import sej.internal.Debug;

public class CategoryTotalsUseCaseTest extends AbstractUseCaseTest
{

	CallFrame call( Class _cls, String _name ) throws Exception
	{
		return new CallFrame( _cls.getMethod( _name ) );
	}


	public void testCategoryTotals() throws Exception
	{
		runUseCase( "CategoryTotals", new UseCase()
		{

			public void defineEngine( Spreadsheet _model, SpreadsheetBinder.Section _root ) throws Exception
			{
				SpreadsheetBinder.Section categories = _root.defineRepeatingSection( _model.getRange( "Categories" ),
						Orientation.VERTICAL, call( Inputs.class, "categories" ), CategoryInput.class, null, null );
				SpreadsheetBinder.Section elements = categories.defineRepeatingSection( _model.getRange( "Elements" ),
						Orientation.VERTICAL, call( CategoryInput.class, "elements" ), ElementInput.class, null, null );
				elements.defineInputCell( _model.getCell( "ElementAmount" ), call( ElementInput.class, "amount" ) );

				// TODO categories.defineOutputCell( CATEGORY_TOTAL, ValueType.DOUBLE );

				_root.defineOutputCell( _model.getCell( "GrandTotal" ), call( Outputs.class, "grandTotal" ) );
			}

			public void useEngine( SaveableEngine _engine ) throws Exception
			{
				Debug.saveEngine( _engine, "/temp/categ.jar" );
				
				Outputs computation = (Outputs) _engine.getComputationFactory().newComputation( new Inputs() );

				assertEquals( 59, computation.grandTotal() );

				// TODO assertEquals( 18, cat1.getInteger( CATEGORY_TOTAL ) );
				// TODO assertEquals( 21, cat2.getInteger( CATEGORY_TOTAL ) );
				// TODO assertEquals( 20, cat3.getInteger( CATEGORY_TOTAL ) );
			}

		}, Inputs.class, Outputs.class );
	}

	public static final class Inputs
	{
		public CategoryInput[] categories()
		{
			return new CategoryInput[] { new CategoryInput( 3, 4, 5, 6 ), new CategoryInput( 10, 11 ),
					new CategoryInput( 20 ) };
		}
	}

	public static final class CategoryInput
	{
		private final int[] eltValues;

		public CategoryInput(int... _eltValues)
		{
			this.eltValues = _eltValues;
		}

		public ElementInput[] elements()
		{
			final ElementInput[] result = new ElementInput[ this.eltValues.length ];
			for (int i = 0; i < result.length; i++) {
				result[ i ] = new ElementInput( this.eltValues[ i ] );
			}
			return result;
		}
	}

	public static final class ElementInput
	{
		private final int value;

		public ElementInput(int _value)
		{
			this.value = _value;
		}

		public int amount()
		{
			return this.value;
		}
	}

	public static interface Outputs
	{
		public int grandTotal();
	}


}
