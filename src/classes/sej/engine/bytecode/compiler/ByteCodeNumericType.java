package sej.engine.bytecode.compiler;

import java.math.BigDecimal;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.ModelError;
import sej.NumericType;
import sej.engine.expressions.Operator;


public abstract class ByteCodeNumericType
{

	ByteCodeNumericType()
	{
		super();
	}

	public static ByteCodeNumericType typeFor( NumericType _type ) throws ModelError
	{
		if (Double.TYPE == _type.getValueType()) {
			return new DoubleType();
		}
		else if (BigDecimal.class == _type.getValueType()) {
			return new BigDecimalType( _type.getScale() );
		}
		else {
			throw new ModelError.UnsupportedDataType( "Unsupported numeric type for byte code compilation." );
		}
	}


	public abstract Type getType();
	public abstract int getReturnOpcode();

	public String getDescriptor()
	{
		return getType().getDescriptor();
	}

	public void compile( GeneratorAdapter _adapter, Operator _operator, int _numberOfArguments ) throws ModelError
	{
		switch (_operator) {

		case NOOP:
			return;

		default:
			throw new ModelError.UnsupportedOperator( "The operator '"
					+ _operator.getSymbol() + "' is not supported here." );
		}
	}

	public abstract void compileConversionFromDouble( GeneratorAdapter _mv );

	

	static final class DoubleType extends ByteCodeNumericType
	{

		@Override
		public Type getType()
		{
			return Type.DOUBLE_TYPE;
		}

		
		@Override
		public int getReturnOpcode()
		{
			return Opcodes.DRETURN;
		}

		
		@Override
		public void compile( GeneratorAdapter _mv, Operator _operator, int _numberOfArguments ) throws ModelError
		{
			switch (_operator) {

			case PLUS:
				_mv.visitInsn( Opcodes.DADD );
				break;

			case MINUS:
				if (1 == _numberOfArguments) {
					_mv.visitInsn( Opcodes.DNEG );
				}
				else {
					_mv.visitInsn( Opcodes.DSUB );
				}
				break;

			case TIMES:
				_mv.visitInsn( Opcodes.DMUL );
				break;

			case DIV:
				_mv.visitInsn( Opcodes.DDIV );
				break;

			case PERCENT:
				_mv.visitLdcInsn( 100.0 );
				_mv.visitInsn( Opcodes.DDIV );
				break;

			case EXP:
				_mv.visitMethodInsn( Opcodes.INVOKESTATIC, ByteCodeCompiler.MATH.getInternalName(), "pow", "(DD)D" );
				break;

			case MIN:
				_mv.visitMethodInsn( Opcodes.INVOKESTATIC, ByteCodeCompiler.RUNTIME.getInternalName(), "min", "(DD)D" );
				break;

			case MAX:
				_mv.visitMethodInsn( Opcodes.INVOKESTATIC, ByteCodeCompiler.RUNTIME.getInternalName(), "max", "(DD)D" );
				break;

			default:
				super.compile( _mv, _operator, _numberOfArguments );
			}
		}

		
		@Override
		public void compileConversionFromDouble( GeneratorAdapter _mv )
		{
			// Already is a double.
		}

	}


	static final class BigDecimalType extends ByteCodeNumericType
	{
		private static final String BNAME = ByteCodeCompiler.BIGDECIMAL.getInternalName();
		private static final String B = ByteCodeCompiler.BIGDECIMAL.getDescriptor();
		private static final String V2B = "()" + B;
		private static final String I2B = "(I)" + B;
		private static final String D2B = "(D)" + B;
		private static final String B2B = "(" + B + ")" + B;


		public BigDecimalType(int _scale)
		{
			super();
		}

		
		@Override
		public Type getType()
		{
			return Type.getType( BigDecimal.class );
		}

		
		@Override
		public int getReturnOpcode()
		{
			return Opcodes.ARETURN;
		}

		
		@Override
		public void compile( GeneratorAdapter _mv, Operator _operator, int _numberOfArguments ) throws ModelError
		{
			switch (_operator) {

			case PLUS:
				_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "add", B2B );
				break;

			case MINUS:
				if (1 == _numberOfArguments) {
					_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "negate", V2B );
				}
				else {
					_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "subtract", B2B );
				}
				break;

			case TIMES:
				_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "multiply", B2B );
				break;

			case DIV:
				_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "divide", B2B );
				break;

			case PERCENT:
				_mv.push( 2 );
				_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "movePointLeft", I2B );
				break;

			case EXP:
				_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "intValue", "()I" );
				_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "pow", I2B );
				break;

			case MIN:
				_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "min", B2B );
				break;

			case MAX:
				_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "max", B2B );
				break;

			default:
				super.compile( _mv, _operator, _numberOfArguments );
			}
		}

		
		@Override
		public void compileConversionFromDouble( GeneratorAdapter _mv )
		{
			_mv.visitMethodInsn( Opcodes.INVOKESTATIC, BNAME, "valueOf", D2B );
		}
		
	}

}
