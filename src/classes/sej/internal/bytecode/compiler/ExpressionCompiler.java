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
import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.Aggregator;
import sej.CompilerException;
import sej.Function;
import sej.Operator;
import sej.internal.expressions.DataType;
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

abstract class ExpressionCompiler
{
	protected static final Type BOOLEAN_CLASS = Type.getType( Boolean.class );
	protected static final Type BOOLEAN_TYPE = Type.BOOLEAN_TYPE;
	protected static final String BOOL2Z = "(" + BOOLEAN_CLASS.getDescriptor() + ")" + BOOLEAN_TYPE.getDescriptor();
	protected final static Type LONG_CLASS = Type.getType( Long.class );
	protected final static Type LONG_TYPE = Type.LONG_TYPE;
	protected static final String J2LONG = "(" + LONG_TYPE.getDescriptor() + ")" + LONG_CLASS.getDescriptor();
	protected static final String LONG2J = "(" + LONG_CLASS.getDescriptor() + ")" + LONG_TYPE.getDescriptor();

	private static final ExpressionNode TRUENODE = new ExpressionNodeForConstantValue( Boolean.TRUE, DataType.NUMERIC );
	private static final ExpressionNode FALSENODE = new ExpressionNodeForConstantValue( Boolean.FALSE, DataType.NUMERIC );

	private final MethodCompiler methodCompiler;
	private final GeneratorAdapter mv;

	ExpressionCompiler(MethodCompiler _methodCompiler)
	{
		super();
		this.methodCompiler = _methodCompiler;
		this.mv = _methodCompiler.mv();
	}

	protected final MethodCompiler method()
	{
		return this.methodCompiler;
	}

	protected final SectionCompiler section()
	{
		return method().section();
	}

	protected abstract TypeCompiler typeCompiler();

	protected final DataType dataType()
	{
		return typeCompiler().dataType();
	}

	protected final Type runtimeType()
	{
		return typeCompiler().runtimeType();
	}

	protected final Type type()
	{
		return typeCompiler().type();
	}

	protected final String typeDescriptor()
	{
		return typeCompiler().typeDescriptor();
	}

	protected final GeneratorAdapter mv()
	{
		return this.mv;
	}


	protected final void compile( ExpressionNode _node ) throws CompilerException
	{
		try {
			compileInner( _node );
		}
		catch (InnerExpressionException e) {
			throw e;
		}
		catch (CompilerException e) {
			throw new InnerExpressionException( _node, e );
		}
	}

	private final void compileInner( ExpressionNode _node ) throws CompilerException
	{
		if (null == _node) {
			mv().visitInsn( Opcodes.ACONST_NULL );
			return;
		}

		final DataType nodeType = _node.getDataType();

		if (null == nodeType) {
			throw new CompilerException.UnsupportedDataType( "Internal error: Data type not set on node "
					+ _node.describe() + "." );
		}

		else if (DataType.NULL != nodeType && dataType() != nodeType) {
			method().expressionCompiler( nodeType ).compile( _node );
			compileConversionFrom( nodeType );
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
			throw new CompilerException.UnsupportedExpression( "Internal error: unsupported node type "
					+ _node.describe() + "." );
		}
	}


	private final boolean needsIf( Operator _operator )
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


	protected final void compileConst( Object _value ) throws CompilerException
	{
		typeCompiler().compileConst( mv(), _value );
	}


	protected void compileOperator( ExpressionNodeForOperator _node ) throws CompilerException
	{
		final List<ExpressionNode> args = _node.arguments();
		final Operator op = _node.getOperator();
		switch (args.size()) {

			case 0:
				throw new CompilerException.UnsupportedExpression(
						"Internal error: must have at least one argument for operator node " + _node.describe() + "." );

			case 1:
				compile( args.get( 0 ) );
				compileOperator( op, 1 );
				break;

			default:
				compile( args.get( 0 ) );
				for (int i = 1; i < args.size(); i++) {
					compile( args.get( i ) );
					compileOperator( op, 2 );
				}

		}
	}


