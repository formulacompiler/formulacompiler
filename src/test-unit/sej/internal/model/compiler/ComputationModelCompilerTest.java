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
package sej.internal.model.compiler;

import java.text.NumberFormat;

import sej.CompilerException;
import sej.Function;
import sej.Operator;
import sej.Orientation;
import sej.SEJ;
import sej.SpreadsheetBinder;
import sej.CompilerException.SectionExtentNotCovered;
import sej.SpreadsheetBinder.Section;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForFunction;
import sej.internal.expressions.ExpressionNodeForOperator;
import sej.internal.model.CellModel;
import sej.internal.model.ComputationModel;
import sej.internal.model.SectionModel;
import sej.internal.model.optimizer.ReferenceCounter;
import sej.internal.spreadsheet.CellIndex;
import sej.internal.spreadsheet.CellInstance;
import sej.internal.spreadsheet.CellRange;
import sej.internal.spreadsheet.CellWithConstant;
import sej.internal.spreadsheet.CellWithLazilyParsedExpression;
import sej.internal.spreadsheet.ExpressionNodeForCell;
import sej.internal.spreadsheet.ExpressionNodeForRange;
import sej.internal.spreadsheet.RowImpl;
import sej.internal.spreadsheet.SheetImpl;
import sej.internal.spreadsheet.SpreadsheetImpl;
import sej.tests.utils.AbstractTestBase;
import sej.tests.utils.Inputs;
import sej.tests.utils.Outputs;
import sej.tests.utils.WorksheetBuilderWithBands;

public class ComputationModelCompilerTest extends AbstractTestBase
{


	public void testModelBuilding() throws Exception
	{
		NumberFormat format = (NumberFormat) NumberFormat.getNumberInstance().clone();
		format.setMaximumFractionDigits( 2 );

		SpreadsheetImpl workbook = new SpreadsheetImpl();
		SheetImpl sheet = new SheetImpl( workbook );
		RowImpl r1 = new RowImpl( sheet );

		CellInstance i1 = new CellWithConstant( r1, 1.0 );
		CellInstance i2 = new CellWithConstant( r1, 6.0 );
		CellInstance o1 = new CellWithConstant( r1, 2.0 );
		CellInstance x1 = new CellWithConstant( r1, 3.0 );
		CellInstance x2 = new CellWithConstant( r1, 4.0 );
		CellInstance x3 = new CellWithLazilyParsedExpression( r1, plus( ref( x2 ), ref( x1 ) ) );
		new CellWithConstant( r1, 5.0 ); // unused
		CellInstance o2 = new CellWithLazilyParsedExpression( r1, plus( plus( plus( ref( i1 ), ref( x1 ) ), ref( x3 ) ),
				ix( workbook, 100, 100 ) ) );
		o2.setNumberFormat( format );
		CellInstance io1 = new CellWithConstant( r1, 7.0 );

		SpreadsheetBinder def = newBinder( workbook );
		Section rootDef = def.getRoot();
		rootDef.defineInputCell( i1.getCellIndex(), getInput( "getOne" ) );
		rootDef.defineInputCell( i2.getCellIndex(), getInput( "getTwo" ) );
		rootDef.defineInputCell( io1.getCellIndex(), getInput( "getThree" ) );
		rootDef.defineOutputCell( o1.getCellIndex(), getOutput( "getA" ) );
		rootDef.defineOutputCell( o2.getCellIndex(), getOutput( "getB" ) );
		rootDef.defineOutputCell( io1.getCellIndex(), getOutput( "getC" ) );

		ComputationModelCompiler compiler = new ComputationModelCompiler( def.getBinding(), SEJ.DOUBLE );
		ComputationModel model = compiler.buildNewModel();

		model.traverse( new ReferenceCounter() );
		SectionModel root = model.getRoot();

		assertEquals( 0, root.getSections().size() );

		CellModel o1m = root.getCells().get( 0 );
		assertEquals( "C1", o1m.getName() );
		assertEquals( false, o1m.isInput() );
		assertEquals( true, o1m.isOutput() );
		assertEquals( 1, o1m.getReferenceCount() );
		assertEquals( CellModel.UNLIMITED, o1m.getMaxFractionalDigits() );
		assertEquals( 2.0, (Double) o1m.getConstantValue(), 0.1 );

		CellModel o2m = root.getCells().get( 1 );
		assertEquals( "H1", o2m.getName() );
		assertEquals( false, o2m.isInput() );
		assertEquals( true, o2m.isOutput() );
		assertEquals( 1, o2m.getReferenceCount() );
		assertEquals( 2, o2m.getMaxFractionalDigits() );
		assertEquals( "(((Inputs.getOne() + D1) + F1) + #NULL)", o2m.getExpression().describe() );

		CellModel i1m = root.getCells().get( 2 );
		assertEquals( "Inputs.getOne()", i1m.getName() );
		assertEquals( true, i1m.isInput() );
		assertEquals( false, i1m.isOutput() );
		assertEquals( 1, i1m.getReferenceCount() );
		assertEquals( CellModel.UNLIMITED, i1m.getMaxFractionalDigits() );
		assertEquals( 1.0, (Double) i1m.getConstantValue(), 0.1 );

		CellModel x1m = root.getCells().get( 3 );
		assertEquals( "D1", x1m.getName() );
		assertEquals( false, x1m.isInput() );
		assertEquals( false, x1m.isOutput() );
		assertEquals( 2, x1m.getReferenceCount() );
		assertEquals( CellModel.UNLIMITED, x1m.getMaxFractionalDigits() );
		assertEquals( 3.0, (Double) x1m.getConstantValue(), 0.1 );

		CellModel x3m = root.getCells().get( 4 );
		assertEquals( "F1", x3m.getName() );
		assertEquals( false, x3m.isInput() );
		assertEquals( false, x3m.isOutput() );
		assertEquals( 1, x3m.getReferenceCount() );
		assertEquals( CellModel.UNLIMITED, x3m.getMaxFractionalDigits() );
		assertEquals( "(E1 + D1)", x3m.getExpression().describe() );
		assertSame( x3.getExpression(), x3m.getExpression().getDerivedFrom() );
		assertSame( x3.getExpression().arguments().get( 0 ), x3m.getExpression().arguments().get( 0 ).getDerivedFrom() );

		CellModel x2m = root.getCells().get( 5 );
		assertEquals( "E1", x2m.getName() );
		assertEquals( false, x2m.isInput() );
		assertEquals( false, x2m.isOutput() );
		assertEquals( 1, x2m.getReferenceCount() );
		assertEquals( CellModel.UNLIMITED, x2m.getMaxFractionalDigits() );
		assertEquals( 4.0, (Double) x2m.getConstantValue(), 0.1 );

		CellModel io1m = root.getCells().get( 6 );
		assertEquals( "Inputs.getThree()", io1m.getName() );
		assertEquals( true, io1m.isInput() );
		assertEquals( true, io1m.isOutput() );
		assertEquals( 1, io1m.getReferenceCount() );
		assertEquals( CellModel.UNLIMITED, io1m.getMaxFractionalDigits() );
		assertEquals( 7.0, (Double) io1m.getConstantValue(), 0.1 );

		assertEquals( 7, root.getCells().size() );
	}


