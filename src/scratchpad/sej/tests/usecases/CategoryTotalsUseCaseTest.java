package sej.tests.usecases;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import sej.Band;
import sej.BandElement;
import sej.CompilerNameSpace;
import sej.Computation;
import sej.Engine;
import sej.ModelError;
import sej.Orientation;
import sej.Spreadsheet;
import sej.ValueType;
import sej.tests.usecases.AbstractUseCaseTest;

public class CategoryTotalsUseCaseTest extends AbstractUseCaseTest
{


	public void testCategoryTotals() throws IOException, ModelError, SecurityException, NoSuchMethodException, InvocationTargetException
	{
		runUseCase( "CategoryTotals", new UseCase()
		{
			private static final String GRAND_TOTAL = "GrandTotal";
			private static final String CATEGORY_TOTAL = "CategoryTotal";
			private static final String ELEMENTS = "Elements";
			private static final String CATEGORIES = "Categories";
			private static final String ELEMENT_AMOUNT = "ElementAmount";


			public void defineEngine( Spreadsheet _model, CompilerNameSpace _root ) throws ModelError
			{
				CompilerNameSpace categories = _root.defineBand( CATEGORIES, Orientation.VERTICAL );
				CompilerNameSpace elements = categories.defineBand( ELEMENTS, Orientation.VERTICAL );
				elements.defineInputCell( ELEMENT_AMOUNT, ValueType.DOUBLE );
				categories.defineOutputCell( CATEGORY_TOTAL, ValueType.DOUBLE );
				_root.defineOutputCell( GRAND_TOTAL, ValueType.DOUBLE );
			}


			public void useEngine( Engine _engine ) throws InvocationTargetException
			{
				Computation computation = _engine.newComputation();
				Band categories = computation.getBand( CATEGORIES );
				
				BandElement cat1 = makeBand( categories, 3, 4, 5, 6 );
				BandElement cat2 = makeBand( categories, 10, 11 );
				BandElement cat3 = makeBand( categories, 20 );
				
				assertEquals( 59, computation.getInteger( GRAND_TOTAL ));
				assertEquals( 18, cat1.getInteger( CATEGORY_TOTAL ));
				assertEquals( 21, cat2.getInteger( CATEGORY_TOTAL ));
				assertEquals( 20, cat3.getInteger( CATEGORY_TOTAL ));
			}


			private BandElement makeBand( Band _categories, int... _amounts )
			{
				BandElement result = _categories.newElement();
				Band elts = result.getBand( ELEMENTS );
				for (int amt : _amounts) {
					elts.newElement().setInteger( ELEMENT_AMOUNT, amt );
				}
				return result;
			}

		} );
	}


}
