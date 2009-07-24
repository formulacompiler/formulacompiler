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

package org.formulacompiler.spreadsheet.internal.binder;

import java.io.IOException;
import java.lang.reflect.Method;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.FormulaCompiler;
import org.formulacompiler.spreadsheet.Orientation;
import org.formulacompiler.spreadsheet.SpreadsheetBinder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.SpreadsheetBinder.Config;
import org.formulacompiler.spreadsheet.SpreadsheetBinder.Section;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.CellRange;
import org.formulacompiler.spreadsheet.internal.CellWithConstant;
import org.formulacompiler.spreadsheet.internal.CellWithExpression;
import org.formulacompiler.spreadsheet.internal.RowImpl;
import org.formulacompiler.spreadsheet.internal.SheetImpl;
import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;
import org.formulacompiler.tests.utils.AbstractSpreadsheetTestCase;
import org.formulacompiler.tests.utils.Inputs;
import org.formulacompiler.tests.utils.Outputs;
import org.formulacompiler.tests.utils.WorksheetBuilderWithBands;

public class SpreadsheetBinderTest extends AbstractSpreadsheetTestCase
{
	protected SpreadsheetImpl workbook;
	protected SheetImpl sheet;
	protected RowImpl row;
	protected CellWithExpression formula;
	protected WorksheetBuilderWithBands dyn;
	protected Section def;


	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		this.workbook = new SpreadsheetImpl();
		this.sheet = new SheetImpl( this.workbook );
		this.row = new RowImpl( this.sheet );
		this.formula = new CellWithExpression( this.row );
		this.dyn = new WorksheetBuilderWithBands( this.sheet );