	public void testSumOverBandWithOuterRef() throws Exception
	{
		SpreadsheetImpl workbook = new SpreadsheetImpl();
		SheetImpl sheet = new SheetImpl( workbook );
		RowImpl r1 = new RowImpl( sheet );
		new CellWithLazilyParsedExpression( r1, null );
		WorksheetBuilderWithBands bld = new WorksheetBuilderWithBands( sheet );

		SpreadsheetBinder def = newBinder( workbook );
		bld.defineWorkbook( def.getRoot() );

		ComputationModelCompiler compiler = new ComputationModelCompiler( def.getBinding(), SEJ.DOUBLE );
		ComputationModel model = compiler.buildNewModel();
		SectionModel root = model.getRoot();

		assertEquals( 2, root.getCells().size() );
		assertEquals( 1, root.getSections().size() );

		CellModel result = root.getCells().get( 0 );
		CellModel constant = root.getCells().get( 1 );
		SectionModel range = root.getSections().get( 0 );

		assertEquals( "B1", constant.getName() );
		assertEquals( "A1", result.getName() );

		assertEquals( root, range.getSection() );
		assertEquals( 4, range.getCells().size() );
		assertEquals( 0, range.getSections().size() );

		CellModel x1 = range.getCells().get( 0 );
		CellModel i1 = range.getCells().get( 1 );
		CellModel i2 = range.getCells().get( 2 );
		CellModel i3 = range.getCells().get( 3 );

		assertEquals( "SUM( Inputs.getDetails()~>D2 )", result.getExpression().describe() );
		assertEquals( "(SUM( Inputs.getOne(), Inputs.getTwo(), Inputs.getThree() ) * <~B1)", x1.getExpression().describe() );

		assertEquals( "D2", x1.getName() );
		assertEquals( "Inputs.getOne()", i1.getName() );
		assertEquals( "Inputs.getTwo()", i2.getName() );
		assertEquals( "Inputs.getThree()", i3.getName() );
	}


