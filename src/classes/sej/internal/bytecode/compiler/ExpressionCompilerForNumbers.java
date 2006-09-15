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
import java.util.Date;
import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.Aggregator;
import sej.CompilerException;
import sej.Function;
import sej.NumericType;
import sej.Operator;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForAggregator;
import sej.internal.expressions.ExpressionNodeForFunction;
import sej.internal.expressions.ExpressionNodeForOperator;
import sej.runtime.ScaledLong;

abstract class ExpressionCompilerForNumbers extends ExpressionCompiler
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

	ExpressionCompilerForNumbers(MethodCompiler _methodCompiler, NumericType _numericType)
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


	protected abstract void compileConversionFromInt() throws CompilerException;
	protected abstract void compileConversionToInt() throws CompilerException;


	protected abstract boolean compileConversionFrom( ScaledLong _scale ) throws CompilerException;
	protected abstract boolean compileConversionTo( ScaledLong _scale ) throws CompilerException;


	@Override
	protected void compileConversionTo( Class _class ) throws CompilerException
	{
		if (_class == Boolean.TYPE) {
			compileRuntimeMethod( "booleanFromNum", "(" + typeDescriptor() + ")Z" );
		}
		else if (_class == Boolean.class) {
			compileRuntimeMethod( "booleanFromNum", "(" + typeDescriptor() + ")Z" );
			compileConversionToBoxed( _class, Boolean.TYPE, Boolean.class );
		}
		else if (_class == Date.class) {
			compileRuntimeMethod( "dateFromNum", "(" + typeDescriptor() + ")Ljava/util/Date;" );
		}
		else if (_class == String.class) {
			compileConversionToString();
		}
		else {
			super.compileConversionTo( _class );
		}
	}


	protected void compileConversionToString() throws CompilerException
	{
		throw new CompilerException.UnsupportedDataType( "Cannot convert from a " + this + " to a string." );
	}

	
	@Override
	protected void compileConversionFrom( Class _class ) throws CompilerException
	{
		if (_class == Boolean.TYPE) {
			compileRuntimeMethod( "booleanToNum", "(Z)" + typeDescriptor() );
		}
		else if (_class == Boolean.class) {
			compileRuntimeMethod( "unboxBoolean", BOOL2Z );
			compileRuntimeMethod( "booleanToNum", "(Z)" + typeDescriptor() );
		}
		else if (_class == Date.class) {
			compileRuntimeMethod( "dateToNum", "(Ljava/util/Date;)" + typeDescriptor() );
		}
		else {
			super.compileConversionFrom( _class );
		}
	}


	@Override
	protected void innerCompileConversionFromResultOf( Method _method ) throws CompilerException
	{
		final Class returnType = _method.getReturnType();
		if (returnType == Long.TYPE || returnType == Long.class) {
			final ScaledLong scale = scaleOf( _method );
			if (scale != null && scale.value() != 0) {
				if (returnType == Long.class) {
					compileRuntimeMethod( "unboxLong", LONG2J );
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
						ByteCodeEngineCompiler.compileValueOf( mv(), "java/lang/Long", J2LONG, Long.TYPE );
					}
				}
				else {
					throw new CompilerException.UnsupportedDataType( "Scaled long results not supported by " + this + "." );
				}
				return;
			}
		}
		compileConversionTo( returnType );
	}


	protected final ScaledLong scaleOf( Method _method )
	{
		final ScaledLong typeScale = _method.getDeclaringClass().getAnnotation( ScaledLong.class );
		final ScaledLong mtdScale = _method.getAnnotation( ScaledLong.class );
		final ScaledLong scale = (mtdScale != null) ? mtdScale : typeScale;
		return scale;
	}


	protected final boolean compileConversionToBoxed( Class _returnType, Class _unboxed, Class _boxed,
			int... _conversionOpcodes )
	{
		if (_returnType == _boxed) {
			compileInstructions( _conversionOpcodes );
			final Type unboxedType = Type.getType( _unboxed );
			final Type boxedType = Type.getType( _boxed );

			ByteCodeEngineCompiler.compileValueOf( mv(), boxedType.getInternalName(), "("
					+ unboxedType.getDescriptor() + ")" + boxedType.getDescriptor(), _unboxed );

			return true;
		}
		else {
			return false;
		}
	}


	protected final boolean compileConversionToBoxable( Class _returnType, Class _unboxed, Class _boxed,
			int... _conversionOpcodes )
	{
		if (_returnType == _unboxed) {
			compileInstructions( _conversionOpcodes );
			return true;
		}
		else {
			return compileConversionToBoxed( _returnType, _unboxed, _boxed, _conversionOpcodes );
		}
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
		final ExpressionCompilerForStrings str = method().stringCompiler();
		final List<ExpressionNode> args = _node.arguments();
		switch (_node.getFunction()) {

			case ROUND:
				compileStdFunction( _node );
				return;

			case TODAY:
				compileStdFunction( _node );
				return;

			case MATCH:
				compileHelpedExpr( new HelperCompilerForMatch( section(), _node ) );
				return;
				
			case LEN:
				switch (_node.cardinality()) {
					case 1:
						str.compile( args.get(0) );
						mv().visitMethodInsn( Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I" );
						compileConversionFromInt();
						return;
				}
				break;

			case EXACT:
				switch (_node.cardinality()) {
					case 2:
						str.compile( args.get( 0 ) );
						str.compile( args.get( 1 ) );
						compileRuntimeMethod( "stdEXACT", "(Ljava/lang/String;Ljava/lang/String;)Z" );
						compileConversionFrom( Boolean.TYPE );
						return;
				}
				break;
				
			case SEARCH:
			case FIND:
				switch (_node.cardinality()) {
					case 2:
					case 3:
						str.compile( args.get( 0 ) );
						str.compile( args.get( 1 ) );
						if (_node.cardinality() > 2) {
							compile( args.get( 2 ));
							compileConversionToInt();
						}
						else {
							mv().visitInsn( Opcodes.ICONST_1 );
						}
						compileRuntimeMethod( "std" + _node.getFunction().getName(), "(Ljava/lang/String;Ljava/lang/String;I)I" );
						compileConversionFromInt();
						return;
				}
				break;
				
		}
		super.compileFunction( _node );
	}


	protected void compileStdFunction( ExpressionNodeForFunction _node ) throws CompilerException
	{
		final boolean needsContext = doesStdFunctionNeedContext( _node );
		final StringBuilder descriptorBuilder = new StringBuilder();
		final String typeDescriptor = typeDescriptor();
		descriptorBuilder.append( '(' );
		for (ExpressionNode arg : _node.arguments()) {
			compile( arg );
			descriptorBuilder.append( typeDescriptor );
		}
		if (needsContext) appendStdFunctionContext( descriptorBuilder );
		descriptorBuilder.append( ')' );
		descriptorBuilder.append( typeDescriptor );
		if (needsContext) {
			compileRuntimeMethodWithContext( "std" + _node.getFunction().getName(), descriptorBuilder.toString() );
		}
		else {
			compileRuntimeMethod( "std" + _node.getFunction().getName(), descriptorBuilder.toString() );
		}
	}

	protected boolean doesStdFunctionNeedContext( ExpressionNodeForFunction _node )
	{
		return false;
	}

	protected void appendStdFunctionContext( StringBuilder _descriptorBuilder )
	{
		throw new IllegalStateException( "Internal error: appendStdFunctionContext() is not applicable for " + this + "." );
	}

	protected void compileRuntimeMethodWithContext( String _string, String _string2 )
	{
		throw new IllegalStateException( "Internal error: compileRuntimeMethodWithContext() is not applicable for " + this + "." );
	}


	protected void compileRound()
	{
		compileRuntimeMethod( "round", roundMethodSignature() );
	}

	protected abstract String roundMethodSignature();

	
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

			else if (this.node instanceof ExpressionNodeForAggregator) {
				final ExpressionNodeForAggregator aggNode = (ExpressionNodeForAggregator) this.node;
				final Aggregator aggregator = aggNode.getAggregator();

				switch (aggregator) {
					case AND:
						compileAnd();
						return;
					case OR:
						compileOr();
						return;
				}
			}

			else if (this.node instanceof ExpressionNodeForFunction) {
				final ExpressionNodeForFunction fnNode = (ExpressionNodeForFunction) this.node;
				final Function fn = fnNode.getFunction();

				switch (fn) {

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
			ExpressionCompilerForNumbers.this.compileComparison( _comparisonOpcode );
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
