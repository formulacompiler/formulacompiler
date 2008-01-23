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

import java.math.BigDecimal;
import java.math.MathContext;

import org.formulacompiler.compiler.FormulaCompiler;
import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.compiler.internal.CallFrameImpl;
import org.formulacompiler.compiler.internal.bytecode.ByteCodeEngineCompiler;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ComputationModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCellModel;
import org.formulacompiler.compiler.internal.model.SectionModel;
import org.formulacompiler.compiler.internal.model.analysis.TypeAnnotator;
import org.formulacompiler.compiler.internal.model.optimizer.IntermediateResultsInliner;
import org.formulacompiler.runtime.ComputationFactory;

import junit.framework.TestCase;


@SuppressWarnings("unqualified-field-access")
public class PrecisionTest extends TestCase
{
	private ComputationModel cm;
	private SectionModel sm;
	private CellModel rm;
	private CellModel am;
	private CellModel bm;


	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		cm = new ComputationModel( Inputs.class, Outputs.class );
		sm = cm.getRoot();
		rm = new CellModel( sm, "result" );
		rm.makeOutput( new CallFrameImpl( Outputs.class.getMethod( "result" ) ) );
		am = new CellModel( sm, "a" );
		am.makeInput( new CallFrameImpl( Inputs.class.getMethod( "getA" ) ) );
		bm = new CellModel( sm, "b" );
		bm.makeInput( new CallFrameImpl( Inputs.class.getMethod( "getB" ) ) );
	}


	public void testPassthrough() throws Exception
	{
		rm.setExpression( cell( am ) );
		/*
		 * There is no precision adjustment as the setting only affects effective arithmetic
		 * operations.
		 */
		assertResult( "12345678", 3, "12345678" );
		assertResult( "12345678", 4, "12345678" );
		assertResult( "12345678", 5, "12345678" );
	}

	public void testSum() throws Exception
	{
		rm.setExpression( op( Operator.PLUS, cell( am ), cell( am ) ) );
		assertResult( "24700000", 3, "12345678" );
		assertResult( "24690000", 4, "12345678" );
		assertResult( "24691000", 5, "12345678" );
	}

	public void testDifference() throws Exception
	{
		rm.setExpression( op( Operator.MINUS, cell( am ), cell( bm ) ) );
		assertResult( "75300000", 3, "87654321", "12345678" );
		assertResult( "75310000", 4, "87654321", "12345678" );
		assertResult( "75309000", 5, "87654321", "12345678" );
	}

	public void testProduct() throws Exception
	{
		rm.setExpression( op( Operator.TIMES, cell( am ), cell( bm ) ) );
		assertResult( "123000000", 3, "12345678", "10" );
		assertResult( "123500000", 4, "12345678", "10" );
		assertResult( "123460000", 5, "12345678", "10" );
	}

	public void testDivision() throws Exception
	{
		rm.setExpression( op( Operator.DIV, cell( am ), cell( bm ) ) );
		assertResult( "1230000", 3, "12345678", "10" );
		assertResult( "1235000", 4, "12345678", "10" );
		assertResult( "1234600", 5, "12345678", "10" );
	}


	private ExpressionNodeForCellModel cell( final CellModel _model )
	{
		return new ExpressionNodeForCellModel( _model );
	}


	private void assertResult( String _expected, int _precision, String... _inputs ) throws Exception
	{
		cm.traverse( new IntermediateResultsInliner() );
		cm.traverse( new TypeAnnotator() );
		ByteCodeEngineCompiler.Config config = new ByteCodeEngineCompiler.Config();
		config.model = cm;
		config.numericType = FormulaCompiler.getNumericType( BigDecimal.class, new MathContext( _precision ) );
		ByteCodeEngineCompiler compiler = new ByteCodeEngineCompiler( config );
		SaveableEngine engine = compiler.compile();
		ComputationFactory factory = engine.getComputationFactory();
		Inputs inputs = new Inputs( _inputs );
		Outputs outputs = (Outputs) factory.newComputation( inputs );
		assertEquals( _expected, outputs.result().toPlainString() );
	}


	public static final class Inputs
	{
		private final BigDecimal[] inputs;

		public Inputs(String... _inputs)
		{
			super();
			this.inputs = new BigDecimal[ _inputs.length ];
			for (int i = 0; i < _inputs.length; i++) {
				this.inputs[ i ] = new BigDecimal( _inputs[ i ] );
			}
		}

		public BigDecimal getA()
		{
			return this.inputs[ 0 ];
		}

		public BigDecimal getB()
		{
			return this.inputs[ 1 ];
		}
	}

	public static interface Outputs
	{
		BigDecimal result();
	}

}
