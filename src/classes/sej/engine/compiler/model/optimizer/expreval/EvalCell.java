package sej.engine.compiler.model.optimizer.expreval;

import sej.engine.compiler.model.CellModel;
import sej.engine.compiler.model.ExpressionNodeForCellModel;
import sej.engine.compiler.model.util.InterpretedNumericType;
import sej.expressions.ExpressionNode;

public class EvalCell extends EvalShadow
{

	public EvalCell(ExpressionNode _node, InterpretedNumericType _type)
	{
		super( _node, _type );
	}

	@Override
	protected Object evaluateToConst( Object[] _args )
	{
		final ExpressionNodeForCellModel cellNode = (ExpressionNodeForCellModel) getNode();
		final CellModel cellModel = cellNode.getCellModel();

		if (null == cellModel) {
			return null;
		}

		if (cellModel.isInput()) {
			return getNode();
		}

		final Object constantValue = cellModel.getConstantValue();
		if (null != constantValue) {
			return constantValue;
		}

		final ExpressionNode expression = cellModel.getExpression();
		if (null != expression) {
			final Object constResult = EvalShadow.evaluate( expression, getType() );
			if (constResult instanceof ExpressionNode) {
				return getNode();
			}
			else {
				return constResult;
			}
		}

		return null;
	}

}
