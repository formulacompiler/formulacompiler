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
import java.util.Map;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.CompilerException;
import sej.Function;
import sej.Operator;
import sej.internal.expressions.DataType;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForAggregator;
import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.expressions.ExpressionNodeForFold;
import sej.internal.expressions.ExpressionNodeForFunction;
import sej.internal.expressions.ExpressionNodeForLet;
import sej.internal.expressions.ExpressionNodeForLetVar;
import sej.internal.expressions.ExpressionNodeForOperator;
import sej.internal.expressions.LetDictionary;
import sej.internal.model.CellModel;
import sej.internal.model.ExpressionNodeForCellModel;
import sej.internal.model.ExpressionNodeForParentSectionModel;
import sej.internal.model.ExpressionNodeForSubExpr;
import sej.internal.model.ExpressionNodeForSubSectionModel;

abstract class ExpressionCompiler
{
	protected static final Type BOOLEAN_CLASS = Type.getType( Boolean.class );
	protected static final Type BOOLEAN_TYPE = Type.BOOLEAN_TYPE;
	protected static final String BOOL2Z = "(" + BOOLEAN_CLASS.getDescriptor() + ")" + BOOLEAN_TYPE.getDescriptor();
	protected final static Type LONG_CLASS = Type.getType( Long.class );
	protected final static Type LONG_TYPE = Type.LONG_TYPE;
	protected static final String J2LONG = "(" + LONG_TYPE.getDescriptor() + ")" + LONG_CLASS.getDescriptor();
	protected static final String LONG2J = "(" + LONG_CLASS.getDescriptor() + ")" + LONG_TYPE.getDescriptor();

	protected static final Object CHAINED_FIRST_ARG = new Object();

	private static final ExpressionNode TRUENODE = new ExpressionNodeForConstantValue( Boolean.TRUE, DataType.NUMERIC );
	private static final ExpressionNode FALSENODE = new ExpressionNodeForConstantValue( Boolean.FALSE, DataType.NUMERIC );

	private final LetDictionary<Object> letDict = new LetDictionary<Object>();
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

	protected final LetDictionary<Object> letDict()
	{
		return this.letDict;
	}


	protected final int localsOffset()
	{
		return method().localsOffset();
	}

	protected final void incLocalsOffset( int _by )
	{
		method().incLocalsOffset( _by );
	}

	protected final void resetLocalsTo( int _to )
	{
		method().resetLocalsTo( _to );
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

		else if (_node instanceof ExpressionNodeForLet) {
			final ExpressionNodeForLet node = (ExpressionNodeForLet) _node;
			compileLet( node );
		}

		else if (_node instanceof ExpressionNodeForLetVar) {
			final ExpressionNodeForLetVar node = (ExpressionNodeForLetVar) _node;
			compileLetVar( node.varName() );
		}

		else if (_node instanceof ExpressionNodeForFold) {
			final ExpressionNodeForFold node = (ExpressionNodeForFold) _node;
			compileFold( node );
		}

		else if (_node instanceof ExpressionNodeForInnerFoldedObjRef) {
			final ExpressionNodeForInnerFoldedObjRef node = (ExpressionNodeForInnerFoldedObjRef) _node;
			compileInnerFoldedObjRef( node );
		}

		else if (_node instanceof ExpressionNodeForSubExpr) {
			if (_node.cardinality() != 1) {
				throw new CompilerException.UnsupportedExpression(
						"Internal error: subexpr node must have exactly one argument" );
			}
			compile( _node.argument( 0 ) );
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
				compileOperatorWithFirstArgOnStack( op, null );
				break;

			default:
				compile( args.get( 0 ) );
				for (int i = 1; i < args.size(); i++) {
					compileOperatorWithFirstArgOnStack( op, args.get( i ) );
				}

		}
	}


	protected void compileOperatorWithFirstArgOnStack( Operator _operator, ExpressionNode _secondArg )
			throws CompilerException
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


	protected void compileAggregation( ExpressionNodeForAggregator _node ) throws CompilerException
	{
		throw new CompilerException.UnsupportedExpression( "Aggregator "
				+ _node.getAggregator() + " is not supported for " + this + " engines." );
	}


	final void compileRef( CellComputation _cell )
	{
		compileRef( _cell.getMethodName() );
	}


