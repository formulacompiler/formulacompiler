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
import org.formulacompiler.runtime.Engine;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBinder.Section;


public class LineItemPriceUseCaseTest extends AbstractUseCaseTest
{


	public void testComputeLineItemPrice() throws Exception
	{
		runUseCase( "LineItemPrice", new LineItemPriceUseCase(), Inputs.class, Outputs.class );
	}


	private final class LineItemPriceUseCase implements UseCase
	{
		public void defineEngine( EngineBuilder _builder, Spreadsheet _model, Section _root ) throws Exception
		{
			defineInput( _builder, _model, _root, "ArticlePrice" );
			defineInput( _builder, _model, _root, "NumberSold" );
			defineOutput( _builder, _model, _root, "Total" );
		}

		private void defineInput( EngineBuilder _builder, Spreadsheet _model, Section _root, final String _cellName )
				throws Exception
		{
			_root.defineInputCell( _model.getCell( _cellName ), Inputs.class.getMethod( "get" + _cellName ) );
		}

		private void defineOutput( EngineBuilder _builder, Spreadsheet _model, Section _root, final String _cellName )
				throws Exception
		{
			_root.defineOutputCell( _model.getCell( _cellName ), Outputs.class.getMethod( "get" + _cellName ) );
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

		public Inputs( double _articlePrice, double _numberSold )
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
