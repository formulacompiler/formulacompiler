package sej.internal.bytecode.compiler;

import sej.CompilerException;
import sej.internal.expressions.ExpressionNode;

final class InnerExpressionException extends CompilerException
{
	private final ExpressionNode errorNode;

	
	InnerExpressionException(ExpressionNode _errorNode, CompilerException _cause)
	{
		super( _cause.getMessage(), _cause );
		this.errorNode = _errorNode;
	}

	
	ExpressionNode getErrorNode()
	{
		return this.errorNode;
	}
	
	@Override
	public CompilerException getCause()
	{
		return (CompilerException) super.getCause();
	}
	
}