	private final void compileRef( String _getterName, SectionCompiler _section, int _localThis )
	{
		final GeneratorAdapter mv = mv();
		mv.visitVarInsn( Opcodes.ALOAD, _localThis );
		mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, _section.classInternalName(), _getterName, typeCompiler()
				.typeGetterDesc() );
	}

	private final void compileRef( String _getterName )
	{
		compileRef( _getterName, section(), 0 );
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


	private static final class EvaluateIntoLocal
	{
		final ExpressionNode node;
		final int local;

		public EvaluateIntoLocal(int _local, ExpressionNode _node)
		{
			super();
			this.local = _local;
			this.node = _node;
		}

	}


	private final void compileLet( ExpressionNodeForLet _node ) throws CompilerException
	{
		// Note: It is important to allocate the local here rather than at the point of first
		// evaluation. Otherwise it could get reused when the first evaluation is within a local
		// scope.
		final Object oldVar = letDict().let( _node.varName(), new EvaluateIntoLocal( compileNewLocal(), _node.value() ) );
		try {
			compile( _node.in() );
		}
		finally {
			letDict().unlet( _node.varName(), oldVar );
		}
	}


	private final void compileLetVar( String _varName ) throws CompilerException
	{
		final Object val = letDict().lookup( _varName );
		if (val == CHAINED_FIRST_ARG) {
			// ignore
		}
		else if (val instanceof ExpressionNode) {
			compile( (ExpressionNode) val );
		}
		else if (val instanceof EvaluateIntoLocal) {
			final EvaluateIntoLocal eval = (EvaluateIntoLocal) val;
			compile( eval.node );
			compileDup();
			compileStoreLocal( eval.local );
			letDict().set( _varName, eval.local );
		}
		else if (val instanceof Integer) {
			compileLoadLocal( (Integer) val );
		}
		else {
			throw new CompilerException.NameNotFound( "The variable " + _varName + " is not bound in this context." );
		}
	}

	protected final int compileStoreToNewLocal()
	{
		final int local = compileNewLocal();
		compileStoreLocal( local );
		return local;
	}

	protected final int compileDupAndStoreToNewLocal()
	{
		compileDup();
		return compileStoreToNewLocal();
	}

	protected final int compileNewLocal()
	{
		return method().newLocal( type().getSize() );
	}

	protected void compileDup()
	{
		mv().visitInsn( Opcodes.DUP );
	}

	protected final void compileStoreLocal( int _local )
	{
		mv().visitVarInsn( type().getOpcode( Opcodes.ISTORE ), _local );
	}

	protected final void compileLoadLocal( int _local )
	{
		mv().visitVarInsn( type().getOpcode( Opcodes.ILOAD ), _local );
	}


	final void copyAndForcePendingLetsFrom( LetDictionary<Object> _lets ) throws CompilerException
	{
		for (final Map.Entry<String, Object> let : _lets.entrySet()) {
			final String letName = let.getKey();
			final Object letValue = let.getValue();
			if (letValue instanceof EvaluateIntoLocal) {
				final ExpressionNode pendingNode = ((EvaluateIntoLocal) letValue).node;
				compile( pendingNode );
				letDict().set( letName, compileDupAndStoreToNewLocal() );
			}
		}
	}


	private final void compileFold( ExpressionNodeForFold _node ) throws CompilerException
	{
		final FoldContext foldContext = new FoldContext( _node, section() );
		if (isSubSectionIn( _node.elements() )) {
			compileHelpedExpr( new HelperCompilerForIterativeFold( section(), _node.elements(), foldContext, letDict() ) );
		}
		else {
			// LATER For folds that support this, we could skip the initial value in favour of the
			// first element
			compile( foldContext.node.initialAccumulatorValue() );
			compileChainedFoldOverNonRepeatingElements( foldContext, foldContext.node.elements() );
		}
	}

	final boolean isSubSectionIn( Iterable<ExpressionNode> _elts )
	{
		for (ExpressionNode elt : _elts) {
			if (elt instanceof ExpressionNodeForSubSectionModel) return true;
		}
		return false;
	}

	final void compileChainedFoldOverNonRepeatingElements( FoldContext _context, Iterable<ExpressionNode> _elts )
			throws CompilerException
	{
		final String accName = _context.node.accumulatorName();
		final Object accOld = letDict().let( accName, CHAINED_FIRST_ARG );
		try {
			final int reuseLocalsAt = localsOffset();
			for (final ExpressionNode elt : _elts) {
				if (elt instanceof ExpressionNodeForSubExpr) {
					compileChainedFoldOverNonRepeatingElements( _context, elt.arguments() );
				}
				else if (!(elt instanceof ExpressionNodeForSubSectionModel)) {
					resetLocalsTo( reuseLocalsAt );
					compileElementFold( _context, elt );
				}
			}
		}
		finally {
			letDict().unlet( accName, accOld );
		}
	}

	final void compileElementFold( FoldContext _context, ExpressionNode _elt ) throws CompilerException
	{
		final String eltName = _context.node.elementName();
		final ExpressionNode eltBinding = (_context.localThis == 0) ? _elt : new ExpressionNodeForInnerFoldedObjRef(
				_context, _elt );
		final Object eltOld = letDict().let( eltName, eltBinding );
		try {
			compile( _context.node.accumulatingStep() );
		}
		finally {
			letDict().unlet( eltName, eltOld );
		}
	}


	private final void compileInnerFoldedObjRef( ExpressionNodeForInnerFoldedObjRef _node ) throws CompilerException
	{
		final ExpressionNode ref = _node.argument( 0 );
		if (ref instanceof ExpressionNodeForConstantValue) {
			compile( ref );
		}
		else {
			String getterName;
			if (ref instanceof ExpressionNodeForCellModel) {
				final ExpressionNodeForCellModel cellRef = (ExpressionNodeForCellModel) ref;
				final CellComputation cellMethod = _node.context().section.cellComputation( cellRef.getCellModel() );
				getterName = cellMethod.getMethodName();
			}
			else {
				final ValueMethodCompiler exprMethod = _node.context().section.compileMethodForExpression( ref );
				getterName = exprMethod.methodName();
			}
			compileRef( getterName, _node.context().section, _node.context().localThis );
		}
	}


	final void compileHelpedExpr( HelperCompiler _compiler ) throws CompilerException
	{
		_compiler.compile();
		compileRef( _compiler.methodName() );
	}


	protected final void compileRuntimeMethod( String _methodName, String _methodSig )
	{
		typeCompiler().compileRuntimeMethod( mv(), _methodName, _methodSig );
	}


	protected static interface ForEachElementCompilation
	{
		void compile( int _xi ) throws CompilerException;
	}

	protected abstract void compile_scanArray( ForEachElementCompilation _forElement ) throws CompilerException;

}
