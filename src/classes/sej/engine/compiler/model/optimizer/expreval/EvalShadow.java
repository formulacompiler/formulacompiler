package sej.engine.compiler.model.optimizer.expreval;

import java.util.List;

import sej.engine.compiler.model.util.InterpretedNumericType;
import sej.expressions.ExpressionNode;
import sej.expressions.ExpressionNodeForConstantValue;
import sej.expressions.ExpressionNodeShadow;

public abstract class EvalShadow extends ExpressionNodeShadow
{
	
	public static Object evaluate( ExpressionNode _expr, InterpretedNumericType _type )
	{
		EvalShadow shadow = (EvalShadow) ExpressionNodeShadow.shadow( _expr, new EvalShadowBuilder( _type ) );
		return shadow.eval();
	}

	
	private final InterpretedNumericType type;

	EvalShadow(ExpressionNode _node, InterpretedNumericType _type)
	{
		super( _node );
		this.type = _type;
	}

	public InterpretedNumericType getType()
	{
		return this.type;
	}


	public Object eval()
	{
		final Object[] argValues = evaluateArguments();
		return evaluateToConstOrExprWithConstantArgsFixed( argValues );
	}


	protected int cardinality()
	{
		return getArguments().size();
	}


	private Object[] evaluateArguments()
	{
		final int card = cardinality();
		final Object[] argValues = new Object[ card ];
		for (int iArg = 0; iArg < card; iArg++) {
			argValues[ iArg ] = evaluateArgument( iArg );
		}
		return argValues;
	}


	protected Object evaluateArgument( int _index )
	{
		final EvalShadow argShadow = (EvalShadow) getArguments().get( _index );
		return argShadow.eval();
	}


	protected Object evaluateToConstOrExprWithConstantArgsFixed( Object[] _args )
	{
		if (hasOnlyConstantArgs( _args )) {
			return evaluateToConst( _args );
		}
		else {
			return nodeWithConstantArgsFixed( _args );
		}
	}


	protected boolean hasOnlyConstantArgs( Object[] _args )
	{
		for (Object arg : _args) {
			if (!isConstant( arg )) return false;
		}
		return true;
	}

	protected boolean isConstant( final Object firstArg )
	{
		return !(firstArg instanceof ExpressionNode);
	}

	protected Object nodeWithConstantArgsFixed( Object[] _args )
	{
		final List<ExpressionNode> argNodes = getNode().getArguments();
		int iArg = 0;
		for (Object arg : _args) {
			if (!(arg instanceof ExpressionNode)) {
				argNodes.set( iArg, new ExpressionNodeForConstantValue( arg ) );
			}
			iArg++;
		}
		return getNode();
	}


	protected abstract Object evaluateToConst( Object[] _args );


}