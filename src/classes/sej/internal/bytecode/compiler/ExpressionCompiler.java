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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.CompilerException;
import sej.Function;
import sej.Operator;
import sej.internal.expressions.DataType;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.expressions.ExpressionNodeForFold;
import sej.internal.expressions.ExpressionNodeForFold1st;
import sej.internal.expressions.ExpressionNodeForFunction;
import sej.internal.expressions.ExpressionNodeForLet;
import sej.internal.expressions.ExpressionNodeForLetVar;
import sej.internal.expressions.ExpressionNodeForOperator;
import sej.internal.expressions.LetDictionary;
import sej.internal.expressions.LetDictionary.LetEntry;
import sej.internal.model.CellModel;
import sej.internal.model.ExpressionNodeForCellModel;
import sej.internal.model.ExpressionNodeForParentSectionModel;
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

	protected final SectionCompiler sectionInContext()
	{
		return method().sectionInContext();
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

	protected final LetDictionary letDict()
	{
		return method().letDict();
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

				case AND:
				case OR:
					compileIf( node, TRUENODE, FALSENODE );
					break;

				default:
					compileFunction( node );
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

		else if (_node instanceof ExpressionNodeForFold1st) {
			final ExpressionNodeForFold1st node = (ExpressionNodeForFold1st) _node;
			compileFold1st( node );
		}

		else {
			throw new CompilerException.UnsupportedExpression( "Internal error: unsupported node type "
					+ _node.describe() + "." );
		}
	}


	private final boolean needsIf( Operator _operator )
	{
		switch (_operator) {
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
				Iterable<LetEntry> closure = closureOf( _node );
				compileHelpedExpr( new HelperCompilerForIndex( sectionInContext(), _node, closure ), closure );
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
		compileRef( _getterName, method().sectionInContext(), method().objectInContext() );
	}


	private final void compileRef( CellModel _cell )
	{
		compileRef( sectionInContext().cellComputation( _cell ) );
	}


	private final void compileRef( ExpressionNodeForParentSectionModel _node ) throws CompilerException
	{
		final SectionCompiler section = sectionInContext();
		final SectionCompiler parent = section.parentSectionCompiler();
		final int parentObject = method().newLocal( 1 ); // Object
		final GeneratorAdapter mv = mv();
		mv.visitVarInsn( Opcodes.ALOAD, method().objectInContext() );
		mv.getField( section.classType(), ByteCodeEngineCompiler.PARENT_MEMBER_NAME, parent.classType() );
		mv().visitVarInsn( Opcodes.ASTORE, parentObject );
		compileInContextOfObject( parent, parentObject, _node );
	}

	protected final void compileInContextOfObject( final SectionCompiler _section, int _object, ExpressionNode _node )
			throws CompilerException
	{
		final int oldLocals = method().localsOffset();
		try {

			final SectionCompiler oldSection = method().sectionInContext();
			final int oldObject = method().objectInContext();
			try {
				method().setObjectInContext( _section, _object );
				compile( _node.argument( 0 ) );
			}
			finally {
				method().setObjectInContext( oldSection, oldObject );
			}
		}

		finally {
			method().resetLocalsTo( oldLocals );
		}
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


	protected final Iterable<LetEntry> closureOf( Iterable<ExpressionNode> _nodes )
	{
		Collection<LetEntry> closure = new ArrayList<LetEntry>();
		addToClosure( closure, _nodes );
		return closure;
	}

	protected final Iterable<LetEntry> closureOf( ExpressionNode _node )
	{
		Collection<LetEntry> closure = new ArrayList<LetEntry>();
		addToClosure( closure, _node );
		return closure;
	}

	private void addToClosure( Collection<LetEntry> _closure, Iterable<ExpressionNode> _nodes )
	{
		for (ExpressionNode node : _nodes)
			addToClosure( _closure, node );
	}

	private static final Object INNER_DEF = new Object();

	private final void addToClosure( Collection<LetEntry> _closure, ExpressionNode _node )
	{
		if (null == _node) {
			// ignore
		}
		else if (_node instanceof ExpressionNodeForLetVar) {
			final ExpressionNodeForLetVar letVar = (ExpressionNodeForLetVar) _node;
			final LetEntry found = letDict().find( letVar.varName() );
			if (null != found && INNER_DEF != found.value) {
				_closure.add( found );
			}
		}
		else if (_node instanceof ExpressionNodeForLet) {
			final ExpressionNodeForLet let = (ExpressionNodeForLet) _node;
			addToClosure( _closure, let.value() );
			addToClosureWithInnerDefs( _closure, let.in(), let.varName() );
		}
		else if (_node instanceof ExpressionNodeForFold) {
			final ExpressionNodeForFold fold = (ExpressionNodeForFold) _node;
			addToClosure( _closure, fold.initialAccumulatorValue() );
			addToClosureWithInnerDefs( _closure, fold.accumulatingStep(), fold.accumulatorName(), fold.elementName() );
			addToClosure( _closure, fold.elements() );
		}
		else if (_node instanceof ExpressionNodeForFold1st) {
			final ExpressionNodeForFold1st fold = (ExpressionNodeForFold1st) _node;
			addToClosure( _closure, fold.initialAccumulatorValue() );
			addToClosureWithInnerDefs( _closure, fold.firstValue(), fold.firstName() );
			addToClosureWithInnerDefs( _closure, fold.accumulatingStep(), fold.accumulatorName(), fold.elementName() );
			addToClosure( _closure, fold.emptyValue() );
			addToClosure( _closure, fold.elements() );
		}
		else {
			addToClosure( _closure, _node.arguments() );
		}
	}

	private void addToClosureWithInnerDefs( Collection<LetEntry> _closure, ExpressionNode _node, String... _names )
	{
		for (int i = 0; i < _names.length; i++) {
			letDict().let( _names[ i ], null, INNER_DEF );
		}
		try {
			addToClosure( _closure, _node );
		}
		finally {
			for (int i = _names.length - 1; i >= 0; i--) {
				letDict().unlet( _names[ i ] );
			}
		}
	}


	private final void compileLet( ExpressionNodeForLet _node ) throws CompilerException
	{
		// Note: It is important to allocate the local here rather than at the point of first
		// evaluation. Otherwise it could get reused when the first evaluation is within a local
		// scope.
		letDict().let( _node.varName(), _node.getDataType(), new EvaluateIntoLocal( compileNewLocal(), _node.value() ) );
		try {
			compile( _node.in() );
		}
		finally {
			letDict().unlet( _node.varName() );
		}
	}


	private final void compileLetVar( String _varName ) throws CompilerException
	{
		final Object val = letDict().lookup( _varName );
		compileLetValue( _varName, val );
	}

	private final void compileLetValue( String _name, Object _value ) throws CompilerException
	{
		if (_value == CHAINED_FIRST_ARG) {
			// ignore
		}
		else if (_value instanceof ExpressionNode) {
			compile( (ExpressionNode) _value );
		}
		else if (_value instanceof EvaluateIntoLocal) {
			final EvaluateIntoLocal eval = (EvaluateIntoLocal) _value;
			compile( eval.node );
			compileDup();
			compileStoreLocal( eval.local );
			letDict().set( _name, eval.local );
		}
		else if (_value instanceof Integer) {
			compileLoadLocal( (Integer) _value );
		}
		else {
			throw new CompilerException.NameNotFound( "The variable " + _name + " is not bound in this context." );
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


	private final void compileFold( ExpressionNodeForFold _node ) throws CompilerException
	{
		final FoldContext foldContext = new FoldContext( _node, section().engineCompiler() );
		if (isSubSectionIn( _node.elements() )) {
			Iterable<LetEntry> closure = closureOf( _node );
			compileHelpedExpr( new HelperCompilerForIterativeFold( sectionInContext(), _node.elements(), foldContext,
					closure ), closure );
		}
		else if (_node.canInlineFirst()) {
			final Iterable<ExpressionNode> elts = _node.elements();
			final ExpressionNode first = firstStaticElementIn( elts );
			compile( first );
			compileChainedFoldOverNonRepeatingElements( foldContext, elts, first );
		}
		else {
			compile( _node.initialAccumulatorValue() );
			compileChainedFoldOverNonRepeatingElements( foldContext, _node.elements(), null );
		}
	}

	private final void compileFold1st( ExpressionNodeForFold1st _node ) throws CompilerException
	{
		final FoldContext foldContext = new FoldContext( _node, section().engineCompiler() );
		if (isSubSectionIn( _node.elements() )) {
			Iterable<LetEntry> closure = closureOf( _node );
			compileHelpedExpr( new HelperCompilerForIterativeFold1st( sectionInContext(), _node.elements(), foldContext,
					closure ), closure );
		}
		else {
			final Iterable<ExpressionNode> elts = foldContext.node.elements();
			final ExpressionNode first = firstStaticElementIn( elts );
			compileElementAccess( foldContext, _node.firstName(), first, _node.firstValue() );
			compileChainedFoldOverNonRepeatingElements( foldContext, elts, first );
		}
	}

	final boolean isSubSectionIn( Iterable<ExpressionNode> _elts )
	{
		for (ExpressionNode elt : _elts) {
			if (elt instanceof ExpressionNodeForSubSectionModel) {
				return true;
			}
		}
		return false;
	}

	final ExpressionNode firstStaticElementIn( Iterable<ExpressionNode> _elts )
	{
		for (ExpressionNode elt : _elts) {
			if (!(elt instanceof ExpressionNodeForSubSectionModel)) {
				return elt;
			}
		}
		return null;
	}

	final void compileChainedFoldOverNonRepeatingElements( FoldContext _context, Iterable<ExpressionNode> _elts,
			ExpressionNode _except ) throws CompilerException
	{
		final String accName = _context.node.accumulatorName();
		letDict().let( accName, _context.node.initialAccumulatorValue().getDataType(), CHAINED_FIRST_ARG );
		try {
			final int reuseLocalsAt = localsOffset();
			for (final ExpressionNode elt : _elts) {
				if ((elt != _except) && !(elt instanceof ExpressionNodeForSubSectionModel)) {
					resetLocalsTo( reuseLocalsAt );
					compileElementFold( _context, elt );
				}
			}
		}
		finally {
			letDict().unlet( accName );
		}
	}

	final void compileElementFold( FoldContext _context, ExpressionNode _elt ) throws CompilerException
	{
		compileElementAccess( _context, _context.node.elementName(), _elt, _context.node.accumulatingStep() );
	}

	final void compileElementAccess( FoldContext _context, String _eltName, ExpressionNode _elt, ExpressionNode _expr )
			throws CompilerException
	{
		letDict().let( _eltName, _elt.getDataType(), _elt );
		try {
			compile( _expr );
		}
		finally {
			letDict().unlet( _eltName );
		}
	}


	final void compileHelpedExpr( HelperCompiler _compiler, Iterable<LetEntry> _closure ) throws CompilerException
	{
		_compiler.compile();

		final GeneratorAdapter mv = mv();
		mv.visitVarInsn( Opcodes.ALOAD, method().objectInContext() );
		for (LetEntry entry : _closure) {
			compileLetValue( entry.name, entry.value );
		}

		mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, sectionInContext().classInternalName(), _compiler.methodName(),
				_compiler.methodDescriptor() );
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


	protected static interface ForEachElementWithFirstCompilation
	{
		void compileIsFirst() throws CompilerException;
		void compileHaveFirst() throws CompilerException;
		void compileFirst( int _xi ) throws CompilerException;
		void compileElement( int _xi ) throws CompilerException;
	}

	protected abstract void compile_scanArrayWithFirst( ForEachElementWithFirstCompilation _forElement )
			throws CompilerException;

}
