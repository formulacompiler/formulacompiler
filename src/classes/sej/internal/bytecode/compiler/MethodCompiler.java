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
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.Aggregator;
import sej.CallFrame;
import sej.CompilerException;
import sej.Function;
import sej.Operator;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForAggregator;
import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.expressions.ExpressionNodeForFunction;
import sej.internal.expressions.ExpressionNodeForOperator;
import sej.internal.model.CellModel;
import sej.internal.model.ExpressionNodeForCellModel;
import sej.internal.model.ExpressionNodeForParentSectionModel;
import sej.internal.model.ExpressionNodeForPartialAggregation;
import sej.internal.model.ExpressionNodeForSubSectionModel;
import sej.internal.model.Aggregation.NonNullCountingAggregation;


abstract class MethodCompiler
{
	private static final ExpressionNode TRUENODE = new ExpressionNodeForConstantValue( Boolean.TRUE );
	private static final ExpressionNode FALSENODE = new ExpressionNodeForConstantValue( Boolean.FALSE );

	private final SectionCompiler section;
	private final String methodName;
	private final String methodDescriptor;
	private final GeneratorAdapter mv;


	MethodCompiler(SectionCompiler _section, int _access, String _methodName, String _descriptor)
	{
		super();
		this.section = _section;
		this.methodName = _methodName;
		this.methodDescriptor = _descriptor;
		this.mv = section().newMethod( _access | Opcodes.ACC_FINAL, _methodName, _descriptor );
	}


	SectionCompiler section()
	{
		return this.section;
	}

	NumericTypeCompiler numericType()
	{
		return section().numericType();
	}

	protected Type runtimeType()
	{
		return numericType().runtimeType();
	}

	String methodName()
	{
		return this.methodName;
	}

	String methodDescriptor()
	{
		return this.methodDescriptor;
	}

	protected ClassWriter cw()
	{
		return section().cw();
	}

	protected GeneratorAdapter mv()
	{
		return this.mv;
	}


	void compile() throws CompilerException
	{
		beginCompilation();
		compileBody();
		endCompilation();
	}


	protected void beginCompilation()
	{
		mv().visitCode();
	}


	protected void endCompilation()
	{
		section().endMethod( mv() );
	}


	protected abstract void compileBody() throws CompilerException;


	protected void compileRef( MethodVisitor _mv, CellComputation _cell )
	{
		compileRef( _mv, _cell.getMethodName() );
	}

