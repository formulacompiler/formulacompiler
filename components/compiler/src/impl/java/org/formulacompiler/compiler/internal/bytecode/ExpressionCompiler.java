/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * This file is part of the Abacus Formula Compiler (AFC).
 *
 * For commercial licensing, please contact sales(at)formulacompiler.com.
 *
 * AFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AFC.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.formulacompiler.compiler.internal.bytecode;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.internal.bytecode.MethodCompiler.GeneratedRef;
import org.formulacompiler.compiler.internal.bytecode.MethodCompiler.LocalArrayRef;
import org.formulacompiler.compiler.internal.bytecode.MethodCompiler.LocalRef;
import org.formulacompiler.compiler.internal.bytecode.MethodCompiler.LocalValueRef;
import org.formulacompiler.compiler.internal.expressions.ArrayDescriptor;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.ExpressionBuilder;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldDatabase;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldList;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldVectors;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForLet;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForLetVar;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForLogging;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForMakeArray;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForMaxValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForMinValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForOperator;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForSwitch;
import org.formulacompiler.compiler.internal.expressions.InnerExpressionException;
import org.formulacompiler.compiler.internal.expressions.LetDictionary;
import org.formulacompiler.compiler.internal.expressions.LetDictionary.LetEntry;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCellModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCount;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForParentSectionModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForSubSectionModel;
import org.formulacompiler.runtime.ComputationException;
import org.formulacompiler.runtime.FormulaException;
import org.formulacompiler.runtime.New;
import org.formulacompiler.runtime.NotAvailableException;
import org.formulacompiler.runtime.spreadsheet.CellAddress;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;


abstract class ExpressionCompiler
{
	private static final Type COMPUTATION_ERROR_TYPE = Type.getType( ComputationException.class );
	private static final Type NOT_AVAILABLE_ERROR_TYPE = Type.getType( NotAvailableException.class );
	private static final Type ARITHMETIC_EXCEPTION_TYPE = Type.getType( ArithmeticException.class );
	protected static final Type FORMULA_ERROR_TYPE = Type.getType( FormulaException.class );

	protected static final Object TOP_OF_STACK = new Object();

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

		else if (_node instanceof ExpressionNodeForMinValue) {
			typeCompiler().compileMinValue( mv() );
		}

		else if (_node instanceof ExpressionNodeForMaxValue) {
			typeCompiler().compileMaxValue( mv() );
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
				compileIf( node, ExpressionBuilder.TRUE, ExpressionBuilder.FALSE );
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
					compileIf( node, ExpressionBuilder.TRUE, ExpressionBuilder.FALSE );
					break;

				case AND:
				case OR:
					compileIf( node, ExpressionBuilder.TRUE, ExpressionBuilder.FALSE );
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

		else if (_node instanceof ExpressionNodeForMakeArray) {
			compileMakeArray( (ExpressionNodeForMakeArray) _node );
		}

		else if (_node instanceof ExpressionNodeForFoldList) {
			compileFoldList( (ExpressionNodeForFoldList) _node );
		}

		else if (_node instanceof ExpressionNodeForFoldVectors) {
			compileFoldVectors( (ExpressionNodeForFoldVectors) _node );
		}

		else if (_node instanceof ExpressionNodeForFoldDatabase) {
			compileFoldDatabase( (ExpressionNodeForFoldDatabase) _node );
		}

		else if (_node instanceof ExpressionNodeForLogging) {
			compileLogging( (ExpressionNodeForLogging) _node );
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

			case ISERROR:
				compileIsException( _node, true, ERROR_TYPES, NO_TYPES );
				break;

			case ISERR:
				compileIsException( _node, true, ERR_TYPES, NA_TYPES );
				break;

			case ISNA:
				compileIsException( _node, false, NA_TYPES, ERR_TYPES );
				break;

			default:
				throw new CompilerException.UnsupportedExpression( "Function "
						+ fun + " is not supported for " + this + " engines." );
		}
	}

