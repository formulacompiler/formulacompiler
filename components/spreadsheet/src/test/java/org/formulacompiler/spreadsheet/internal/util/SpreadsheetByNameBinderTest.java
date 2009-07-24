/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
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

package org.formulacompiler.spreadsheet.internal.util;

import java.lang.reflect.Method;
import java.util.Map;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBinder;
import org.formulacompiler.spreadsheet.SpreadsheetByNameBinder;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.Spreadsheet.Cell;
import org.formulacompiler.spreadsheet.SpreadsheetByNameBinder.CellBinder;
import org.formulacompiler.spreadsheet.internal.binder.SpreadsheetBinderImpl;
import org.formulacompiler.spreadsheet.internal.binding.CellBinding;
import org.formulacompiler.spreadsheet.internal.binding.InputCellBinding;
import org.formulacompiler.spreadsheet.internal.binding.OutputCellBinding;
import org.formulacompiler.spreadsheet.internal.binding.WorkbookBinding;
import org.formulacompiler.spreadsheet.internal.builder.SpreadsheetBuilderImpl;

import junit.framework.TestCase;

@SuppressWarnings( "unqualified-field-access" )
public class SpreadsheetByNameBinderTest extends TestCase
{


	// DO NOT REFORMAT BELOW THIS LINE
	public static abstract class Outputs
	{
		public double getGetter() { return 0.0; }
		public double plain() { return 0.0; }
		public double both() { return 0.0; }
		public double getBoth() { return 0.0; }
		public double isImplemented() { return 0.0; }
		public final double isFinal() { return 0.0; }
		public static double isStatic() { return 0.0; }
		public double hasParameters( int _p ) { return 0.0; }
	}
	
	public static abstract class OutputsWithAbstractMethod extends Outputs 
	{
		public abstract double isAbstract();
	}

	public static abstract class Inputs
	{
		// ---- Inputs
		public abstract double getGetter();
		public abstract double plain();
		public abstract double both();
		public abstract double getBoth();
		public abstract double isAbstract();
		public double isImplemented() { return 0.0; }
		public final double isFinal() { return 0.0; }
		public static double isStatic() { return 0.0; }
		public abstract double hasParameters( int _p );
		// ---- Inputs
	}
	// DO NOT REFORMAT ABOVE THIS LINE


	public void testInputsByCellName() throws Exception
	{
		new InputBindingTester()
		{

			@Override
			protected void bind( CellBinder _inputs ) throws CompilerException
			{
				_inputs.bindAllNamedCellsToMethods();
			}

		}.test();
	}

	public void testInputsByMethod() throws Exception
	{
		new InputBindingTester()
		{

			@Override
			protected void bind( CellBinder _inputs ) throws CompilerException
			{
				_inputs.bindAllMethodsToNamedCells();
			}

		}.test();
	}

	private abstract class InputBindingTester extends AbstractInputBindingTester
	{

		@Override
		public void test() throws Exception
		{
			// ---- testInputs
			bindsTo( "getter", Inputs.class.getMethod( "getGetter" ) );
			binds( "getGetter" );
			binds( "plain" );
			doesNotBind( "getPlain" ); // there is no method "getPlain()" or "getGetPlain()"
			binds( "both" ); // precise name is used if found
			binds( "getBoth" ); // ditto
			doesNotBind( "unbound" );
			binds( "isAbstract" );
			binds( "isImplemented" );
			binds( "isFinal" );
			binds( "isStatic" );
			doesNotBind( "hasParameters" );
			// ---- testInputs
		}

	}


	public void testOutputsByCellName() throws Exception
	{
		new OutputBindingTester()
		{

			@Override
			protected void bind( CellBinder _outputs ) throws CompilerException
			{
				_outputs.bindAllNamedCellsToMethods();
			}

		}.test();
	}

	public void testOutputsByMethod() throws Exception
	{
		new OutputBindingTester()
		{

			@Override
			protected void bind( CellBinder _outputs ) throws CompilerException
			{
				_outputs.bindAllMethodsToNamedCells();
			}

		}.test();
	}

	private abstract class OutputBindingTester extends AbstractOutputBindingTester
	{

