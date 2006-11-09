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
package sej.internal.model.optimizer.consteval;

import sej.internal.expressions.ExpressionNode;
import sej.internal.model.CellModel;
import sej.internal.model.ExpressionNodeForCellModel;
import sej.internal.model.util.InterpretedNumericType;

public class EvalCell extends EvalShadow
{

	public EvalCell(ExpressionNode _node, InterpretedNumericType _type)
	{
		super( _node, _type );
	}

	@Override
	protected Object evaluateToConst( Object[] _args )
	{
		final ExpressionNodeForCellModel cellNode = (ExpressionNodeForCellModel) node();
		final CellModel cellModel = cellNode.getCellModel();

		if (null == cellModel) {
			return null;
		}

		if (cellModel.isInput()) {
			return node();
		}

		final Object constantValue = cellModel.getConstantValue();
		if (null != constantValue) {
			if (constantValue instanceof Boolean) {
				boolean bool = ((Boolean) constantValue).booleanValue();
				return type().adjustConstantValue( Double.valueOf( bool ? 1 : 0 ) );
			}
			return constantValue;
		}

		final ExpressionNode expression = cellModel.getExpression();
		if (null != expression) {
			final Object constResult = EvalShadow.evaluate( expression, type() );
			if (constResult instanceof ExpressionNode) {
				
				// Do not need to clone leaf node.
				assert node().arguments().size() == 0;
				return node();
				
			}
			else {
				return constResult;
			}
		}

		return null;
	}

}
