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

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.CompilerException;
import sej.Function;
import sej.NumericType;
import sej.Operator;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForFunction;
import sej.internal.expressions.ExpressionNodeForOperator;
import sej.internal.expressions.LetDictionary.LetEntry;
import sej.internal.model.ExpressionNodeForSubSectionModel;
import sej.runtime.ScaledLong;

abstract class ExpressionCompilerForNumbers_Base extends ExpressionCompilerForAll_Generated
{
	protected final static Type NUMBER_CLASS = Type.getType( Number.class );
	protected final static String N = NUMBER_CLASS.getDescriptor();

	static ExpressionCompilerForNumbers compilerFor( MethodCompiler _methodCompiler, NumericType _numericType )
	{
		if (Double.TYPE == _numericType.getValueType()) {
			return new ExpressionCompilerForDoubles( _methodCompiler, _numericType );
		}
		else if (Long.TYPE == _numericType.getValueType()) {
			return new ExpressionCompilerForScaledLongs( _methodCompiler, _numericType );
		}
		else if (BigDecimal.class == _numericType.getValueType()) {
			return new ExpressionCompilerForBigDecimals( _methodCompiler, _numericType );
		}
		else {
			throw new IllegalArgumentException( "Unsupported data type " + _numericType + " for byte code compilation." );
		}
	}


	private final NumericType numericType;

	public ExpressionCompilerForNumbers_Base(MethodCompiler _methodCompiler, NumericType _numericType)
	{
		super( _methodCompiler );
		this.numericType = _numericType;
	}

	
	NumericType numericType()
	{
		return this.numericType;
	}

	@Override
	protected TypeCompiler typeCompiler()
	{
		return section().engineCompiler().numberCompiler();
	}


	protected abstract boolean compileConversionFrom( ScaledLong _scale ) throws CompilerException;
	protected abstract boolean compileConversionTo( ScaledLong _scale ) throws CompilerException;


	protected final void compileConversionFromInt() throws CompilerException
	{
		compile_util_fromInt();
		compileScaleUp();
	}

	protected final void compileConversionToInt() throws CompilerException
	{
		compileScaleDown();
		compile_util_toInt();
	}


	@Override
	protected void compileConversionTo( Class _class ) throws CompilerException
	{
		final Class unboxed = unboxed( _class );
		if (null == unboxed) {
			compileConversionToUnboxed( _class );
		}
		else {
			compileConversionToUnboxed( unboxed );
			compileBoxing( _class );
		}
	}


	protected abstract boolean isScaled();

	@SuppressWarnings("unused")
	protected void compileScaleUp() throws CompilerException
	{
		if (isScaled()) throw new IllegalStateException( "No scaling for " + toString() );
	}

	@SuppressWarnings("unused")
	protected void compileScaleDown() throws CompilerException
	{
		if (isScaled()) throw new IllegalStateException( "No scaling for " + toString() );
	}


	protected void compileConversionToUnboxed( Class _class ) throws CompilerException
	{
		if (_class == Long.TYPE) {
			compileScaleDown();
			compile_util_toLong();
		}

		else if (_class == Integer.TYPE) {
			compileScaleDown();
			compile_util_toInt();
		}

		else if (_class == Short.TYPE) {
			compileScaleDown();
			compile_util_toShort();
		}

		else if (_class == Byte.TYPE) {
			compileScaleDown();
			compile_util_toByte();
		}

		else if (_class == Boolean.TYPE) {
			compileScaleDown();
			compile_util_toBoolean();
		}

		else if (_class == Character.TYPE) {
			compileScaleDown();
			compile_util_toCharacter();
		}

		else if (_class == Double.TYPE) {
			if (isScaled()) {
				compile_util_toDouble_Scaled();
			}
			else {
				compile_util_toDouble();
			}
		}

		else if (_class == Float.TYPE) {
			if (isScaled()) {
				compile_util_toFloat_Scaled();
			}
			else {
				compile_util_toFloat();
			}
		}

		else if (_class == BigInteger.class) {
			compileScaleDown();
			compile_util_toBigInteger();
		}

		else if (_class == BigDecimal.class) {
			if (isScaled()) {
				compile_util_toBigDecimal_Scaled();
			}
			else {
				compile_util_toBigDecimal();
			}
		}

		else if (_class == Date.class) {
			compile_util_toDate();
		}

		else if (_class == String.class) {
			compile_util_toString();
		}

		else {
			super.compileConversionTo( _class );
		}
	}

