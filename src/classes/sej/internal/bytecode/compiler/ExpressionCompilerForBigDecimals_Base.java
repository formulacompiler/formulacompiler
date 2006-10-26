package sej.internal.bytecode.compiler;

import sej.CompilerException;
import sej.NumericType;

abstract class ExpressionCompilerForBigDecimals_Base extends ExpressionCompilerForNumbers
{
	protected final int fixedScale;
	protected final int roundingMode;


	public ExpressionCompilerForBigDecimals_Base(MethodCompiler _methodCompiler, NumericType _numericType)
	{
		super( _methodCompiler, _numericType );
		this.fixedScale = _numericType.getScale();
		this.roundingMode = _numericType.getRoundingMode();
	}


	@Override
	protected final boolean isScaled()
	{
		return false;
	}


	protected final void compile_fixedScale()
	{
		mv().push( this.fixedScale );
	}

	protected final void compile_roundingMode()
	{
		mv().push( this.roundingMode );
	}


	protected final boolean needsValueAdjustment()
	{
		return (this.fixedScale != NumericType.UNDEFINED_SCALE);
	}

	protected final void compileValueAdjustment() throws CompilerException
	{
		if (needsValueAdjustment()) {
			compile_util_adjustValue();
		}
	}

	protected abstract void compile_util_adjustValue() throws CompilerException;


}