	protected void compileOperator( Operator _operator, int _numberOfArguments ) throws CompilerException
	{
		throw new CompilerException.UnsupportedExpression( "Operator "
				+ _operator + " is not supported for " + this + " engines." );
	}


	protected abstract void compileComparison( int _comparisonOpcode ) throws CompilerException;


	protected final void compileZero() throws CompilerException
	{
		typeCompiler().compileZero( mv() );
	}


	protected void compileConversionFrom( Class _class ) throws CompilerException
	{
		throw new CompilerException.UnsupportedDataType( "Cannot convert from a "
				+ _class.getName() + " to a " + this + "." );
	}


	protected void compileConversionFrom( DataType _type ) throws CompilerException
	{
		throw new CompilerException.UnsupportedDataType( "Cannot convert from a " + _type + " to a " + this + "." );
	}


	protected final void compileConversionFromResultOf( Method _method ) throws CompilerException
	{
		try {
			innerCompileConversionFromResultOf( _method );
		}
		catch (CompilerException e) {
			e.addMessageContext( "\nCaused by return type of input '" + _method + "'." );
			throw e;
		}
	}

	protected abstract void innerCompileConversionFromResultOf( Method _method ) throws CompilerException;


	protected void compileConversionTo( Class _class ) throws CompilerException
	{
		throw new CompilerException.UnsupportedDataType( "Cannot convert from a "
				+ this + " to a " + _class.getName() + "." );
	}


	protected final void compileConversionToResultOf( Method _method ) throws CompilerException
	{
		try {
			innerCompileConversionToResultOf( _method );
		}
		catch (CompilerException e) {
			e.addMessageContext( "\nCaused by return type of input '" + _method + "'." );
			throw e;
		}
	}

	protected abstract void innerCompileConversionToResultOf( Method _method ) throws CompilerException;


	protected void compileFunction( ExpressionNodeForFunction _node ) throws CompilerException
	{
		final Function fun = _node.getFunction();
		switch (fun) {

			case INDEX:
				compileHelpedExpr( new HelperCompilerForIndex( section(), _node ) );
				break;

			default:
				throw new CompilerException.UnsupportedExpression( "Function "
						+ fun + " is not supported for " + this + " engines." );
		}
	}


	private final void compileIf( ExpressionNodeForFunction _node ) throws CompilerException
	{
		compileIf( _node.arguments().get( 0 ), _node.arguments().get( 1 ), _node.arguments().get( 2 ) );
	}


	private final void compileIf( ExpressionNode _test, ExpressionNode _ifTrue, ExpressionNode _ifFalse )
			throws CompilerException
	{
		final GeneratorAdapter mv = mv();
		final Label notMet = mv.newLabel();
		final Label done = mv.newLabel();

		method().numericCompiler().compileTest( _test, notMet );

		compile( _ifTrue );

		mv.visitJumpInsn( Opcodes.GOTO, done );
		mv.mark( notMet );

		compile( _ifFalse );

		mv.mark( done );
	}


	private final void compileAggregation( ExpressionNodeForAggregator _node ) throws CompilerException
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


	private final void compileMapReduceAggregator( ExpressionNodeForAggregator _node ) throws CompilerException
	{
		final Aggregator aggregator = _node.getAggregator();
		final Operator reductor = aggregator.getReductor();
		compileMapReduceAggregator( _node, reductor );
	}