	protected abstract void compile_util_toByte() throws CompilerException;
	protected abstract void compile_util_toShort() throws CompilerException;
	protected abstract void compile_util_toInt() throws CompilerException;
	protected abstract void compile_util_toLong() throws CompilerException;
	protected abstract void compile_util_toDouble() throws CompilerException;
	protected abstract void compile_util_toFloat() throws CompilerException;
	protected abstract void compile_util_toBigDecimal() throws CompilerException;
	protected abstract void compile_util_toBigInteger() throws CompilerException;
	protected abstract void compile_util_toBoolean() throws CompilerException;
	protected abstract void compile_util_toCharacter() throws CompilerException;
	protected abstract void compile_util_toDate() throws CompilerException;
	protected abstract void compile_util_toString() throws CompilerException;

	@SuppressWarnings("unused")
	protected void compile_util_toDouble_Scaled() throws CompilerException
	{
		throw new IllegalStateException( "No scaling for " + toString() );
	}

	@SuppressWarnings("unused")
	protected void compile_util_toFloat_Scaled() throws CompilerException
	{
		throw new IllegalStateException( "No scaling for " + toString() );
	}

	@SuppressWarnings("unused")
	protected void compile_util_toBigDecimal_Scaled() throws CompilerException
	{
		throw new IllegalStateException( "No scaling for " + toString() );
	}


	private Class unboxed( Class _class )
	{
		if (Byte.class == _class) {
			return Byte.TYPE;
		}
		else if (Short.class == _class) {
			return Short.TYPE;
		}
		else if (Integer.class == _class) {
			return Integer.TYPE;
		}
		else if (Long.class == _class) {
			return Long.TYPE;
		}
		else if (Float.class == _class) {
			return Float.TYPE;
		}
		else if (Double.class == _class) {
			return Double.TYPE;
		}
		else if (Character.class == _class) {
			return Character.TYPE;
		}
		else if (Boolean.class == _class) {
			return Boolean.TYPE;
		}
		return null;
	}


	private void compileBoxing( Class _class ) throws CompilerException
	{
		if (Byte.class == _class) {
			compile_util_boxByte();
		}
		else if (Short.class == _class) {
			compile_util_boxShort();
		}
		else if (Integer.class == _class) {
			compile_util_boxInteger();
		}
		else if (Long.class == _class) {
			compile_util_boxLong();
		}
		else if (Float.class == _class) {
			compile_util_boxFloat();
		}
		else if (Double.class == _class) {
			compile_util_boxDouble();
		}
		else if (Character.class == _class) {
			compile_util_boxCharacter();
		}
		else if (Boolean.class == _class) {
			compile_util_boxBoolean();
		}
	}


	protected void compileConversionToString() throws CompilerException
	{
		compile_util_toString();
	}


	@Override
	protected void compileConversionFrom( Class _class ) throws CompilerException
	{
		compileConversionFromUnboxed( compileUnboxing( _class ) );
	}


