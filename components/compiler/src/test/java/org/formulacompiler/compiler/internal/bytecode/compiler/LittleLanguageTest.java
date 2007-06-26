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

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.FormulaCompiler;
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
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForLet;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForLetVar;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForMakeArray;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForOperator;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForReduce;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ComputationModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCellModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForSubSectionModel;
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
import org.formulacompiler.tests.utils.OutputsWithoutCaching;


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
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutCaching.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel r = new CellModel( rootModel, "r" );

		a.setConstantValue( 1.0 );

		final ExpressionNodeForCellModel val = new ExpressionNodeForCellModel( a );
		final ExpressionNodeForLetVar x = new ExpressionNodeForLetVar( "x" );
		final ExpressionNodeForOperator expr = new ExpressionNodeForOperator( Operator.PLUS, x, x );
		r.setExpression( new ExpressionNodeForLet( "x", val, expr ) );

		a.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleIncr" ) ) );
		r.makeOutput( new CallFrame( OutputsWithoutCaching.class.getMethod( "getResult" ) ) );

		assertDoubleResult( new Inputs().getDoubleIncr() * 2, engineModel );
	}


	public void testLetUsedInIfAndElse() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutCaching.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel b = new CellModel( rootModel, "b" );
		final CellModel r = new CellModel( rootModel, "r" );

		a.setConstantValue( 1.0 );
		b.setConstantValue( 2.0 );

		final ExpressionNode ca = new ExpressionNodeForCellModel( a );
		final ExpressionNode cb = new ExpressionNodeForCellModel( b );
		final ExpressionNode x = new ExpressionNodeForLetVar( "x" );
		final ExpressionNode one = new ExpressionNodeForConstantValue( 1 );
		final ExpressionNode plus = new ExpressionNodeForOperator( Operator.PLUS, x, x );
		final ExpressionNode cond = new ExpressionNodeForOperator( Operator.EQUAL, cb, one );
		final ExpressionNode ifElse = new ExpressionNodeForFunction( Function.IF, cond, plus, x );
		final ExpressionNode test = new ExpressionNodeForOperator( Operator.PLUS, ifElse, x );
		r.setExpression( new ExpressionNodeForLet( "x", ca, test ) );

		a.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleIncr" ) ) );
		b.makeInput( new CallFrame( Inputs.class.getMethod( "getOne" ) ) );
		r.makeOutput( new CallFrame( OutputsWithoutCaching.class.getMethod( "getResult" ) ) );

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
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutCaching.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel b = new CellModel( rootModel, "b" );
		final CellModel r = new CellModel( rootModel, "r" );

		a.setConstantValue( 1.0 );
		b.setConstantValue( 2.0 );

		final ExpressionNode ca = new ExpressionNodeForCellModel( a );
		final ExpressionNode cb = new ExpressionNodeForCellModel( b );
		final ExpressionNode x = new ExpressionNodeForLetVar( "x" );
		final ExpressionNode one = new ExpressionNodeForConstantValue( 1 );
		final ExpressionNode plus = new ExpressionNodeForOperator( Operator.PLUS, x, x );
		final ExpressionNode cond = new ExpressionNodeForOperator( Operator.EQUAL, cb, one );
		final ExpressionNode ifElse = new ExpressionNodeForFunction( Function.IF, cond, plus, one );
		final ExpressionNode test = new ExpressionNodeForOperator( Operator.PLUS, ifElse, x );
		r.setExpression( new ExpressionNodeForLet( "x", ca, test ) );

		a.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleIncr" ) ) );
		b.makeInput( new CallFrame( Inputs.class.getMethod( "getOne" ) ) );
		r.makeOutput( new CallFrame( OutputsWithoutCaching.class.getMethod( "getResult" ) ) );

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
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutCaching.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel b = new CellModel( rootModel, "b" );
		final CellModel r = new CellModel( rootModel, "r" );

		a.setConstantValue( 1.0 );
		b.setConstantValue( 2.0 );

		final ExpressionNode ca = new ExpressionNodeForCellModel( a );
		final ExpressionNode cb = new ExpressionNodeForCellModel( b );
		final ExpressionNode x = new ExpressionNodeForLetVar( "x" );
		final ExpressionNode one = new ExpressionNodeForConstantValue( 1 );
		final ExpressionNode plus = new ExpressionNodeForOperator( Operator.PLUS, x, x );
		final ExpressionNode cond = new ExpressionNodeForOperator( Operator.EQUAL, cb, one );
		final ExpressionNode ifElse = new ExpressionNodeForFunction( Function.IF, cond, one, plus );
		final ExpressionNode test = new ExpressionNodeForOperator( Operator.PLUS, ifElse, x );
		r.setExpression( new ExpressionNodeForLet( "x", ca, test ) );

		a.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleIncr" ) ) );
		b.makeInput( new CallFrame( Inputs.class.getMethod( "getOne" ) ) );
		r.makeOutput( new CallFrame( OutputsWithoutCaching.class.getMethod( "getResult" ) ) );

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
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutCaching.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel b = new CellModel( rootModel, "b" );
		final CellModel c = new CellModel( rootModel, "c" );
		final CellModel r = new CellModel( rootModel, "r" );

		a.setConstantValue( 1.0 );
		b.setConstantValue( 2.0 );
		c.setConstantValue( 3.0 );

		final ExpressionNode init = new ExpressionNodeForConstantValue( _mayReduce ? 17 : 0 );
		final ExpressionNode fold = new ExpressionNodeForOperator( Operator.PLUS, new ExpressionNodeForLetVar( "acc" ),
				new ExpressionNodeForLetVar( "xi" ) );
		final ExpressionNode[] args = { new ExpressionNodeForCellModel( a ), new ExpressionNodeForCellModel( b ),
				new ExpressionNodeForCellModel( c ) };

		r.setExpression( new ExpressionNodeForFold( "acc", init, "xi", fold, _mayReduce, args ) );

		a.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleA" ) ) );
		b.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleB" ) ) );
		c.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleC" ) ) );
		r.makeOutput( new CallFrame( OutputsWithoutCaching.class.getMethod( "getResult" ) ) );

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

		final ExpressionNode init = new ExpressionNodeForConstantValue( _1stOK && !_sectionOnly ? 17 : 0 );
		final ExpressionNode fold = new ExpressionNodeForOperator( Operator.PLUS, new ExpressionNodeForLetVar( "acc" ),
				new ExpressionNodeForLetVar( "xi" ) );
		ExpressionNode[] args;
		if (_sectionOnly) {
			args = new ExpressionNode[] { new ExpressionNodeForSubSectionModel( subModel, new ExpressionNodeForCellModel(
					c ) ) };
		}
		else {
			args = new ExpressionNode[] { new ExpressionNodeForCellModel( a ), new ExpressionNodeForCellModel( b ),
					new ExpressionNodeForSubSectionModel( subModel, new ExpressionNodeForCellModel( c ) ) };
		}

		r.setExpression( new ExpressionNodeForFold( "acc", init, "xi", fold, _1stOK, args ) );

		subModel.makeInput( new CallFrame( Inputs.class.getMethod( "getDetails" ) ) );
		a.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleA" ) ) );
		b.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleB" ) ) );
		c.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleC" ) ) );
		r.makeOutput( new CallFrame( OutputsWithoutCaching.class.getMethod( "getResult" ) ) );

		final Inputs i = this.inputs;
		assertDoubleResult( (_sectionOnly ? 0 : i.getDoubleA() + i.getDoubleB()) + i.getDoubleC() * N_DET, engineModel );
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

		final ExpressionNode init = new ExpressionNodeForConstantValue( 0 );
		final ExpressionNode fold = new ExpressionNodeForOperator( Operator.PLUS, new ExpressionNodeForLetVar( "acc" ),
				new ExpressionNodeForLetVar( "xi" ) );
		final ExpressionNode[] args = {
				new ExpressionNodeForCellModel( a ),
				new ExpressionNodeForCellModel( b ),
				new ExpressionNodeForSubSectionModel( subModel, new ExpressionNodeForSubSectionModel( subsubModel,
						new ExpressionNodeForCellModel( d ) ), new ExpressionNodeForCellModel( c ) ) };

		r.setExpression( new ExpressionNodeForFold( "acc", init, "xi", fold, false, args ) );

		subModel.makeInput( new CallFrame( Inputs.class.getMethod( "getDetails" ) ) );
		subsubModel.makeInput( new CallFrame( Inputs.class.getMethod( "getDetails" ) ) );
		a.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleA" ) ) );
		b.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleB" ) ) );
		c.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleC" ) ) );
		d.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleA" ) ) );
		r.makeOutput( new CallFrame( OutputsWithoutCaching.class.getMethod( "getResult" ) ) );

		final Inputs i = this.inputs;
		assertDoubleResult( i.getDoubleA() * (N_DET * N_DET + 1) + i.getDoubleB() + i.getDoubleC() * N_DET, engineModel );
	}


	public void testRewritingOfVARP() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutCaching.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel b = new CellModel( rootModel, "b" );
		final CellModel c = new CellModel( rootModel, "c" );
		final CellModel r = new CellModel( rootModel, "r" );

		a.setConstantValue( 1.0 );
		b.setConstantValue( 2.0 );
		c.setConstantValue( 3.0 );

		final ExpressionNode[] args = { new ExpressionNodeForCellModel( a ), new ExpressionNodeForCellModel( b ),
				new ExpressionNodeForCellModel( c ) };

		r.setExpression( new ExpressionNodeForFunction( Function.VARP, args ) );

		a.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleA" ) ) );
		b.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleB" ) ) );
		c.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleC" ) ) );
		r.makeOutput( new CallFrame( OutputsWithoutCaching.class.getMethod( "getResult" ) ) );

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

		final ExpressionNode[] args = {
				new ExpressionNodeForCellModel( a ),
				new ExpressionNodeForSubSectionModel( subModel, new ExpressionNodeForCellModel( b ),
						new ExpressionNodeForCellModel( c ) ) };

		r.setExpression( new ExpressionNodeForFunction( Function.VARP, args ) );

		subModel.makeInput( new CallFrame( Inputs.class.getMethod( "getDetails" ) ) );

		a.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleA" ) ) );
		b.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleB" ) ) );
		c.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleC" ) ) );
		r.makeOutput( new CallFrame( OutputsWithoutCaching.class.getMethod( "getResult" ) ) );

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
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutCaching.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel b = new CellModel( rootModel, "b" );
		final CellModel c = new CellModel( rootModel, "c" );
		final CellModel r = new CellModel( rootModel, "r" );

		a.setConstantValue( 1.0 );
		b.setConstantValue( 2.0 );
		c.setConstantValue( 3.0 );

		final ExpressionNode other = new ExpressionNodeForConstantValue( 17 );
		final ExpressionNode fold = new ExpressionNodeForOperator( Operator.PLUS, new ExpressionNodeForLetVar( "acc" ),
				new ExpressionNodeForLetVar( "xi" ) );
		final ExpressionNode[] args = { new ExpressionNodeForCellModel( a ), new ExpressionNodeForCellModel( b ),
				new ExpressionNodeForCellModel( c ) };

		r.setExpression( new ExpressionNodeForReduce( "acc", "xi", fold, other, args ) );

		a.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleA" ) ) );
		b.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleB" ) ) );
		c.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleC" ) ) );
		r.makeOutput( new CallFrame( OutputsWithoutCaching.class.getMethod( "getResult" ) ) );

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

		final ExpressionNode other = new ExpressionNodeForConstantValue( 17 );
		final ExpressionNode fold = new ExpressionNodeForOperator( Operator.PLUS, new ExpressionNodeForLetVar( "acc" ),
				new ExpressionNodeForLetVar( "xi" ) );
		final ExpressionNode[] args = {
				new ExpressionNodeForSubSectionModel( subModel, new ExpressionNodeForCellModel( c ) ),
				new ExpressionNodeForCellModel( a ), new ExpressionNodeForCellModel( b ) };

		r.setExpression( new ExpressionNodeForReduce( "acc", "xi", fold, other, args ) );

		subModel.makeInput( new CallFrame( Inputs.class.getMethod( "getDetails" ) ) );
		a.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleA" ) ) );
		b.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleB" ) ) );
		c.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleC" ) ) );
		r.makeOutput( new CallFrame( OutputsWithoutCaching.class.getMethod( "getResult" ) ) );

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

		final ExpressionNode other = new ExpressionNodeForConstantValue( 17 );
		final ExpressionNode fold = new ExpressionNodeForOperator( Operator.PLUS, new ExpressionNodeForLetVar( "acc" ),
				new ExpressionNodeForLetVar( "xi" ) );
		final ExpressionNode[] args = {
				new ExpressionNodeForSubSectionModel( subModel1, new ExpressionNodeForCellModel( a ),
						new ExpressionNodeForCellModel( b ) ),
				new ExpressionNodeForSubSectionModel( subModel2, new ExpressionNodeForCellModel( c ) ) };

		r.setExpression( new ExpressionNodeForReduce( "acc", "xi", fold, other, args ) );

		subModel1.makeInput( new CallFrame( Inputs.class.getMethod( "getDetails" ) ) );
		subModel2.makeInput( new CallFrame( Inputs.class.getMethod( "getOtherDetails" ) ) );
		a.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleA" ) ) );
		b.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleB" ) ) );
		c.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleC" ) ) );
		r.makeOutput( new CallFrame( OutputsWithoutCaching.class.getMethod( "getResult" ) ) );

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

		final ExpressionNode other = new ExpressionNodeForConstantValue( 17 );
		final ExpressionNode fold = new ExpressionNodeForOperator( Operator.PLUS, new ExpressionNodeForLetVar( "acc" ),
				new ExpressionNodeForLetVar( "xi" ) );

		final ExpressionNode[] args = {
				new ExpressionNodeForSubSectionModel( subModel1, new ExpressionNodeForCellModel( a ),
						new ExpressionNodeForCellModel( b ) ),
				new ExpressionNodeForSubSectionModel( subModel2, new ExpressionNodeForSubSectionModel( subsubModel,
						new ExpressionNodeForCellModel( d ) ), new ExpressionNodeForCellModel( c ) ) };

		r.setExpression( new ExpressionNodeForReduce( "acc", "xi", fold, other, args ) );

		subModel1.makeInput( new CallFrame( Inputs.class.getMethod( "getOtherDetails" ) ) );
		subModel2.makeInput( new CallFrame( Inputs.class.getMethod( "getDetails" ) ) );
		subsubModel.makeInput( new CallFrame( Inputs.class.getMethod( "getDetails" ) ) );
		a.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleA" ) ) );
		b.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleB" ) ) );
		c.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleC" ) ) );
		d.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleA" ) ) );
		r.makeOutput( new CallFrame( OutputsWithoutCaching.class.getMethod( "getResult" ) ) );

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
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutCaching.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel b = new CellModel( rootModel, "b" );
		final CellModel c = new CellModel( rootModel, "c" );
		final CellModel r = new CellModel( rootModel, "r" );

		a.setConstantValue( 1.0 );
		b.setConstantValue( 2.0 );
		c.setConstantValue( 3.0 );

		final ExpressionNode init = new ExpressionNodeForConstantValue( 0 );
		final ExpressionNode fold = new ExpressionNodeForOperator( Operator.PLUS, new ExpressionNodeForLetVar( "acc" ),
				new ExpressionNodeForOperator( Operator.TIMES, new ExpressionNodeForLetVar( "xi" ),
						new ExpressionNodeForLetVar( "i" ) ) );
		final ExpressionNode[] args = { new ExpressionNodeForCellModel( a ), new ExpressionNodeForCellModel( b ),
				new ExpressionNodeForCellModel( c ) };
		final ExpressionNode arr = new ExpressionNodeForMakeArray( new ExpressionNodeForArrayReference(
				new ArrayDescriptor( 1, 1, 3 ), args ) );

		r.setExpression( new ExpressionNodeForFoldArray( "acc", init, "xi", "i", fold, arr ) );

		a.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleA" ) ) );
		b.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleB" ) ) );
		c.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleC" ) ) );
		r.makeOutput( new CallFrame( OutputsWithoutCaching.class.getMethod( "getResult" ) ) );

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
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutCaching.class );
		final SectionModel rootModel = engineModel.getRoot();
		this.rootModel = rootModel;

		final ExpressionNodeForArrayReference table = makeRange( DATATABLE );

		final ExpressionNode filter = new ExpressionNodeForOperator( Operator.EQUAL,
				new ExpressionNodeForLetVar( "col0" ), new ExpressionNodeForConstantValue( "Apple" ) );

		final ExpressionNode col = new ExpressionNodeForConstantValue( 5 );

		final CellModel r = new CellModel( rootModel, "r" );
		final ExpressionNodeForConstantValue init = new ExpressionNodeForConstantValue( 0.0 );
		final ExpressionNodeForOperator fold = new ExpressionNodeForOperator( Operator.PLUS, new ExpressionNodeForLetVar(
				"r" ), new ExpressionNodeForLetVar( "xi" ) );
		r.setExpression( new ExpressionNodeForDatabaseFold( table.arrayDescriptor(), "col", filter, "r", init, "xi",
				fold, 4, null, col, DATATYPES, false, false, table ) );

		r.makeOutput( new CallFrame( OutputsWithoutCaching.class.getMethod( "getResult" ) ) );

		assertDoubleResult( 225.0, engineModel );
	}


	public void testDatabaseFoldWithDynamicCriteria() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutCaching.class );
		final SectionModel rootModel = engineModel.getRoot();
		this.rootModel = rootModel;

		final Object[][] data = { { "Apple", 18.0, 20.0, 14.0, 105.0 }, { "Pear", 12.0, 12.0, 10.0, 96.0 },
				{ "Cherry", 13.0, 14.0, 9.0, 105.00 }, { "Apple", 14.0, 15.0, 10.0, 75.00 },
				{ "Pear", 2.0, 8.0, 8.0, 76.80 }, { "Apple", 8.0, 9.0, 6.0, 45.00 } };
		final ExpressionNodeForArrayReference table = makeRange( data );

		final CellModel a = new CellModel( rootModel, "a" );
		a.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleB" ) ) );

		// Note: -var is a let that is evaluated every time it is accessed. Used here as a closure
		// param for the helper method.
		final ExpressionNode filter = new ExpressionNodeForOperator( Operator.GREATER, new ExpressionNodeForLetVar(
				"col1" ), new ExpressionNodeForLetVar( "-crit0" ) );

		final ExpressionNode col = new ExpressionNodeForConstantValue( 5 );

		final CellModel r = new CellModel( rootModel, "r" );
		final ExpressionNodeForConstantValue init = new ExpressionNodeForConstantValue( 0.0 );
		final ExpressionNodeForOperator fold = new ExpressionNodeForOperator( Operator.PLUS, new ExpressionNodeForLetVar(
				"r" ), new ExpressionNodeForLetVar( "xi" ) );
		r.setExpression( new ExpressionNodeForLet( "-crit0", new ExpressionNodeForCellModel( a ),
				new ExpressionNodeForDatabaseFold( table.arrayDescriptor(), "col", filter, "r", init, "xi", fold, 4, null,
						col, DATATYPES, false, false, table ) ) );

		r.makeOutput( new CallFrame( OutputsWithoutCaching.class.getMethod( "getResult" ) ) );

		assertDoubleResult( 426.0, engineModel );
	}


	public void testDatabaseFoldZeroWhenEmpty() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutCaching.class );
		final SectionModel rootModel = engineModel.getRoot();
		this.rootModel = rootModel;

		final ExpressionNodeForArrayReference table = makeRange( DATATABLE );

		final ExpressionNode filter = new ExpressionNodeForOperator( Operator.EQUAL,
				new ExpressionNodeForLetVar( "col0" ), new ExpressionNodeForConstantValue( "NotHere" ) );

		final ExpressionNode col = new ExpressionNodeForConstantValue( 5 );

		final CellModel r = new CellModel( rootModel, "r" );
		final ExpressionNodeForConstantValue init = new ExpressionNodeForConstantValue( 1.0 );
		final ExpressionNodeForOperator fold = new ExpressionNodeForOperator( Operator.TIMES,
				new ExpressionNodeForLetVar( "r" ), new ExpressionNodeForLetVar( "xi" ) );
		r.setExpression( new ExpressionNodeForDatabaseFold( table.arrayDescriptor(), "col", filter, "r", init, "xi",
				fold, 4, null, col, DATATYPES, false, true, table ) );

		r.makeOutput( new CallFrame( OutputsWithoutCaching.class.getMethod( "getResult" ) ) );

		assertDoubleResult( 0.0, engineModel );
	}


	public void testDatabaseReduce() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutCaching.class );
		final SectionModel rootModel = engineModel.getRoot();
		this.rootModel = rootModel;

		final Object[][] data = { { "Apple", 18.0, 20.0, 14.0, -105.0 }, { "Pear", 12.0, 12.0, 10.0, -96.0 },
				{ "Cherry", 13.0, 14.0, 9.0, -105.00 }, { "Apple", 14.0, 15.0, 10.0, -75.00 },
				{ "Pear", 9.0, 8.0, 8.0, -76.80 }, { "Apple", 8.0, 9.0, 6.0, -45.00 } };
		final ExpressionNodeForArrayReference table = makeRange( data );

		final ExpressionNode filter = new ExpressionNodeForOperator( Operator.EQUAL,
				new ExpressionNodeForLetVar( "col0" ), new ExpressionNodeForConstantValue( "Apple" ) );

		final ExpressionNode col = new ExpressionNodeForConstantValue( 5 );

		final CellModel r = new CellModel( rootModel, "r" );
		final ExpressionNodeForConstantValue init = new ExpressionNodeForConstantValue( 0.0 );
		final ExpressionNodeForOperator fold = new ExpressionNodeForOperator( Operator.INTERNAL_MAX,
				new ExpressionNodeForLetVar( "r" ), new ExpressionNodeForLetVar( "xi" ) );
		r.setExpression( new ExpressionNodeForDatabaseFold( table.arrayDescriptor(), "col", filter, "r", init, "xi",
				fold, 4, null, col, DATATYPES, true, true, table ) );

		r.makeOutput( new CallFrame( OutputsWithoutCaching.class.getMethod( "getResult" ) ) );

		assertDoubleResult( -45.0, engineModel );
	}


	// LATER ITER-support
	/*
	 * public void testBlock() throws Exception { final ComputationModel engineModel = new
	 * ComputationModel( Inputs.class, OutputsWithoutCaching.class ); final SectionModel rootModel =
	 * engineModel.getRoot(); final CellModel a = new CellModel( rootModel, "a" ); final CellModel b =
	 * new CellModel( rootModel, "b" ); final CellModel c = new CellModel( rootModel, "c" ); final
	 * CellModel r = new CellModel( rootModel, "r" );
	 * 
	 * a.setConstantValue( 1.0 ); b.setConstantValue( 2.0 ); c.setConstantValue( 3.0 );
	 * 
	 * final ExpressionNode[] args = { new ExpressionNodeForCellModel( a ), new
	 * ExpressionNodeForCellModel( b ), new ExpressionNodeForCellModel( c ) }; final ExpressionNode
	 * s0 = new ExpressionNodeForSet( "s", new ExpressionNodeForConstantValue( 0 ) ); final
	 * ExpressionNode ss0 = new ExpressionNodeForSet( "ss", new ExpressionNodeForConstantValue( 0 ) );
	 * final ExpressionNode splus = new ExpressionNodeForSet( "s", new ExpressionNodeForOperator(
	 * Operator.PLUS, new ExpressionNodeForLetVar( "s" ), new ExpressionNodeForLetVar( "xi" ) ) );
	 * final ExpressionNode ssplus = new ExpressionNodeForSet( "ss", new ExpressionNodeForOperator(
	 * Operator.PLUS, new ExpressionNodeForLetVar( "ss" ), new ExpressionNodeForOperator(
	 * Operator.TIMES, new ExpressionNodeForLetVar( "xi" ), new ExpressionNodeForLetVar( "xi" ) ) ) );
	 * final ExpressionNode letin = new ExpressionNodeForSeq( splus, ssplus ); final ExpressionNode
	 * letall = new ExpressionNodeForLetAll( "xi", letin, args );
	 * 
	 * r.setExpression( new ExpressionNodeForBlock( new String[] { "s", "ss" }, s0, ss0, letall, done ) );
	 * 
	 * a.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleA" ) ) ); b.makeInput( new
	 * CallFrame( Inputs.class.getMethod( "getDoubleB" ) ) ); c.makeInput( new CallFrame(
	 * Inputs.class.getMethod( "getDoubleC" ) ) ); r.makeOutput( new CallFrame(
	 * OutputsWithoutCaching.class.getMethod( "getResult" ) ) );
	 * 
	 * final Inputs i = new Inputs();
	 * 
	 * assertDoubleResult( i.getDoubleA() + i.getDoubleB() + i.getDoubleC(), engineModel ); }
	 */


	private void assertDoubleResult( final double _expectedResult, final ComputationModel _engineModel )
			throws Exception
	{
		final OutputsWithoutCaching outputs = newOutputs( _engineModel, FormulaCompiler.DOUBLE );
		final double d = outputs.getResult();
		assertEquals( _expectedResult, d, 0.000001 );
	}

	private OutputsWithoutCaching newOutputs( ComputationModel _engineModel, NumericType _numericType ) throws Exception
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
		return (OutputsWithoutCaching) factory.newComputation( this.inputs );
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
				return new ExpressionNodeForCellModel( new CellModel( this.rootModel, str.substring( 1 ) ) );
			}
		}
		return new ExpressionNodeForConstantValue( _value );
	}

}