	public void testBandWithOutputCells() throws Exception
	{
		SpreadsheetImpl workbook = new SpreadsheetImpl();
		SheetImpl sheet = new SheetImpl( workbook );
		RowImpl r1 = new RowImpl( sheet );
		new CellWithLazilyParsedExpression( r1, null );
		WorksheetBuilderWithBands bld = new WorksheetBuilderWithBands( sheet );

		SpreadsheetBinder def = newBinder( workbook );
		bld.defineRange( def.getRoot() );
		bld.details.defineOutputCell( bld.r1c4.getCellIndex(), getInput( "getOne" ) );

		ComputationModelCompiler compiler = new ComputationModelCompiler( def.getBinding(), SEJ.DOUBLE );
		ComputationModel model = compiler.buildNewModel();
		SectionModel root = model.getRoot();

		assertEquals( 1, root.getCells().size() );
		assertEquals( 1, root.getSections().size() );

		CellModel constant = root.getCells().get( 0 );
		SectionModel range = root.getSections().get( 0 );

		assertEquals( "B1", constant.getName() );

		assertEquals( root, range.getSection() );
		assertEquals( 4, range.getCells().size() );
		assertEquals( 0, range.getSections().size() );

		CellModel o1 = range.getCells().get( 0 );
		CellModel i1 = range.getCells().get( 1 );
		CellModel i2 = range.getCells().get( 2 );
		CellModel i3 = range.getCells().get( 3 );

		assertEquals( "D2", o1.getName() );
		assertEquals( "Inputs.getOne()", i1.getName() );
		assertEquals( "Inputs.getTwo()", i2.getName() );
		assertEquals( "Inputs.getThree()", i3.getName() );

		assertEquals( "(SUM( Inputs.getOne(), Inputs.getTwo(), Inputs.getThree() ) * <~B1)", o1.getExpression().describe() );
	}


	public void testBandCellWithSumOverOuterBand() throws Exception
	{
		SpreadsheetImpl workbook = new SpreadsheetImpl();
		SheetImpl sheet = new SheetImpl( workbook );
		RowImpl r1 = new RowImpl( sheet );

		CellInstance i1 = new CellWithConstant( r1, 1.0 );
		CellInstance o1 = new CellWithLazilyParsedExpression( r1, sum( refRange( i1, i1 ) ) );

		SpreadsheetBinder def = newBinder( workbook );
		Section rootDef = def.getRoot();
		SpreadsheetBinder.Section inputsDef = rootDef.defineRepeatingSection( rng( i1, i1 ), Orientation.HORIZONTAL,
				getInput( "getDetails" ), Inputs.class, null, null );
		SpreadsheetBinder.Section outputsDef = rootDef.defineRepeatingSection( rng( o1, o1 ), Orientation.HORIZONTAL,
				getInput( "getDetails" ), Inputs.class, getOutput( "getDetails" ), Outputs.class );
		inputsDef.defineInputCell( i1.getCellIndex(), getInput( "getOne" ) );
		outputsDef.defineOutputCell( o1.getCellIndex(), getOutput( "getA" ) );

		ComputationModelCompiler compiler = new ComputationModelCompiler( def.getBinding(), SEJ.DOUBLE );
		try {
			ComputationModel model = compiler.buildNewModel();
			SectionModel root = model.getRoot();
	
			assertEquals( 0, root.getCells().size() );
			assertEquals( 2, root.getSections().size() );
	
			SectionModel outputs = root.getSections().get( 0 );
			SectionModel inputs = root.getSections().get( 1 );
	
			assertEquals( "Inputs.getDetails()", outputs.getName() );
			assertEquals( "Inputs.getDetails()", inputs.getName() );
	
			final CellModel output = outputs.getCells().get( 0 );
			assertEquals( "SUM( <~Inputs.getDetails()~>Inputs.getOne() )", output.getExpression().describe() );
			fail();
		}
		catch (CompilerException.ReferenceToOuterInnerCell e) {
			// expected
		}
	}