	protected void compileRef( MethodVisitor _mv, String _getterName )
	{
		_mv.visitVarInsn( Opcodes.ALOAD, 0 );
		_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, section().classInternalName(), _getterName, "()"
				+ numericType().descriptor() );
	}


	protected void pushConstParam( Class _type, Object _constantValue ) throws CompilerException
	{
		if (null == _constantValue) {
			mv().visitInsn( Opcodes.ACONST_NULL );
		}

		else if (_type == Byte.TYPE) {
			mv().push( ((Number) _constantValue).byteValue() );
		}
		else if (_type == Byte.class) {
			mv().push( ((Number) _constantValue).byteValue() );
			ByteCodeEngineCompiler.compileValueOf( mv(), "java/lang/Byte", "(B)Ljava/lang/Byte;", Byte.TYPE );
		}

		else if (_type == Short.TYPE) {
			mv().push( ((Number) _constantValue).shortValue() );
		}
		else if (_type == Short.class) {
			mv().push( ((Number) _constantValue).shortValue() );
			ByteCodeEngineCompiler.compileValueOf( mv(), "java/lang/Short", "(S)Ljava/lang/Short;", Short.TYPE );
		}

		else if (_type == Integer.TYPE) {
			mv().push( ((Number) _constantValue).intValue() );
		}
		else if (_type == Integer.class) {
			mv().push( ((Number) _constantValue).intValue() );
			ByteCodeEngineCompiler.compileValueOf( mv(), "java/lang/Integer", "(I)Ljava/lang/Integer;", Integer.TYPE );
		}

		else if (_type == Long.TYPE) {
			mv().push( ((Number) _constantValue).longValue() );
		}
		else if (_type == Long.class) {
			mv().push( ((Number) _constantValue).longValue() );
			ByteCodeEngineCompiler.compileValueOf( mv(), "java/lang/Long", "(J)Ljava/lang/Long;", Long.TYPE );
		}

		else if (_type == Double.TYPE) {
			mv().push( ((Number) _constantValue).doubleValue() );
		}
		else if (_type == Double.class) {
			mv().push( ((Number) _constantValue).doubleValue() );
			ByteCodeEngineCompiler.compileValueOf( mv(), "java/lang/Double", "(D)Ljava/lang/Double;", Double.TYPE );
		}

		else if (_type == Float.TYPE) {
			mv().push( ((Number) _constantValue).floatValue() );
		}
		else if (_type == Float.class) {
			mv().push( ((Number) _constantValue).floatValue() );
			ByteCodeEngineCompiler.compileValueOf( mv(), "java/lang/Float", "(F)Ljava/lang/Float;", Float.TYPE );
		}

		else if (_type == Character.TYPE) {
			mv().push( ((Character) _constantValue).charValue() );
		}
		else if (_type == Character.class) {
			mv().push( ((Character) _constantValue).charValue() );
			ByteCodeEngineCompiler.compileValueOf( mv(), "java/lang/Character", "(C)Ljava/lang/Character;", Character.TYPE );
		}

		else if (_type == Boolean.TYPE) {
			mv().push( ((Boolean) _constantValue).booleanValue() );
		}
		else if (_type == Boolean.class) {
			mv().push( ((Boolean) _constantValue).booleanValue() );
			mv().visitMethodInsn( Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;" ); // I know JRE 1.4 handles this
		}

		else if (_type == String.class) {
			mv().visitLdcInsn( _constantValue );
		}

		else if (_type == Date.class) {
			mv().visitLdcInsn( _constantValue );
		}

		else if (_constantValue instanceof Enum) {
			final Enum enumValue = (Enum) _constantValue;
			final Type enumType = Type.getType( enumValue.getDeclaringClass() );
			final Type instanceType = Type.getType( enumValue.getClass() );
			mv().getStatic( enumType, enumValue.name(), instanceType );
		}

		else {
			throw new CompilerException.UnsupportedDataType( "The data type '"
					+ _type + "' is not supported as an input method parameter" );
		}
	}


	protected void compileConst( Object _constantValue ) throws CompilerException
	{
		numericType().compileConst( mv(), _constantValue );
	}


	protected void compileNumericInput( CallFrame _callChainToCall ) throws CompilerException
	{
		compileInputGetterCall( _callChainToCall );
		numericType().compileToNum( mv(), _callChainToCall.getMethod() );
	}


	protected void compileInputGetterCall( CallFrame _callChainToCall ) throws CompilerException
	{
		final CallFrame[] frames = _callChainToCall.getFrames();
		final boolean isStatic = Modifier.isStatic( frames[ 0 ].getMethod().getModifiers() );

		if (!isStatic) {
			mv().loadThis();
			mv().getField( section().classType(), ByteCodeEngineCompiler.INPUTS_MEMBER_NAME, section().inputType() );
		}

		Class contextClass = section().inputClass();
		for (CallFrame frame : frames) {
			final Method method = frame.getMethod();
			final Object[] args = frame.getArgs();
			if (null != args) {
				final Class[] types = method.getParameterTypes();
				for (int i = 0; i < args.length; i++) {
					final Object arg = args[ i ];
					final Class type = types[ i ];
					pushConstParam( type, arg );
				}
			}
			int opcode = Opcodes.INVOKEVIRTUAL;
			if (contextClass.isInterface()) opcode = Opcodes.INVOKEINTERFACE;
			else if (isStatic) opcode = Opcodes.INVOKESTATIC;

			mv().visitMethodInsn( opcode, Type.getType( contextClass ).getInternalName(), method.getName(),
					Type.getMethodDescriptor( method ) );

			contextClass = method.getReturnType();
		}
	}


	protected void compileExpr( ExpressionNode _node ) throws CompilerException
	{
		if (null == _node) {
			mv().visitInsn( Opcodes.ACONST_NULL );
		}

		else if (_node instanceof ExpressionNodeForConstantValue) {
			final ExpressionNodeForConstantValue node = (ExpressionNodeForConstantValue) _node;
			compileConst( node.getValue() );
		}

		else if (_node instanceof ExpressionNodeForCellModel) {
			final ExpressionNodeForCellModel node = (ExpressionNodeForCellModel) _node;
			CellModel cell = (node).getCellModel();
			compileRef( cell );
		}

		else if (_node instanceof ExpressionNodeForParentSectionModel) {
			final ExpressionNodeForParentSectionModel node = (ExpressionNodeForParentSectionModel) _node;
			compileRef( node );
		}

		else if (_node instanceof ExpressionNodeForSubSectionModel) {
			final ExpressionNodeForSubSectionModel node = (ExpressionNodeForSubSectionModel) _node;
			compileRef( node );
		}

		else if (_node instanceof ExpressionNodeForOperator) {
			final ExpressionNodeForOperator node = (ExpressionNodeForOperator) _node;
			if (needsIf( node.getOperator() )) {
				compileIf( node, TRUENODE, FALSENODE );
			}
			else {
				compileOperator( node );
			}
		}

		else if (_node instanceof ExpressionNodeForFunction) {
			final ExpressionNodeForFunction node = (ExpressionNodeForFunction) _node;
			switch (node.getFunction()) {

				case IF:
					compileIf( node );
					break;

				case NOT:
					compileIf( node, TRUENODE, FALSENODE );
					break;

				default:
					compileFunction( node );
			}
		}

		else if (_node instanceof ExpressionNodeForAggregator) {
			final ExpressionNodeForAggregator node = (ExpressionNodeForAggregator) _node;
			switch (node.getAggregator()) {

				case AND:
				case OR:
					compileIf( node, TRUENODE, FALSENODE );
					break;

				default:
					compileAggregation( node );
			}
		}

		else {
			unsupported( _node );
		}

	}


	private boolean needsIf( Operator _operator )
	{
		switch (_operator) {
			case AND:
			case OR:
			case EQUAL:
			case NOTEQUAL:
			case LESS:
			case LESSOREQUAL:
			case GREATER:
			case GREATEROREQUAL:
				return true;
			default:
				return false;
		}
	}


	private void compileOperator( ExpressionNodeForOperator _node ) throws CompilerException
	{
		final List<ExpressionNode> args = _node.arguments();
		final Operator op = _node.getOperator();
		switch (args.size()) {

			case 0:
				unsupported( _node );
				break;

			case 1:
				compileExpr( args.get( 0 ) );
				compileOperator( op, 1 );
				break;

			default:
				compileExpr( args.get( 0 ) );
				for (int i = 1; i < args.size(); i++) {
					compileExpr( args.get( i ) );
					compileOperator( op, 2 );
				}

		}
	}


	protected void compileOperator( Operator _operator, int _numberOfArguments ) throws CompilerException
	{
		numericType().compile( mv(), _operator, _numberOfArguments );
	}


	private void compileFunction( ExpressionNodeForFunction _node ) throws CompilerException
	{
		switch (_node.getFunction()) {

			case ROUND:
				compileStdFunction( _node );
				break;

			case TODAY:
				compileStdFunction( _node );
				break;

			case INDEX:
				compileHelpedExpr( new HelperCompilerForIndex( section(), _node ) );
				break;

			case MATCH:
				compileHelpedExpr( new HelperCompilerForMatch( section(), _node ) );
				break;

			default:
				unsupported( _node );
		}
	}


	private void compileStdFunction( ExpressionNodeForFunction _node ) throws CompilerException
	{
		final StringBuilder argTypeBuilder = new StringBuilder();
		for (ExpressionNode arg : _node.arguments()) {
			compileExpr( arg );
			argTypeBuilder.append( numericType().descriptor() );
		}
		numericType().compileStdFunction( mv(), _node.getFunction(), argTypeBuilder.toString() );
	}


	private void compileIf( ExpressionNodeForFunction _node ) throws CompilerException
	{
		compileIf( _node.arguments().get( 0 ), _node.arguments().get( 1 ), _node.arguments().get( 2 ) );
	}


	private void compileIf( ExpressionNode _test, ExpressionNode _ifTrue, ExpressionNode _ifFalse )
			throws CompilerException
	{
		final Label notMet = mv().newLabel();
		final Label done = mv().newLabel();

		new TestCompilerBranchingWhenFalse( _test, notMet ).compile();

		compileExpr( _ifTrue );

		mv().visitJumpInsn( Opcodes.GOTO, done );
		mv().mark( notMet );

		compileExpr( _ifFalse );

		mv().mark( done );
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

		void compile() throws CompilerException
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
						compileExpr( this.node.arguments().get( 0 ) );
						compileExpr( this.node.arguments().get( 1 ) );
						compileComparison( operator );
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

		protected abstract TestCompiler newInverseCompiler( ExpressionNode _node, Label _branchTo );
		protected abstract void compileAnd() throws CompilerException;
		protected abstract void compileOr() throws CompilerException;
		protected abstract void compileComparison( Operator _comparison ) throws CompilerException;

		protected void compileComparison( int _ifOpcode, int _comparisonOpcode )
		{
			numericType().compileComparison( mv(), _comparisonOpcode );
			mv().visitJumpInsn( _ifOpcode, this.branchTo );
		}

		private void compileNot() throws CompilerException
		{
			final List<ExpressionNode> args = this.node.arguments();
			if (0 < args.size()) {
				newInverseCompiler( args.get( 0 ), this.branchTo ).compile();
			}
			else {
				unsupported( this.node );
			}
		}

		void compileValue() throws CompilerException
		{
			compileExpr( this.node );
			numericType().compileZero( mv() );
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
				new TestCompilerBranchingWhenTrue( arg, met ).compile();
				iArg++;
			}
			final ExpressionNode lastArg = this.node.arguments().get( iArg );
			new TestCompilerBranchingWhenFalse( lastArg, this.branchTo ).compile();
			mv().mark( met );
		}

		@Override
		protected void compileAnd() throws CompilerException
		{
			for (ExpressionNode arg : this.node.arguments()) {
				new TestCompilerBranchingWhenFalse( arg, this.branchTo ).compile();
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
				new TestCompilerBranchingWhenTrue( arg, this.branchTo ).compile();
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
				new TestCompilerBranchingWhenFalse( arg, notMet ).compile();
				iArg++;
			}
			final ExpressionNode lastArg = this.node.arguments().get( iArg );
			new TestCompilerBranchingWhenTrue( lastArg, this.branchTo ).compile();
			mv().mark( notMet );
		}

		@Override
		protected void compileBooleanTest() throws CompilerException
		{
			mv().visitJumpInsn( Opcodes.IFNE, this.branchTo );
		}
	}


	private void compileAggregation( ExpressionNodeForAggregator _node ) throws CompilerException
	{
		final Aggregator aggregator = _node.getAggregator();

		switch (aggregator) {

			case COUNT:
				compileCount( _node );
				break;

			case AVERAGE:
				compileAverage( _node );
				break;

			default:
				compileMapReduceAggregator( _node );

		}
	}


	private void compileMapReduceAggregator( ExpressionNodeForAggregator _node ) throws CompilerException
	{
		final Aggregator aggregator = _node.getAggregator();
		final Operator reductor = aggregator.getReductor();
		compileMapReduceAggregator( _node, reductor );
	}


	protected void compileMapReduceAggregator( ExpressionNodeForAggregator _node, Operator _reductor )
			throws CompilerException
	{
		if (null == _reductor) unsupported( _node );
		boolean first = true;
		for (ExpressionNode arg : _node.arguments()) {
			if (arg instanceof ExpressionNodeForSubSectionModel) {
				ExpressionNodeForSubSectionModel subArg = (ExpressionNodeForSubSectionModel) arg;
				compileIteration( _reductor, subArg );
			}
			else {
				compileExpr( arg );
			}
			if (first) first = false;
			else compileOperator( _reductor, 2 );
		}

		if (_node instanceof ExpressionNodeForPartialAggregation) {
			ExpressionNodeForPartialAggregation partialAggNode = (ExpressionNodeForPartialAggregation) _node;
			compileConst( partialAggNode.getPartialAggregation().accumulator );
			compileOperator( _reductor, 2 );
		}
	}


	private void compileCount( ExpressionNodeForAggregator _node )
	{
		long statics = 0;
		for (ExpressionNode arg : _node.arguments()) {
			if (!(arg instanceof ExpressionNodeForSubSectionModel)) {
				statics++;
			}
		}
		mv().push( statics );

		for (ExpressionNode arg : _node.arguments()) {
			if (arg instanceof ExpressionNodeForSubSectionModel) {
				ExpressionNodeForSubSectionModel subArg = (ExpressionNodeForSubSectionModel) arg;
				SubSectionCompiler sub = section().subSectionCompiler( subArg.getSectionModel() );

				mv().loadThis();
				section().compileCallToGetterFor( mv(), sub );
				mv().arrayLength();
				mv().visitInsn( Opcodes.I2L );
				mv().push( (long) subArg.arguments().size() );
				mv().visitInsn( Opcodes.LMUL );
				mv().visitInsn( Opcodes.LADD );

			}
		}

		if (_node instanceof ExpressionNodeForPartialAggregation) {
			ExpressionNodeForPartialAggregation partialAggNode = (ExpressionNodeForPartialAggregation) _node;
			NonNullCountingAggregation agg = (NonNullCountingAggregation) partialAggNode.getPartialAggregation();
			mv().push( (long) agg.numberOfNonNullArguments );
			mv().visitInsn( Opcodes.LADD );
		}

		numericType().compileToNum( mv(), Long.TYPE );
	}


	private void compileAverage( ExpressionNodeForAggregator _node ) throws CompilerException
	{
		compileMapReduceAggregator( _node, Operator.PLUS );
		compileCount( _node );
		numericType().compile( mv(), Operator.DIV, 2 );
	}


	private void compileIteration( Operator _reductor, ExpressionNodeForSubSectionModel _node ) throws CompilerException
	{
		compileHelpedExpr( new HelperCompilerForIteration( section(), _reductor, _node ) );
	}


	private void compileRef( CellModel _cell )
	{
		compileRef( mv(), section().cellComputation( _cell ) );
	}


	private void compileRef( ExpressionNodeForParentSectionModel _node ) throws CompilerException
	{
		final SectionCompiler section = section();
		final SectionCompiler parent = section.parentSectionCompiler();
		final NumericMethodCompiler parentExpr = parent.compileExpr( _node.arguments().get( 0 ) );

		mv().loadThis();
		mv().getField( section.classType(), ByteCodeEngineCompiler.PARENT_MEMBER_NAME, parent.classType() );
		mv().visitMethodInsn( Opcodes.INVOKEVIRTUAL, parent.classInternalName(), parentExpr.methodName(),
				parentExpr.methodDescriptor() );

	}

	private void compileRef( ExpressionNodeForSubSectionModel _node ) throws CompilerException
	{
		throw new CompilerException.ReferenceToInnerCellNotAggregated( "Cannot reference an inner cell of a section from an outer cell without aggregating it." );
	}


	private void compileHelpedExpr( HelperCompiler _compiler ) throws CompilerException
	{
		_compiler.compile();
		compileRef( mv(), _compiler.methodName() );
	}


	protected void compileLog( String _message )
	{
		mv().dup2();
		mv().push( _message );
		mv().visitMethodInsn( Opcodes.INVOKESTATIC, runtimeType().getInternalName(), "logDouble",
				"(DLjava/lang/String;)V" );
	}


	protected void unsupported( ExpressionNode _node ) throws CompilerException
	{
		throw new CompilerException.UnsupportedExpression( "The expression " + _node.describe() + " is not supported." );
	}

}
