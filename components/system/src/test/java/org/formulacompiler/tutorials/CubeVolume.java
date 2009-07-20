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

import java.util.List;

import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.runtime.Computation;
import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.runtime.New;
import org.formulacompiler.runtime.event.CellComputationEvent;
import org.formulacompiler.runtime.event.CellComputationListener;
import org.formulacompiler.runtime.spreadsheet.SpreadsheetCellComputationEvent;
import org.formulacompiler.spreadsheet.ConstantExpressionOptimizationListener;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.tests.MultiFormatTestFactory;

import junit.framework.Test;

public class CubeVolume extends MultiFormatTestFactory.SpreadsheetFormatTestCase
{
	public void testComputationListener() throws Exception
	{
		// ---- compileEngine
		final EngineBuilder b = SpreadsheetCompiler.newEngineBuilder();
		b.loadSpreadsheet( getPath() );
		b.setInputClass( Inputs.class );
		b.setOutputClass( Outputs.class );
		b.createCellNamesFromRowTitles();
		b.bindAllByName();
		b./**/setComputationListenerEnabled( true )/**/;
		final SaveableEngine e = b.compile();
		// ---- compileEngine

		// ---- setComputationListener
		final Computation.Config config = /**/new Computation.Config()/**/;
		final TestComputationListener listener = new TestComputationListener();
		config./**/cellComputationListener/**/ = listener;
		final ComputationFactory f = e.getComputationFactory( /**/config/**/ );
		// ---- setComputationListener

		// ---- compute
		final Outputs c = (Outputs) f.newComputation( new Inputs() );
		assertEquals( 1001.0, c.getVolume(), 1e-10 );
		// ---- compute

		// ---- checkComputationEvents
		assertEquals( 5, listener.events.size() );
		assertEvent( "-> 7.0 in Sheet1!B1(Length)", listener.events.get( 0 ) );
		assertEvent( "-> 11.0 in Sheet1!B2(Width)", listener.events.get( 1 ) );
		assertEvent( "77.0 in Sheet1!B4(Area)", listener.events.get( 2 ) );
		assertEvent( "-> 13.0 in Sheet1!B3(Height)", listener.events.get( 3 ) );
		assertEvent( "<- 1001.0 in Sheet1!B5(Volume)", listener.events.get( 4 ) );
		// ---- checkComputationEvents
	}

	public void testCompilationListener() throws Exception
	{
		// ---- createBuilder
		final EngineBuilder b = SpreadsheetCompiler.newEngineBuilder();
		b.loadSpreadsheet( getPath() );
		b.setInputClass( Inputs2.class );
		b.setOutputClass( Outputs.class );
		b.createCellNamesFromRowTitles();
		b.bindAllByName();
		final TestConstExprOptListener constExprOptListener = new TestConstExprOptListener();
		b./**/setConstantExpressionOptimizationListener/**/( constExprOptListener );
		// ---- createBuilder
		b.setComputationListenerEnabled( true );

		// ---- checkCompilationEvents
		final SaveableEngine e = b.compile();

		assertEquals( 1, constExprOptListener.events.size() );
		assertEvent( "6.0 in Sheet1!B4(Area)", constExprOptListener.events.get( 0 ) );
		// ---- checkCompilationEvents

		final Computation.Config config = new Computation.Config();
		final TestComputationListener listener = new TestComputationListener();
		config.cellComputationListener = listener;
		final ComputationFactory f = e.getComputationFactory( config );
		final Outputs c = (Outputs) f.newComputation( new Inputs2() );

		assertEquals( 102.0, c.getVolume(), 0.0001 );

		assertEquals( 2, listener.events.size() );
		assertEvent( "-> 17.0 in Sheet1!B3(Height)", listener.events.get( 0 ) );
		assertEvent( "<- 102.0 in Sheet1!B5(Volume)", listener.events.get( 1 ) );
	}

	private String getPath()
	{
		return "src/test/data/org/formulacompiler/tutorials/CubeVolume" + getSpreadsheetExtension();
	}

	private static void assertEvent( String _expected, SpreadsheetCellComputationEvent _actual )
	{
		final StringBuilder sb = new StringBuilder();
		if (_actual.isInput()) sb.append( "-> " );
		if (_actual.isOutput()) sb.append( "<- " );
		sb.append( _actual.getValue() ).append( " in " ).append( _actual.getCellInfo() );
		assertEquals( _expected, sb.toString() );
	}


	public static Test suite()
	{
		return MultiFormatTestFactory.testSuite( CubeVolume.class );
	}


	// ---- inputs
	public static class Inputs
	{
		public double /**/getLength()/**/
		{
			return 7;
		}

		public double /**/getWidth()/**/
		{
			return 11;
		}

		public double /**/getHeight()/**/
		{
			return 13;
		}
	}
	// ---- inputs

	// ---- inputs2
	public static class Inputs2
	{
		public double /**/getHeight()/**/
		{
			return 17;
		}
	}
	// ---- inputs2

	// ---- outputs

	public static interface Outputs
	{
		double /**/getVolume()/**/;
	}
	// ---- outputs

	// ---- CompilationListener
	private class TestConstExprOptListener implements /**/ConstantExpressionOptimizationListener/**/
	{
		final List<SpreadsheetCellComputationEvent> events = New.list();

		public void /**/constantCellCalculated/**/( /**/SpreadsheetCellComputationEvent/**/ _event )
		{
			this.events.add( _event );
		}
	}
	// ---- CompilationListener

	// ---- ComputationListener
	private class TestComputationListener implements /**/CellComputationListener/**/
	{
		final List<SpreadsheetCellComputationEvent> events = New.list();

		public void /**/cellCalculated/**/( /**/CellComputationEvent/**/ _event )
		{
			this.events.add( (SpreadsheetCellComputationEvent) _event );
		}
	}
	// ---- ComputationListener

}
