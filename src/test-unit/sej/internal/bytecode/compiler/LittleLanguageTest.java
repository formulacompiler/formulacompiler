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
package sej.internal.bytecode.compiler;

import sej.CallFrame;
import sej.Function;
import sej.NumericType;
import sej.Operator;
import sej.SEJ;
import sej.SaveableEngine;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.expressions.ExpressionNodeForFold;
import sej.internal.expressions.ExpressionNodeForFold1st;
import sej.internal.expressions.ExpressionNodeForFunction;
import sej.internal.expressions.ExpressionNodeForLet;
import sej.internal.expressions.ExpressionNodeForLetVar;
import sej.internal.expressions.ExpressionNodeForOperator;
import sej.internal.model.CellModel;
import sej.internal.model.ComputationModel;
import sej.internal.model.ExpressionNodeForCellModel;
import sej.internal.model.ExpressionNodeForSubSectionModel;
import sej.internal.model.SectionModel;
import sej.internal.model.analysis.TypeAnnotator;
import sej.internal.model.optimizer.ConstantSubExpressionEliminator;
import sej.internal.model.optimizer.IntermediateResultsInliner;
import sej.internal.model.rewriting.ModelRewriter;
import sej.internal.model.rewriting.SubstitutionInliner;
import sej.runtime.ComputationFactory;
import sej.tests.utils.AbstractTestBase;
import sej.tests.utils.Inputs;
import sej.tests.utils.Outputs;
import sej.tests.utils.OutputsWithoutCaching;