	public void testBandCellWithSumOverInnerBand() throws Exception
	{
		SpreadsheetImpl workbook = new SpreadsheetImpl();
		SheetImpl sheet = new SheetImpl( workbook );

		RowImpl r1 = new RowImpl( sheet );
		RowImpl r2 = new RowImpl( sheet );

		CellInstance i1 = new CellWithConstant( r1, 1.0 );
		CellInstance x1 = new CellWithLazilyParsedExpression( r2, sum( refRange( i1, i1 ) ) );
		CellInstance o1 = new CellWithLazilyParsedExpression( r1, sum( refRange( x1, x1 ) ) );
		CellInstance o2 = new CellWithLazilyParsedExpression( r1, sum( refRange( i1, i1 ) ) );

		SpreadsheetBinder def = newBinder( workbook );
		Section rootDef = def.getRoot();
		SpreadsheetBinder.Section outerDef = rootDef.defineRepeatingSection( rng( i1, x1 ), Orientation.HORIZONTAL,
				getInput( "getDetails" ), Inputs.class, null, null );
		SpreadsheetBinder.Section innerDef = outerDef.defineRepeatingSection( rng( i1, i1 ), Orientation.VERTICAL,
				getInput( "getSubDetails" ), Inputs.class, null, null );
		innerDef.defineInputCell( i1.getCellIndex(), getInput( "getOne" ) );
		rootDef.defineOutputCell( o1.getCellIndex(), getOutput( "getA" ) );
		rootDef.defineOutputCell( o2.getCellIndex(), getOutput( "getB" ) );

		ComputationModelCompiler compiler = new ComputationModelCompiler( def.getBinding(), SEJ.DOUBLE );
		ComputationModel model = compiler.buildNewModel();
		SectionModel root = model.getRoot();

		assertEquals( 2, root.getCells().size() );
		assertEquals( 1, root.getSections().size() );

		CellModel line = root.getCells().get( 0 );
		CellModel global = root.getCells().get( 1 );
		SectionModel outer = root.getSections().get( 0 );

		assertEquals( "B1", line.getName() );
		assertEquals( "C1", global.getName() );
		assertEquals( "Inputs.getDetails()", outer.getName() );

		assertEquals( 1, outer.getCells().size() );
		assertEquals( 1, outer.getSections().size() );

		CellModel inter = outer.getCells().get( 0 );
		SectionModel inner = outer.getSections().get( 0 );

		assertEquals( "A2", inter.getName() );
		assertEquals( "Inputs.getSubDetails()", inner.getName() );

		assertEquals( 1, inner.getCells().size() );
		assertEquals( 0, inner.getSections().size() );

		CellModel inp = inner.getCells().get( 0 );

		assertEquals( "Inputs.getOne()", inp.getName() );

		assertEquals( "SUM( Inputs.getDetails()~>Inputs.getSubDetails()~>Inputs.getOne() )", global.getExpression().describe() );
		assertEquals( "SUM( Inputs.getDetails()~>A2 )", line.getExpression().describe() );
		assertEquals( "SUM( Inputs.getSubDetails()~>Inputs.getOne() )", inter.getExpression().describe() );
	}


	public void testSumOverRangeThatDoesNotFullyCoverItsBandsExtent() throws Exception
	{
		SpreadsheetImpl workbook = new SpreadsheetImpl();
		SheetImpl sheet = new SheetImpl( workbook );
		RowImpl r1 = new RowImpl( sheet );

		CellInstance i1 = new CellWithConstant( r1, 1.0 );
		CellInstance i2 = new CellWithConstant( r1, 2.0 );
		CellInstance i3 = new CellWithConstant( r1, 3.0 );
		CellInstance o1 = new CellWithLazilyParsedExpression( r1, sum( refRange( i1, i1 ) ) );
		CellInstance o2 = new CellWithLazilyParsedExpression( r1, sum( refRange( i1, i2 ) ) );
		CellInstance o3 = new CellWithLazilyParsedExpression( r1, sum( refRange( i2, i2 ) ) );
		CellInstance o4 = new CellWithLazilyParsedExpression( r1, sum( refRange( i2, i3 ) ) );

		CellRange rng = rng( i1, i3 );

		failForRangeNotCoveringBandExtent( workbook, o1, rng );
		failForRangeNotCoveringBandExtent( workbook, o2, rng );
		failForRangeNotCoveringBandExtent( workbook, o3, rng );
		failForRangeNotCoveringBandExtent( workbook, o4, rng );
	}


	private void failForRangeNotCoveringBandExtent( SpreadsheetImpl workbook, CellInstance output, CellRange rng )
			throws Exception
	{
		SpreadsheetBinder def = newBinder( workbook );
		Section rootDef = def.getRoot();
		rootDef.defineRepeatingSection( rng, Orientation.HORIZONTAL, getInput( "getDetails" ), Inputs.class, null, null );
		rootDef.defineOutputCell( output.getCellIndex(), getOutput( "getResult" ) );

		ComputationModelCompiler compiler = new ComputationModelCompiler( def.getBinding(), SEJ.DOUBLE );
		try {
			compiler.buildNewModel();
			fail( "Definition for " + output + " accepted even though it does not cover the full range." );
		}
		catch (SectionExtentNotCovered e) {
			// expected
		}
	}


