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
package sej.engine.compiler.model.compiler;

import java.text.NumberFormat;

import sej.Compiler;
import sej.ModelError;
import sej.Orientation;
import sej.ModelError.SectionExtentNotCovered;
import sej.engine.compiler.definition.EngineDefinition;
import sej.engine.compiler.model.CellModel;
import sej.engine.compiler.model.EngineModel;
import sej.engine.compiler.model.SectionModel;
import sej.engine.compiler.model.optimizer.ReferenceCounter;
import sej.engine.expressions.Aggregator;
import sej.engine.expressions.ExpressionNode;
import sej.engine.expressions.ExpressionNodeForAggregator;
import sej.engine.expressions.ExpressionNodeForOperator;
import sej.engine.expressions.Operator;
import sej.model.CellIndex;
import sej.model.CellInstance;
import sej.model.CellRange;
import sej.model.CellWithConstant;
import sej.model.CellWithLazilyParsedExpression;
import sej.model.ExpressionNodeForCell;
import sej.model.ExpressionNodeForRange;
import sej.model.Row;
import sej.model.Sheet;
import sej.model.Workbook;
import sej.tests.utils.AbstractTestBase;
import sej.tests.utils.WorksheetBuilderWithBands;

public class EngineModelCompilerTest extends AbstractTestBase
{


	public void testModelBuilding() throws ModelError, SecurityException, NoSuchMethodException
	{
		NumberFormat format = (NumberFormat) NumberFormat.getNumberInstance().clone();
		format.setMaximumFractionDigits( 2 );

		Workbook workbook = new Workbook();
		Sheet sheet = new Sheet( workbook );
		Row r1 = new Row( sheet );

		CellInstance i1 = new CellWithConstant( r1, 1.0 );
		CellInstance i2 = new CellWithConstant( r1, 6.0 );
		CellInstance o1 = new CellWithConstant( r1, 2.0 );
		CellInstance x1 = new CellWithConstant( r1, 3.0 );
		CellInstance x2 = new CellWithConstant( r1, 4.0 );
		CellInstance x3 = new CellWithLazilyParsedExpression( r1, plus( ref( x2 ), ref( x1 ) ) );
		new CellWithConstant( r1, 5.0 ); // unused
		CellInstance o2 = new CellWithLazilyParsedExpression( r1, plus( plus( plus( ref( i1 ), ref( x1 ) ), ref( x3 ) ),
				ix( 100, 100 ) ) );
		o2.setNumberFormat( format );
		CellInstance io1 = new CellWithConstant( r1, 7.0 );

		EngineDefinition def = new EngineDefinition( workbook );
		def.defineInputCell( i1.getCellIndex(), getInput( "getOne" ) );
		def.defineInputCell( i2.getCellIndex(), getInput( "getTwo" ) );
		def.defineInputCell( io1.getCellIndex(), getInput( "getThree" ) );
		def.defineOutputCell( o1.getCellIndex(), getOutput( "getA" ) );
		def.defineOutputCell( o2.getCellIndex(), getOutput( "getB" ) );
		def.defineOutputCell( io1.getCellIndex(), getOutput( "getC" ) );

		EngineModelCompiler compiler = new EngineModelCompiler( def );
		EngineModel model = compiler.buildNewModel();
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
		assertEquals( "(((getOne() + D1) + F1) + #NULL)", o2m.getExpression().describe() );

		CellModel i1m = root.getCells().get( 2 );
		assertEquals( "getOne()", i1m.getName() );
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

		CellModel x2m = root.getCells().get( 5 );
		assertEquals( "E1", x2m.getName() );
		assertEquals( false, x2m.isInput() );
		assertEquals( false, x2m.isOutput() );
		assertEquals( 1, x2m.getReferenceCount() );
		assertEquals( CellModel.UNLIMITED, x2m.getMaxFractionalDigits() );
		assertEquals( 4.0, (Double) x2m.getConstantValue(), 0.1 );

		CellModel io1m = root.getCells().get( 6 );
		assertEquals( "getThree()", io1m.getName() );
		assertEquals( true, io1m.isInput() );
		assertEquals( true, io1m.isOutput() );
		assertEquals( 1, io1m.getReferenceCount() );
		assertEquals( CellModel.UNLIMITED, io1m.getMaxFractionalDigits() );
		assertEquals( 7.0, (Double) io1m.getConstantValue(), 0.1 );

		assertEquals( 8, root.getCells().size() );
	}