		Config cfg = new SpreadsheetBinder.Config();
		cfg.spreadsheet = this.workbook;
		cfg.inputClass = Inputs.class;
		cfg.outputClass = Outputs.class;
		this.def = SpreadsheetCompiler.newSpreadsheetBinder( cfg ).getRoot();
	}


	public void testInputMethodReuse() throws Exception
	{
		this.def.defineInputCell( this.dyn.r1c1.getCellIndex(), getInput( "getOne" ) );
		this.def.defineInputCell( this.dyn.r1c2.getCellIndex(), getInput( "getOne" ) );
	}


	public void testNoDuplicateInputCells() throws Exception
	{
		this.def.defineInputCell( this.dyn.r1c1.getCellIndex(), getInput( "getOne" ) );
		try {
			this.def.defineInputCell( this.dyn.r1c1.getCellIndex(), getInput( "getTwo" ) );
			fail();
		}
		catch (CompilerException.DuplicateDefinition e) {
			// expected
		}
	}


	public void testOutputCellReuse() throws Exception
	{
		this.def.defineOutputCell( this.dyn.r1c1.getCellIndex(), getOutput( "getA" ) );
		this.def.defineOutputCell( this.dyn.r1c1.getCellIndex(), getOutput( "getB" ) );
	}


	public void testNoDuplicateOutputMethods() throws Exception
	{
		this.def.defineOutputCell( this.dyn.r1c1.getCellIndex(), getOutput( "getResult" ) );
		try {
			this.def.defineOutputCell( this.dyn.r1c2.getCellIndex(), getOutput( "getResult" ) );
			fail();
		}
		catch (CompilerException.DuplicateDefinition e) {
			// expected
		}

	}


	public void testNoCheckedExceptionsOnInputs() throws Exception
	{
		assertBinds( "RuntimeException" );
		assertBinds( "Unchecked" );
		assertHasCheckedExceptions( "Exception" );
		assertHasCheckedExceptions( "Checked" );
	}

	private void assertBinds( String _typeName ) throws Exception
	{
		assertChainBinds( "throws" + _typeName );
		assertChainBinds( "chained", "throws" + _typeName );
		assertChainBinds( "chainedThrows" + _typeName, "doesntThrow" );
	}

	private void assertHasCheckedExceptions( String _typeName ) throws Exception
	{
		assertChainHasCheckedExceptionsAt( 0, "throws" + _typeName );
		assertChainHasCheckedExceptionsAt( 1, "chained", "throws" + _typeName );
		assertChainHasCheckedExceptionsAt( 0, "chainedThrows" + _typeName, "doesntThrow" );
	}

	private void assertChainBinds( String... _mtdNames ) throws Exception
	{
		assertChainHasCheckedExceptionsAt( -1, _mtdNames );
	}

	private void assertChainHasCheckedExceptionsAt( int _faultyMethodIndex, String... _mtdNames ) throws Exception
	{
		Class<TestInputs> inp = TestInputs.class;
		Method[] mtds = new Method[ _mtdNames.length ];
		CallFrame frame = null;
		for (int i = 0; i < mtds.length; i++) {
			mtds[ i ] = inp.getMethod( _mtdNames[ i ] );
			if (0 == i) frame = FormulaCompiler.newCallFrame( mtds[ i ] );
			else frame = frame.chain( mtds[ i ] );
		}

		SpreadsheetImpl workbook = new SpreadsheetImpl();
		SheetImpl sheet = new SheetImpl( workbook );
		RowImpl row = new RowImpl( sheet );
		CellIndex cell = new CellWithConstant( row, 1.0 ).getCellIndex();
		SpreadsheetBinder def = SpreadsheetCompiler.newSpreadsheetBinder( workbook, TestInputs.class, Outputs.class );
		Section rootDef = def.getRoot();

		if (_faultyMethodIndex >= 0) {
			try {
				rootDef.defineInputCell( cell, frame );
				fail();
			}
			catch (IllegalArgumentException e) {
				assertEquals( "Input "
						+ mtds[ _faultyMethodIndex ]
						+ " throws checked exceptions; cannot be accessed by Abacus Formula Compiler", e.getMessage() );
			}
		}
		else {
			rootDef.defineInputCell( cell, frame );
		}
	}

	public static interface TestInputs
	{
		double throwsException() throws Exception;
		double throwsChecked() throws IOException;
		double throwsRuntimeException() throws RuntimeException;
		double throwsUnchecked() throws IllegalArgumentException;
		double doesntThrow();
		TestInputs chainedThrowsException() throws Exception;
		TestInputs chainedThrowsChecked() throws IOException;
		TestInputs chainedThrowsRuntimeException() throws RuntimeException;
		TestInputs chainedThrowsUnchecked() throws IllegalArgumentException;
		TestInputs chained();
	}


	public void testAllowMixedBands() throws Exception
	{
		final CellRange vert = CellRange.getCellRange( this.dyn.r1c1.getCellIndex(), this.dyn.r1c1.getCellIndex() );
		this.def.defineRepeatingSection( vert, Orientation.VERTICAL, getInput( "getDetails" ), Inputs.class, null, null );

		final CellRange horiz = CellRange.getCellRange( this.dyn.r2c2.getCellIndex(), this.dyn.r2c2.getCellIndex() );
		this.def.defineRepeatingSection( horiz, Orientation.HORIZONTAL, getInput( "getSubDetails" ), Inputs.class, null,
				null );
	}


	public void testNoOverlappingVerticalBands() throws Exception
	{
		Orientation orientation = Orientation.VERTICAL;
		final CellRange one = CellRange.getCellRange( this.dyn.r2c2.getCellIndex(), this.dyn.r3c3.getCellIndex() );
		this.def.defineRepeatingSection( one, orientation, getInput( "getDetails" ), Inputs.class, null, null );

		acceptBand( this.def, this.dyn.r1c1, this.dyn.r1c1, orientation );
		failForBand( this.def, this.dyn.r1c1, this.dyn.r2c1, orientation );
		failForBand( this.def, this.dyn.r1c1, this.dyn.r3c1, orientation );
		failForBand( this.def, this.dyn.r1c1, this.dyn.r4c1, orientation );
		failForBand( this.def, this.dyn.r2c1, this.dyn.r2c1, orientation );
		failForBand( this.def, this.dyn.r2c1, this.dyn.r3c1, orientation );
		failForBand( this.def, this.dyn.r2c1, this.dyn.r3c1, orientation );
		failForBand( this.def, this.dyn.r3c1, this.dyn.r3c1, orientation );
		failForBand( this.def, this.dyn.r3c1, this.dyn.r4c1, orientation );
		acceptBand( this.def, this.dyn.r4c1, this.dyn.r4c1, orientation );
	}


	public void testNoOverlappingHorizontalBands() throws Exception
	{
		Orientation orientation = Orientation.HORIZONTAL;
		final CellRange one = CellRange.getCellRange( this.dyn.r2c2.getCellIndex(), this.dyn.r3c3.getCellIndex() );
		this.def.defineRepeatingSection( one, orientation, getInput( "getDetails" ), Inputs.class, null, null );

		acceptBand( this.def, this.dyn.r1c1, this.dyn.r1c1, orientation );
		failForBand( this.def, this.dyn.r1c1, this.dyn.r1c2, orientation );
		failForBand( this.def, this.dyn.r1c1, this.dyn.r1c3, orientation );
		failForBand( this.def, this.dyn.r1c1, this.dyn.r1c4, orientation );
		failForBand( this.def, this.dyn.r1c2, this.dyn.r1c2, orientation );
		failForBand( this.def, this.dyn.r1c2, this.dyn.r1c3, orientation );
		failForBand( this.def, this.dyn.r1c2, this.dyn.r1c3, orientation );
		failForBand( this.def, this.dyn.r1c3, this.dyn.r1c3, orientation );
		failForBand( this.def, this.dyn.r1c3, this.dyn.r1c4, orientation );
		acceptBand( this.def, this.dyn.r1c4, this.dyn.r1c4, orientation );
	}


	private void acceptBand( Section _def, CellInstance _from, CellInstance _to, Orientation _orientation )
			throws Exception
	{
		final CellRange two = CellRange.getCellRange( _from.getCellIndex(), _to.getCellIndex() );
		_def.defineRepeatingSection( two, _orientation, getInput( "getDetails" ), Inputs.class, null, null );
	}


	private void failForBand( Section _def, CellInstance _from, CellInstance _to, Orientation _orientation )
			throws Exception
	{
		final CellRange two = CellRange.getCellRange( _from.getCellIndex(), _to.getCellIndex() );
		try {
			_def.defineRepeatingSection( two, _orientation, getInput( "getDetails" ), Inputs.class, null, null );
			fail( "No overlap reported for " + two );
		}
		catch (SpreadsheetException.SectionOverlap e) {
			// expected
		}
	}


	public void testAllowDisjointHorizontalBands() throws Exception
	{
		final CellRange one = CellRange.getCellRange( this.dyn.r1c1.getCellIndex(), this.dyn.r2c1.getCellIndex() );
		this.def.defineRepeatingSection( one, Orientation.HORIZONTAL, getInput( "getDetails" ), Inputs.class, null, null );

		final CellRange two = CellRange.getCellRange( this.dyn.r1c2.getCellIndex(), this.dyn.r2c2.getCellIndex() );
		this.def.defineRepeatingSection( two, Orientation.HORIZONTAL, getInput( "getDetails" ), Inputs.class, null, null );
	}


	public void testAllowDisjointVerticalBands() throws Exception
	{
		final CellRange one = CellRange.getCellRange( this.dyn.r1c1.getCellIndex(), this.dyn.r1c2.getCellIndex() );
		this.def.defineRepeatingSection( one, Orientation.VERTICAL, getInput( "getDetails" ), Inputs.class, null, null );

		final CellRange two = CellRange.getCellRange( this.dyn.r2c1.getCellIndex(), this.dyn.r2c2.getCellIndex() );
		this.def.defineRepeatingSection( two, Orientation.VERTICAL, getInput( "getDetails" ), Inputs.class, null, null );
	}


	public void testNoDefsOutsideBand() throws Exception
	{
		CellRange one = CellRange.getCellRange( this.dyn.r2c2.getCellIndex(), this.dyn.r3c3.getCellIndex() );
		Section band = this.def.defineRepeatingSection( one, Orientation.VERTICAL, getInput( "getDetails" ),
				Inputs.class, null, null );

		failForOutsideDef( band, this.dyn.r1c1 );
		failForOutsideDef( band, this.dyn.r4c1 );
		failForOutsideDef( band, this.dyn.r1c1, this.dyn.r1c4 );
		failForOutsideDef( band, this.dyn.r1c1, this.dyn.r2c4 );
		failForOutsideDef( band, this.dyn.r3c1, this.dyn.r4c4 );
		failForOutsideDef( band, this.dyn.r4c1, this.dyn.r4c4 );
	}


	private void failForOutsideDef( Section _band, CellInstance _cell ) throws Exception
	{
		try {
			_band.defineInputCell( _cell.getCellIndex(), getInput( "getOne" ) );
			fail( "Definition for " + _cell.getCellIndex() + " accepted, but was not in band" );
		}
		catch (SpreadsheetException.NotInSection e) {
			// expected
		}
	}


	private void failForOutsideDef( Section _band, CellInstance _from, CellInstance _to ) throws Exception
	{
		final CellRange rng = CellRange.getCellRange( _from.getCellIndex(), _to.getCellIndex() );
		try {
			_band
					.defineRepeatingSection( rng, Orientation.VERTICAL, getInput( "getSubDetails" ), Inputs.class, null,
							null );
			fail( "Definition for " + rng + " accepted, but was not in band" );
		}
		catch (SpreadsheetException.NotInSection e) {
			// expected
		}
	}


	public void testConfigValidation() throws Exception
	{
		Config cfg = new SpreadsheetBinder.Config();
		cfg.spreadsheet = this.workbook;
		cfg.inputClass = Inputs.class;
		cfg.outputClass = Inputs.class;
		cfg.validate();

		cfg.spreadsheet = null;
		assertInvalid( cfg, "spreadsheet" );
		cfg.spreadsheet = this.workbook;

		// LATER more tests here
	}


	private static void assertInvalid( Config _cfg, String _string )
	{
		try {
			_cfg.validate();
		}
		catch (IllegalArgumentException e) {
			if (!e.getMessage().contains( _string )) throw e;
		}
	}


}