		@Override
		public void test() throws Exception
		{
			// ---- testOutputs
			bindsTo( "getter", Outputs.class.getMethod( "getGetter" ) );
			binds( "getGetter" );
			binds( "plain" );
			doesNotBind( "getPlain" );
			binds( "both" );
			binds( "getBoth" );
			doesNotBind( "unbound" );
			binds( "isAbstract"/* -skip- */, OutputsWithAbstractMethod.class/* -skip- */ );
			binds( "isImplemented" );
			/**/doesNotBind( "isFinal" );/**/
			/**/doesNotBind( "isStatic" );/**/
			doesNotBind( "hasParameters" );
			// ---- testOutputs
		}

	}


	public void testInputsByCellNameWithPrefix() throws Exception
	{
		new InputWithPrefixBindingTester()
		{

			@Override
			protected void bind( CellBinder _inputs ) throws CompilerException
			{
				_inputs.bindAllPrefixedNamedCellsToMethods( "P_" );
			}

		}.test();
	}

	public void testInputsByMethodWithPrefix() throws Exception
	{
		new InputWithPrefixBindingTester()
		{

			@Override
			protected void bind( CellBinder _inputs ) throws CompilerException
			{
				_inputs.bindAllMethodsToPrefixedNamedCells( "P_" );
			}

		}.test();
	}

	private abstract class InputWithPrefixBindingTester extends AbstractInputBindingTester
	{

		@Override
		public void test() throws Exception
		{
			// ---- testInputsWithPrefix
			doesNotBind( "getter" );
			doesNotBind( "getGetter" );
			doesNotBind( "plain" );
			doesNotBind( "getPlain" );
			bindsTo( "P_getter", Inputs.class.getMethod( "getGetter" ) );
			bindsTo( "P_getGetter", Inputs.class.getMethod( "getGetter" ) );
			bindsTo( "P_plain", Inputs.class.getMethod( "plain" ) );
			doesNotBind( "P_getPlain" );
			bindsTo( "P_both", Inputs.class.getMethod( "both" ) );
			bindsTo( "P_getBoth", Inputs.class.getMethod( "getBoth" ) );
			// ---- testInputsWithPrefix
		}

	}


	public void testOutputsByCellNameWithPrefix() throws Exception
	{
		new OutputWithPrefixBindingTester()
		{

			@Override
			protected void bind( CellBinder _outputs ) throws CompilerException
			{
				_outputs.bindAllPrefixedNamedCellsToMethods( "P_" );
			}

		}.test();
	}

	public void testOutputsByMethodWithPrefix() throws Exception
	{
		new OutputWithPrefixBindingTester()
		{

			@Override
			protected void bind( CellBinder _outputs ) throws CompilerException
			{
				_outputs.bindAllMethodsToPrefixedNamedCells( "P_" );
			}

		}.test();
	}

	private abstract class OutputWithPrefixBindingTester extends AbstractOutputBindingTester
	{

		@Override
		public void test() throws Exception
		{
			// ---- testOutputsWithPrefix
			doesNotBind( "getter" );
			doesNotBind( "getGetter" );
			doesNotBind( "plain" );
			doesNotBind( "getPlain" );
			bindsTo( "P_getter", Outputs.class.getMethod( "getGetter" ) );
			bindsTo( "P_getGetter", Outputs.class.getMethod( "getGetter" ) );
			bindsTo( "P_plain", Outputs.class.getMethod( "plain" ) );
			doesNotBind( "P_getPlain" );
			bindsTo( "P_both", Outputs.class.getMethod( "both" ) );
			bindsTo( "P_getBoth", Outputs.class.getMethod( "getBoth" ) );
			// ---- testOutputsWithPrefix
		}

	}


	private abstract class AbstractInputBindingTester extends AbstractBindingTester
	{

		@Override
		protected Class boundClass()
		{
			return Inputs.class;
		}

		@Override
		protected SpreadsheetByNameBinder.CellBinder cellBinder( SpreadsheetByNameBinder _binder )
				throws CompilerException
		{
			return _binder.inputs();
		}

		@Override
		protected InputCellBinding bindingFor( WorkbookBinding _binding, Cell _cell )
		{
			return _binding.getInputs().get( _cell );
		}

	}

