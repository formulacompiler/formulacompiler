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

import sej.NumericType;
import sej.internal.expressions.ExpressionNode;
import sej.internal.model.AbstractComputationModelVisitor;
import sej.internal.model.CellModel;
import sej.internal.model.SectionModel;
import sej.internal.model.optimizer.consteval.EvalShadow;
import sej.internal.model.util.InterpretedNumericType;


final public class ConstantSubExpressionEliminator extends AbstractComputationModelVisitor
{
	private final InterpretedNumericType numericType;


	public ConstantSubExpressionEliminator(InterpretedNumericType _type)
	{
		super();
		this.numericType = _type;
	}


	public ConstantSubExpressionEliminator(NumericType _type)
	{
		this( InterpretedNumericType.typeFor( _type ) );
	}


	public InterpretedNumericType getNumericType()
	{
		return this.numericType;
	}


	@Override
	public boolean visit( CellModel _cell )
	{
		ExpressionNode sourceExpr = _cell.getExpression();
		if (null == sourceExpr) {
			// _cell.setConstantValue( )
		}
		else {
			Object optimizedResult = eliminateConstantsFrom( sourceExpr, _cell.getSection() );
			if (optimizedResult instanceof ExpressionNode) {
				ExpressionNode optimizedExpr = (ExpressionNode) optimizedResult;
				_cell.setExpression( optimizedExpr );
			}
			else {
				_cell.setExpression( null );
				_cell.setConstantValue( optimizedResult );
			}
		}
		return true;
	}


	private Object eliminateConstantsFrom( ExpressionNode _expr, SectionModel _section )
	{
		if (null == _expr) return null;
		return EvalShadow.evaluate( _expr, getNumericType() );
	}


}