	protected final void compileMapReduceAggregator( ExpressionNodeForAggregator _node, Operator _reductor )
			throws CompilerException
	{
		if (null == _reductor)
			throw new CompilerException.UnsupportedExpression( "Internal error: No MapReduce reductor for aggregation "
					+ _node.describe() + "." );
		boolean first = true;
		for (ExpressionNode arg : _node.arguments()) {
			if (arg instanceof ExpressionNodeForSubSectionModel) {
				ExpressionNodeForSubSectionModel subArg = (ExpressionNodeForSubSectionModel) arg;
				compileIteration( _reductor, subArg );
			}
			else {
				compile( arg );
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


	private final void compileCount( ExpressionNodeForAggregator _node ) throws CompilerException
	{
		final GeneratorAdapter mv = mv();

		long statics = 0;
		for (ExpressionNode arg : _node.arguments()) {
			if (!(arg instanceof ExpressionNodeForSubSectionModel)) {
				statics++;
			}
		}
		mv.push( statics );

		for (ExpressionNode arg : _node.arguments()) {
			if (arg instanceof ExpressionNodeForSubSectionModel) {
				ExpressionNodeForSubSectionModel subArg = (ExpressionNodeForSubSectionModel) arg;
				SubSectionCompiler sub = section().subSectionCompiler( subArg.getSectionModel() );

				mv.loadThis();
				section().compileCallToGetterFor( mv, sub );
				mv.arrayLength();
				mv.visitInsn( Opcodes.I2L );
				mv.push( (long) subArg.arguments().size() );
				mv.visitInsn( Opcodes.LMUL );
				mv.visitInsn( Opcodes.LADD );

			}
		}

		if (_node instanceof ExpressionNodeForPartialAggregation) {
			ExpressionNodeForPartialAggregation partialAggNode = (ExpressionNodeForPartialAggregation) _node;
			NonNullCountingAggregation agg = (NonNullCountingAggregation) partialAggNode.getPartialAggregation();
			mv.push( (long) agg.numberOfNonNullArguments );
			mv.visitInsn( Opcodes.LADD );
		}

		compileConversionFrom( Long.TYPE );
	}


	private final void compileAverage( ExpressionNodeForAggregator _node ) throws CompilerException
	{
		compileMapReduceAggregator( _node, Operator.PLUS );
		compileCount( _node );
		compileOperator( Operator.DIV, 2 );
	}


	private final void compileIteration( Operator _reductor, ExpressionNodeForSubSectionModel _node )
			throws CompilerException
	{
		compileHelpedExpr( new HelperCompilerForIteration( section(), _reductor, _node ) );
	}


	final void compileRef( CellComputation _cell )
	{
		compileRef( _cell.getMethodName() );
	}


	private final void compileRef( String _getterName )
	{
		final GeneratorAdapter mv = mv();
		mv.visitVarInsn( Opcodes.ALOAD, 0 );
		mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, section().classInternalName(), _getterName, "()" + typeDescriptor() );
	}


	private final void compileRef( CellModel _cell )
	{
		compileRef( section().cellComputation( _cell ) );
	}


	private final void compileRef( ExpressionNodeForParentSectionModel _node ) throws CompilerException
	{
		final SectionCompiler section = section();
		final SectionCompiler parent = section.parentSectionCompiler();
		final MethodCompiler parentExpr = parent.compileMethodForExpression( _node.arguments().get( 0 ) );

		final GeneratorAdapter mv = mv();
		mv.loadThis();
		mv.getField( section.classType(), ByteCodeEngineCompiler.PARENT_MEMBER_NAME, parent.classType() );
		mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, parent.classInternalName(), parentExpr.methodName(), parentExpr
				.methodDescriptor() );

	}

	private final void compileRef( ExpressionNodeForSubSectionModel _node ) throws CompilerException
	{
		throw new CompilerException.ReferenceToInnerCellNotAggregated(
				"Cannot reference an inner cell of a section from an outer cell without aggregating it." );
	}


	protected final void compileHelpedExpr( HelperCompiler _compiler ) throws CompilerException
	{
		_compiler.compile();
		compileRef( _compiler.methodName() );
	}


	protected final void compileRuntimeMethod( String _methodName, String _methodSig )
	{
		typeCompiler().compileRuntimeMethod( mv(), _methodName, _methodSig );
	}

}
