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

package org.formulacompiler.spreadsheet.internal.compiler;

import java.text.NumberFormat;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForOperator;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ComputationModel;
import org.formulacompiler.compiler.internal.model.SectionModel;
import org.formulacompiler.compiler.internal.model.optimizer.ReferenceCounter;
import org.formulacompiler.spreadsheet.Orientation;
import org.formulacompiler.spreadsheet.SpreadsheetBinder;
import org.formulacompiler.spreadsheet.SpreadsheetBinder.Section;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.internal.BaseSpreadsheet;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.CellRange;
import org.formulacompiler.spreadsheet.internal.CellWithConstant;
import org.formulacompiler.spreadsheet.internal.CellWithExpression;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForCell;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForRange;
import org.formulacompiler.spreadsheet.internal.RowImpl;
import org.formulacompiler.spreadsheet.internal.SheetImpl;
import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;
import org.formulacompiler.tests.utils.AbstractStandardInputsOutputsTestCase;
import org.formulacompiler.tests.utils.Inputs;
import org.formulacompiler.tests.utils.Outputs;
import org.formulacompiler.tests.utils.WorksheetBuilderWithBands;

public class SpreadsheetToModelCompilerTest extends AbstractStandardInputsOutputsTestCase
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
 		CellWithExpression x3 = new CellWithExpression( r1, plus( ref( x2 ), ref( x1 ) ) );
		new CellWithConstant( r1, 5.0 ); // unused
		CellInstance o2 = new CellWithExpression( r1, plus( plus( plus( ref( i1 ), ref( x1 ) ), ref( x3 ) ),
				ix( workbook, 100, 100 ) ) );
		o2.applyNumberFormat( format );
		CellInstance io1 = new CellWithConstant( r1, 7.0 );

		SpreadsheetBinder def = newBinder( workbook );
		Section rootDef = def.getRoot();
		rootDef.defineInputCell( i1.getCellIndex(), getInput( "getOne" ) );
		rootDef.defineInputCell( i2.getCellIndex(), getInput( "getTwo" ) );
		rootDef.defineInputCell( io1.getCellIndex(), getInput( "getThree" ) );
		rootDef.defineOutputCell( o1.getCellIndex(), getOutput( "getA" ) );
		rootDef.defineOutputCell( o2.getCellIndex(), getOutput( "getB" ) );
		rootDef.defineOutputCell( io1.getCellIndex(), getOutput( "getC" ) );

		SpreadsheetToModelCompiler compiler = new SpreadsheetToModelCompiler( def.getBinding(),
				SpreadsheetCompiler.DOUBLE );
		ComputationModel model = compiler.compile();

		model.traverse( new ReferenceCounter() );
		SectionModel root = model.getRoot();

		assertEquals( 0, root.getSections().size() );

		CellModel o1m = root.getCells().get( 0 );
		assertEquals( "Sheet1!C1", o1m.toString() );
		assertEquals( false, o1m.isInput() );
		assertEquals( true, o1m.isOutput() );
		assertEquals( 1, o1m.getReferenceCount() );
		assertEquals( NumericType.UNLIMITED_FRACTIONAL_DIGITS, o1m.getMaxFractionalDigits() );
		assertEquals( 2.0, (Double) o1m.getConstantValue(), 0.1 );

		CellModel o2m = root.getCells().get( 1 );
		assertEquals( "Sheet1!H1", o2m.toString() );
		assertEquals( false, o2m.isInput() );
		assertEquals( true, o2m.isOutput() );
		assertEquals( 1, o2m.getReferenceCount() );
		assertEquals( 2, o2m.getMaxFractionalDigits() );
		assertEquals( "(((Inputs.getOne() + Sheet1!D1) + Sheet1!F1) + #NULL)", o2m.getExpression().describe() );

		CellModel i1m = root.getCells().get( 2 );
		assertEquals( "Inputs.getOne()", i1m.toString() );
		assertEquals( true, i1m.isInput() );
		assertEquals( false, i1m.isOutput() );
		assertEquals( 1, i1m.getReferenceCount() );
		assertEquals( NumericType.UNLIMITED_FRACTIONAL_DIGITS, i1m.getMaxFractionalDigits() );
		assertEquals( 1.0, (Double) i1m.getConstantValue(), 0.1 );

		CellModel x1m = root.getCells().get( 3 );
		assertEquals( "Sheet1!D1", x1m.toString() );
		assertEquals( false, x1m.isInput() );
		assertEquals( false, x1m.isOutput() );
		assertEquals( 2, x1m.getReferenceCount() );
		assertEquals( NumericType.UNLIMITED_FRACTIONAL_DIGITS, x1m.getMaxFractionalDigits() );
		assertEquals( 3.0, (Double) x1m.getConstantValue(), 0.1 );

		CellModel x3m = root.getCells().get( 4 );
		assertEquals( "Sheet1!F1", x3m.toString() );
		assertEquals( false, x3m.isInput() );
		assertEquals( false, x3m.isOutput() );
		assertEquals( 1, x3m.getReferenceCount() );
		assertEquals( NumericType.UNLIMITED_FRACTIONAL_DIGITS, x3m.getMaxFractionalDigits() );
		assertEquals( "(Sheet1!E1 + Sheet1!D1)", x3m.getExpression().describe() );
		assertSame( x3.getExpression(), x3m.getExpression().getDerivedFrom() );
		assertSame( x3.getExpression().arguments().get( 0 ), x3m.getExpression().arguments().get( 0 ).getDerivedFrom() );

		CellModel x2m = root.getCells().get( 5 );
		assertEquals( "Sheet1!E1", x2m.toString() );
		assertEquals( false, x2m.isInput() );
		assertEquals( false, x2m.isOutput() );
		assertEquals( 1, x2m.getReferenceCount() );
		assertEquals( NumericType.UNLIMITED_FRACTIONAL_DIGITS, x2m.getMaxFractionalDigits() );
		assertEquals( 4.0, (Double) x2m.getConstantValue(), 0.1 );

		CellModel io1m = root.getCells().get( 6 );
		assertEquals( "Inputs.getThree()", io1m.toString() );
		assertEquals( true, io1m.isInput() );
		assertEquals( true, io1m.isOutput() );
		assertEquals( 1, io1m.getReferenceCount() );
		assertEquals( NumericType.UNLIMITED_FRACTIONAL_DIGITS, io1m.getMaxFractionalDigits() );
		assertEquals( 7.0, (Double) io1m.getConstantValue(), 0.1 );

		assertEquals( 7, root.getCells().size() );
	}


	public void testSumOverBandWithOuterRef() throws Exception
	{
		SpreadsheetImpl workbook = new SpreadsheetImpl();
		SheetImpl sheet = new SheetImpl( workbook );
		RowImpl r1 = new RowImpl( sheet );
		new CellWithExpression( r1, null );
		WorksheetBuilderWithBands bld = new WorksheetBuilderWithBands( sheet );

		SpreadsheetBinder def = newBinder( workbook );
		bld.defineWorkbook( def.getRoot() );

		SpreadsheetToModelCompiler compiler = new SpreadsheetToModelCompiler( def.getBinding(),
				SpreadsheetCompiler.DOUBLE );
		ComputationModel model = compiler.compile();
		SectionModel root = model.getRoot();

		assertEquals( 2, root.getCells().size() );
		assertEquals( 1, root.getSections().size() );

		CellModel result = root.getCells().get( 0 );
		CellModel constant = root.getCells().get( 1 );
		SectionModel range = root.getSections().get( 0 );

		assertEquals( "Sheet1!B1", constant.toString() );
		assertEquals( "Sheet1!A1", result.toString() );

		assertEquals( root, range.getSection() );
		assertEquals( 4, range.getCells().size() );
		assertEquals( 0, range.getSections().size() );

		CellModel x1 = range.getCells().get( 0 );
		CellModel i1 = range.getCells().get( 1 );
		CellModel i2 = range.getCells().get( 2 );
		CellModel i3 = range.getCells().get( 3 );

		assertEquals( "SUM( Inputs.getDetails()~>Sheet1!D2 )", result.getExpression().describe() );
		assertEquals( "(SUM( Inputs.getOne(), Inputs.getTwo(), Inputs.getThree() ) * <~Sheet1!B1)",
				x1.getExpression().describe() );

		assertEquals( "Sheet1!D2", x1.toString() );
		assertEquals( "Inputs.getOne()", i1.toString() );
		assertEquals( "Inputs.getTwo()", i2.toString() );
		assertEquals( "Inputs.getThree()", i3.toString() );
	}


	public void testBandWithOutputCells() throws Exception
	{
		SpreadsheetImpl workbook = new SpreadsheetImpl();
		SheetImpl sheet = new SheetImpl( workbook );
		RowImpl r1 = new RowImpl( sheet );
		new CellWithExpression( r1, null );
		WorksheetBuilderWithBands bld = new WorksheetBuilderWithBands( sheet );

		SpreadsheetBinder def = newBinder( workbook );
		bld.defineRange( def.getRoot() );
		bld.details.defineOutputCell( bld.r1c4.getCellIndex(), getInput( "getOne" ) );

		SpreadsheetToModelCompiler compiler = new SpreadsheetToModelCompiler( def.getBinding(),
				SpreadsheetCompiler.DOUBLE );
		ComputationModel model = compiler.compile();
		SectionModel root = model.getRoot();

		assertEquals( 1, root.getCells().size() );
		assertEquals( 1, root.getSections().size() );

		CellModel constant = root.getCells().get( 0 );
		SectionModel range = root.getSections().get( 0 );

		assertEquals( "Sheet1!B1", constant.toString() );

		assertEquals( root, range.getSection() );
		assertEquals( 4, range.getCells().size() );
		assertEquals( 0, range.getSections().size() );

		CellModel o1 = range.getCells().get( 0 );
		CellModel i1 = range.getCells().get( 1 );
		CellModel i2 = range.getCells().get( 2 );
		CellModel i3 = range.getCells().get( 3 );

		assertEquals( "Sheet1!D2", o1.toString() );
		assertEquals( "Inputs.getOne()", i1.toString() );
		assertEquals( "Inputs.getTwo()", i2.toString() );
		assertEquals( "Inputs.getThree()", i3.toString() );

		assertEquals( "(SUM( Inputs.getOne(), Inputs.getTwo(), Inputs.getThree() ) * <~Sheet1!B1)",
				o1.getExpression().describe() );
	}


	public void testBandCellWithSumOverOuterBand() throws Exception
	{
		SpreadsheetImpl workbook = new SpreadsheetImpl();
		SheetImpl sheet = new SheetImpl( workbook );
		RowImpl r1 = new RowImpl( sheet );

		CellInstance i1 = new CellWithConstant( r1, 1.0 );
		CellInstance o1 = new CellWithExpression( r1, sum( refRange( i1, i1 ) ) );

		SpreadsheetBinder def = newBinder( workbook );
		Section rootDef = def.getRoot();
		SpreadsheetBinder.Section inputsDef = rootDef.defineRepeatingSection( rng( i1, i1 ), Orientation.HORIZONTAL,
				getInput( "getDetails" ), Inputs.class, null, null );
		SpreadsheetBinder.Section outputsDef = rootDef.defineRepeatingSection( rng( o1, o1 ), Orientation.HORIZONTAL,
				getInput( "getDetails" ), Inputs.class, getOutput( "getDetails" ), Outputs.class );
		inputsDef.defineInputCell( i1.getCellIndex(), getInput( "getOne" ) );
		outputsDef.defineOutputCell( o1.getCellIndex(), getOutput( "getA" ) );

		SpreadsheetToModelCompiler compiler = new SpreadsheetToModelCompiler( def.getBinding(),
				SpreadsheetCompiler.DOUBLE );
		try {
			ComputationModel model = compiler.compile();
			SectionModel root = model.getRoot();

			assertEquals( 0, root.getCells().size() );
			assertEquals( 2, root.getSections().size() );

			SectionModel outputs = root.getSections().get( 0 );
			SectionModel inputs = root.getSections().get( 1 );

			assertEquals( "Inputs.getDetails()", outputs.toString() );
			assertEquals( "Inputs.getDetails()", inputs.toString() );

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
		CellInstance x1 = new CellWithExpression( r2, sum( refRange( i1, i1 ) ) );
		CellInstance o1 = new CellWithExpression( r1, sum( refRange( x1, x1 ) ) );
		CellInstance o2 = new CellWithExpression( r1, sum( refRange( i1, i1 ) ) );

		SpreadsheetBinder def = newBinder( workbook );
		Section rootDef = def.getRoot();
		SpreadsheetBinder.Section outerDef = rootDef.defineRepeatingSection( rng( i1, x1 ), Orientation.HORIZONTAL,
				getInput( "getDetails" ), Inputs.class, null, null );
		SpreadsheetBinder.Section innerDef = outerDef.defineRepeatingSection( rng( i1, i1 ), Orientation.VERTICAL,
				getInput( "getSubDetails" ), Inputs.class, null, null );
		innerDef.defineInputCell( i1.getCellIndex(), getInput( "getOne" ) );
		rootDef.defineOutputCell( o1.getCellIndex(), getOutput( "getA" ) );
		rootDef.defineOutputCell( o2.getCellIndex(), getOutput( "getB" ) );

		SpreadsheetToModelCompiler compiler = new SpreadsheetToModelCompiler( def.getBinding(),
				SpreadsheetCompiler.DOUBLE );
		ComputationModel model = compiler.compile();
		SectionModel root = model.getRoot();

		assertEquals( 2, root.getCells().size() );
		assertEquals( 1, root.getSections().size() );

		CellModel line = root.getCells().get( 0 );
		CellModel global = root.getCells().get( 1 );
		SectionModel outer = root.getSections().get( 0 );

		assertEquals( "Sheet1!B1", line.toString() );
		assertEquals( "Sheet1!C1", global.toString() );
		assertEquals( "Inputs.getDetails()", outer.toString() );

		assertEquals( 1, outer.getCells().size() );
		assertEquals( 1, outer.getSections().size() );

		CellModel inter = outer.getCells().get( 0 );
		SectionModel inner = outer.getSections().get( 0 );

		assertEquals( "Sheet1!A2", inter.toString() );
		assertEquals( "Inputs.getSubDetails()", inner.toString() );

		assertEquals( 1, inner.getCells().size() );
		assertEquals( 0, inner.getSections().size() );

		CellModel inp = inner.getCells().get( 0 );

		assertEquals( "Inputs.getOne()", inp.toString() );

		assertEquals( "SUM( Inputs.getDetails()~>Inputs.getSubDetails()~>Inputs.getOne() )", global.getExpression()
				.describe() );
		assertEquals( "SUM( Inputs.getDetails()~>Sheet1!A2 )", line.getExpression().describe() );
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
		CellInstance o1 = new CellWithExpression( r1, sum( refRange( i1, i1 ) ) );
		CellInstance o2 = new CellWithExpression( r1, sum( refRange( i1, i2 ) ) );
		CellInstance o3 = new CellWithExpression( r1, sum( refRange( i2, i2 ) ) );
		CellInstance o4 = new CellWithExpression( r1, sum( refRange( i2, i3 ) ) );

		CellRange rng = rng( i1, i3 );

		failForRangeNotCoveringBandExtent( workbook, o1, rng );
		failForRangeNotCoveringBandExtent( workbook, o2, rng );
		failForRangeNotCoveringBandExtent( workbook, o3, rng );
		failForRangeNotCoveringBandExtent( workbook, o4, rng );
	}


	private void failForRangeNotCoveringBandExtent( BaseSpreadsheet workbook, CellInstance output, CellRange rng )
			throws Exception
	{
		SpreadsheetBinder def = newBinder( workbook );
		Section rootDef = def.getRoot();
		rootDef.defineRepeatingSection( rng, Orientation.HORIZONTAL, getInput( "getDetails" ), Inputs.class, null, null );
		rootDef.defineOutputCell( output.getCellIndex(), getOutput( "getResult" ) );

		SpreadsheetToModelCompiler compiler = new SpreadsheetToModelCompiler( def.getBinding(),
				SpreadsheetCompiler.DOUBLE );
		try {
			compiler.compile();
			fail( "Definition for " + output + " accepted even though it does not cover the full range." );
		}
		catch (SpreadsheetException.SectionExtentNotCovered e) {
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
		CellInstance o1 = new CellWithExpression( r11, sum( refRange( i1, i1 ) ) );

		SpreadsheetBinder def = newBinder( workbook );
		Section rootDef = def.getRoot();
		rootDef.defineInputCell( i1.getCellIndex(), getInput( "getOne" ) );
		rootDef.defineOutputCell( o1.getCellIndex(), getOutput( "getResult" ) );

		SpreadsheetToModelCompiler compiler = new SpreadsheetToModelCompiler( def.getBinding(),
				SpreadsheetCompiler.DOUBLE );
		ComputationModel model = compiler.compile();
		SectionModel root = model.getRoot();

		assertEquals( 2, root.getCells().size() );

		CellModel _o1 = root.getCells().get( 0 );
		CellModel _i1 = root.getCells().get( 1 );

		assertEquals( "One!A1", _o1.toString() );
		assertEquals( "Inputs.getOne()", _i1.toString() );
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
		CellInstance o1 = new CellWithExpression( r21, sum( refRange( i1, i1 ) ) );

		SpreadsheetBinder def = newBinder( workbook );
		Section rootDef = def.getRoot();
		rootDef.defineInputCell( i1.getCellIndex(), getInput( "getOne" ) );
		rootDef.defineOutputCell( o1.getCellIndex(), getOutput( "getResult" ) );

		SpreadsheetToModelCompiler compiler = new SpreadsheetToModelCompiler( def.getBinding(),
				SpreadsheetCompiler.DOUBLE );
		ComputationModel model = compiler.compile();
		SectionModel root = model.getRoot();

		assertEquals( 2, root.getCells().size() );

		CellModel _o1 = root.getCells().get( 0 );
		CellModel _i1 = root.getCells().get( 1 );

		assertEquals( "Two!A1", _o1.toString() );
		assertEquals( "Inputs.getOne()", _i1.toString() );
		assertEquals( "SUM( Inputs.getOne() )", _o1.getExpression().describe() );
	}


	private SpreadsheetBinder newBinder( BaseSpreadsheet _workbook )
	{
		return SpreadsheetCompiler.newSpreadsheetBinder( _workbook, Inputs.class, Outputs.class );
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


	private ExpressionNode ix( BaseSpreadsheet _s, int _col, int _row )
	{
		return new ExpressionNodeForCell( new CellIndex( _s, 0, _col, _row ) );
	}


	private ExpressionNode refRange( CellInstance _from, CellInstance _to )
	{
		return new ExpressionNodeForRange( rng( _from, _to ) );
	}


	private CellRange rng( CellInstance _from, CellInstance _to )
	{
		return CellRange.getCellRange( _from.getCellIndex(), _to.getCellIndex() );
	}


}
