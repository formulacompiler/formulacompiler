package sej.expressions;

import java.util.ArrayList;
import java.util.List;

public abstract class ExpressionNodeShadow
{
	private final ExpressionNode node;
	private final List<ExpressionNodeShadow> arguments = new ArrayList<ExpressionNodeShadow>();

	public ExpressionNodeShadow(ExpressionNode _node)
	{
		super();
		this.node = _node;
	}

	public ExpressionNode getNode()
	{
		return this.node;
	}

	public List<ExpressionNodeShadow> getArguments()
	{
		return this.arguments;
	}

	public static ExpressionNodeShadow shadow( ExpressionNode _node, Builder _builder )
	{
		final ExpressionNodeShadow result = _builder.shadow( _node );
		final List<ExpressionNodeShadow> resultArgs = result.getArguments();
		for (ExpressionNode argNode : _node.getArguments()) {
			resultArgs.add( shadow( argNode, _builder ) );
		}
		return result;
	}

	public static interface Builder
	{
		ExpressionNodeShadow shadow( ExpressionNode _node );
	}

}
