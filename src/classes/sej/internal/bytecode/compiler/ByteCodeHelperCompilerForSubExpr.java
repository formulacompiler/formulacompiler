package sej.internal.bytecode.compiler;

import sej.CompilerException;
import sej.internal.expressions.ExpressionNode;

public class ByteCodeHelperCompilerForSubExpr extends ByteCodeHelperCompiler
{
	private final ExpressionNode node;

	ByteCodeHelperCompilerForSubExpr(ByteCodeSectionCompiler _section, ExpressionNode _node)
	{
		super( _section, 0 );
		this.node = _node;
	}

	@Override
	protected void compileBody() throws CompilerException
	{
		compileExpr( this.node );
	}

}
