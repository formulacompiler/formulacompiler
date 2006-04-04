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
import java.util.Date;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.CallFrame;
import sej.ModelError;
import sej.engine.Runtime_v1;
import sej.engine.compiler.model.CellModel;
import sej.engine.compiler.model.ExpressionNodeForCellModel;
import sej.engine.compiler.model.ExpressionNodeForParentSectionModel;
import sej.engine.compiler.model.ExpressionNodeForSubSectionModel;
import sej.engine.expressions.Aggregator;
import sej.engine.expressions.ExpressionNode;
import sej.engine.expressions.ExpressionNodeForAggregator;
import sej.engine.expressions.ExpressionNodeForConstantValue;
import sej.engine.expressions.ExpressionNodeForFunction;
import sej.engine.expressions.ExpressionNodeForIf;
import sej.engine.expressions.ExpressionNodeForOperator;
import sej.engine.expressions.Operator;


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
		this.mv = new GeneratorAdapter( cw().visitMethod( Opcodes.ACC_FINAL, getMethodName(), "()D", null, null ),
				Opcodes.ACC_FINAL, getMethodName(), "()D" );
	}


	public ByteCodeSectionCompiler getSection()
	{
		return this.section;
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
		mv().visitInsn( Opcodes.DRETURN );
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
		_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, getSection().engine.getInternalName(), _getterName, "()D" );
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
		if (null == _constantValue) {
			mv().visitInsn( Opcodes.DCONST_0 );
		}
		else if (_constantValue instanceof Number) {
			double val = ((Number) _constantValue).doubleValue();
			mv().push( val );
		}
		else if (_constantValue instanceof Boolean) {
			double val = ((Boolean) _constantValue) ? 1 : 0;
			mv().push( val );
		}
		else if (_constantValue instanceof Date) {
			Date date = (Date) _constantValue;
			double val = Runtime_v1.dateToExcel( date );
			mv().push( val );
		}
		else {
			throw new ModelError.UnsupportedDataType( "The data type "
					+ _constantValue.getClass().getName() + " is not supported for constant " + _constantValue.toString() );
		}
	}


	protected void compileInput( CallFrame _callChainToCall )
	{
		final CallFrame[] frames = _callChainToCall.getFrames();

		mv().loadThis();
		mv().getField( getSection().engine, ByteCodeCompiler.InputsMemberName, getSection().inputs );

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
			mv().visitMethodInsn( Opcodes.INVOKESTATIC, ByteCodeCompiler.Runtime.getInternalName(), "dateToExcel",
					"(Ljava/util/Date;)D" );
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
		if (Operator.MINUS == _operator && 1 == _numberOfArguments) {
			mv().visitInsn( Opcodes.DNEG );
		}
		else {
			_operator.compileTo( mv() );
		}
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
		final String args = "DDDDDDDDD";
		for (ExpressionNode arg : _node.getArguments()) {
			compileExpr( arg );
		}
		mv().visitMethodInsn( Opcodes.INVOKESTATIC, ByteCodeCompiler.Runtime.getInternalName(),
				"std" + _node.getFunction().getName(), "(" + args.substring( 0, _node.getArguments().size() ) + ")D" );
	}


	private void compileIf( ExpressionNodeForIf _node ) throws ModelError
	{
		final Label notMet = mv().newLabel();
		final Label done = mv().newLabel();
		
		compileTest( _node.getArguments().get(0), false, notMet );

		compileExpr( _node.getArguments().get( 1 ) );

		mv().visitJumpInsn( Opcodes.GOTO, done );
		mv().mark( notMet );
		
		compileExpr( _node.getArguments().get( 2 ) );
		
		mv().mark( done );
	}
	
	
	private void compileTest( ExpressionNode _node, boolean _branchWhenResultIs, Label _branchTo ) throws ModelError
	{
		unsupported( _node );
	}


	private boolean compileDirectIf( ExpressionNodeForIf _node ) throws ModelError
	{
		final ExpressionNode firstArg = _node.getArguments().get( 0 );
		if (firstArg instanceof ExpressionNodeForOperator) {
			final ExpressionNodeForOperator firstOp = (ExpressionNodeForOperator) firstArg;
			final Operator operator = firstOp.getOperator();

			int compOpcode = Opcodes.DCMPL;
			int testOpcode;
			if (Operator.EQUAL == operator) testOpcode = Opcodes.IFNE;
			else if (Operator.NOTEQUAL == operator) testOpcode = Opcodes.IFEQ;
			else if (Operator.GREATER == operator) testOpcode = Opcodes.IFLE;
			else if (Operator.GREATEROREQUAL == operator) testOpcode = Opcodes.IFLT;
			else if (Operator.LESS == operator) {
				testOpcode = Opcodes.IFGE;
				compOpcode = Opcodes.DCMPG;
			}
			else if (Operator.LESSOREQUAL == operator) {
				testOpcode = Opcodes.IFGT;
				compOpcode = Opcodes.DCMPG;
			}
			else return false;

			compileExpr( firstArg.getArguments().get( 0 ) );
			compileExpr( firstArg.getArguments().get( 1 ) );

			Label notMet = new Label();
			Label done = new Label();
			mv().visitInsn( compOpcode );
			mv().visitJumpInsn( testOpcode, notMet );
			compileExpr( _node.getArguments().get( 1 ) );
			mv().visitJumpInsn( Opcodes.GOTO, done );
			mv().visitLabel( notMet );
			compileExpr( _node.getArguments().get( 2 ) );
			mv().visitLabel( done );

			return true;
		}
		return false;
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
		if (null == reductor) unsupported( _node );

		boolean first = true;
		for (ExpressionNode arg : _node.getArguments()) {
			if (arg instanceof ExpressionNodeForSubSectionModel) {
				ExpressionNodeForSubSectionModel subArg = (ExpressionNodeForSubSectionModel) arg;
				compileIteration( reductor, subArg );
			}
			else {
				compileExpr( arg );
			}
			if (first) first = false;
			else compileOperator( reductor, 2 );
		}
		Aggregator.Aggregation aggregation = _node.getPartialAggregation();
		if (null != aggregation) {
			compileConst( aggregation.getResult() );
			compileOperator( reductor, 2 );
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
		mv().visitMethodInsn( Opcodes.INVOKESTATIC, ByteCodeCompiler.Runtime.getInternalName(), "logDouble",
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
