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
package sej.internal.model.optimizer;

import java.math.BigDecimal;

import sej.NumericType;
import sej.api.expressions.Aggregator;
import sej.api.expressions.ExpressionNode;
import sej.api.expressions.ExpressionNodeForAggregator;
import sej.api.expressions.ExpressionNodeForConstantValue;
import sej.api.expressions.ExpressionNodeForOperator;
import sej.api.expressions.Operator;
import sej.internal.model.CellModel;
import sej.internal.model.ComputationModel;
import sej.internal.model.ExpressionNodeForCellModel;
import sej.internal.model.ExpressionNodeForParentSectionModel;
import sej.internal.model.ExpressionNodeForSubSectionModel;
import sej.internal.model.SectionModel;
import sej.tests.utils.AbstractTestBase;
import sej.tests.utils.Inputs;
import sej.tests.utils.Outputs;

public abstract class AbstractOptimizerTest extends AbstractTestBase
{
	protected ComputationModel model;
	protected SectionModel root;
	protected CellModel constCell;
	protected CellModel constExpr;
	protected CellModel constSum;
	protected CellModel constRefSum;
	protected SectionModel band;
	protected CellModel bandExpr;
	protected CellModel bandOther;
	protected CellModel bandRefSum;


	@SuppressWarnings("unqualified-field-access")
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		model = new ComputationModel( Inputs.class, Outputs.class );
		root = model.getRoot();

		constCell = new CellModel( root, "Const" );
		constCell.setConstantValue( 1.0 );

		constExpr = new CellModel( root, "ConstExpr" );
		constExpr.setExpression( cst( 2.0 ) );

		constSum = new CellModel( root, "ConstSum" );
		constSum.setExpression( plus( cst( 1.0 ), cst( 2.0 ) ) );

		constRefSum = new CellModel( root, "ConstRefSum" );
		constRefSum.setExpression( plus( ref( constCell ), ref( constExpr ) ) );

		band = new SectionModel( root, "Band", Inputs.class, Outputs.class );

		bandExpr = new CellModel( band, "BandExpr" );
		bandExpr.setExpression( cst( 10.0 ) );

		bandOther = new CellModel( band, "BandOther" );
		bandOther.setExpression( cst( 11.0 ) );

		bandRefSum = new CellModel( band, "BandRefSum" );
		bandRefSum.setExpression( plus( plus( cst( 12.0 ), sum( ref( bandExpr ), ref( bandOther ) ) ), outer( band,
				ref( constRefSum ) ) ) );
	}


	protected static void assertConst( double _expected, CellModel _constCell )
	{
		Double actual = (Double) _constCell.getConstantValue();
		assertNotNull( Double.toString( _expected ), actual );
		assertEquals( _expected, actual, 0.01 );
	}


	protected void assertBigConst( String _expected, CellModel _constCell )
	{
		BigDecimal value = (BigDecimal) _constCell.getConstantValue();
		assertNotNull( _expected, value );
		String actual = NumericType.BIGDECIMAL8.valueToConciseString( value );
		assertEquals( _expected, actual );
	}


	protected static void assertExpr( String _expected, CellModel _exprCell )
	{
		assertNotNull( _expected, _exprCell.getExpression() );
		String actual = _exprCell.getExpression().describe();
		assertEquals( _expected, actual );
	}


	protected void assertRefs( int _expected, CellModel _cell )
	{
		assertEquals( _expected, _cell.getReferenceCount() );
	}


	protected ExpressionNode cst( double _d )
	{
		return new ExpressionNodeForConstantValue( _d );
	}


	protected ExpressionNode ref( CellModel _cell )
	{
		return new ExpressionNodeForCellModel( _cell );
	}


	protected ExpressionNode outer( SectionModel _band, ExpressionNode... _args )
	{
		ExpressionNodeForParentSectionModel result = new ExpressionNodeForParentSectionModel( _band.getSection() );
		for (ExpressionNode arg : _args)
			result.getArguments().add( arg );
		return result;
	}


	protected ExpressionNode inner( SectionModel _band, ExpressionNode... _args )
	{
		ExpressionNodeForSubSectionModel result = new ExpressionNodeForSubSectionModel( _band );
		for (ExpressionNode arg : _args)
			result.getArguments().add( arg );
		return result;
	}


	protected ExpressionNode plus( ExpressionNode _a, ExpressionNode _b )
	{
		return new ExpressionNodeForOperator( Operator.PLUS, _a, _b );
	}


	protected ExpressionNode sum( ExpressionNode... _args )
	{
		return new ExpressionNodeForAggregator( Aggregator.SUM, _args );
	}


	protected void makeConstCellInput() throws NoSuchMethodException
	{
		this.constCell.makeInput( getInput( "getOne" ) );
	}


}
