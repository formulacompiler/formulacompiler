package sej.internal.bytecode.compiler;

import sej.NumericType;

abstract class ExpressionCompilerForDoubles_Base extends ExpressionCompilerForNumbers
{

	public ExpressionCompilerForDoubles_Base(MethodCompiler _methodCompiler, NumericType _numericType)
	{
		super( _methodCompiler, _numericType );
	}	

}