	private abstract class AbstractOutputBindingTester extends AbstractBindingTester
	{

		@Override
		protected Class boundClass()
		{
			return OutputsWithAbstractMethod.class;
		}

		@Override
		protected SpreadsheetByNameBinder.CellBinder cellBinder( SpreadsheetByNameBinder _binder )
				throws CompilerException
		{
			return _binder.outputs();
		}

		@Override
		protected OutputCellBinding bindingFor( WorkbookBinding _binding, Cell _cell )
		{
			for (OutputCellBinding binding : _binding.getOutputs()) {
				if (binding.getIndex().equals( _cell )) {
					return binding;
				}
			}
			return null;
		}

	}

	private abstract class AbstractBindingTester
	{
		private Spreadsheet spreadsheet;
		private SpreadsheetBinder binder;
		private SpreadsheetByNameBinder byName;


		abstract public void test() throws Exception;

		protected abstract Class boundClass();

		protected abstract SpreadsheetByNameBinder.CellBinder cellBinder( SpreadsheetByNameBinder _binder )
				throws CompilerException;

		protected abstract void bind( SpreadsheetByNameBinder.CellBinder _cellBinder ) throws CompilerException;

		protected abstract CellBinding bindingFor( WorkbookBinding _binding, Cell _cell );


		void binds( String _cellName ) throws Exception
		{
			binds( _cellName, Outputs.class );
		}

		void binds( String _cellName, Class _outputClass ) throws Exception
		{
			bindsTo( _cellName, boundClass().getMethod( _cellName ), _outputClass );
		}

		void bindsTo( String _cellName, Method _method ) throws CompilerException
		{
			bindsTo( _cellName, _method, Outputs.class );
		}

		void bindsTo( String _cellName, Method _method, Class _outputClass ) throws CompilerException
		{
			String cellName = _cellName.toUpperCase();
			buildByNameBinder( cellName, _outputClass );
			bind( cellBinder( byName ) );
			assertEquals( 0, byName.cellNamesLeftUnbound().size() );
			WorkbookBinding binding = (WorkbookBinding) binder.getBinding();
			Cell cell = spreadsheet.getCell( cellName );
			CellBinding cellBinding = bindingFor( binding, cell );
			assertNotNull( cellBinding );
			assertEquals( _method, cellBinding.boundCall().getMethod() );
			byName.failIfCellNamesAreStillUnbound();
		}

		void doesNotBind( String _cellName ) throws CompilerException
		{
			String cellName = _cellName.toUpperCase();
			buildByNameBinder( cellName, Outputs.class );
			bind( cellBinder( byName ) );
			final Map<String, Spreadsheet.Range> unbound = byName.cellNamesLeftUnbound();
			assertEquals( 1, unbound.size() );
			assertEquals( cellName, unbound.keySet().iterator().next() );
			try {
				byName.failIfCellNamesAreStillUnbound();
			}
			catch (SpreadsheetException.NameNotFound e) {
				assertEquals( "There is no input or output method named "
						+ cellName + "() or get" + cellName + "() to bind the cell " + cellName
						+ " to (character case is irrelevant).", e.getMessage() );
			}
		}

		private void buildByNameBinder( final String cellName, Class _outputClass )
		{
			buildBinder( cellName, _outputClass );
			SpreadsheetByNameBinder.Config cfg = new SpreadsheetByNameBinder.Config();
			cfg.binder = this.binder;
			this.byName = new SpreadsheetByNameBinderImpl( cfg );
		}

		private void buildBinder( String _cellName, Class _outputClass )
		{
			buildSpreadsheet( _cellName );
			SpreadsheetBinder.Config cfg = new SpreadsheetBinder.Config();
			cfg.spreadsheet = this.spreadsheet;
			cfg.inputClass = Inputs.class;
			cfg.outputClass = _outputClass;
			this.binder = new SpreadsheetBinderImpl( cfg );
		}

		private void buildSpreadsheet( String _cellName )
		{
			SpreadsheetBuilderImpl build = new SpreadsheetBuilderImpl();
			build.newCell( build.cst( 1 ) ).nameCell( _cellName.toUpperCase() );
			this.spreadsheet = build.getSpreadsheet();
		}

	}

}
