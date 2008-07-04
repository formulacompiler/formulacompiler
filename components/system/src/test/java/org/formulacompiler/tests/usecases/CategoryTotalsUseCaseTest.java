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

package org.formulacompiler.tests.usecases;

import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.runtime.Resettable;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Orientation;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBinder;
import org.formulacompiler.tests.MultiFormatTestFactory;

import junit.framework.Test;


public class CategoryTotalsUseCaseTest extends AbstractUseCaseTest
{

	public void testCategoryTotals() throws Exception
	{
		runUseCase( "CategoryTotals", new UseCase()
		{

			public void defineEngine( EngineBuilder _builder, Spreadsheet _model, SpreadsheetBinder.Section _root )
					throws Exception
			{
				SpreadsheetBinder.Section cats = _root.defineRepeatingSection( _model.getRange( "Categories" ),
						Orientation.VERTICAL, Input.class.getMethod( "categories" ), CategoryInput.class, Output.class
								.getMethod( "categories" ), CategoryOutput.class );

				SpreadsheetBinder.Section elements = cats.defineRepeatingSection( _model.getRange( "Elements" ),
						Orientation.VERTICAL, CategoryInput.class.getMethod( "elements" ), ElementInput.class, null, null );

				elements.defineInputCell( _model.getCell( "ElementAmount" ), ElementInput.class.getMethod( "amount" ) );

				cats
						.defineOutputCell( _model.getCell( "CategoryTotal" ), CategoryOutput.class
								.getMethod( "categoryTotal" ) );

				_root.defineOutputCell( _model.getCell( "GrandTotal" ), Output.class.getMethod( "grandTotal" ) );
			}

			public void useEngine( SaveableEngine _engine ) throws Exception
			{
				Output computation = (Output) _engine.getComputationFactory().newComputation( new Input() );

				assertEquals( 59, computation.grandTotal() );

				CategoryOutput[] cats = computation.categories();
				assertEquals( 3, cats.length );
				assertEquals( 18, cats[ 0 ].categoryTotal() );
				assertEquals( 21, cats[ 1 ].categoryTotal() );
				assertEquals( 20, cats[ 2 ].categoryTotal() );
			}

		}, Input.class, Output.class );
	}


	public static Test suite()
	{
		return MultiFormatTestFactory.testSuite( CategoryTotalsUseCaseTest.class );
	}


	public static final class Input
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

		public CategoryInput( int... _eltValues )
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

		public ElementInput( int _value )
		{
			this.value = _value;
		}

		public int amount()
		{
			return this.value;
		}
	}

	public static interface Output extends Resettable
	{
		public int grandTotal();
		public CategoryOutput[] categories();
	}

	public static interface CategoryOutput
	{
		public int categoryTotal();
	}


}