	public void testSumOverBandWithOuterRef() throws ModelError
	{
		Workbook workbook = new Workbook();
		Sheet sheet = new Sheet( workbook );
		Row r1 = new Row( sheet );
		new CellWithLazilyParsedExpression( r1, null );
		WorksheetBuilderWithBands bld = new WorksheetBuilderWithBands( sheet );

		EngineDefinition def = new EngineDefinition( workbook );
		bld.defineCompiler( def );

		EngineModelCompiler compiler = new EngineModelCompiler( def );
		EngineModel model = compiler.buildNewModel();
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

		assertEquals( "SUM( getDetails().D2 )", result.getExpression().describe() );
		assertEquals( "(SUM( {getOne(),getTwo(),getThree()} ) * ..B1)", x1.getExpression().describe() );

		assertEquals( "D2", x1.getName() );
		assertEquals( "getOne()", i1.getName() );
		assertEquals( "getTwo()", i2.getName() );
		assertEquals( "getThree()", i3.getName() );
	}


	public void testBandWithOutputCells() throws ModelError, SecurityException, NoSuchMethodException
	{
		Workbook workbook = new Workbook();
		Sheet sheet = new Sheet( workbook );
		Row r1 = new Row( sheet );
		new CellWithLazilyParsedExpression( r1, null );
		WorksheetBuilderWithBands bld = new WorksheetBuilderWithBands( sheet );

		EngineDefinition def = new EngineDefinition( workbook );
		bld.defineRange( def );
		bld.details.defineOutputCell( bld.r1c4.getCellIndex(), getInput( "getOne" ) );

		EngineModelCompiler compiler = new EngineModelCompiler( def );
		EngineModel model = compiler.buildNewModel();
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
		assertEquals( "getOne()", i1.getName() );
		assertEquals( "getTwo()", i2.getName() );
		assertEquals( "getThree()", i3.getName() );

		assertEquals( "(SUM( {getOne(),getTwo(),getThree()} ) * ..B1)", o1.getExpression().describe() );
	}


	public void testBandCellWithSumOverOuterBand() throws ModelError, SecurityException, NoSuchMethodException
	{
		Workbook workbook = new Workbook();
		Sheet sheet = new Sheet( workbook );
		Row r1 = new Row( sheet );

		CellInstance i1 = new CellWithConstant( r1, 1.0 );
		CellInstance o1 = new CellWithLazilyParsedExpression( r1, sum( refRange( i1, i1 ) ) );

		EngineDefinition def = new EngineDefinition( workbook );
		Compiler.Section inputsDef = def.defineRepeatingSection( rng( i1, i1 ), Orientation.HORIZONTAL,
				getInput( "getDetails" ), null );
		Compiler.Section outputsDef = def.defineRepeatingSection( rng( o1, o1 ), Orientation.HORIZONTAL,
				getInput( "getDetails" ), getOutput( "getDetails" ) );
		inputsDef.defineInputCell( i1.getCellIndex(), getInput( "getOne" ) );
		outputsDef.defineOutputCell( o1.getCellIndex(), getOutput( "getA" ) );

		EngineModelCompiler compiler = new EngineModelCompiler( def );
		EngineModel model = compiler.buildNewModel();
		SectionModel root = model.getRoot();

		assertEquals( 0, root.getCells().size() );
		assertEquals( 2, root.getSections().size() );

		SectionModel outputs = root.getSections().get( 0 );
		SectionModel inputs = root.getSections().get( 1 );

		assertEquals( "getDetails()", outputs.getName() );
		assertEquals( "getDetails()", inputs.getName() );

		final CellModel output = outputs.getCells().get( 0 );
		assertEquals( "SUM( ..getDetails().getOne() )", output.getExpression().describe() );
	}


