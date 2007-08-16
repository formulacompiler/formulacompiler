/*
 * Copyright (c) 2006 by Abacus Research AG, Switzerland.
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
package org.formulacompiler.compiler.internal.bytecode;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.internal.bytecode.MethodCompiler.LocalArrayRef;
import org.formulacompiler.compiler.internal.bytecode.MethodCompiler.LocalRef;
import org.formulacompiler.compiler.internal.bytecode.MethodCompiler.LocalValueRef;
import org.formulacompiler.compiler.internal.expressions.ArrayDescriptor;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForDatabaseFold;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFold;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldArray;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForLet;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForLetVar;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForMakeArray;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForOperator;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForReduce;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForSwitch;
import org.formulacompiler.compiler.internal.expressions.InnerExpressionException;
import org.formulacompiler.compiler.internal.expressions.LetDictionary;
import org.formulacompiler.compiler.internal.expressions.LetDictionary.LetEntry;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCellModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCount;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForParentSectionModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForSubSectionModel;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;


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

	private final MethodCompiler methodCompiler;
	private final GeneratorAdapter mv;

	ExpressionCompiler( MethodCompiler _methodCompiler )
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

	protected final Iterable<LetEntry> closureOf( ExpressionNode _node )
	{
		return method().closureOf( _node );
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
			compileConst( ((ExpressionNodeForConstantValue) _node).value() );
		}

		else if (_node instanceof ExpressionNodeForCellModel) {
			compileRef( ((ExpressionNodeForCellModel) _node).getCellModel() );
		}

		else if (_node instanceof ExpressionNodeForParentSectionModel) {
			compileRef( (ExpressionNodeForParentSectionModel) _node );
		}

		else if (_node instanceof ExpressionNodeForSubSectionModel) {
			compileRef( (ExpressionNodeForSubSectionModel) _node );
		}

		else if (_node instanceof ExpressionNodeForArrayReference) {
			compileRef( (ExpressionNodeForArrayReference) _node );
		}

		else if (_node instanceof ExpressionNodeForOperator) {
			final ExpressionNodeForOperator node = (ExpressionNodeForOperator) _node;
			if (needsIf( node.getOperator() )) {
				compileIf( node, ExpressionNode.TRUENODE, ExpressionNode.FALSENODE );
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
					compileIf( node, ExpressionNode.TRUENODE, ExpressionNode.FALSENODE );
					break;

				case AND:
				case OR:
					compileIf( node, ExpressionNode.TRUENODE, ExpressionNode.FALSENODE );
					break;

				default:
					compileFunction( node );
			}
		}

		else if (_node instanceof ExpressionNodeForSwitch) {
			compileSwitch( (ExpressionNodeForSwitch) _node );
		}

		else if (_node instanceof ExpressionNodeForCount) {
			compileCount( (ExpressionNodeForCount) _node );
		}

		else if (_node instanceof ExpressionNodeForLet) {
			compileLet( (ExpressionNodeForLet) _node );
		}

		else if (_node instanceof ExpressionNodeForLetVar) {
			compileLetVar( ((ExpressionNodeForLetVar) _node).varName() );
		}

		else if (_node instanceof ExpressionNodeForFold) {
			compileFold( (ExpressionNodeForFold) _node );
		}

		else if (_node instanceof ExpressionNodeForReduce) {
			compileReduce( (ExpressionNodeForReduce) _node );
		}

		else if (_node instanceof ExpressionNodeForFoldArray) {
			compileFoldArray( (ExpressionNodeForFoldArray) _node );
		}

		else if (_node instanceof ExpressionNodeForMakeArray) {
			compileMakeArray( (ExpressionNodeForMakeArray) _node );
		}

		else if (_node instanceof ExpressionNodeForDatabaseFold) {
			compileDatabaseFold( (ExpressionNodeForDatabaseFold) _node );
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


	protected void compileConst( Object _value ) throws CompilerException
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


	protected abstract int compileComparison( int _ifOpcode, int _comparisonOpcode ) throws CompilerException;


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
				compileIndex( _node );
				break;

			default:
				throw new CompilerException.UnsupportedExpression( "Function "
						+ fun + " is not supported for " + this + " engines." );
		}
	}


	private final void compileIndex( ExpressionNodeForFunction _node ) throws CompilerException
	{
		final ExpressionNodeForArrayReference array = (ExpressionNodeForArrayReference) _node.argument( 0 );
		switch (_node.cardinality()) {
			case 2:
				if (array.arrayDescriptor().numberOfColumns() == 1) {
					compileIndex( array, _node.argument( 1 ), null );
				}
				else if (array.arrayDescriptor().numberOfRows() == 1) {
					compileIndex( array, null, _node.argument( 1 ) );
				}
				else {
					throw new CompilerException.UnsupportedExpression(
							"INDEX with single index expression must not have two-dimensional range argument." );
				}
				return;
			case 3:
				compileIndex( array, _node.argument( 1 ), _node.argument( 2 ) );
				return;
		}
		throw new CompilerException.UnsupportedExpression( "INDEX must have two or three arguments." );
	}

	private final void compileIndex( ExpressionNodeForArrayReference _array, ExpressionNode _row, ExpressionNode _col )
			throws CompilerException
	{
		final GeneratorAdapter mv = mv();
		final MethodCompiler mtd = method();
		final ExpressionCompilerForNumbers numCompiler = mtd.numericCompiler();

		// Push receiver for index switch method.
		mtd.mv().visitVarInsn( Opcodes.ALOAD, mtd.objectInContext() );

		// Compute index value.
		final ArrayDescriptor desc = _array.arrayDescriptor();
		final int cols = desc.numberOfColumns();
		if (cols == 1) {
			if (!isNullOrOne( _col )) {
				throw new CompilerException.UnsupportedExpression( "Column index must be 1." );
			}
			// <row> - 1;
			numCompiler.compileInt( _row );
			mv.push( 1 );
			mv.visitInsn( Opcodes.ISUB );
		}
		else if (desc.numberOfRows() == 1) {
			if (!isNullOrOne( _row )) {
				throw new CompilerException.UnsupportedExpression( "Row index must be 1." );
			}
			// <col> - 1;
			numCompiler.compileInt( _col );
			mv.push( 1 );
			mv.visitInsn( Opcodes.ISUB );
		}
		else {
			// (<row> - 1) * <num_cols>) + (<col> - 1);
			if (isNullOrOne( _row )) {
				numCompiler.compileInt( _col );
				mv.push( 1 );
				mv.visitInsn( Opcodes.ISUB );
			}
			else {
				numCompiler.compileInt( _row );
				mv.push( 1 );
				mv.visitInsn( Opcodes.ISUB );

				mv.push( cols );
				mv.visitInsn( Opcodes.IMUL );

				numCompiler.compileInt( _col );
				mv.push( 1 );
				mv.visitInsn( Opcodes.ISUB );

				mv.visitInsn( Opcodes.IADD );
			}
		}

		IndexerCompiler indexer = section().getIndexerFor( _array );
		indexer.compileCall( mv );
	}

	private final boolean isNullOrOne( ExpressionNode _node )
	{
		if (_node == null) return true;
		if (_node instanceof ExpressionNodeForConstantValue) {
			ExpressionNodeForConstantValue constNode = (ExpressionNodeForConstantValue) _node;
			final Number constValue = (Number) constNode.value();
			return (constValue == null || constValue.intValue() == 1);
		}
		return false;
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

		final Set<DelayedLet> outerSetsInTrueBranch = compileTrackingSetsOfOuterLets( _ifTrue );
		revertSetsOfOuterLets( outerSetsInTrueBranch, null );

		mv.visitJumpInsn( Opcodes.GOTO, done );
		mv.mark( notMet );

		final Set<DelayedLet> outerSetsInFalseBranch = compileTrackingSetsOfOuterLets( _ifFalse );
		revertSetsOfOuterLets( outerSetsInFalseBranch, outerSetsInTrueBranch );

		mv.mark( done );
	}

	private final Set<DelayedLet> compileTrackingSetsOfOuterLets( final ExpressionNode _node ) throws CompilerException
	{
		final Object oldState = method().beginTrackingSetsOfOuterLets();
		try {

			compile( _node );

			return method().trackedSetsOfOuterLets();
		}
		finally {
			method().endTrackingSetsOfOuterLets( oldState );
		}
	}

	private final void revertSetsOfOuterLets( Set<DelayedLet> _ifInThisSetOnly, Set<DelayedLet> _notIfInThisSetToo )
	{
		for (final DelayedLet let : _ifInThisSetOnly) {
			if (_notIfInThisSetToo != null && _notIfInThisSetToo.contains( let )) {
				method().trackSetOfLet( let );
			}
			else {
				method().letDict().set( let.name, let );
			}
		}
	}


	private void compileSwitch( ExpressionNodeForSwitch _node ) throws CompilerException
	{
		final Iterable<LetEntry> closure = closureOf( _node );
		compileHelpedExpr( new HelperCompilerForSwitch( sectionInContext(), _node, closure ), closure );
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


	final void compileRef( CellModel _cell )
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
		throw new CompilerException.ReferenceToInnerCellNotAggregated();
	}


	private final void compileRef( ExpressionNodeForArrayReference _node ) throws CompilerException
	{
		throw new CompilerException.ReferenceToArrayNotAggregated();
	}


	protected abstract void compileCount( ExpressionNodeForCount _node ) throws CompilerException;


	private final void compileLet( ExpressionNodeForLet _node ) throws CompilerException
	{
		final String varName = _node.varName();
		if (_node.isSymbolic()) {
			throw new IllegalArgumentException( "Cannot compile symbolic lets." );
		}
		else if (!_node.shouldCache()) {
			/*
			 * Note: Used internally to pass outer values as single-eval method arguments to helper
			 * methods by wrapping the construct in a series of LETs. The closure then automatically
			 * declares the parameters and passes the values to them.
			 */
			letDict().let( varName, _node.value().getDataType(), _node.value() );
			try {
				compile( _node.in() );
			}
			finally {
				letDict().unlet( varName );
			}
		}
		else {
			/*
			 * Note: It is important to allocate the local here rather than at the point of first
			 * evaluation. Otherwise it could get reused when the first evaluation is within a local
			 * scope.
			 */
			final LocalRef local = compileNewLocal( method().isArray( _node.value() ) );
			letDict().let( varName, _node.value().getDataType(),
					new DelayedLet( varName, local, _node.value(), method().letTrackingNestingLevel() ) );
			try {
				compile( _node.in() );
			}
			finally {
				letDict().unlet( varName );
			}
		}
	}


	private final void compileLetVar( String _varName ) throws CompilerException
	{
		final Object val = letDict().lookup( _varName );
		compileLetValue( _varName, val );
	}

	final void compileLetValue( String _name, Object _value ) throws CompilerException
	{
		if (_value == CHAINED_FIRST_ARG) {
			// ignore
		}
		else if (_value instanceof ExpressionNode) {
			compile( (ExpressionNode) _value );
		}
		else if (_value instanceof DelayedLet) {
			final DelayedLet letDef = (DelayedLet) _value;
			compile( letDef.node );
			compileDup( letDef.local.isArray() );
			compileStoreLocal( letDef.local );
			letDict().set( _name, letDef.local );
			method().trackSetOfLet( letDef );
		}
		else if (_value instanceof LocalRef) {
			compileLoadLocal( (LocalRef) _value );
		}
		else {
			throw new CompilerException.NameNotFound( "The variable " + _name + " is not bound in this context." );
		}
	}

	protected final LocalRef compileStoreToNewLocal( boolean _isArray )
	{
		final LocalRef local = compileNewLocal( _isArray );
		compileStoreLocal( local );
		return local;
	}

	protected final LocalRef compileDupAndStoreToNewLocal( boolean _isArray )
	{
		compileDup( _isArray );
		return compileStoreToNewLocal( _isArray );
	}

	protected final LocalRef compileNewLocal( boolean _isArray )
	{
		if (_isArray) {
			return new LocalArrayRef( method().newLocal( 1 ) );
		}
		else {
			return new LocalValueRef( method().newLocal( type().getSize() ) );
		}
	}

	protected final void compileDup( boolean _isArray )
	{
		if (_isArray) {
			mv().visitInsn( Opcodes.DUP );
		}
		else {
			compileDup();
		}
	}

	protected void compileDup()
	{
		mv().visitInsn( Opcodes.DUP );
	}

	protected final void compileStoreLocal( LocalRef _local )
	{
		if (_local.isArray()) {
			mv().visitVarInsn( Opcodes.ASTORE, _local.offset );
		}
		else {
			mv().visitVarInsn( type().getOpcode( Opcodes.ISTORE ), _local.offset );
		}
	}

	protected final void compileLoadLocal( LocalRef _localRef )
	{
		if (_localRef instanceof LocalArrayRef) {
			mv().visitVarInsn( Opcodes.ALOAD, _localRef.offset );
		}
		else {
			mv().visitVarInsn( type().getOpcode( Opcodes.ILOAD ), _localRef.offset );
		}
	}


	private final void compileFold( ExpressionNodeForFold _node ) throws CompilerException
	{
		final FoldContext foldContext = new FoldContext( _node, section().engineCompiler() );
		if (isSubSectionIn( _node.elements() )) {
			final Iterable<LetEntry> closure = closureOf( _node );
			compileHelpedExpr( new HelperCompilerForIterativeFold( sectionInContext(), _node.elements(), foldContext,
					closure ), closure );
		}
		else if (_node.mayReduce()) {
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

	private final void compileReduce( ExpressionNodeForReduce _node ) throws CompilerException
	{
		final FoldContext foldContext = new FoldContext( _node, section().engineCompiler() );
		if (isSubSectionIn( _node.elements() )) {
			final Iterable<LetEntry> closure = closureOf( _node );
			compileHelpedExpr( new HelperCompilerForIterativeReduce( sectionInContext(), _node.elements(), foldContext,
					closure ), closure );
		}
		else {
			final Iterable<ExpressionNode> elts = foldContext.node.elements();
			final ExpressionNode first = firstStaticElementIn( elts );
			compile( first );
			compileChainedFoldOverNonRepeatingElements( foldContext, elts, first );
		}
	}

	private final void compileFoldArray( ExpressionNodeForFoldArray _node ) throws CompilerException
	{
		final FoldContext foldContext = new FoldContext( _node, section().engineCompiler() );
		final Iterable<LetEntry> closure = closureOf( _node );
		compileHelpedExpr( new HelperCompilerForArrayFold( sectionInContext(), _node.array(), foldContext, closure ),
				closure );
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


	private final void compileMakeArray( ExpressionNodeForMakeArray _node ) throws CompilerException
	{
		compileArray( _node.argument( 0 ) );
	}


	final void compileHelpedExpr( HelperCompiler _compiler, Iterable<LetEntry> _closure ) throws CompilerException
	{
		_compiler.compile();
		method().compileCalleeAndClosure( _closure );
		mv().visitMethodInsn( Opcodes.INVOKEVIRTUAL, sectionInContext().classInternalName(), _compiler.methodName(),
				_compiler.methodDescriptor() );
	}


	protected final void compileRuntimeMethod( String _methodName, String _methodSig )
	{
		typeCompiler().compileRuntimeMethod( mv(), _methodName, _methodSig );
	}

	protected void compile_environment()
	{
		section().compileEnvironmentAccess( mv() );
	}

	protected void compile_computationTime()
	{
		method().section().compileComputationTimeAccess( mv() );
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


	protected final void compileArray( ExpressionNode _arrayNode ) throws CompilerException
	{
		final GeneratorAdapter mv = mv();
		final ExpressionNodeForArrayReference arr = (ExpressionNodeForArrayReference) _arrayNode;
		mv.push( arr.arrayDescriptor().numberOfElements() );
		compileNewArray();
		int i = 0;
		for (ExpressionNode arg : arr.arguments()) {
			mv.visitInsn( Opcodes.DUP );
			mv.push( i++ );
			compile( arg );
			mv.visitInsn( arrayStoreOpcode() );
		}
	}

	protected abstract void compileNewArray();
	protected abstract int arrayStoreOpcode();


	private final void compileDatabaseFold( ExpressionNodeForDatabaseFold _node ) throws CompilerException
	{
		final FoldContext foldContext = new FoldContext( _node, section().engineCompiler() );
		final Iterable<LetEntry> closure = closureOf( _node );
		compileHelpedExpr( new HelperCompilerForDatabaseFold( sectionInContext(), _node, foldContext, closure ), closure );
	}


}
