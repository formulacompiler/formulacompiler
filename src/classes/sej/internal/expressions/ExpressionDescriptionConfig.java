package sej.internal.expressions;

public final class ExpressionDescriptionConfig
{
	private final ExpressionNode focusedNode;
	private final String focusStartMarker;
	private final String focusEndMarker;

	public ExpressionDescriptionConfig(ExpressionNode _focusedNode, String _focusStartMarker, String _focusEndMarker)
	{
		super();
		this.focusedNode = _focusedNode;
		this.focusStartMarker = _focusStartMarker;
		this.focusEndMarker = _focusEndMarker;
	}

	
	public boolean isFocused( ExpressionNode _node )
	{
		if (_node == null) {
			return false;
		}
		return (_node == this.focusedNode); 
	}


	public String focusStartMarker()
	{
		return this.focusStartMarker;
	}


	public String focusEndMarker()
	{
		return this.focusEndMarker;
	}

}
