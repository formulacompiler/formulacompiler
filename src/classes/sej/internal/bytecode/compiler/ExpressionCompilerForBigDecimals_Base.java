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
