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
package sej.engine.compiler.definition;

import sej.Compiler;
import sej.ModelError;
import sej.Orientation;
import sej.Compiler.Section;
import sej.model.CellInstance;
import sej.model.CellRange;
import sej.model.CellWithLazilyParsedExpression;
import sej.model.Row;
import sej.model.Sheet;
import sej.model.Workbook;
import sej.tests.utils.AbstractTestBase;
import sej.tests.utils.WorksheetBuilderWithBands;

public class EngineDefinitionTest extends AbstractTestBase
{
	protected Workbook workbook;
	protected Sheet sheet;
	protected Row row;
	protected CellWithLazilyParsedExpression formula;


	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		this.workbook = new Workbook();
		this.sheet = new Sheet( this.workbook );
		this.row = new Row( this.sheet );
		this.formula = new CellWithLazilyParsedExpression( this.row );
	}


	public void testInputMethodReuse() throws ModelError, SecurityException, NoSuchMethodException
	{
		WorksheetBuilderWithBands dyn = new WorksheetBuilderWithBands( this.sheet );
		Section def = new EngineDefinition( this.workbook ).getRoot();

		def.defineInputCell( dyn.r1c1.getCellIndex(), getInput( "getOne" ) );
		def.defineInputCell( dyn.r1c2.getCellIndex(), getInput( "getOne" ) );
	}


	public void testNoDuplicateInputCells() throws ModelError, SecurityException, NoSuchMethodException
	{
		WorksheetBuilderWithBands dyn = new WorksheetBuilderWithBands( this.sheet );
		Section def = new EngineDefinition( this.workbook ).getRoot();

		def.defineInputCell( dyn.r1c1.getCellIndex(), getInput( "getOne" ) );
		try {
			def.defineInputCell( dyn.r1c1.getCellIndex(), getInput( "getTwo" ) );
			fail();
		}
		catch (ModelError.DuplicateDefinition e) {
			// expected
		}
	}


	public void testOutputCellReuse() throws ModelError, SecurityException, NoSuchMethodException
	{
		WorksheetBuilderWithBands dyn = new WorksheetBuilderWithBands( this.sheet );
		Section def = new EngineDefinition( this.workbook ).getRoot();

		def.defineOutputCell( dyn.r1c1.getCellIndex(), getOutput( "getA" ) );
		def.defineOutputCell( dyn.r1c1.getCellIndex(), getOutput( "getB" ) );
	}


	public void testNoDuplicateOutputMethods() throws ModelError, SecurityException, NoSuchMethodException
	{
		WorksheetBuilderWithBands dyn = new WorksheetBuilderWithBands( this.sheet );
		Section def = new EngineDefinition( this.workbook ).getRoot();

		def.defineOutputCell( dyn.r1c1.getCellIndex(), getOutput( "getResult" ) );
		try {
			def.defineOutputCell( dyn.r1c2.getCellIndex(), getOutput( "getResult" ) );
			fail();
		}
		catch (ModelError.DuplicateDefinition e) {
			// expected
		}

	}


	public void testNoMixedBands() throws ModelError, NoSuchMethodException
	{
		WorksheetBuilderWithBands dyn = new WorksheetBuilderWithBands( this.sheet );
		Section def = new EngineDefinition( this.workbook ).getRoot();

		CellRange vert = new CellRange( dyn.r1c1.getCellIndex(), dyn.r1c1.getCellIndex() );
		def.defineRepeatingSection( vert, Orientation.VERTICAL, getInput( "getDetails" ), null );

		CellRange horiz = new CellRange( dyn.r2c2.getCellIndex(), dyn.r2c2.getCellIndex() );
		try {
			def.defineRepeatingSection( horiz, Orientation.HORIZONTAL, getInput( "getSubDetails" ), null );
			fail();
		}
		catch (ModelError.SectionOrientation e) {
			// expected
		}
	}


	public void testNoOverlappingVerticalBands() throws ModelError, NoSuchMethodException
	{
		WorksheetBuilderWithBands dyn = new WorksheetBuilderWithBands( this.sheet );
		Section def = new EngineDefinition( this.workbook ).getRoot();

		Orientation orientation = Orientation.VERTICAL;
		CellRange one = new CellRange( dyn.r2c2.getCellIndex(), dyn.r3c3.getCellIndex() );
		def.defineRepeatingSection( one, orientation, getInput( "getDetails" ), null );

		acceptBand( def, dyn.r1c1, dyn.r1c1, orientation );
		failForBand( def, dyn.r1c1, dyn.r2c1, orientation );
		failForBand( def, dyn.r1c1, dyn.r3c1, orientation );
		failForBand( def, dyn.r1c1, dyn.r4c1, orientation );
		failForBand( def, dyn.r2c1, dyn.r2c1, orientation );
		failForBand( def, dyn.r2c1, dyn.r3c1, orientation );
		failForBand( def, dyn.r2c1, dyn.r3c1, orientation );
		failForBand( def, dyn.r3c1, dyn.r3c1, orientation );
		failForBand( def, dyn.r3c1, dyn.r4c1, orientation );
		acceptBand( def, dyn.r4c1, dyn.r4c1, orientation );
	}


	public void testNoOverlappingHorizontalBands() throws ModelError, NoSuchMethodException
	{
		WorksheetBuilderWithBands dyn = new WorksheetBuilderWithBands( this.sheet );
		Section def = new EngineDefinition( this.workbook ).getRoot();

		Orientation orientation = Orientation.HORIZONTAL;
		CellRange one = new CellRange( dyn.r2c2.getCellIndex(), dyn.r3c3.getCellIndex() );
		def.defineRepeatingSection( one, orientation, getInput( "getDetails" ), null );

		acceptBand( def, dyn.r1c1, dyn.r1c1, orientation );
		failForBand( def, dyn.r1c1, dyn.r1c2, orientation );
		failForBand( def, dyn.r1c1, dyn.r1c3, orientation );
		failForBand( def, dyn.r1c1, dyn.r1c4, orientation );
		failForBand( def, dyn.r1c2, dyn.r1c2, orientation );
		failForBand( def, dyn.r1c2, dyn.r1c3, orientation );
		failForBand( def, dyn.r1c2, dyn.r1c3, orientation );
		failForBand( def, dyn.r1c3, dyn.r1c3, orientation );
		failForBand( def, dyn.r1c3, dyn.r1c4, orientation );
		acceptBand( def, dyn.r1c4, dyn.r1c4, orientation );
	}


	private void acceptBand( Section _def, CellInstance _from, CellInstance _to, Orientation _orientation )
			throws ModelError, NoSuchMethodException
	{
		CellRange two = new CellRange( _from.getCellIndex(), _to.getCellIndex() );
		_def.defineRepeatingSection( two, _orientation, getInput( "getDetails" ), null );
	}


	private void failForBand( Section _def, CellInstance _from, CellInstance _to, Orientation _orientation )
			throws ModelError, NoSuchMethodException
	{
		CellRange two = new CellRange( _from.getCellIndex(), _to.getCellIndex() );
		try {
			_def.defineRepeatingSection( two, _orientation, getInput( "getDetails" ), null );
			fail( "No overlap reported for " + two );
		}
		catch (ModelError.SectionOverlap e) {
			// expected
		}
	}


	public void testAllowDisjointHorizontalBands() throws ModelError, NoSuchMethodException
	{
		WorksheetBuilderWithBands dyn = new WorksheetBuilderWithBands( this.sheet );
		Section def = new EngineDefinition( this.workbook ).getRoot();

		CellRange one = new CellRange( dyn.r1c1.getCellIndex(), dyn.r2c1.getCellIndex() );
		def.defineRepeatingSection( one, Orientation.HORIZONTAL, getInput( "getDetails" ), null );

		CellRange two = new CellRange( dyn.r1c2.getCellIndex(), dyn.r2c2.getCellIndex() );
		def.defineRepeatingSection( two, Orientation.HORIZONTAL, getInput( "getDetails" ), null );
	}


	public void testAllowDisjointVerticalBands() throws ModelError, NoSuchMethodException
	{
		WorksheetBuilderWithBands dyn = new WorksheetBuilderWithBands( this.sheet );
		Section def = new EngineDefinition( this.workbook ).getRoot();

		CellRange one = new CellRange( dyn.r1c1.getCellIndex(), dyn.r1c2.getCellIndex() );
		def.defineRepeatingSection( one, Orientation.VERTICAL, getInput( "getDetails" ), null );

		CellRange two = new CellRange( dyn.r2c1.getCellIndex(), dyn.r2c2.getCellIndex() );
		def.defineRepeatingSection( two, Orientation.VERTICAL, getInput( "getDetails" ), null );
	}


	public void testNoDefsOutsideBand() throws ModelError, NoSuchMethodException
	{
		WorksheetBuilderWithBands dyn = new WorksheetBuilderWithBands( this.sheet );
		Section def = new EngineDefinition( this.workbook ).getRoot();

		CellRange one = new CellRange( dyn.r2c2.getCellIndex(), dyn.r3c3.getCellIndex() );
		Compiler.Section band = def.defineRepeatingSection( one, Orientation.VERTICAL, getInput( "getDetails" ), null );

		failForOutsideDef( band, dyn.r1c1 );
		failForOutsideDef( band, dyn.r4c1 );
		failForOutsideDef( band, dyn.r1c1, dyn.r1c4 );
		failForOutsideDef( band, dyn.r1c1, dyn.r2c4 );
		failForOutsideDef( band, dyn.r3c1, dyn.r4c4 );
		failForOutsideDef( band, dyn.r4c1, dyn.r4c4 );
	}


	private void failForOutsideDef( Compiler.Section _band, CellInstance _cell ) throws ModelError,
			NoSuchMethodException
	{
		try {
			_band.defineInputCell( _cell.getCellIndex(), getInput( "getOne" ) );
			fail( "Definition for " + _cell.getCellIndex() + " accepted, but was not in band" );
		}
		catch (ModelError.NotInSection e) {
			// expected
		}
	}


	private void failForOutsideDef( Compiler.Section _band, CellInstance _from, CellInstance _to ) throws ModelError,
			NoSuchMethodException
	{
		CellRange rng = new CellRange( _from.getCellIndex(), _to.getCellIndex() );
		try {
			_band.defineRepeatingSection( rng, Orientation.VERTICAL, getInput( "getSubDetails" ), null );
			fail( "Definition for " + rng + " accepted, but was not in band" );
		}
		catch (ModelError.NotInSection e) {
			// expected
		}
	}


}
