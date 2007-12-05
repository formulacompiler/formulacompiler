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

import static org.formulacompiler.compiler.Operator.*;
import static org.formulacompiler.compiler.internal.expressions.ExpressionBuilder.*;
import static org.formulacompiler.compiler.internal.model.ComputationModelBuilder.*;

import org.formulacompiler.compiler.FormulaCompiler;
import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.compiler.internal.bytecode.ByteCodeEngineCompiler;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldDatabase;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldDefinition;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldList;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldVectors;
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
import org.formulacompiler.runtime.New;
import org.formulacompiler.tests.utils.AbstractStandardInputsOutputsTestCase;
import org.formulacompiler.tests.utils.Inputs;
import org.formulacompiler.tests.utils.Outputs;
import org.formulacompiler.tests.utils.OutputsWithoutReset;


public class LittleLanguageTest extends AbstractStandardInputsOutputsTestCase
{
	private static final int N_DET = 3;
	private final Inputs inputs = new Inputs();

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

		final CellModel a, b, c;
		a = inp( rootModel, "a", "getDoubleA" );
		b = inp( rootModel, "b", "getDoubleB" );
		c = cst( rootModel, "c", 3.0 );

		final ExpressionNode fold = defSum( _mayReduce, _mayReduce );
		final ExpressionNode apply = new ExpressionNodeForFoldList( fold, cells( a, b, c ) );
		result( rootModel, apply );

		final Inputs i = this.inputs;
		assertDoubleResult( i.getDoubleA() + i.getDoubleB() + 3.0, engineModel );
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

