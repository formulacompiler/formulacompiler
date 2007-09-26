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
package org.formulacompiler.compiler.internal.bytecode.compiler;

import static org.formulacompiler.compiler.internal.expressions.ExpressionBuilder.*;
import static org.formulacompiler.compiler.internal.model.ComputationModelBuilder.*;

import org.formulacompiler.compiler.FormulaCompiler;
import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.compiler.internal.bytecode.ByteCodeEngineCompiler;
import org.formulacompiler.compiler.internal.expressions.ArrayDescriptor;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForDatabaseFold;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFold;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldArray;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForMakeArray;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForOperator;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForReduce;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForSwitch;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForSwitchCase;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ComputationModel;
import org.formulacompiler.compiler.internal.model.SectionModel;
import org.formulacompiler.compiler.internal.model.analysis.TypeAnnotator;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;
import org.formulacompiler.compiler.internal.model.optimizer.ConstantSubExpressionEliminator;
import org.formulacompiler.compiler.internal.model.optimizer.IntermediateResultsInliner;
import org.formulacompiler.compiler.internal.model.rewriting.ModelRewriter;
import org.formulacompiler.compiler.internal.model.rewriting.SubstitutionInliner;
import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.tests.utils.AbstractIOTestBase;
import org.formulacompiler.tests.utils.Inputs;
import org.formulacompiler.tests.utils.Outputs;
import org.formulacompiler.tests.utils.OutputsWithoutReset;


