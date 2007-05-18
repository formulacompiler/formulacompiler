/*
 * Copyright (c) 2006 by Abacus Research AG, Switzerland.
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
package sej.internal.spreadsheet.binder;

import sej.compiler.CompilerException;
import sej.internal.spreadsheet.CellInstance;
import sej.internal.spreadsheet.CellRange;
import sej.internal.spreadsheet.CellWithLazilyParsedExpression;
import sej.internal.spreadsheet.RowImpl;
import sej.internal.spreadsheet.SheetImpl;
import sej.internal.spreadsheet.SpreadsheetImpl;
import sej.spreadsheet.Orientation;
import sej.spreadsheet.SEJ;
import sej.spreadsheet.SpreadsheetBinder;
import sej.spreadsheet.SpreadsheetException;
import sej.spreadsheet.SpreadsheetBinder.Config;
import sej.spreadsheet.SpreadsheetBinder.Section;
import sej.tests.utils.AbstractSpreadsheetTestBase;
import sej.tests.utils.Inputs;
import sej.tests.utils.Outputs;
import sej.tests.utils.WorksheetBuilderWithBands;

public class SpreadsheetBinderTest extends AbstractSpreadsheetTestBase
{
	protected SpreadsheetImpl workbook;
	protected SheetImpl sheet;
	protected RowImpl row;
	protected CellWithLazilyParsedExpression formula;
	protected WorksheetBuilderWithBands dyn;
	protected Section def;


	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		this.workbook = new SpreadsheetImpl();
		this.sheet = new SheetImpl( this.workbook );
		this.row = new RowImpl( this.sheet );
		this.formula = new CellWithLazilyParsedExpression( this.row );
		this.dyn = new WorksheetBuilderWithBands( this.sheet );

		Config cfg = new SpreadsheetBinder.Config();
		cfg.spreadsheet = this.workbook;
		cfg.inputClass = Inputs.class;
		cfg.outputClass = Outputs.class;
		this.def = SEJ.newSpreadsheetBinder( cfg ).getRoot();
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


	public void testAllowMixedBands() throws Exception
	{
		CellRange vert = new CellRange( this.dyn.r1c1.getCellIndex(), this.dyn.r1c1.getCellIndex() );
		this.def.defineRepeatingSection( vert, Orientation.VERTICAL, getInput( "getDetails" ), Inputs.class, null, null );

		CellRange horiz = new CellRange( this.dyn.r2c2.getCellIndex(), this.dyn.r2c2.getCellIndex() );
		this.def.defineRepeatingSection( horiz, Orientation.HORIZONTAL, getInput( "getSubDetails" ), Inputs.class, null,
				null );
	}


	public void testNoOverlappingVerticalBands() throws Exception
	{
		Orientation orientation = Orientation.VERTICAL;
		CellRange one = new CellRange( this.dyn.r2c2.getCellIndex(), this.dyn.r3c3.getCellIndex() );
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
		CellRange one = new CellRange( this.dyn.r2c2.getCellIndex(), this.dyn.r3c3.getCellIndex() );
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
		CellRange two = new CellRange( _from.getCellIndex(), _to.getCellIndex() );
		_def.defineRepeatingSection( two, _orientation, getInput( "getDetails" ), Inputs.class, null, null );
	}


	private void failForBand( Section _def, CellInstance _from, CellInstance _to, Orientation _orientation )
			throws Exception
	{
		CellRange two = new CellRange( _from.getCellIndex(), _to.getCellIndex() );
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
		CellRange one = new CellRange( this.dyn.r1c1.getCellIndex(), this.dyn.r2c1.getCellIndex() );
		this.def.defineRepeatingSection( one, Orientation.HORIZONTAL, getInput( "getDetails" ), Inputs.class, null, null );

		CellRange two = new CellRange( this.dyn.r1c2.getCellIndex(), this.dyn.r2c2.getCellIndex() );
		this.def.defineRepeatingSection( two, Orientation.HORIZONTAL, getInput( "getDetails" ), Inputs.class, null, null );
	}


	public void testAllowDisjointVerticalBands() throws Exception
	{
		CellRange one = new CellRange( this.dyn.r1c1.getCellIndex(), this.dyn.r1c2.getCellIndex() );
		this.def.defineRepeatingSection( one, Orientation.VERTICAL, getInput( "getDetails" ), Inputs.class, null, null );

		CellRange two = new CellRange( this.dyn.r2c1.getCellIndex(), this.dyn.r2c2.getCellIndex() );
		this.def.defineRepeatingSection( two, Orientation.VERTICAL, getInput( "getDetails" ), Inputs.class, null, null );
	}


	public void testNoDefsOutsideBand() throws Exception
	{
		CellRange one = new CellRange( this.dyn.r2c2.getCellIndex(), this.dyn.r3c3.getCellIndex() );
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
		CellRange rng = new CellRange( _from.getCellIndex(), _to.getCellIndex() );
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