	protected void compileConversionFromUnboxed( Class _class ) throws CompilerException
	{
		if (_class == Integer.TYPE || _class == Short.TYPE || _class == Byte.TYPE) {
			compile_util_fromInt();
			compileScaleUp();
		}

		else if (_class == Long.TYPE) {
			compile_util_fromLong();
			compileScaleUp();
		}

		else if (_class == Double.TYPE) {
			if (isScaled()) {
				compile_util_fromDouble_Scaled();
			}
			else {
				compile_util_fromDouble();
			}
		}

		else if (_class == Float.TYPE) {
			if (isScaled()) {
				compile_util_fromFloat_Scaled();
			}
			else {
				compile_util_fromFloat();
			}
		}

		else if (BigDecimal.class.isAssignableFrom( _class )) {
			if (isScaled()) {
				compile_util_fromBigDecimal_Scaled();
			}
			else {
				compile_util_fromBigDecimal();
			}
		}

		else if (BigInteger.class.isAssignableFrom( _class )) {
			compile_util_fromBigInteger();
			compileScaleUp();
		}

		else if (Number.class.isAssignableFrom( _class )) {
			compile_util_fromNumber();
			compileScaleUp();
		}

		else if (_class == Boolean.TYPE) {
			compile_util_fromBoolean();
		}

		else if (Date.class.isAssignableFrom( _class )) {
			compile_util_fromDate();
		}

		else {
			super.compileConversionFrom( _class );
		}
	}

	protected abstract void compile_util_fromInt() throws CompilerException;
	protected abstract void compile_util_fromLong() throws CompilerException;
	protected abstract void compile_util_fromDouble() throws CompilerException;
	protected abstract void compile_util_fromFloat() throws CompilerException;
	protected abstract void compile_util_fromNumber() throws CompilerException;
	protected abstract void compile_util_fromBoolean() throws CompilerException;
	protected abstract void compile_util_fromDate() throws CompilerException;

	protected void compile_util_fromBigDecimal() throws CompilerException
	{
		compile_util_fromNumber();
	}

	protected void compile_util_fromBigInteger() throws CompilerException
	{
		compile_util_fromNumber();
	}

	@SuppressWarnings("unused")
	protected void compile_util_fromDouble_Scaled() throws CompilerException
	{
		throw new IllegalStateException( "No scaling for " + toString() );
	}

	@SuppressWarnings("unused")
	protected void compile_util_fromFloat_Scaled() throws CompilerException
	{
		throw new IllegalStateException( "No scaling for " + toString() );
	}

	@SuppressWarnings("unused")
	protected void compile_util_fromBigDecimal_Scaled() throws CompilerException
	{
		throw new IllegalStateException( "No scaling for " + toString() );
	}


	private Class compileUnboxing( Class _class ) throws CompilerException
	{
		if (Byte.class.isAssignableFrom( _class )) {
			compile_util_unboxByte();
			return Byte.TYPE;
		}
		else if (Short.class.isAssignableFrom( _class )) {
			compile_util_unboxShort();
			return Short.TYPE;
		}
		else if (Integer.class.isAssignableFrom( _class )) {
			compile_util_unboxInteger();
			return Integer.TYPE;
		}
		else if (Long.class.isAssignableFrom( _class )) {
			compile_util_unboxLong();
			return Long.TYPE;
		}
		else if (Float.class.isAssignableFrom( _class )) {
			compile_util_unboxFloat();
			return Float.TYPE;
		}
		else if (Double.class.isAssignableFrom( _class )) {
			compile_util_unboxDouble();
			return Double.TYPE;
		}
		else if (Character.class.isAssignableFrom( _class )) {
			compile_util_unboxCharacter();
			return Character.TYPE;
		}
		else if (Boolean.class.isAssignableFrom( _class )) {
			compile_util_unboxBoolean();
			return Boolean.TYPE;
		}
		return _class;
	}


	@Override
	protected void innerCompileConversionFromResultOf( Method _method ) throws CompilerException
	{
		final Class returnType = _method.getReturnType();
		if (returnType == Long.TYPE || returnType == Long.class) {
			final ScaledLong scale = scaleOf( _method );
			if (scale != null && scale.value() != 0) {
				if (returnType == Long.class) {
					compile_util_unboxLong();
				}
				if (!compileConversionFrom( scale )) {
					throw new CompilerException.UnsupportedDataType( "Scaled long inputs not supported by " + this + "." );
				}
				return;
			}
		}
		compileConversionFrom( returnType );
	}