	public void testBandCellWithSumOverInnerBand() throws ModelError, SecurityException, NoSuchMethodException
	{
		Workbook workbook = new Workbook();
		Sheet sheet = new Sheet( workbook );

		Row r1 = new Row( sheet );
		Row r2 = new Row( sheet );

		CellInstance i1 = new CellWithConstant( r1, 1.0 );
		CellInstance x1 = new CellWithLazilyParsedExpression( r2, sum( refRange( i1, i1 ) ) );
		CellInstance o1 = new CellWithLazilyParsedExpression( r1, sum( refRange( x1, x1 ) ) );
		CellInstance o2 = new CellWithLazilyParsedExpression( r1, sum( refRange( i1, i1 ) ) );

		EngineDefinition def = new EngineDefinition( workbook );
		Compiler.Section outerDef = def.defineRepeatingSection( rng( i1, x1 ), Orientation.HORIZONTAL,
				getInput( "getDetails" ), null );
		Compiler.Section innerDef = outerDef.defineRepeatingSection( rng( i1, i1 ), Orientation.VERTICAL,
				getInput( "getSubDetails" ), null );
		innerDef.defineInputCell( i1.getCellIndex(), getInput( "getOne" ) );
		def.defineOutputCell( o1.getCellIndex(), getOutput( "getA" ) );
		def.defineOutputCell( o2.getCellIndex(), getOutput( "getB" ) );

		EngineModelCompiler compiler = new EngineModelCompiler( def );
		EngineModel model = compiler.buildNewModel();
		SectionModel root = model.getRoot();

		assertEquals( 2, root.getCells().size() );
		assertEquals( 1, root.getSections().size() );

		CellModel line = root.getCells().get( 0 );
		CellModel global = root.getCells().get( 1 );
		SectionModel outer = root.getSections().get( 0 );

		assertEquals( "B1", line.getName() );
		assertEquals( "C1", global.getName() );
		assertEquals( "getDetails()", outer.getName() );

		assertEquals( 1, outer.getCells().size() );
		assertEquals( 1, outer.getSections().size() );

		CellModel inter = outer.getCells().get( 0 );
		SectionModel inner = outer.getSections().get( 0 );

		assertEquals( "A2", inter.getName() );
		assertEquals( "getSubDetails()", inner.getName() );

		assertEquals( 1, inner.getCells().size() );
		assertEquals( 0, inner.getSections().size() );

		CellModel inp = inner.getCells().get( 0 );

		assertEquals( "getOne()", inp.getName() );

		assertEquals( "SUM( getDetails().getSubDetails().getOne() )", global.getExpression().describe() );
		assertEquals( "SUM( getDetails().A2 )", line.getExpression().describe() );
		assertEquals( "SUM( getSubDetails().getOne() )", inter.getExpression().describe() );
	}


	public void testSumOverRangeThatDoesNotFullyCoverItsBandsExtent() throws ModelError, SecurityException, NoSuchMethodException
	{
		Workbook workbook = new Workbook();
		Sheet sheet = new Sheet( workbook );
		Row r1 = new Row( sheet );

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


	private void failForRangeNotCoveringBandExtent( Workbook workbook, CellInstance output, CellRange rng )
			throws ModelError, SecurityException, NoSuchMethodException
	{
		EngineDefinition def = new EngineDefinition( workbook );
		def.defineRepeatingSection( rng, Orientation.HORIZONTAL, getInput( "getDetails"), null );
		def.defineOutputCell( output.getCellIndex(), getOutput( "getResult" ));
		EngineModelCompiler compiler = new EngineModelCompiler( def );
		try {
			compiler.buildNewModel();
			fail( "Definition for " + output + " accepted even though it does not cover the full range." );
		}
		catch (SectionExtentNotCovered e) {
			// expected
		}
	}


	private ExpressionNode plus( ExpressionNode _a, ExpressionNode _b )
	{
		return new ExpressionNodeForOperator( Operator.PLUS, _a, _b );
	}


	private ExpressionNode sum( ExpressionNode... _args )
	{
		return new ExpressionNodeForAggregator( Aggregator.SUM, _args );
	}


	private ExpressionNode ref( CellInstance _cell )
	{
		return new ExpressionNodeForCell( _cell.getCellIndex() );
	}


	private ExpressionNode ix( int _col, int _row )
	{
		return new ExpressionNodeForCell( new CellIndex( 0, _col, _row ) );
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