	public void testReferenceToSecondarySheet() throws Exception
	{
		SpreadsheetImpl workbook = new SpreadsheetImpl();
		SheetImpl sheet1 = new SheetImpl( workbook, "One" );
		SheetImpl sheet2 = new SheetImpl( workbook, "Two" );
		RowImpl r11 = new RowImpl( sheet1 );
		RowImpl r21 = new RowImpl( sheet2 );
		
		CellInstance i1 = new CellWithConstant( r21, 1.0 );
		CellInstance o1 = new CellWithLazilyParsedExpression( r11, sum( refRange( i1, i1 ) ) );

		SpreadsheetBinder def = newBinder( workbook );
		Section rootDef = def.getRoot();
		rootDef.defineInputCell( i1.getCellIndex(), getInput( "getOne" ) );
		rootDef.defineOutputCell( o1.getCellIndex(), getOutput( "getResult" ) );

		ComputationModelCompiler compiler = new ComputationModelCompiler( def.getBinding(), SEJ.DOUBLE );
		ComputationModel model = compiler.buildNewModel();
		SectionModel root = model.getRoot();

		assertEquals( 2, root.getCells().size() );

		CellModel _o1 = root.getCells().get( 0 );
		CellModel _i1 = root.getCells().get( 1 );

		assertEquals( "A1", _o1.getName() );
		assertEquals( "Inputs.getOne()", _i1.getName() );
		assertEquals( "SUM( Inputs.getOne() )", _o1.getExpression().describe() );
	}
	
	
	public void testReferenceFromSecondarySheet() throws Exception
	{
		SpreadsheetImpl workbook = new SpreadsheetImpl();
		SheetImpl sheet1 = new SheetImpl( workbook, "One" );
		SheetImpl sheet2 = new SheetImpl( workbook, "Two" );
		RowImpl r11 = new RowImpl( sheet1 );
		RowImpl r21 = new RowImpl( sheet2 );
		
		CellInstance i1 = new CellWithConstant( r11, 1.0 );
		CellInstance o1 = new CellWithLazilyParsedExpression( r21, sum( refRange( i1, i1 ) ) );

		SpreadsheetBinder def = newBinder( workbook );
		Section rootDef = def.getRoot();
		rootDef.defineInputCell( i1.getCellIndex(), getInput( "getOne" ) );
		rootDef.defineOutputCell( o1.getCellIndex(), getOutput( "getResult" ) );

		ComputationModelCompiler compiler = new ComputationModelCompiler( def.getBinding(), SEJ.DOUBLE );
		ComputationModel model = compiler.buildNewModel();
		SectionModel root = model.getRoot();

		assertEquals( 2, root.getCells().size() );

		CellModel _o1 = root.getCells().get( 0 );
		CellModel _i1 = root.getCells().get( 1 );

		assertEquals( "'Two'!A1", _o1.getName() );
		assertEquals( "Inputs.getOne()", _i1.getName() );
		assertEquals( "SUM( Inputs.getOne() )", _o1.getExpression().describe() );
	}

	
	private SpreadsheetBinder newBinder( SpreadsheetImpl _workbook )
	{
		return SEJ.newSpreadsheetBinder( _workbook, Inputs.class, Outputs.class );
	}


	private ExpressionNode plus( ExpressionNode _a, ExpressionNode _b )
	{
		return new ExpressionNodeForOperator( Operator.PLUS, _a, _b );
	}


	private ExpressionNode sum( ExpressionNode... _args )
	{
		return new ExpressionNodeForFunction( Function.SUM, _args );
	}


	private ExpressionNode ref( CellInstance _cell )
	{
		return new ExpressionNodeForCell( _cell.getCellIndex() );
	}


	private ExpressionNode ix( SpreadsheetImpl _s, int _col, int _row )
	{
		return new ExpressionNodeForCell( new CellIndex( _s, 0, _col, _row ) );
	}


	private ExpressionNode refRange( CellInstance _from, CellInstance _to )
	{
		return new ExpressionNodeForRange( rng( _from, _to ) );
	}


	private CellRange rng( CellInstance _from, CellInstance _to )
	{
		return new CellRange( _from.getCellIndex(), _to.getCellIndex() );
	}


}
