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

package org.formulacompiler.compiler.internal.model.optimizer;

import java.math.BigDecimal;
import java.util.Collections;

import org.formulacompiler.compiler.FormulaCompiler;
import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForOperator;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ComputationModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCellModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForParentSectionModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForSubSectionModel;
import org.formulacompiler.compiler.internal.model.SectionModel;
import org.formulacompiler.tests.utils.AbstractStandardInputsOutputsTestCase;
import org.formulacompiler.tests.utils.Inputs;
import org.formulacompiler.tests.utils.Outputs;


public abstract class AbstractOptimizerTest extends AbstractStandardInputsOutputsTestCase
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


	@SuppressWarnings( "unqualified-field-access" )
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
		String actual = FormulaCompiler.BIGDECIMAL_SCALE8.valueToConciseString( value );
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
		Collections.addAll( result.arguments(), _args );
		return result;
	}


	protected ExpressionNode inner( SectionModel _band, ExpressionNode... _args )
	{
		ExpressionNodeForSubSectionModel result = new ExpressionNodeForSubSectionModel( _band );
		Collections.addAll( result.arguments(), _args );
		return result;
	}


	protected ExpressionNode plus( ExpressionNode _a, ExpressionNode _b )
	{
		return new ExpressionNodeForOperator( Operator.PLUS, _a, _b );
	}


	protected ExpressionNode sum( ExpressionNode... _args )
	{
		return new ExpressionNodeForFunction( Function.SUM, _args );
	}


	protected void makeConstCellInput() throws NoSuchMethodException
	{
		this.constCell.makeInput( getInput( "getOne" ) );
	}


}