	private static final Type[] NO_TYPES = {};
	private static final Type[] ERROR_TYPES = { COMPUTATION_ERROR_TYPE, ARITHMETIC_EXCEPTION_TYPE };
	private static final Type[] ERR_TYPES = { FORMULA_ERROR_TYPE, ARITHMETIC_EXCEPTION_TYPE };
	private static final Type[] NA_TYPES = { NOT_AVAILABLE_ERROR_TYPE };


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
		if (cols == 1 && isNullOrZeroOrOne( _col )) {
			// <row> - 1;
			numCompiler.compileInt( _row );
			mv.push( 1 );
			mv.visitInsn( Opcodes.ISUB );
		}
		else {
			final int rows = desc.numberOfRows();
			if (rows == 1 && isNullOrZeroOrOne( _row )) {
				// <col> - 1;
				numCompiler.compileInt( _col );
				mv.push( 1 );
				mv.visitInsn( Opcodes.ISUB );
			}
			else {
				// Push receiver for linearizer method.
				mtd.mv().visitVarInsn( Opcodes.ALOAD, mtd.objectInContext() );
				numCompiler.compileInt( _row );
				numCompiler.compileInt( _col );
				section().getLinearizerFor( rows, cols ).compileCall( mv );
			}
		}
		section().getIndexerFor( _array ).compileCall( mv );
	}

	private final boolean isNullOrZeroOrOne( ExpressionNode _node )
	{
		if (_node == null) return true;
		if (_node instanceof ExpressionNodeForConstantValue) {
			final ExpressionNodeForConstantValue constNode = (ExpressionNodeForConstantValue) _node;
			final Number constValue = (Number) constNode.value();
			return (constValue == null || constValue.intValue() == 0 || constValue.intValue() == 1);
		}
		return false;
	}


	private void compileIsException( final ExpressionNodeForFunction _node, final boolean _testForErrors,
			final Type[] _handledTypesReturningTrue, final Type[] _handledTypesReturningFalse ) throws CompilerException
	{
		/*
		 * Move the handler into its own method because exception handlers clobber the stack.
		 */
		final Iterable<LetEntry> closure = closureOf( _node );
		compileHelpedExpr( new HelperCompiler( section(), _node, closure )
		{

			@Override
			protected void compileBody() throws CompilerException
			{
				final GeneratorAdapter mv = this.mv();
				final ExpressionCompiler ec = expressionCompiler();
				final Label handled = mv.newLabel();

				final Label beginHandling = mv.mark();
				ec.compile( _node.argument( 0 ) );
				ec.compileExceptionalValueTest( _testForErrors );
				mv.goTo( handled );
				final Label endHandling = mv.mark();

				for (final Type a_handledTypesReturningTrue : _handledTypesReturningTrue) {
					mv.catchException( beginHandling, endHandling, a_handledTypesReturningTrue );
					mv.visitVarInsn( Opcodes.ASTORE, method().newLocal( 1 ) );
					ec.compileConst( Boolean.TRUE );
					mv.goTo( handled );
				}

				for (final Type a_handledTypesReturningFalse : _handledTypesReturningFalse) {
					mv.catchException( beginHandling, endHandling, a_handledTypesReturningFalse );
					mv.visitVarInsn( Opcodes.ASTORE, method().newLocal( 1 ) );
					ec.compileConst( Boolean.FALSE );
					mv.goTo( handled );
				}

				mv.mark( handled );
			}

		}, closure );
	}

	protected void compileExceptionalValueTest( boolean _testForErrors ) throws CompilerException
	{
		compilePop();
		compileConst( Boolean.FALSE );
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
		switch (_node.type()) {

			case BYVAL:
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
				break;

			case BYNAME:
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
				break;

			default:
				throw new CompilerException.UnsupportedExpression( "Cannot compile this type of LET." );

		}
	}


	private final void compileLetVar( String _varName ) throws CompilerException
	{
		if (this.forbiddenLetVars.contains( _varName )) {
			throw new IllegalArgumentException( "Cannot compile a letvar named "
					+ _varName + " when already compiling one of that name - rewriter bug?" );
		}
		final Object val = letDict().lookup( _varName );
		compileLetValue( _varName, val );
	}

	final void compileLetValue( String _name, Object _value ) throws CompilerException
	{
		if (_value == TOP_OF_STACK) {
			// ignore
		}
		else if (_value instanceof ExpressionNode) {
			compile( (ExpressionNode) _value );
		}
		else if (_value instanceof DelayedLet) {
			final DelayedLet letDef = (DelayedLet) _value;
			this.forbiddenLetVars.add( letDef.name );
			try {
				compile( letDef.node );
			}
			finally {
				this.forbiddenLetVars.remove( letDef.name );
			}
			compileDup( letDef.local.isArray() );
			compileStoreLocal( letDef.local );
			letDict().set( _name, letDef.local );
			method().trackSetOfLet( letDef );
		}
		else if (_value instanceof LocalRef) {
			compileLoadLocal( (LocalRef) _value );
		}
		else if (_value instanceof GeneratedRef) {
			((GeneratedRef) _value).compile( this );
		}
		else {
			throw new CompilerException.NameNotFound( "The variable " + _name + " is not bound in this context." );
		}
	}

	private final Set<String> forbiddenLetVars = New.set();


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

	protected void compilePop()
	{
		mv().visitInsn( Opcodes.POP );
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

	protected void compile_sectionInfo()
	{
		section().compileSectionInfoAccess( mv() );
	}

	protected void compile_computationMode()
	{
		method().section().compileComputationModeAccess( mv() );
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


	private final void compileFoldList( ExpressionNodeForFoldList _node ) throws CompilerException
	{
		if (ChainedFoldCompiler.isChainable( _node.fold() )) {
			if (new ChainedFoldCompiler( this, _node ).compile()) {
				return;
			}
			if (!_node.fold().isSpecialWhenEmpty()) {
				final Iterable<LetEntry> closure = closureOf( _node );
				compileHelpedExpr( new HelperCompilerForFoldChained( sectionInContext(), _node, closure ), closure );
				return;
			}
		}
		final Iterable<LetEntry> closure = closureOf( _node );
		compileHelpedExpr( new HelperCompilerForFoldList( sectionInContext(), _node, closure ), closure );
	}

	private final void compileFoldVectors( ExpressionNodeForFoldVectors _node ) throws CompilerException
	{
		final Iterable<LetEntry> closure = closureOf( _node );
		compileHelpedExpr( new HelperCompilerForFoldVectors( sectionInContext(), _node, closure ), closure );
	}

	private final void compileFoldDatabase( ExpressionNodeForFoldDatabase _node ) throws CompilerException
	{
		final Iterable<LetEntry> closure = closureOf( _node );
		compileHelpedExpr( new HelperCompilerForFoldDatabase( sectionInContext(), _node, closure ), closure );
	}

	private void compileLogging( final ExpressionNodeForLogging _expressionNodeForLogging ) throws CompilerException
	{
		compile( _expressionNodeForLogging.argument( 0 ) );

		final Object source = _expressionNodeForLogging.getSource();
		if (source instanceof CellAddress) {
			final CellAddress cellAddress = (CellAddress) source;
			final String definedName = _expressionNodeForLogging.getDefinedName();
			final boolean input = _expressionNodeForLogging.isInput();
			final boolean output = _expressionNodeForLogging.isOutput();
			compileLogging( cellAddress, definedName, input, output );
		}
	}

	void compileLogging( final CellAddress _cellAddress, final String _name, boolean _input, boolean _output ) throws CompilerException
	{
		final GeneratorAdapter mv = mv();
		final int valLocal = mv.newLocal( type() );
		mv.storeLocal( valLocal );
		mv.loadLocal( valLocal );
		if (DataType.NUMERIC.equals( dataType() )) {
			compileConversionTo( Number.class );
		}

		compile_util_log( _cellAddress.getSheetName(), _cellAddress.getColumnIndex(), _cellAddress.getRowIndex(), _name, _input, _output );

		mv.loadLocal( valLocal );
	}

	protected abstract void compile_util_log( String _sheetName, int _columnIndex, int _rowIndex,
			String _definedName, boolean _input, boolean _output ) throws CompilerException;


	static final boolean isSubSectionIn( Iterable<ExpressionNode> _elts )
	{
		for (ExpressionNode elt : _elts) {
			if (elt instanceof ExpressionNodeForSubSectionModel) {
				return true;
			}
		}
		return false;
	}

}