	private void checkFoldOverSection( boolean _mayReduce, boolean _sectionOnly ) throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, Outputs.class );
		final SectionModel rootModel = engineModel.getRoot();
		final SectionModel subModel = new SectionModel( rootModel, "Sub", Inputs.class, null );
		subModel.makeInput( getInput( "getDetails" ) );

		final CellModel a, b, c;
		a = inp( rootModel, "a", "getDoubleA" );
		b = inp( rootModel, "b", "getDoubleB" );
		c = inp( subModel, "c", "getDoubleC" );

		final ExpressionNode fold = defSum( _mayReduce && !_sectionOnly, _mayReduce );
		final ExpressionNode[] args;
		if (_sectionOnly) {
			args = New.array( sub( subModel, cell( c ) ) );
		}
		else {
			args = New.array( cell( a ), cell( b ), sub( subModel, cell( c ) ) );
		}

		final ExpressionNode apply = new ExpressionNodeForFoldList( fold, args );
		result( rootModel, apply );

		final Inputs i = this.inputs;
		assertDoubleResult( (_sectionOnly? 0 : i.getDoubleA() + i.getDoubleB()) + i.getDoubleC() * N_DET, engineModel );
	}


	public void testFoldOverNestedSections() throws Exception
	{
		checkFoldOverNestedSections( false );
	}

	public void testFoldOrReduceOverNestedSections() throws Exception
	{
		checkFoldOverNestedSections( true );
	}

	private void checkFoldOverNestedSections( boolean _mayReduce ) throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, Outputs.class );

		final SectionModel rootModel, subModel, subsubModel;
		rootModel = engineModel.getRoot();
		subModel = new SectionModel( rootModel, "Sub", Inputs.class, null );
		subModel.makeInput( getInput( "getDetails" ) );
		subsubModel = new SectionModel( subModel, "SubSub", Inputs.class, null );
		subsubModel.makeInput( getInput( "getDetails" ) );

		final CellModel a, b, c, d;
		a = inp( rootModel, "a", "getDoubleA" );
		b = inp( rootModel, "b", "getDoubleB" );
		c = inp( subModel, "c", "getDoubleC" );
		d = inp( subsubModel, "d", "getDoubleA" );

		final ExpressionNode fold = defSum( false, _mayReduce );
		final ExpressionNode[] args = { cell( a ), cell( b ), sub( subModel, sub( subsubModel, cell( d ) ), cell( c ) ) };
		final ExpressionNode apply = new ExpressionNodeForFoldList( fold, args );
		result( rootModel, apply );

		final Inputs i = this.inputs;
		assertDoubleResult( i.getDoubleA() * (N_DET * N_DET + 1) + i.getDoubleB() + i.getDoubleC() * N_DET, engineModel );
	}


	public void testNestedFold() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, Outputs.class );

		final SectionModel rootModel, subModel, subsubModel;
		rootModel = engineModel.getRoot();
		subModel = new SectionModel( rootModel, "Sub", Inputs.class, null );
		subModel.makeInput( getInput( "getDetails" ) );
		subsubModel = new SectionModel( subModel, "SubSub", Inputs.class, null );
		subsubModel.makeInput( getInput( "getDetails" ) );

		final CellModel a, b;
		final ExpressionNode subapply, apply;
		a = inp( subsubModel, "a", "getDoubleA" );
		subapply = new ExpressionNodeForFoldList( defSum( false, true ), sub( subsubModel, cell( a ) ) );
		b = expr( subModel, "b", subapply );
		apply = new ExpressionNodeForFoldList( defSum( false, true ), sub( subModel, cell( b ) ) );
		result( rootModel, apply );

		final Inputs i = this.inputs;
		assertDoubleResult( i.getDoubleA() * N_DET * N_DET, engineModel );
	}


	public void testReduceOverSectionAndCell() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, Outputs.class );

		final SectionModel rootModel, subModel;
		rootModel = engineModel.getRoot();
		subModel = new SectionModel( rootModel, "Sub", Inputs.class, null );
		subModel.makeInput( getInput( "getDetails" ) );

		final CellModel a, b, c;
		a = inp( rootModel, "a", "getDoubleA" );
		b = inp( rootModel, "b", "getDoubleB" );
		c = inp( subModel, "c", "getDoubleC" );

		final ExpressionNode fold = defSum( true, true );
		final ExpressionNode[] args = { sub( subModel, cell( c ) ), cell( a ), cell( b ) };
		final ExpressionNode apply = new ExpressionNodeForFoldList( fold, args );
		result( rootModel, apply );

		final Inputs i = this.inputs;
		assertDoubleResult( i.getDoubleA() + i.getDoubleB() + i.getDoubleC() * N_DET, engineModel );
	}


	public void testReduceOverTwoSections() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, Outputs.class );

		final SectionModel rootModel, subModel1, subModel2;
		rootModel = engineModel.getRoot();
		subModel1 = new SectionModel( rootModel, "Sub", Inputs.class, null );
		subModel1.makeInput( getInput( "getDetails" ) );
		subModel2 = new SectionModel( rootModel, "Sub", Inputs.class, null );
		subModel2.makeInput( getInput( "getOtherDetails" ) );

		final CellModel a, b, c;
		a = inp( subModel1, "a", "getDoubleA" );
		b = inp( subModel1, "b", "getDoubleB" );
		c = inp( subModel2, "c", "getDoubleC" );

		final ExpressionNode fold = defSum( false, true );
		final ExpressionNode[] args = { sub( subModel1, cell( a ), cell( b ) ), sub( subModel2, cell( c ) ) };
		final ExpressionNode apply = new ExpressionNodeForFoldList( fold, args );
		result( rootModel, apply );

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
		assertEquals( 0, outputs.getResult(), 0.0000001 );
	}


	private ExpressionNode defSum( boolean _bogusInitialValue, boolean _mayReduce )
	{
		final ExpressionNode init, step, fold;
		init = _bogusInitialValue? cst( 17 ) : ZERO;
		step = op( PLUS, var( "acc" ), var( "xi" ) );
		fold = new ExpressionNodeForFoldDefinition( "acc", init, null, "xi", step, true, _mayReduce );
		return fold;
	}


	public void testCovar() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutReset.class );
		final SectionModel rootModel = engineModel.getRoot();

		final CellModel a1, b1, c1, a2, b2, c2;
		a1 = inp( rootModel, "a1", "getDoubleA" );
		b1 = inp( rootModel, "b1", "getDoubleB" );
		c1 = inp( rootModel, "c1", "getDoubleC" );
		a2 = inp( rootModel, "a2", "getDoubleD" );
		b2 = inp( rootModel, "b2", "getDoubleE" );
		c2 = inp( rootModel, "c2", "getDoubleF" );

		final ExpressionNode fold = defCovar();

		final ExpressionNode x, y, apply;
		x = vector( cells( a1, b1, c1 ) );
		y = vector( cells( a2, b2, c2 ) );
		apply = new ExpressionNodeForFoldVectors( fold, x, y );
		result( rootModel, apply );

		final Inputs i = this.inputs;
		final double a, b, c, d, e, f, esx, esy, esxy, en, eCovar;
		a = i.getDoubleA();
		b = i.getDoubleB();
		c = i.getDoubleC();
		d = i.getDoubleD();
		e = i.getDoubleE();
		f = i.getDoubleF();
		esx = a + b + c;
		esy = d + e + f;
		esxy = a * d + b * e + c * f;
		en = 3;
		eCovar = (esxy - esx * esy / en) / en;
		assertDoubleResult( eCovar, engineModel );
	}


	public void testCovarOverSection() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutReset.class );
		final SectionModel rootModel = engineModel.getRoot();
		final SectionModel subModel = new SectionModel( rootModel, "Sub", Inputs.class, null );
		subModel.makeInput( getInput( "getDetails" ) );

		final CellModel a1, b1, c1, a2, b2, c2;
		a1 = inp( rootModel, "a1", "getDoubleA" );
		a2 = inp( rootModel, "a2", "getDoubleB" );
		b1 = inp( subModel, "b1", "getDoubleC" );
		b2 = inp( subModel, "b2", "getDoubleD" );
		c1 = inp( rootModel, "c1", "getDoubleE" );
		c2 = inp( rootModel, "c2", "getDoubleF" );

		final ExpressionNode fold = defCovar();

		final ExpressionNode x, y, apply;
		x = vector( cell( a1 ), sub( subModel, cell( b1 ) ), cell( c1 ) );
		y = vector( cell( a2 ), sub( subModel, cell( b2 ) ), cell( c2 ) );
		apply = new ExpressionNodeForFoldVectors( fold, x, y );
		result( rootModel, apply );

		final Inputs i = this.inputs;
		double a, b, esx, esy, esxy, eCovar;
		int en;

		esx = esy = esxy = en = 0;

		a = i.getDoubleA();
		b = i.getDoubleB();
		esx += a;
		esy += b;
		esxy += a * b;
		en++;
		for (Inputs d : i.getDetails()) {
			a = d.getDoubleC();
			b = d.getDoubleD();
			esx += a;
			esy += b;
			esxy += a * b;
			en++;
		}
		a = i.getDoubleE();
		b = i.getDoubleF();
		esx += a;
		esy += b;
		esxy += a * b;
		en++;

		eCovar = (esxy - esx * esy / en) / en;
		assertDoubleResult( eCovar, engineModel );
	}


	private ExpressionNode defCovar()
	{
		final ExpressionNode sx0, sy0, sxy0, n, sx, sxi, xi, sy, syi, yi, sxy, sxyi, merge, fold;
		sx0 = sy0 = sxy0 = ZERO;
		n = var( "n" );
		sx = var( "sx" );
		sy = var( "sy" );
		sxy = var( "sxy" );
		xi = var( "xi" );
		yi = var( "yi" );
		sxi = op( PLUS, sx, xi );
		syi = op( PLUS, sy, yi );
		sxyi = op( PLUS, sxy, op( TIMES, xi, yi ) );
		merge = op( DIV, op( MINUS, sxy, op( DIV, op( TIMES, sx, sy ), n ) ), n );
		fold = new ExpressionNodeForFoldDefinition( New.array( "sx", "sy", "sxy" ), New.array( sx0, sy0, sxy0 ), null,
				New.array( "xi", "yi" ), New.array( sxi, syi, sxyi ), "n", merge, ZERO, true, false );
		return fold;
	}


	public void testNPV() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutReset.class );
		final SectionModel rootModel = engineModel.getRoot();

		final CellModel a, b, c;
		a = inp( rootModel, "a", "getDoubleA" );
		b = inp( rootModel, "b", "getDoubleB" );
		c = inp( rootModel, "c", "getDoubleC" );

		final ExpressionNode fold, apply, let;
		fold = defNPV();
		apply = new ExpressionNodeForFoldList( fold, cells( a, b, c ) );
		let = let( "rate1", op( PLUS, cst( 0.3 ), ONE ), apply );
		result( rootModel, let );

		final Inputs in = this.inputs;
		final double r1 = 1.3;
		final double want = in.getDoubleA() / r1 + in.getDoubleB() / r1 / r1 + in.getDoubleC() / r1 / r1 / r1;
		assertDoubleResult( want, engineModel );
	}


	public void testNPVOverSection() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutReset.class );
		final SectionModel rootModel = engineModel.getRoot();
		final SectionModel subModel = new SectionModel( rootModel, "Sub", Inputs.class, null );
		subModel.makeInput( getInput( "getDetails" ) );

		final CellModel a, b, c;
		a = inp( rootModel, "a", "getDoubleA" );
		b = inp( subModel, "b", "getDoubleB" );
		c = inp( rootModel, "c", "getDoubleC" );

		final ExpressionNode[] args = New.array( cell( a ), sub( subModel, cell( b ) ), cell( c ) );
		final ExpressionNode fold, apply, let;
		fold = defNPV();
		apply = new ExpressionNodeForFoldList( fold, args );
		let = let( "rate1", op( PLUS, cst( 0.3 ), ONE ), apply );
		result( rootModel, let );

		final Inputs in = this.inputs;
		final double r1 = 1.3;
		int i = 1;
		double v, r;
		r = 0;

		v = in.getDoubleA();
		r += v / Math.pow( r1, i++ );
		for (Inputs d : in.getDetails()) {
			v = d.getDoubleB();
			r += v / Math.pow( r1, i++ );
		}
		v = in.getDoubleC();
		r += v / Math.pow( r1, i++ );

		assertDoubleResult( r, engineModel );
	}


	private ExpressionNode defNPV()
	{
		final ExpressionNode rate1, r, i, vi, init, step, fold;
		rate1 = var( "rate1" );
		r = var( "r" );
		i = var( "i" );
		vi = var( "vi" );
		init = ZERO;
		step = op( PLUS, r, op( DIV, vi, op( EXP, rate1, i ) ) );
		fold = new ExpressionNodeForFoldDefinition( "r", init, "i", "vi", step, false, false );
		return fold;
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

		final ExpressionNode fold, table, filter, col, apply;
		fold = defSum( false, true );
		table = matrix( DATATABLE );
		filter = op( Operator.EQUAL, var( "col0" ), cst( "Apple" ) );
		col = cst( 5 );
		apply = new ExpressionNodeForFoldDatabase( fold, DATATYPES, "col", filter, 4, null, col, table );
		result( rootModel, apply );

		assertDoubleResult( 225.0, engineModel );
	}

	public void testDatabaseFoldWithDynamicCriteria() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutReset.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = inp( rootModel, "a", "getDoubleB" );

		final Object[][] data = { { "Apple", 18.0, 20.0, 14.0, 105.0 }, { "Pear", 12.0, 12.0, 10.0, 96.0 },
				{ "Cherry", 13.0, 14.0, 9.0, 105.00 }, { "Apple", 14.0, 15.0, 10.0, 75.00 },
				{ "Pear", 2.0, 8.0, 8.0, 76.80 }, { "Apple", 8.0, 9.0, 6.0, 45.00 } };

		final ExpressionNode fold, table, filter, col, apply, letCrit;
		fold = defSum( false, true );
		table = matrix( data );
		filter = op( Operator.GREATER, var( "col1" ), var( "crit0" ) );
		col = cst( 5 );
		apply = new ExpressionNodeForFoldDatabase( fold, DATATYPES, "col", filter, 4, null, col, table );
		letCrit = letByName( "crit0", cell( a ), apply ); // by-name emulates closure
		result( rootModel, letCrit );

		assertDoubleResult( 426.0, engineModel );
	}


	public void testSwitch() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutReset.class );
		final SectionModel rootModel = engineModel.getRoot();

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


	protected CellModel inp( SectionModel _section, String _name, String _getter ) throws Exception
	{
		final CellModel result = cst( _section, _name, 0.0 );
		result.makeInput( getInput( _getter ) );
		return result;
	}

	protected CellModel out( SectionModel _section, String _name, ExpressionNode _expr, String _getter )
			throws Exception
	{
		final CellModel result = expr( _section, _name, _expr );
		result.makeOutput( getOutput( _getter ) );
		return result;
	}

	protected CellModel result( SectionModel _section, ExpressionNode _expr ) throws Exception
	{
		return out( _section, "r", _expr, "getResult" );
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

}
