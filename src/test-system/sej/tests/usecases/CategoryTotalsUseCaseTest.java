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
package sej.tests.usecases;

import sej.CallFrame;
import sej.Orientation;
import sej.SaveableEngine;
import sej.Spreadsheet;
import sej.SpreadsheetBinder;

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
