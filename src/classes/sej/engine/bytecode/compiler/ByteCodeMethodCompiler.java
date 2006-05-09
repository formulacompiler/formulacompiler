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
package sej.engine.bytecode.compiler;

import java.lang.reflect.Method;
import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.CallFrame;
import sej.ModelError;
import sej.engine.compiler.model.CellModel;
import sej.engine.compiler.model.ExpressionNodeForCellModel;
import sej.engine.compiler.model.ExpressionNodeForParentSectionModel;
import sej.engine.compiler.model.ExpressionNodeForPartialAggregation;
import sej.engine.compiler.model.ExpressionNodeForSubSectionModel;
import sej.expressions.Aggregator;
import sej.expressions.ExpressionNode;
import sej.expressions.ExpressionNodeForAggregator;
import sej.expressions.ExpressionNodeForConstantValue;
import sej.expressions.ExpressionNodeForFunction;
import sej.expressions.ExpressionNodeForIf;
import sej.expressions.ExpressionNodeForOperator;
import sej.expressions.Function;
import sej.expressions.Operator;


public abstract class ByteCodeMethodCompiler
{
	private final ByteCodeSectionCompiler section;
	private final String methodName;
	private final GeneratorAdapter mv;


	public ByteCodeMethodCompiler(ByteCodeSectionCompiler _section, String _methodName)
	{
		super();
		this.section = _section;
		this.methodName = _methodName;
		this.mv = newAdapter();
	}


	private GeneratorAdapter newAdapter()
	{
		final String name = getMethodName();
		final String signature = "()" + getNumericType().getDescriptor();
		final int access = Opcodes.ACC_FINAL | Opcodes.ACC_PRIVATE;
		return new GeneratorAdapter( cw().visitMethod( access, name, signature, null, null ), access, name, signature );
	}


	public ByteCodeSectionCompiler getSection()
	{
		return this.section;
	}


	public ByteCodeNumericType getNumericType()
	{
		return getSection().getNumericType();
	}


	protected Type getRuntimeType()
	{
		return getNumericType().getRuntimeType();
	}


	public String getMethodName()
	{
		return this.methodName;
	}


	protected ClassWriter cw()
	{
		return getSection().cw();
	}


	protected GeneratorAdapter mv()
	{
		return this.mv;
	}


	public void compile() throws ModelError
	{
		beginCompilation();
		compileBody();
		endCompilation();
	}


	private void beginCompilation()
	{
		mv().visitCode();
	}


	private void endCompilation()
	{
		mv().visitInsn( getNumericType().getReturnOpcode() );
		mv().endMethod();
		mv().visitEnd();
	}


	protected abstract void compileBody() throws ModelError;


	protected void compileRef( MethodVisitor _mv, ByteCodeCellComputation _cell )
	{
		compileRef( _mv, _cell.getMethodName() );
	}