public class LittleLanguageTest extends AbstractIOTestBase
{
	private static final int N_DET = 3;
	private final Inputs inputs = new Inputs();
	private SectionModel rootModel;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		for (int i = N_DET; i > 0; i--) {
			final Inputs det = new Inputs();
			for (int j = N_DET; j > 0; j--) {
				det.getDetails().add( new Inputs() );
			}
			this.inputs.getDetails().add( det );
		}
	}


	public void testLet() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutReset.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = cst( rootModel, "a", 1.0 );

		final ExpressionNode val = cell( a );
		final ExpressionNode x = var( "x" );
		final ExpressionNode expr = op( Operator.PLUS, x, x );
		final CellModel r = expr( rootModel, "r", let( "x", val, expr ) );

		a.makeInput( getInput( "getDoubleIncr" ) );
		r.makeOutput( getOutput( "getResult" ) );

		assertDoubleResult( new Inputs().getDoubleIncr() * 2, engineModel );
	}


	public void testNestedLetWithSameName() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutReset.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel r = new CellModel( rootModel, "r" );

		a.setConstantValue( 1.0 );

		final ExpressionNode val = cell( a );
		final ExpressionNode outerX = var( "x" );
		final ExpressionNode innerX = var( "x" );
		final ExpressionNode innerLet = let( "x", outerX, innerX );
		final ExpressionNode outerLet = let( "x", val, innerLet );
		r.setExpression( outerLet );

		a.makeInput( getInput( "getDoubleIncr" ) );
		r.makeOutput( getOutput( "getResult" ) );

		final Inputs inp = new Inputs();
		try {
			assertDoubleResult( inp.getDoubleIncr() * 2 + inp.getDoubleIncr(), engineModel );
			fail( "Exception expected" );
		}
		catch (IllegalArgumentException e) {
			assertEquals( "Cannot compile a letvar named x when already compiling one of that name - rewriter bug?", e
					.getMessage() );
		}
	}


	public void testLetUsedInIfAndElse() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutReset.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel b = new CellModel( rootModel, "b" );
		final CellModel r = new CellModel( rootModel, "r" );

		a.setConstantValue( 1.0 );
		b.setConstantValue( 2.0 );

		final ExpressionNode ca = cell( a );
		final ExpressionNode cb = cell( b );
		final ExpressionNode x = var( "x" );
		final ExpressionNode one = cst( 1 );
		final ExpressionNode plus = op( Operator.PLUS, x, x );
		final ExpressionNode cond = op( Operator.EQUAL, cb, one );
		final ExpressionNode ifElse = fun( Function.IF, cond, plus, x );
		final ExpressionNode test = op( Operator.PLUS, ifElse, x );
		r.setExpression( let( "x", ca, test ) );

		a.makeInput( getInput( "getDoubleIncr" ) );
		b.makeInput( getInput( "getOne" ) );
		r.makeOutput( getOutput( "getResult" ) );

		// Test true branch
		final Inputs in = new Inputs();
		assertDoubleResult( in.getDoubleIncr() * 3, engineModel );

		// Test false branch
		this.inputs.setDoubleIncr( 1 );
		this.inputs.setOne( 2 );
		final Inputs in2 = new Inputs();
		assertDoubleResult( in2.getDoubleIncr() * 2, engineModel );
	}

	public void testLetUsedInIfOnly() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutReset.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel b = new CellModel( rootModel, "b" );
		final CellModel r = new CellModel( rootModel, "r" );

		a.setConstantValue( 1.0 );
		b.setConstantValue( 2.0 );

		final ExpressionNode ca = cell( a );
		final ExpressionNode cb = cell( b );
		final ExpressionNode x = var( "x" );
		final ExpressionNode one = cst( 1 );
		final ExpressionNode plus = op( Operator.PLUS, x, x );
		final ExpressionNode cond = op( Operator.EQUAL, cb, one );
		final ExpressionNode ifElse = fun( Function.IF, cond, plus, one );
		final ExpressionNode test = op( Operator.PLUS, ifElse, x );
		r.setExpression( let( "x", ca, test ) );

		a.makeInput( getInput( "getDoubleIncr" ) );
		b.makeInput( getInput( "getOne" ) );
		r.makeOutput( getOutput( "getResult" ) );

		/*
		 * Since we are initializing x only in one branch of the IF, the final access to x outside of
		 * the IF has to retrieve incr() again. So we are seeing two calls to incr() here.
		 */

		// Test true branch
		final Inputs in = new Inputs();
		assertDoubleResult( in.getDoubleIncr() * 2 + in.getDoubleIncr(), engineModel );

		// Test false branch
		this.inputs.setDoubleIncr( 1 );
		this.inputs.setOne( 2 );
		final Inputs in2 = new Inputs();
		assertDoubleResult( 1.0 + in2.getDoubleIncr(), engineModel );
	}

	public void testLetUsedInElseOnly() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutReset.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel b = new CellModel( rootModel, "b" );
		final CellModel r = new CellModel( rootModel, "r" );

		a.setConstantValue( 1.0 );
		b.setConstantValue( 2.0 );

		final ExpressionNode ca = cell( a );
		final ExpressionNode cb = cell( b );
		final ExpressionNode x = var( "x" );
		final ExpressionNode one = cst( 1 );
		final ExpressionNode plus = op( Operator.PLUS, x, x );
		final ExpressionNode cond = op( Operator.EQUAL, cb, one );
		final ExpressionNode ifElse = fun( Function.IF, cond, one, plus );
		final ExpressionNode test = op( Operator.PLUS, ifElse, x );
		r.setExpression( let( "x", ca, test ) );

		a.makeInput( getInput( "getDoubleIncr" ) );
		b.makeInput( getInput( "getOne" ) );
		r.makeOutput( getOutput( "getResult" ) );

		/*
		 * Since we are initializing x only in one branch of the IF, the final access to x outside of
		 * the IF has to retrieve incr() again. So we are seeing two calls to incr() here.
		 */

		// Test true branch
		final Inputs in = new Inputs();
		assertDoubleResult( 1.0 + in.getDoubleIncr(), engineModel );

		// Test false branch
		this.inputs.setDoubleIncr( 1 );
		this.inputs.setOne( 2 );
		final Inputs in2 = new Inputs();
		assertDoubleResult( in2.getDoubleIncr() * 2 + in2.getDoubleIncr(), engineModel );
	}

	// LATER NestedIFs


	public void testFold() throws Exception
	{
		checkFold( false );
	}

	public void testFoldOrReduce() throws Exception
	{
		checkFold( true );
	}

	private void checkFold( boolean _mayReduce ) throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutReset.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel b = new CellModel( rootModel, "b" );
		final CellModel c = new CellModel( rootModel, "c" );
		final CellModel r = new CellModel( rootModel, "r" );

		a.setConstantValue( 1.0 );
		b.setConstantValue( 2.0 );
		c.setConstantValue( 3.0 );

		final ExpressionNode init = cst( _mayReduce? 17 : 0 );
		final ExpressionNode fold = op( Operator.PLUS, var( "acc" ), var( "xi" ) );
		final ExpressionNode[] args = { cell( a ), cell( b ), cell( c ) };

		r.setExpression( new ExpressionNodeForFold( "acc", init, "xi", fold, _mayReduce, args ) );

		a.makeInput( getInput( "getDoubleA" ) );
		b.makeInput( getInput( "getDoubleB" ) );
		c.makeInput( getInput( "getDoubleC" ) );
		r.makeOutput( getOutput( "getResult" ) );

		final Inputs i = this.inputs;
		assertDoubleResult( i.getDoubleA() + i.getDoubleB() + i.getDoubleC(), engineModel );
	}


	public void testFoldOverSection() throws Exception
	{
		checkFoldOverSection( false, false );
	}

	public void testFoldOrReduceOverSection() throws Exception
	{
		checkFoldOverSection( true, false );
	}

	public void testFoldOverSectionOnly() throws Exception
	{
		checkFoldOverSection( false, true );
	}

	public void testFoldOrReduceOverSectionOnly() throws Exception
	{
		checkFoldOverSection( true, true );
	}

	private void checkFoldOverSection( boolean _1stOK, boolean _sectionOnly ) throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, Outputs.class );
		final SectionModel rootModel = engineModel.getRoot();
		final SectionModel subModel = new SectionModel( rootModel, "Sub", Inputs.class, null );
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel b = new CellModel( rootModel, "b" );
		final CellModel c = new CellModel( subModel, "c" );
		final CellModel r = new CellModel( rootModel, "r" );

		a.setConstantValue( 1.0 );
		b.setConstantValue( 2.0 );
		c.setConstantValue( 3.0 );

		final ExpressionNode init = cst( _1stOK && !_sectionOnly? 17 : 0 );
		final ExpressionNode fold = op( Operator.PLUS, var( "acc" ), var( "xi" ) );
		ExpressionNode[] args;
		if (_sectionOnly) {
			args = new ExpressionNode[] { sub( subModel, cell( c ) ) };
		}
		else {
			args = new ExpressionNode[] { cell( a ), cell( b ), sub( subModel, cell( c ) ) };
		}

		r.setExpression( new ExpressionNodeForFold( "acc", init, "xi", fold, _1stOK, args ) );

		subModel.makeInput( getInput( "getDetails" ) );
		a.makeInput( getInput( "getDoubleA" ) );
		b.makeInput( getInput( "getDoubleB" ) );
		c.makeInput( getInput( "getDoubleC" ) );
		r.makeOutput( getOutput( "getResult" ) );

		final Inputs i = this.inputs;
		assertDoubleResult( (_sectionOnly? 0 : i.getDoubleA() + i.getDoubleB()) + i.getDoubleC() * N_DET, engineModel );
	}


	public void testFoldOverNestedSection() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, Outputs.class );
		final SectionModel rootModel = engineModel.getRoot();
		final SectionModel subModel = new SectionModel( rootModel, "Sub", Inputs.class, null );
		final SectionModel subsubModel = new SectionModel( subModel, "SubSub", Inputs.class, null );
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel b = new CellModel( rootModel, "b" );
		final CellModel c = new CellModel( subModel, "c" );
		final CellModel d = new CellModel( subsubModel, "d" );
		final CellModel r = new CellModel( rootModel, "r" );

		a.setConstantValue( 1.0 );
		b.setConstantValue( 2.0 );
		c.setConstantValue( 3.0 );
		d.setConstantValue( 4.0 );

		final ExpressionNode init = cst( 0 );
		final ExpressionNode fold = op( Operator.PLUS, var( "acc" ), var( "xi" ) );
		final ExpressionNode[] args = { cell( a ), cell( b ), sub( subModel, sub( subsubModel, cell( d ) ), cell( c ) ) };

		r.setExpression( new ExpressionNodeForFold( "acc", init, "xi", fold, false, args ) );

		subModel.makeInput( getInput( "getDetails" ) );
		subsubModel.makeInput( getInput( "getDetails" ) );
		a.makeInput( getInput( "getDoubleA" ) );
		b.makeInput( getInput( "getDoubleB" ) );
		c.makeInput( getInput( "getDoubleC" ) );
		d.makeInput( getInput( "getDoubleA" ) );
		r.makeOutput( getOutput( "getResult" ) );

		final Inputs i = this.inputs;
		assertDoubleResult( i.getDoubleA() * (N_DET * N_DET + 1) + i.getDoubleB() + i.getDoubleC() * N_DET, engineModel );
	}


	public void testRewritingOfVARP() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutReset.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel b = new CellModel( rootModel, "b" );
		final CellModel c = new CellModel( rootModel, "c" );
		final CellModel r = new CellModel( rootModel, "r" );

		a.setConstantValue( 1.0 );
		b.setConstantValue( 2.0 );
		c.setConstantValue( 3.0 );

		final ExpressionNode[] args = { cell( a ), cell( b ), cell( c ) };

		r.setExpression( fun( Function.VARP, args ) );

		a.makeInput( getInput( "getDoubleA" ) );
		b.makeInput( getInput( "getDoubleB" ) );
		c.makeInput( getInput( "getDoubleC" ) );
		r.makeOutput( getOutput( "getResult" ) );

		engineModel.traverse( new ModelRewriter( InterpretedNumericType.typeFor( FormulaCompiler.DOUBLE ) ) );
		engineModel.traverse( new ConstantSubExpressionEliminator( FormulaCompiler.DOUBLE ) );
		engineModel.traverse( new SubstitutionInliner() );

		final Inputs i = this.inputs;
		final double expected = varp( i.getDoubleA(), i.getDoubleB(), i.getDoubleC() );
		assertDoubleResult( expected, engineModel );
	}

	public void testRewritingOfVARPOverSection() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, Outputs.class );
		final SectionModel rootModel = engineModel.getRoot();
		final SectionModel subModel = new SectionModel( rootModel, "Sub", Inputs.class, null );
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel b = new CellModel( subModel, "b" );
		final CellModel c = new CellModel( subModel, "c" );
		final CellModel r = new CellModel( rootModel, "r" );

		a.setConstantValue( 1.0 );
		b.setConstantValue( 2.0 );
		c.setConstantValue( 3.0 );

		final ExpressionNode[] args = { cell( a ), sub( subModel, cell( b ), cell( c ) ) };

		r.setExpression( fun( Function.VARP, args ) );

		subModel.makeInput( getInput( "getDetails" ) );

		a.makeInput( getInput( "getDoubleA" ) );
		b.makeInput( getInput( "getDoubleB" ) );
		c.makeInput( getInput( "getDoubleC" ) );
		r.makeOutput( getOutput( "getResult" ) );

		engineModel.traverse( new ModelRewriter( InterpretedNumericType.typeFor( FormulaCompiler.DOUBLE ) ) );
		engineModel.traverse( new ConstantSubExpressionEliminator( FormulaCompiler.DOUBLE ) );
		engineModel.traverse( new SubstitutionInliner() );

		final Inputs i = this.inputs;
		final double[] vals = new double[ N_DET * 2 + 1 ];
		vals[ 0 ] = i.getDoubleA();
		for (int j = 0; j < N_DET; j++) {
			vals[ j * 2 + 1 ] = i.getDoubleB();
			vals[ j * 2 + 2 ] = i.getDoubleC();
		}
		final double expected = varp( vals );
		assertDoubleResult( expected, engineModel );
	}

	private double varp( double... _xs )
	{
		final double n = _xs.length;
		final double s = sum( _xs );
		final double m = s / n;
		double sse = 0;
		for (double xi : _xs) {
			final double ei = xi - m;
			sse += ei * ei;
		}
		return sse / n;
	}

	private double sum( double... _xs )
	{
		double sum = 0;
		for (double xi : _xs) {
			sum += xi;
		}
		return sum;
	}


	public void testReduce() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutReset.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel b = new CellModel( rootModel, "b" );
		final CellModel c = new CellModel( rootModel, "c" );
		final CellModel r = new CellModel( rootModel, "r" );

		a.setConstantValue( 1.0 );
		b.setConstantValue( 2.0 );
		c.setConstantValue( 3.0 );

		final ExpressionNode other = cst( 17 );
		final ExpressionNode fold = op( Operator.PLUS, var( "acc" ), var( "xi" ) );

		r.setExpression( new ExpressionNodeForReduce( "acc", "xi", fold, other, cells( a, b, c ) ) );

		a.makeInput( getInput( "getDoubleA" ) );
		b.makeInput( getInput( "getDoubleB" ) );
		c.makeInput( getInput( "getDoubleC" ) );
		r.makeOutput( getOutput( "getResult" ) );

		final Inputs i = this.inputs;
		assertDoubleResult( i.getDoubleA() + i.getDoubleB() + i.getDoubleC(), engineModel );
	}


	public void testReduceOverSectionAndCell() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, Outputs.class );
		final SectionModel rootModel = engineModel.getRoot();
		final SectionModel subModel = new SectionModel( rootModel, "Sub", Inputs.class, null );
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel b = new CellModel( rootModel, "b" );
		final CellModel c = new CellModel( subModel, "c" );
		final CellModel r = new CellModel( rootModel, "r" );

		a.setConstantValue( 1.0 );
		b.setConstantValue( 2.0 );
		c.setConstantValue( 3.0 );

		final ExpressionNode other = cst( 17 );
		final ExpressionNode fold = op( Operator.PLUS, var( "acc" ), var( "xi" ) );
		final ExpressionNode[] args = { sub( subModel, cell( c ) ), cell( a ), cell( b ) };

		r.setExpression( new ExpressionNodeForReduce( "acc", "xi", fold, other, args ) );

		subModel.makeInput( getInput( "getDetails" ) );
		a.makeInput( getInput( "getDoubleA" ) );
		b.makeInput( getInput( "getDoubleB" ) );
		c.makeInput( getInput( "getDoubleC" ) );
		r.makeOutput( getOutput( "getResult" ) );

		final Inputs i = this.inputs;
		assertDoubleResult( i.getDoubleA() + i.getDoubleB() + i.getDoubleC() * N_DET, engineModel );
	}


	public void testReduceOverTwoSections() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, Outputs.class );
		final SectionModel rootModel = engineModel.getRoot();
		final SectionModel subModel1 = new SectionModel( rootModel, "Sub1", Inputs.class, null );
		final SectionModel subModel2 = new SectionModel( rootModel, "Sub2", Inputs.class, null );
		final CellModel a = new CellModel( subModel1, "a" );
		final CellModel b = new CellModel( subModel1, "b" );
		final CellModel c = new CellModel( subModel2, "c" );
		final CellModel r = new CellModel( rootModel, "r" );

		a.setConstantValue( 1.0 );
		b.setConstantValue( 2.0 );
		c.setConstantValue( 3.0 );

		final ExpressionNode other = cst( 17 );
		final ExpressionNode fold = op( Operator.PLUS, var( "acc" ), var( "xi" ) );
		final ExpressionNode[] args = { sub( subModel1, cell( a ), cell( b ) ), sub( subModel2, cell( c ) ) };

		r.setExpression( new ExpressionNodeForReduce( "acc", "xi", fold, other, args ) );

		subModel1.makeInput( getInput( "getDetails" ) );
		subModel2.makeInput( getInput( "getOtherDetails" ) );
		a.makeInput( getInput( "getDoubleA" ) );
		b.makeInput( getInput( "getDoubleB" ) );
		c.makeInput( getInput( "getDoubleC" ) );
		r.makeOutput( getOutput( "getResult" ) );

		final Inputs i = this.inputs;

		final int N_OTHER = 4;
		for (int j = 0; j < N_OTHER; j++) {
			i.getOtherDetails().add( new Inputs() );
		}
		final Outputs outputs = (Outputs) newOutputs( engineModel, FormulaCompiler.DOUBLE );

		outputs.reset();
		assertEquals( i.getDoubleA() * N_DET + i.getDoubleB() * N_DET + i.getDoubleC() * N_OTHER, outputs.getResult(),
				0.0000001 );

		i.getDetails().clear();
		outputs.reset();
		assertEquals( i.getDoubleC() * N_OTHER, outputs.getResult(), 0.0000001 );

		i.getOtherDetails().clear();
		outputs.reset();
		assertEquals( 17, outputs.getResult(), 0.0000001 );

	}


	public void testReduceOverNestedSections() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, Outputs.class );
		final SectionModel rootModel = engineModel.getRoot();
		final SectionModel subModel1 = new SectionModel( rootModel, "Sub1", Inputs.class, null );
		final SectionModel subModel2 = new SectionModel( rootModel, "Sub2", Inputs.class, null );
		final SectionModel subsubModel = new SectionModel( subModel2, "SubSub", Inputs.class, null );
		final CellModel a = new CellModel( subModel1, "a" );
		final CellModel b = new CellModel( subModel1, "b" );
		final CellModel c = new CellModel( subModel2, "c" );
		final CellModel d = new CellModel( subsubModel, "d" );
		final CellModel r = new CellModel( rootModel, "r" );

		a.setConstantValue( 1.0 );
		b.setConstantValue( 2.0 );
		c.setConstantValue( 3.0 );
		d.setConstantValue( 4.0 );

		final ExpressionNode other = cst( 17 );
		final ExpressionNode fold = op( Operator.PLUS, var( "acc" ), var( "xi" ) );

		final ExpressionNode[] args = { sub( subModel1, cell( a ), cell( b ) ),
				sub( subModel2, sub( subsubModel, cell( d ) ), cell( c ) ) };

		r.setExpression( new ExpressionNodeForReduce( "acc", "xi", fold, other, args ) );

		subModel1.makeInput( getInput( "getOtherDetails" ) );
		subModel2.makeInput( getInput( "getDetails" ) );
		subsubModel.makeInput( getInput( "getDetails" ) );
		a.makeInput( getInput( "getDoubleA" ) );
		b.makeInput( getInput( "getDoubleB" ) );
		c.makeInput( getInput( "getDoubleC" ) );
		d.makeInput( getInput( "getDoubleA" ) );
		r.makeOutput( getOutput( "getResult" ) );

		final Inputs i = this.inputs;
		final int N_OTHER = 4;
		for (int j = 0; j < N_OTHER; j++) {
			i.getOtherDetails().add( new Inputs() );
		}
		final Outputs outputs = (Outputs) newOutputs( engineModel, FormulaCompiler.DOUBLE );

		outputs.reset();
		assertEquals( i.getDoubleA()
				* N_OTHER + i.getDoubleB() * N_OTHER + i.getDoubleC() * N_DET + i.getDoubleA() * N_DET * N_DET, outputs
				.getResult(), 0.0000001 );

		i.getOtherDetails().clear();
		outputs.reset();
		assertEquals( i.getDoubleC() * N_DET + i.getDoubleA() * N_DET * N_DET, outputs.getResult(), 0.0000001 );

		i.getDetails().iterator().next().getDetails().clear();
		outputs.reset();
		assertEquals( i.getDoubleC() * N_DET + i.getDoubleA() * N_DET * (N_DET - 1), outputs.getResult(), 0.0000001 );

		i.getDetails().clear();
		outputs.reset();
		assertEquals( 17, outputs.getResult(), 0.0000001 );

	}


	public void testFoldArray() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutReset.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel b = new CellModel( rootModel, "b" );
		final CellModel c = new CellModel( rootModel, "c" );
		final CellModel r = new CellModel( rootModel, "r" );

		a.setConstantValue( 1.0 );
		b.setConstantValue( 2.0 );
		c.setConstantValue( 3.0 );

		final ExpressionNode init = cst( 0 );
		final ExpressionNode fold = op( Operator.PLUS, var( "acc" ), op( Operator.TIMES, var( "xi" ), var( "i" ) ) );
		final ExpressionNode[] args = { cell( a ), cell( b ), cell( c ) };
		final ExpressionNode arr = new ExpressionNodeForMakeArray( new ExpressionNodeForArrayReference(
				new ArrayDescriptor( 1, 1, 3 ), args ) );

		r.setExpression( new ExpressionNodeForFoldArray( "acc", init, "xi", "i", fold, arr ) );

		a.makeInput( getInput( "getDoubleA" ) );
		b.makeInput( getInput( "getDoubleB" ) );
		c.makeInput( getInput( "getDoubleC" ) );
		r.makeOutput( getOutput( "getResult" ) );

		final Inputs i = this.inputs;
		assertDoubleResult( i.getDoubleA() + i.getDoubleB() * 2 + i.getDoubleC() * 3, engineModel );
	}


	private static final Object[][] DATATABLE = { { "Apple", 18.0, 20.0, 14.0, 105.0 },
			{ "Pear", 12.0, 12.0, 10.0, 96.0 }, { "Cherry", 13.0, 14.0, 9.0, 105.00 },
			{ "Apple", 14.0, 15.0, 10.0, 75.00 }, { "Pear", 9.0, 8.0, 8.0, 76.80 }, { "Apple", 8.0, 9.0, 6.0, 45.00 } };

	private static final DataType[] DATATYPES = { DataType.STRING, DataType.NUMERIC, DataType.NUMERIC, DataType.NUMERIC,
			DataType.NUMERIC };

	public void testDatabaseFold() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutReset.class );
		final SectionModel rootModel = engineModel.getRoot();
		this.rootModel = rootModel;

		final ExpressionNodeForArrayReference table = makeRange( DATATABLE );

		final ExpressionNode filter = op( Operator.EQUAL, var( "col0" ), cst( "Apple" ) );

		final ExpressionNode col = cst( 5 );

		final CellModel r = new CellModel( rootModel, "r" );
		final ExpressionNodeForConstantValue init = cst( 0.0 );
		final ExpressionNodeForOperator fold = op( Operator.PLUS, var( "r" ), var( "xi" ) );
		r.setExpression( new ExpressionNodeForDatabaseFold( table.arrayDescriptor(), "col", filter, "r", init, "xi",
				fold, 4, null, col, DATATYPES, false, false, table ) );

		r.makeOutput( getOutput( "getResult" ) );

		assertDoubleResult( 225.0, engineModel );
	}


	public void testDatabaseFoldWithDynamicCriteria() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutReset.class );
		final SectionModel rootModel = engineModel.getRoot();
		this.rootModel = rootModel;

		final Object[][] data = { { "Apple", 18.0, 20.0, 14.0, 105.0 }, { "Pear", 12.0, 12.0, 10.0, 96.0 },
				{ "Cherry", 13.0, 14.0, 9.0, 105.00 }, { "Apple", 14.0, 15.0, 10.0, 75.00 },
				{ "Pear", 2.0, 8.0, 8.0, 76.80 }, { "Apple", 8.0, 9.0, 6.0, 45.00 } };
		final ExpressionNodeForArrayReference table = makeRange( data );

		final CellModel a = new CellModel( rootModel, "a" );
		a.makeInput( getInput( "getDoubleB" ) );

		// Note: -var is a let that is evaluated every time it is accessed. Used here as a closure
		// param for the helper method.
		final ExpressionNode filter = op( Operator.GREATER, var( "col1" ), var( "crit0" ) );

		final ExpressionNode col = cst( 5 );

		final CellModel r = new CellModel( rootModel, "r" );
		final ExpressionNodeForConstantValue init = cst( 0.0 );
		final ExpressionNodeForOperator fold = op( Operator.PLUS, var( "r" ), var( "xi" ) );
		final ExpressionNode letCrit = letByName( "crit0", cell( a ), new ExpressionNodeForDatabaseFold( table
				.arrayDescriptor(), "col", filter, "r", init, "xi", fold, 4, null, col, DATATYPES, false, false, table ) );
		r.setExpression( letCrit );

		r.makeOutput( getOutput( "getResult" ) );

		assertDoubleResult( 426.0, engineModel );
	}


	public void testDatabaseFoldZeroWhenEmpty() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutReset.class );
		final SectionModel rootModel = engineModel.getRoot();
		this.rootModel = rootModel;

		final ExpressionNodeForArrayReference table = makeRange( DATATABLE );

		final ExpressionNode filter = op( Operator.EQUAL, var( "col0" ), cst( "NotHere" ) );

		final ExpressionNode col = cst( 5 );

		final CellModel r = new CellModel( rootModel, "r" );
		final ExpressionNodeForConstantValue init = cst( 1.0 );
		final ExpressionNodeForOperator fold = op( Operator.TIMES, var( "r" ), var( "xi" ) );
		r.setExpression( new ExpressionNodeForDatabaseFold( table.arrayDescriptor(), "col", filter, "r", init, "xi",
				fold, 4, null, col, DATATYPES, false, true, table ) );

		r.makeOutput( getOutput( "getResult" ) );

		assertDoubleResult( 0.0, engineModel );
	}


	public void testDatabaseReduce() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutReset.class );
		final SectionModel rootModel = engineModel.getRoot();
		this.rootModel = rootModel;

		final Object[][] data = { { "Apple", 18.0, 20.0, 14.0, -105.0 }, { "Pear", 12.0, 12.0, 10.0, -96.0 },
				{ "Cherry", 13.0, 14.0, 9.0, -105.00 }, { "Apple", 14.0, 15.0, 10.0, -75.00 },
				{ "Pear", 9.0, 8.0, 8.0, -76.80 }, { "Apple", 8.0, 9.0, 6.0, -45.00 } };
		final ExpressionNodeForArrayReference table = makeRange( data );

		final ExpressionNode filter = op( Operator.EQUAL, var( "col0" ), cst( "Apple" ) );

		final ExpressionNode col = cst( 5 );

		final CellModel r = new CellModel( rootModel, "r" );
		final ExpressionNodeForConstantValue init = cst( 0.0 );
		final ExpressionNodeForOperator fold = op( Operator.INTERNAL_MAX, var( "r" ), var( "xi" ) );
		r.setExpression( new ExpressionNodeForDatabaseFold( table.arrayDescriptor(), "col", filter, "r", init, "xi",
				fold, 4, null, col, DATATYPES, true, true, table ) );

		r.makeOutput( getOutput( "getResult" ) );

		assertDoubleResult( -45.0, engineModel );
	}


	public void testSwitch() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutReset.class );
		final SectionModel rootModel = engineModel.getRoot();
		this.rootModel = rootModel;

		final CellModel v = new CellModel( rootModel, "i" );
		final CellModel r = new CellModel( rootModel, "r" );

		v.setConstantValue( 2 );
		v.makeInput( getInput( "getTwo" ) );

		final ExpressionNode valueNode = cell( v );
		final ExpressionNode defaultNode = cst( -10 );
		final ExpressionNodeForSwitchCase caseNode1 = new ExpressionNodeForSwitchCase( cst( 10 ), 1 );
		final ExpressionNodeForSwitchCase caseNode2 = new ExpressionNodeForSwitchCase( cst( 20 ), 2 );
		final ExpressionNodeForSwitchCase caseNode3 = new ExpressionNodeForSwitchCase( cst( 30 ), 3 );
		final ExpressionNode switchNode = new ExpressionNodeForSwitch( valueNode, defaultNode, caseNode1, caseNode2,
				caseNode3 );
		final ExpressionNode plusNode = op( Operator.PLUS, switchNode, cst( 100 ) );

		r.setExpression( plusNode );
		r.makeOutput( getOutput( "getResult" ) );

		assertDoubleResult( 120.0, engineModel );
	}


	private void assertDoubleResult( final double _expectedResult, final ComputationModel _engineModel )
			throws Exception
	{
		final OutputsWithoutReset outputs = newOutputs( _engineModel, FormulaCompiler.DOUBLE );
		final double d = outputs.getResult();
		assertEquals( _expectedResult, d, 0.000001 );
	}

	private OutputsWithoutReset newOutputs( ComputationModel _engineModel, NumericType _numericType ) throws Exception
	{
		_engineModel.traverse( new IntermediateResultsInliner() );
		_engineModel.traverse( new TypeAnnotator() );
		final ByteCodeEngineCompiler.Config config = new ByteCodeEngineCompiler.Config();
		config.model = _engineModel;
		config.numericType = _numericType;
		final ByteCodeEngineCompiler compiler = new ByteCodeEngineCompiler( config );
		final SaveableEngine engine = compiler.compile();

		checkEngine( engine );

		final ComputationFactory factory = engine.getComputationFactory();
		return (OutputsWithoutReset) factory.newComputation( this.inputs );
	}

	private final ExpressionNodeForArrayReference makeRange( Object[][] _rows )
	{
		final int nrows = _rows.length;
		final int ncols = _rows[ 0 ].length;

		final ExpressionNodeForArrayReference result = new ExpressionNodeForArrayReference( new ArrayDescriptor( 1,
				nrows, ncols ) );

		for (Object[] row : _rows) {
			for (Object cell : row) {
				result.addArgument( makeNode( cell ) );
			}
		}

		return result;
	}

	private ExpressionNode makeNode( Object _value )
	{
		if (_value instanceof ExpressionNode) {
			return (ExpressionNode) _value;
		}
		if (_value instanceof String) {
			String str = (String) _value;
			if (str.startsWith( "#" )) {
				return cell( new CellModel( this.rootModel, str.substring( 1 ) ) );
			}
		}
		return cst( _value );
	}

}