public class LittleLanguageTest extends AbstractTestBase
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


	public void testFold() throws Exception
	{
		checkFold( false );
	}

	public void testFold1stOK() throws Exception
	{
		checkFold( true );
	}

	private void checkFold( boolean _1stOK ) throws NoSuchMethodException, Exception
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

		final ExpressionNode init = new ExpressionNodeForConstantValue( _1stOK ? 17 : 0 );
		final ExpressionNode fold = new ExpressionNodeForOperator( Operator.PLUS, new ExpressionNodeForLetVar( "acc" ),
				new ExpressionNodeForLetVar( "xi" ) );
		final ExpressionNode[] args = new ExpressionNode[] { new ExpressionNodeForCellModel( a ),
				new ExpressionNodeForCellModel( b ), new ExpressionNodeForCellModel( c ) };

		r.setExpression( new ExpressionNodeForFold( "acc", init, "xi", fold, _1stOK, args ) );

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

	public void testFold1stOKOverSection() throws Exception
	{
		checkFoldOverSection( true, false );
	}

	public void testFoldOverSectionOnly() throws Exception
	{
		checkFoldOverSection( false, true );
	}

	public void testFold1stOKOverSectionOnly() throws Exception
	{
		checkFoldOverSection( true, true );
	}

	private void checkFoldOverSection( boolean _1stOK, boolean _sectionOnly ) throws NoSuchMethodException, Exception
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
		final ExpressionNode[] args = new ExpressionNode[] {
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

		final ExpressionNode[] args = new ExpressionNode[] { new ExpressionNodeForCellModel( a ),
				new ExpressionNodeForCellModel( b ), new ExpressionNodeForCellModel( c ) };

		r.setExpression( new ExpressionNodeForFunction( Function.VARP, args ) );

		a.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleA" ) ) );
		b.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleB" ) ) );
		c.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleC" ) ) );
		r.makeOutput( new CallFrame( OutputsWithoutCaching.class.getMethod( "getResult" ) ) );

		engineModel.traverse( new ModelRewriter() );
		engineModel.traverse( new ConstantSubExpressionEliminator( SEJ.DOUBLE ) );
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

		final ExpressionNode[] args = new ExpressionNode[] {
				new ExpressionNodeForCellModel( a ),
				new ExpressionNodeForSubSectionModel( subModel, new ExpressionNodeForCellModel( b ),
						new ExpressionNodeForCellModel( c ) ) };

		r.setExpression( new ExpressionNodeForFunction( Function.VARP, args ) );

		subModel.makeInput( new CallFrame( Inputs.class.getMethod( "getDetails" ) ) );

		a.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleA" ) ) );
		b.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleB" ) ) );
		c.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleC" ) ) );
		r.makeOutput( new CallFrame( OutputsWithoutCaching.class.getMethod( "getResult" ) ) );

		engineModel.traverse( new ModelRewriter() );
		engineModel.traverse( new ConstantSubExpressionEliminator( SEJ.DOUBLE ) );
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


	public void testFold1st() throws Exception
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
		final ExpressionNode init = new ExpressionNodeForOperator( Operator.DIV, new ExpressionNodeForLetVar( "x0" ),
				new ExpressionNodeForConstantValue( 2 ) );
		final ExpressionNode fold = new ExpressionNodeForOperator( Operator.PLUS, new ExpressionNodeForLetVar( "acc" ),
				new ExpressionNodeForOperator( Operator.DIV, new ExpressionNodeForLetVar( "xi" ),
						new ExpressionNodeForConstantValue( 2 ) ) );
		final ExpressionNode[] args = new ExpressionNode[] { new ExpressionNodeForCellModel( a ),
				new ExpressionNodeForCellModel( b ), new ExpressionNodeForCellModel( c ) };

		r.setExpression( new ExpressionNodeForFold1st( "x0", init, "acc", "xi", fold, other, args ) );

		a.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleA" ) ) );
		b.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleB" ) ) );
		c.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleC" ) ) );
		r.makeOutput( new CallFrame( OutputsWithoutCaching.class.getMethod( "getResult" ) ) );

		final Inputs i = this.inputs;
		assertDoubleResult( i.getDoubleA() / 2 + i.getDoubleB() / 2 + i.getDoubleC() / 2, engineModel );
	}


	public void testFold1stOverSectionAndCell() throws Exception
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
		final ExpressionNode init = new ExpressionNodeForOperator( Operator.DIV, new ExpressionNodeForLetVar( "x0" ),
				new ExpressionNodeForConstantValue( 2 ) );
		final ExpressionNode fold = new ExpressionNodeForOperator( Operator.PLUS, new ExpressionNodeForLetVar( "acc" ),
				new ExpressionNodeForOperator( Operator.DIV, new ExpressionNodeForLetVar( "xi" ),
						new ExpressionNodeForConstantValue( 2 ) ) );
		final ExpressionNode[] args = new ExpressionNode[] {
				new ExpressionNodeForSubSectionModel( subModel, new ExpressionNodeForCellModel( c ) ),
				new ExpressionNodeForCellModel( a ), new ExpressionNodeForCellModel( b ) };

		r.setExpression( new ExpressionNodeForFold1st( "x0", init, "acc", "xi", fold, other, args ) );

		subModel.makeInput( new CallFrame( Inputs.class.getMethod( "getDetails" ) ) );
		a.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleA" ) ) );
		b.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleB" ) ) );
		c.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleC" ) ) );
		r.makeOutput( new CallFrame( OutputsWithoutCaching.class.getMethod( "getResult" ) ) );

		final Inputs i = this.inputs;
		assertDoubleResult( i.getDoubleA() / 2 + i.getDoubleB() / 2 + i.getDoubleC() / 2 * N_DET, engineModel );
	}


	public void testFold1stOverTwoSections() throws Exception
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
		final ExpressionNode init = new ExpressionNodeForOperator( Operator.DIV, new ExpressionNodeForLetVar( "x0" ),
				new ExpressionNodeForConstantValue( 2 ) );
		final ExpressionNode fold = new ExpressionNodeForOperator( Operator.PLUS, new ExpressionNodeForLetVar( "acc" ),
				new ExpressionNodeForOperator( Operator.DIV, new ExpressionNodeForLetVar( "xi" ),
						new ExpressionNodeForConstantValue( 2 ) ) );
		final ExpressionNode[] args = new ExpressionNode[] {
				new ExpressionNodeForSubSectionModel( subModel1, new ExpressionNodeForCellModel( a ),
						new ExpressionNodeForCellModel( b ) ),
				new ExpressionNodeForSubSectionModel( subModel2, new ExpressionNodeForCellModel( c ) ) };

		r.setExpression( new ExpressionNodeForFold1st( "x0", init, "acc", "xi", fold, other, args ) );

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
		final Outputs outputs = (Outputs) newOutputs( engineModel, SEJ.DOUBLE );

		outputs.reset();
		assertEquals( i.getDoubleA() / 2 * N_DET + i.getDoubleB() / 2 * N_DET + i.getDoubleC() / 2 * N_OTHER, outputs
				.getResult(), 0.0000001 );

		i.getDetails().clear();
		outputs.reset();
		assertEquals( i.getDoubleC() / 2 * N_OTHER, outputs.getResult(), 0.0000001 );

		i.getOtherDetails().clear();
		outputs.reset();
		assertEquals( 17, outputs.getResult(), 0.0000001 );

	}


	public void testFold1stOverNestedSections() throws Exception
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
		final ExpressionNode init = new ExpressionNodeForOperator( Operator.DIV, new ExpressionNodeForLetVar( "x0" ),
				new ExpressionNodeForConstantValue( 2 ) );
		final ExpressionNode fold = new ExpressionNodeForOperator( Operator.PLUS, new ExpressionNodeForLetVar( "acc" ),
				new ExpressionNodeForOperator( Operator.DIV, new ExpressionNodeForLetVar( "xi" ),
						new ExpressionNodeForConstantValue( 2 ) ) );

		final ExpressionNode[] args = new ExpressionNode[] {
				new ExpressionNodeForSubSectionModel( subModel1, new ExpressionNodeForCellModel( a ),
						new ExpressionNodeForCellModel( b ) ),
				new ExpressionNodeForSubSectionModel( subModel2, new ExpressionNodeForSubSectionModel( subsubModel,
						new ExpressionNodeForCellModel( d ) ), new ExpressionNodeForCellModel( c ) ) };

		r.setExpression( new ExpressionNodeForFold1st( "x0", init, "acc", "xi", fold, other, args ) );

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
		final Outputs outputs = (Outputs) newOutputs( engineModel, SEJ.DOUBLE );

		outputs.reset();
		assertEquals( i.getDoubleA()
				/ 2 * N_OTHER + i.getDoubleB() / 2 * N_OTHER + i.getDoubleC() / 2 * N_DET + i.getDoubleA() / 2 * N_DET
				* N_DET, outputs.getResult(), 0.0000001 );

		i.getOtherDetails().clear();
		outputs.reset();
		assertEquals( i.getDoubleC() / 2 * N_DET + i.getDoubleA() / 2 * N_DET * N_DET, outputs.getResult(), 0.0000001 );

		i.getDetails().iterator().next().getDetails().clear();
		outputs.reset();
		assertEquals( i.getDoubleC() / 2 * N_DET + i.getDoubleA() / 2 * N_DET * (N_DET - 1), outputs.getResult(),
				0.0000001 );

		i.getDetails().clear();
		outputs.reset();
		assertEquals( 17, outputs.getResult(), 0.0000001 );

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
	 * final ExpressionNode[] args = new ExpressionNode[] { new ExpressionNodeForCellModel( a ), new
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
		final OutputsWithoutCaching outputs = newOutputs( _engineModel, SEJ.DOUBLE );
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

}
