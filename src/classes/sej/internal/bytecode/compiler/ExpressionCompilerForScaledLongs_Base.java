package sej.internal.bytecode.compiler;

import org.objectweb.asm.Opcodes;

import sej.NumericType;
import sej.internal.NumericTypeImpl;

abstract class ExpressionCompilerForScaledLongs_Base extends ExpressionCompilerForNumbers
{
	protected static final String RUNTIME_CONTEXT_DESCRIPTOR = TypeCompilerForScaledLongs.RUNTIME_CONTEXT_DESCRIPTOR;
	protected static final String RUNTIME_CONTEXT_NAME = TypeCompilerForScaledLongs.RUNTIME_CONTEXT_NAME;

	protected final int scale;
	protected final long one;
	protected final TypeCompilerForScaledLongs longCompiler = ((TypeCompilerForScaledLongs) typeCompiler());


	public ExpressionCompilerForScaledLongs_Base(MethodCompiler _methodCompiler, NumericType _numericType)
	{
		super( _methodCompiler, _numericType );
		this.scale = _numericType.getScale();
		this.one = ((NumericTypeImpl.AbstractLongType) _numericType).one();
	}


	protected final int scale()
	{
		return this.scale;
	}


	@Override
	protected final boolean isScaled()
	{
		return (scale() != 0);
	}


	protected final void compile_scale()
	{
		mv().push( scale() );
	}


	protected final void compile_context()
	{
		this.longCompiler.buildStaticContext();
		mv().visitFieldInsn( Opcodes.GETSTATIC, typeCompiler().rootCompiler().classInternalName(), RUNTIME_CONTEXT_NAME,
				RUNTIME_CONTEXT_DESCRIPTOR );
	}


	protected final void compile_one()
	{
		mv().push( this.one );
	}

}