	@Override
	protected void innerCompileConversionToResultOf( Method _method ) throws CompilerException
	{
		final Class returnType = _method.getReturnType();
		if (returnType == Long.TYPE || returnType == Long.class) {
			final ScaledLong scale = scaleOf( _method );
			if (scale != null && scale.value() != 0) {
				if (compileConversionTo( scale )) {
					if (returnType == Long.class) {
						mv().visitMethodInsn( Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", J2LONG );
					}
				}
				else {
					throw new CompilerException.UnsupportedDataType( "Scaled long results not supported by " + this + "." );
				}
				return;
			}
		}
		if (!isNativeType( returnType )) {
			compileConversionTo( returnType );
		}
	}

	protected abstract boolean isNativeType( Class _type );


	protected final ScaledLong scaleOf( Method _method )
	{
		final ScaledLong typeScale = _method.getDeclaringClass().getAnnotation( ScaledLong.class );
		final ScaledLong mtdScale = _method.getAnnotation( ScaledLong.class );
		final ScaledLong scale = (mtdScale != null) ? mtdScale : typeScale;
		return scale;
	}


	protected final void compileInstructions( int... _conversionOpcodes )
	{
		final GeneratorAdapter mv = mv();
		for (int conv : _conversionOpcodes) {
			mv.visitInsn( conv );
		}
	}


	@Override
	protected void compileFunction( ExpressionNodeForFunction _node ) throws CompilerException
	{
		switch (_node.getFunction()) {

			case MATCH:
				Iterable<LetEntry> closure = closureOf( _node );
				compileHelpedExpr( new HelperCompilerForMatch( sectionInContext(), _node, closure ), closure );
				return;

			case COUNT:
				compileCount( _node );
				return;

		}
		super.compileFunction( _node );
	}


	protected void compileRuntimeFunction( ExpressionNodeForFunction _node ) throws CompilerException
	{
		final boolean needsContext = doesRuntimeFunctionNeedContext( _node );
		final StringBuilder descriptorBuilder = new StringBuilder();
		final String typeDescriptor = typeDescriptor();
		descriptorBuilder.append( '(' );
		for (ExpressionNode arg : _node.arguments()) {
			compile( arg );
			descriptorBuilder.append( typeDescriptor );
		}
		if (needsContext) appendRuntimeFunctionContext( descriptorBuilder );
		descriptorBuilder.append( ')' );
		descriptorBuilder.append( typeDescriptor );
		if (needsContext) {
			compileRuntimeMethodWithContext( "std" + _node.getFunction().getName(), descriptorBuilder.toString() );
		}
		else {
			compileRuntimeMethod( "std" + _node.getFunction().getName(), descriptorBuilder.toString() );
		}
	}

	protected boolean doesRuntimeFunctionNeedContext( ExpressionNodeForFunction _node )
	{
		return false;
	}

	protected void appendRuntimeFunctionContext( StringBuilder _descriptorBuilder )
	{
		throw new IllegalStateException( "Internal error: appendStdFunctionContext() is not applicable for " + this + "." );
	}

	protected void compileRuntimeMethodWithContext( String _string, String _string2 )
	{
		throw new IllegalStateException( "Internal error: compileRuntimeMethodWithContext() is not applicable for "
				+ this + "." );
	}


	protected abstract void compile_util_round( int _maxFractionalDigits ) throws CompilerException;


	private final void compileCount( ExpressionNodeForFunction _node ) throws CompilerException
	{
		final GeneratorAdapter mv = mv();

		int statics = countStatics( _node, 0 );
		mv.push( statics );

		for (ExpressionNode arg : _node.arguments()) {
			if (arg instanceof ExpressionNodeForSubSectionModel) {
				ExpressionNodeForSubSectionModel subArg = (ExpressionNodeForSubSectionModel) arg;
				SubSectionCompiler sub = sectionInContext().subSectionCompiler( subArg.getSectionModel() );

				mv.visitVarInsn( Opcodes.ALOAD, method().objectInContext() );
				sectionInContext().compileCallToGetterFor( mv, sub );
				mv.arrayLength();
				mv.push( subArg.arguments().size() );
				mv.visitInsn( Opcodes.IMUL );
				mv.visitInsn( Opcodes.IADD );

			}
		}

		compileConversionFromInt();
	}

	private int countStatics( ExpressionNode _node, int _n )
	{
		int n = _n;
		for (ExpressionNode arg : _node.arguments()) {
			if (!(arg instanceof ExpressionNodeForSubSectionModel)) {
				n++;
			}
		}
		return n;
	}


	final void compileTest( ExpressionNode _test, Label _notMet ) throws CompilerException
	{
		new TestCompilerBranchingWhenFalse( _test, _notMet ).compileTest();
	}


	private abstract class TestCompiler
	{
		protected ExpressionNode node;
		protected Label branchTo;

		TestCompiler(ExpressionNode _node, Label _branchTo)
		{
			super();
			this.node = _node;
			this.branchTo = _branchTo;
		}

		final void compileTest() throws CompilerException
		{
			if (this.node instanceof ExpressionNodeForOperator) {
				final ExpressionNodeForOperator opNode = (ExpressionNodeForOperator) this.node;
				final Operator operator = opNode.getOperator();

				switch (operator) {

					case AND:
						compileAnd();
						return;

					case OR:
						compileOr();
						return;

					case EQUAL:
					case NOTEQUAL:
					case GREATER:
					case GREATEROREQUAL:
					case LESS:
					case LESSOREQUAL:
						final List<ExpressionNode> args = this.node.arguments();
						compileComparison( operator, args.get( 0 ), args.get( 1 ) );
						return;
				}
			}

			else if (this.node instanceof ExpressionNodeForFunction) {
				final ExpressionNodeForFunction fnNode = (ExpressionNodeForFunction) this.node;
				final Function fn = fnNode.getFunction();

				switch (fn) {

					case AND:
						compileAnd();
						return;

					case OR:
						compileOr();
						return;

					case NOT:
						compileNot();
						return;

				}
			}

			compileValue();
		}


		/**
		 * This method assumes that the data type of the lefthand argument drives the type of the
		 * comparison.
		 */
		private final void compileComparison( Operator _operator, ExpressionNode _left, ExpressionNode _right )
				throws CompilerException
		{
			final ExpressionCompiler leftCompiler = method().expressionCompiler( _left.getDataType() );
			leftCompiler.compile( _left );
			leftCompiler.compile( _right );
			compileComparison( _operator );
		}

		protected abstract TestCompiler newInverseCompiler( ExpressionNode _node, Label _branchTo );
		protected abstract void compileAnd() throws CompilerException;
		protected abstract void compileOr() throws CompilerException;
		protected abstract void compileComparison( Operator _comparison ) throws CompilerException;

		protected final void compileComparison( int _ifOpcode, int _comparisonOpcode ) throws CompilerException
		{
			ExpressionCompilerForNumbers_Base.this.compileComparison( _comparisonOpcode );
			mv().visitJumpInsn( _ifOpcode, this.branchTo );
		}

		private final void compileNot() throws CompilerException
		{
			final List<ExpressionNode> args = this.node.arguments();
			if (1 == args.size()) {
				newInverseCompiler( args.get( 0 ), this.branchTo ).compileTest();
			}
			else {
				throw new CompilerException.UnsupportedExpression( "NOT must have exactly one argument." );
			}
		}

		final void compileValue() throws CompilerException
		{
			compile( this.node );
			compileZero();
			compileComparison( Operator.NOTEQUAL );
		}

		protected abstract void compileBooleanTest() throws CompilerException;
	}


	private class TestCompilerBranchingWhenFalse extends TestCompiler
	{

		TestCompilerBranchingWhenFalse(ExpressionNode _node, Label _branchTo)
		{
			super( _node, _branchTo );
		}

		@Override
		protected void compileComparison( Operator _comparison ) throws CompilerException
		{
			switch (_comparison) {

				case EQUAL:
					compileComparison( Opcodes.IFNE, Opcodes.DCMPL );
					return;

				case NOTEQUAL:
					compileComparison( Opcodes.IFEQ, Opcodes.DCMPL );
					return;

				case GREATER:
					compileComparison( Opcodes.IFLE, Opcodes.DCMPL );
					return;

				case GREATEROREQUAL:
					compileComparison( Opcodes.IFLT, Opcodes.DCMPL );
					return;

				case LESS:
					compileComparison( Opcodes.IFGE, Opcodes.DCMPG );
					return;

				case LESSOREQUAL:
					compileComparison( Opcodes.IFGT, Opcodes.DCMPG );
					return;

			}
		}

		@Override
		protected TestCompiler newInverseCompiler( ExpressionNode _node, Label _branchTo )
		{
			return new TestCompilerBranchingWhenTrue( _node, _branchTo );
		}

		@Override
		protected void compileOr() throws CompilerException
		{
			final Label met = mv().newLabel();
			final int nArg = this.node.arguments().size();
			int iArg = 0;
			while (iArg < nArg - 1) {
				final ExpressionNode arg = this.node.arguments().get( iArg );
				new TestCompilerBranchingWhenTrue( arg, met ).compileTest();
				iArg++;
			}
			final ExpressionNode lastArg = this.node.arguments().get( iArg );
			new TestCompilerBranchingWhenFalse( lastArg, this.branchTo ).compileTest();
			mv().mark( met );
		}

		@Override
		protected void compileAnd() throws CompilerException
		{
			for (ExpressionNode arg : this.node.arguments()) {
				new TestCompilerBranchingWhenFalse( arg, this.branchTo ).compileTest();
			}
		}

		@Override
		protected void compileBooleanTest() throws CompilerException
		{
			mv().visitJumpInsn( Opcodes.IFEQ, this.branchTo );
		}
	}


	private class TestCompilerBranchingWhenTrue extends TestCompiler
	{

		TestCompilerBranchingWhenTrue(ExpressionNode _node, Label _branchTo)
		{
			super( _node, _branchTo );
		}

		@Override
		protected void compileComparison( Operator _comparison ) throws CompilerException
		{
			switch (_comparison) {

				case EQUAL:
					compileComparison( Opcodes.IFEQ, Opcodes.DCMPL );
					return;

				case NOTEQUAL:
					compileComparison( Opcodes.IFNE, Opcodes.DCMPL );
					return;

				case GREATER:
					compileComparison( Opcodes.IFGT, Opcodes.DCMPG );
					return;

				case GREATEROREQUAL:
					compileComparison( Opcodes.IFGE, Opcodes.DCMPG );
					return;

				case LESS:
					compileComparison( Opcodes.IFLT, Opcodes.DCMPL );
					return;

				case LESSOREQUAL:
					compileComparison( Opcodes.IFLE, Opcodes.DCMPL );
					return;

			}
		}

		@Override
		protected TestCompiler newInverseCompiler( ExpressionNode _node, Label _branchTo )
		{
			return new TestCompilerBranchingWhenFalse( _node, _branchTo );
		}

		@Override
		protected void compileOr() throws CompilerException
		{
			for (ExpressionNode arg : this.node.arguments()) {
				new TestCompilerBranchingWhenTrue( arg, this.branchTo ).compileTest();
			}
		}

		@Override
		protected void compileAnd() throws CompilerException
		{
			final Label notMet = mv().newLabel();
			final int nArg = this.node.arguments().size();
			int iArg = 0;
			while (iArg < nArg - 1) {
				final ExpressionNode arg = this.node.arguments().get( iArg );
				new TestCompilerBranchingWhenFalse( arg, notMet ).compileTest();
				iArg++;
			}
			final ExpressionNode lastArg = this.node.arguments().get( iArg );
			new TestCompilerBranchingWhenTrue( lastArg, this.branchTo ).compileTest();
			mv().mark( notMet );
		}

		@Override
		protected void compileBooleanTest() throws CompilerException
		{
			mv().visitJumpInsn( Opcodes.IFNE, this.branchTo );
		}
	}


	@Override
	public String toString()
	{
		return numericType().toString();
	}

}
