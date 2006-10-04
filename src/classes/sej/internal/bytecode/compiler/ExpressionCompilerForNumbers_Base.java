package sej.internal.bytecode.compiler;

import sej.NumericType;

abstract class ExpressionCompilerForNumbers_Base extends ExpressionCompiler
{

	public ExpressionCompilerForNumbers_Base(MethodCompiler _methodCompiler, NumericType _numericType)
	{
		super( _methodCompiler );
	}

}