	protected void compileRef( MethodVisitor _mv, String _getterName )
	{
		_mv.visitVarInsn( Opcodes.ALOAD, 0 );
		_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, getSection().engine.getInternalName(), _getterName, "()"
				+ getNumericType().getDescriptor() );
	}


	protected void pushConstParam( Object _constantValue )
	{
		if (null == _constantValue) {
			mv().visitInsn( Opcodes.ACONST_NULL );
		}
		else if (_constantValue instanceof Double) {
			double val = (Double) _constantValue;
			mv().push( val );
		}
		else if (_constantValue instanceof Integer) {
			int val = (Integer) _constantValue;
			mv().push( val );
		}
		else if (_constantValue instanceof Long) {
			long val = (Long) _constantValue;
			mv().push( val );
		}
		else if (_constantValue instanceof Boolean) {
			boolean val = (Boolean) _constantValue;
			mv().push( val );
		}
		else {
			mv().visitLdcInsn( _constantValue );
		}
	}


	protected void compileConst( Object _constantValue ) throws ModelError
	{
		getNumericType().compileConst( mv(), _constantValue );
	}


	protected void compileInput( CallFrame _callChainToCall )
	{
		final CallFrame[] frames = _callChainToCall.getFrames();

		mv().loadThis();
		mv().getField( getSection().engine, ByteCodeCompiler.INPUTS_MEMBER_NAME, getSection().inputs );

		Class contextClass = getSection().getInputs();
		for (CallFrame frame : frames) {
			final Method method = frame.getMethod();
			if (null != frame.getArgs()) {
				for (Object arg : frame.getArgs()) {
					pushConstParam( arg );
				}
			}
			int opcode = Opcodes.INVOKEVIRTUAL;
			if (contextClass.isInterface()) opcode = Opcodes.INVOKEINTERFACE;

			mv().visitMethodInsn( opcode, Type.getType( contextClass ).getInternalName(), method.getName(),
					Type.getMethodDescriptor( method ) );

			contextClass = method.getReturnType();
		}

		if (java.util.Date.class == contextClass) {
			mv().visitMethodInsn( Opcodes.INVOKESTATIC, getRuntimeType().getInternalName(), "dateToExcel",
					"(Ljava/util/Date;)D" );
		}
		else if (Boolean.TYPE == contextClass) {
			mv().visitMethodInsn( Opcodes.INVOKESTATIC, getRuntimeType().getInternalName(), "booleanToExcel", "(Z)" + getNumericType().getDescriptor() );
		}
	}


	protected void compileExpr( ExpressionNode _node ) throws ModelError
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
			compileOperator( node );
		}

		else if (_node instanceof ExpressionNodeForIf) { // must be before functions!
			final ExpressionNodeForIf node = (ExpressionNodeForIf) _node;
			compileIf( node );
		}

		else if (_node instanceof ExpressionNodeForFunction) {
			final ExpressionNodeForFunction node = (ExpressionNodeForFunction) _node;
			compileFunction( node );
		}

		else if (_node instanceof ExpressionNodeForAggregator) {
			final ExpressionNodeForAggregator node = (ExpressionNodeForAggregator) _node;
			compileAggregation( node );
		}

		else {
			unsupported( _node );
		}

	}


	private void compileOperator( ExpressionNodeForOperator _node ) throws ModelError
	{
		for (ExpressionNode arg : _node.getArguments()) {
			compileExpr( arg );
		}
		compileOperator( _node.getOperator(), _node.getArguments().size() );
	}


	private void compileOperator( Operator _operator, int _numberOfArguments ) throws ModelError
	{
		getNumericType().compile( mv(), _operator, _numberOfArguments );
	}


	private void compileFunction( ExpressionNodeForFunction _node ) throws ModelError
	{
		switch (_node.getFunction()) {

			case ROUND:
				compileStdFunction( _node );
				break;

			// TODO case INDEX:
			// compileHelpedExpr( new ByteCodeHelperCompilerForIndex( getSection(), _node ) );
			// break;

			default:
				unsupported( _node );
		}
	}


	private void compileStdFunction( ExpressionNodeForFunction _node ) throws ModelError
	{
		final StringBuilder typeBuilder = new StringBuilder();
		typeBuilder.append( "(" );

		for (ExpressionNode arg : _node.getArguments()) {
			compileExpr( arg );
			typeBuilder.append( getNumericType().getDescriptor() );
		}

		typeBuilder.append( ")" );
		typeBuilder.append( getNumericType().getDescriptor() );

		mv().visitMethodInsn( Opcodes.INVOKESTATIC, getRuntimeType().getInternalName(),
				"std" + _node.getFunction().getName(), typeBuilder.toString() );
	}


	private void compileIf( ExpressionNodeForIf _node ) throws ModelError
	{
		final Label notMet = mv().newLabel();
		final Label done = mv().newLabel();

		new TestCompilerBranchingWhenFalse( _node.getArguments().get( 0 ), notMet ).compile();

		compileExpr( _node.getArguments().get( 1 ) );

		mv().visitJumpInsn( Opcodes.GOTO, done );
		mv().mark( notMet );

		compileExpr( _node.getArguments().get( 2 ) );

		mv().mark( done );
	}


	private abstract class TestCompiler
	{
		protected ExpressionNode node;
		protected Label branchTo;

		public TestCompiler(ExpressionNode _node, Label _branchTo)
		{
			super();
			this.node = _node;
			this.branchTo = _branchTo;
		}

		void compile() throws ModelError
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
						compileExpr( this.node.getArguments().get( 0 ) );
						compileExpr( this.node.getArguments().get( 1 ) );
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

			if (this.node instanceof ExpressionNodeForFunction) {
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
		protected abstract void compileAnd() throws ModelError;
		protected abstract void compileOr() throws ModelError;
		protected abstract void compileComparison( Operator _comparison ) throws ModelError;

		protected void compileComparison( int _ifOpcode, int _comparisonOpcode )
		{
			getNumericType().compileComparison( mv(), _comparisonOpcode );
			mv().visitJumpInsn( _ifOpcode, this.branchTo );
		}

		private void compileNot() throws ModelError
		{
			final List<ExpressionNode> args = this.node.getArguments();
			if (0 < args.size()) {
				newInverseCompiler( args.get( 0 ), this.branchTo ).compile();
			}
			else {
				unsupported( this.node );
			}
		}

		void compileValue() throws ModelError
		{
			compileExpr( this.node );
			getNumericType().compileZero( mv() );
			compileComparison( Operator.NOTEQUAL );
		}

		protected abstract void compileBooleanTest() throws ModelError;
	}


	private class TestCompilerBranchingWhenFalse extends TestCompiler
	{

		public TestCompilerBranchingWhenFalse(ExpressionNode _node, Label _branchTo)
		{
			super( _node, _branchTo );
		}

		@Override
		protected void compileComparison( Operator _comparison ) throws ModelError
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
		protected void compileOr() throws ModelError
		{
			final Label met = mv().newLabel();
			final int nArg = this.node.getArguments().size();
			int iArg = 0;
			while (iArg < nArg - 1) {
				final ExpressionNode arg = this.node.getArguments().get( iArg );
				new TestCompilerBranchingWhenTrue( arg, met ).compile();
				iArg++;
			}
			final ExpressionNode lastArg = this.node.getArguments().get( iArg );
			new TestCompilerBranchingWhenFalse( lastArg, this.branchTo ).compile();
			mv().mark( met );
		}

		@Override
		protected void compileAnd() throws ModelError
		{
			for (ExpressionNode arg : this.node.getArguments()) {
				new TestCompilerBranchingWhenFalse( arg, this.branchTo ).compile();
			}
		}

		@Override
		protected void compileBooleanTest() throws ModelError
		{
			mv().visitJumpInsn( Opcodes.IFEQ, this.branchTo );
		}
	}


	private class TestCompilerBranchingWhenTrue extends TestCompiler
	{

		public TestCompilerBranchingWhenTrue(ExpressionNode _node, Label _branchTo)
		{
			super( _node, _branchTo );
		}

		@Override
		protected void compileComparison( Operator _comparison ) throws ModelError
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
		protected void compileOr() throws ModelError
		{
			for (ExpressionNode arg : this.node.getArguments()) {
				new TestCompilerBranchingWhenTrue( arg, this.branchTo ).compile();
			}
		}

		@Override
		protected void compileAnd() throws ModelError
		{
			final Label notMet = mv().newLabel();
			final int nArg = this.node.getArguments().size();
			int iArg = 0;
			while (iArg < nArg - 1) {
				final ExpressionNode arg = this.node.getArguments().get( iArg );
				new TestCompilerBranchingWhenFalse( arg, notMet ).compile();
				iArg++;
			}
			final ExpressionNode lastArg = this.node.getArguments().get( iArg );
			new TestCompilerBranchingWhenTrue( lastArg, this.branchTo ).compile();
			mv().mark( notMet );
		}

		@Override
		protected void compileBooleanTest() throws ModelError
		{
			mv().visitJumpInsn( Opcodes.IFNE, this.branchTo );
		}
	}


	private void compileAggregation( ExpressionNodeForAggregator _node ) throws ModelError
	{
		final Aggregator aggregator = _node.getAggregator();

		switch (aggregator) {

			case AVERAGE:
				compileHelpedExpr( new ByteCodeHelperCompilerForAverage( getSection(), _node ) );
				break;

			default:
				compileMapReduceAggregator( _node );

		}
	}


	private void compileMapReduceAggregator( ExpressionNodeForAggregator _node ) throws ModelError
	{
		final Aggregator aggregator = _node.getAggregator();
		final Operator reductor = aggregator.getReductor();
		compileMapReduceAggregator( _node, reductor );
	}


	protected void compileMapReduceAggregator( ExpressionNodeForAggregator _node, final Operator _reductor ) throws ModelError
	{
		if (null == _reductor) unsupported( _node );
		boolean first = true;
		for (ExpressionNode arg : _node.getArguments()) {
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


	private void compileIteration( Operator _reductor, ExpressionNodeForSubSectionModel _node ) throws ModelError
	{
		// TODO compileIteration
		unsupported( _node );
	}


	private void compileRef( CellModel _cell )
	{
		compileRef( mv(), getSection().getCellComputation( _cell ) );
	}


	private void compileRef( ExpressionNodeForParentSectionModel _node ) throws ModelError
	{
		// TODO compileRef
		unsupported( _node );
	}


	private void compileRef( ExpressionNodeForSubSectionModel _node ) throws ModelError
	{
		// TODO compileRef
		unsupported( _node );
	}


	private void compileHelpedExpr( ByteCodeHelperCompiler _compiler ) throws ModelError
	{
		_compiler.compile();
		compileRef( mv(), _compiler.getMethodName() );
	}


	protected void compileLog( String _message )
	{
		mv().dup2();
		mv().push( _message );
		mv().visitMethodInsn( Opcodes.INVOKESTATIC, getRuntimeType().getInternalName(), "logDouble",
				"(DLjava/lang/String;)V" );
	}


	protected void unsupported( ExpressionNode _node ) throws ModelError
	{
		throw new ModelError.UnsupportedExpression( "The expression " + _node.describe() + " is not supported." );
	}


	protected boolean isNull( ExpressionNode _node )
	{
		if (null == _node) return true;
		if (_node instanceof ExpressionNodeForConstantValue) {
			ExpressionNodeForConstantValue constNode = (ExpressionNodeForConstantValue) _node;
			return (null == constNode.getValue());
		}
		return false;
	}

}
