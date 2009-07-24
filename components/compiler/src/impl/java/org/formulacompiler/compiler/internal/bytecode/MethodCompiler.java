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
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldDefinition;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForLet;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForLetVar;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForMakeArray;
import org.formulacompiler.compiler.internal.expressions.LetDictionary;
import org.formulacompiler.compiler.internal.expressions.LetDictionary.LetEntry;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.runtime.New;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

abstract class MethodCompiler
{
	private final SectionCompiler section;
	private final String methodName;
	private final String methodDescriptor;
	private final GeneratorAdapter mv;
	private final LetDictionary letDict = new LetDictionary();

	private ExpressionCompilerForStrings stringCompiler;
	private ExpressionCompilerForNumbers numericCompiler;

	private SectionCompiler sectionInContext;
	private int objectInContext;


	MethodCompiler( SectionCompiler _section, int _access, String _methodName, String _descriptor )
	{
		super();
		this.section = _section;
		this.methodName = _methodName;
		this.methodDescriptor = _descriptor;
		this.mv = section().newMethod( _access, _methodName, _descriptor );
		this.localsOffset = 1 + totalSizeOf( Type.getArgumentTypes( _descriptor ) );
		this.sectionInContext = _section;
		this.objectInContext = 0; // "this"
	}

	private static final int totalSizeOf( Type[] _argTypes )
	{
		int result = 0;
		for (Type t : _argTypes)
			result += t.getSize();
		return result;
	}

	protected static final String descriptorOf( SectionCompiler _section, Iterable<LetEntry> _closure )
	{
		StringBuffer b = new StringBuffer();
		for (LetEntry entry : _closure) {
			if (isArray( entry )) {
				b.append( '[' ).append( _section.engineCompiler().typeCompiler( entry.type ).typeDescriptor() );
			}
			else {
				b.append( _section.engineCompiler().typeCompiler( entry.type ).typeDescriptor() );
			}
		}
		return b.toString();
	}


	final SectionCompiler section()
	{
		return this.section;
	}

	final LetDictionary letDict()
	{
		return this.letDict;
	}

	final int objectInContext()
	{
		return this.objectInContext;
	}

	public SectionCompiler sectionInContext()
	{
		return this.sectionInContext;
	}

	final void setObjectInContext( SectionCompiler _section, int _object )
	{
		this.sectionInContext = _section;
		this.objectInContext = _object;
	}


	final ExpressionCompiler expressionCompiler( DataType _type )
	{
		switch (_type) {
			case STRING:
				return stringCompiler();
			default:
				return numericCompiler();
		}
	}

	final ExpressionCompilerForStrings stringCompiler()
	{
		if (null == this.stringCompiler) this.stringCompiler = new ExpressionCompilerForStrings( this );
		return this.stringCompiler;
	}

	final ExpressionCompilerForNumbers numericCompiler()
	{
		if (null == this.numericCompiler)
			this.numericCompiler = ExpressionCompilerForNumbers.compilerFor( this, section().engineCompiler()
					.getNumericType() );
		return this.numericCompiler;
	}

	@SuppressWarnings( "unchecked" )
	private ExpressionCompiler expressionCompilerFor( Class _clazz )
	{
		return (_clazz.isAssignableFrom( String.class )) ? stringCompiler() : numericCompiler();
	}

	final String methodName()
	{
		return this.methodName;
	}

	final String methodDescriptor()
	{
		return this.methodDescriptor;
	}

	final ClassWriter cw()
	{
		return section().cw();
	}

	final GeneratorAdapter mv()
	{
		return this.mv;
	}


	final void compile() throws CompilerException
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


	final void compileCall( GeneratorAdapter _mv )
	{
		_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, section().classInternalName(), methodName(), methodDescriptor() );
	}


	final void compileInputGetterCall( CallFrame _callChainToCall ) throws CompilerException
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
					if (arg instanceof CellModel) {
						final CellModel argCell = (CellModel) arg;
						final ExpressionCompiler ex = expressionCompilerFor( type );
						ex.compileRef( argCell );
						ex.compileConversionTo( type );
					}
					else {
						pushConstParam( type, arg );
					}
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

	private final void pushConstParam( Class _type, Object _constantValue ) throws CompilerException
	{
		if (null == _constantValue) {
			mv().visitInsn( Opcodes.ACONST_NULL );
		}

		else if (_type == Byte.TYPE) {
			mv().push( ((Number) _constantValue).byteValue() );
		}
		else if (_type == Byte.class) {
			mv().push( ((Number) _constantValue).byteValue() );
			mv().visitMethodInsn( Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;" );
		}

		else if (_type == Short.TYPE) {
			mv().push( ((Number) _constantValue).shortValue() );
		}
		else if (_type == Short.class) {
			mv().push( ((Number) _constantValue).shortValue() );
			mv().visitMethodInsn( Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;" );
		}

		else if (_type == Integer.TYPE) {
			mv().push( ((Number) _constantValue).intValue() );
		}
		else if (_type == Integer.class) {
			mv().push( ((Number) _constantValue).intValue() );
			mv().visitMethodInsn( Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;" );
		}

		else if (_type == Long.TYPE) {
			mv().push( ((Number) _constantValue).longValue() );
		}
		else if (_type == Long.class) {
			mv().push( ((Number) _constantValue).longValue() );
			mv().visitMethodInsn( Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;" );
		}

		else if (_type == Double.TYPE) {
			mv().push( ((Number) _constantValue).doubleValue() );
		}
		else if (_type == Double.class) {
			mv().push( ((Number) _constantValue).doubleValue() );
			mv().visitMethodInsn( Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;" );
		}

		else if (_type == Float.TYPE) {
			mv().push( ((Number) _constantValue).floatValue() );
		}
		else if (_type == Float.class) {
			mv().push( ((Number) _constantValue).floatValue() );
			mv().visitMethodInsn( Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;" );
		}

		else if (_type == Character.TYPE) {
			mv().push( ((Character) _constantValue).charValue() );
		}
		else if (_type == Character.class) {
			mv().push( ((Character) _constantValue).charValue() );
			mv().visitMethodInsn( Opcodes.INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;" );
		}

		else if (_type == Boolean.TYPE) {
			mv().push( ((Boolean) _constantValue).booleanValue() );
		}
		else if (_type == Boolean.class) {
			mv().push( ((Boolean) _constantValue).booleanValue() );
			mv().visitMethodInsn( Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;" );
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
					+ _type + "' is not supported as an input method parameter." );
		}
	}


	protected final void compileExpression( ExpressionNode _node ) throws CompilerException
	{
		expressionCompiler( _node.getDataType() ).compile( _node );
	}


	protected final Iterable<LetEntry> closureOf( Iterable<ExpressionNode> _nodes )
	{
		// Using sorted map to make engines reproducible.
		final Map<String, LetEntry> closure = New.sortedMap();
		addToClosure( closure, _nodes );
		return closure.values();
	}

	protected final Iterable<LetEntry> closureOf( ExpressionNode _node )
	{
		final Map<String, LetEntry> closure = New.sortedMap();
		addToClosure( closure, _node );
		return closure.values();
	}

	private void addToClosure( Map<String, LetEntry> _closure, Iterable<ExpressionNode> _nodes )
	{
		for (ExpressionNode node : _nodes)
			addToClosure( _closure, node );
	}

	private static final Object INNER_DEF = new Object();

	private final void addToClosure( Map<String, LetEntry> _closure, ExpressionNode _node )
	{
		if (null == _node) {
			// ignore
		}
		else if (_node instanceof ExpressionNodeForLetVar) {
			final ExpressionNodeForLetVar letVar = (ExpressionNodeForLetVar) _node;
			final LetEntry found = letDict().find( letVar.varName() );
			if (null != found && INNER_DEF != found.value) {
				// Don't treat repeated occurrences separately.
				_closure.put( found.name, found );
			}
		}
		else if (_node instanceof ExpressionNodeForLet) {
			final ExpressionNodeForLet let = (ExpressionNodeForLet) _node;
			addToClosure( _closure, let.value() );
			addToClosureWithInnerDefs( _closure, let.in(), let.varName() );
		}

		else if (_node instanceof ExpressionNodeForFoldDefinition) {
			final ExpressionNodeForFoldDefinition fold = (ExpressionNodeForFoldDefinition) _node;

			for (int i = 0; i < fold.accuCount(); i++)
				addToClosure( _closure, fold.accuInit( i ) );

			for (int i = 0; i < fold.accuCount(); i++)
				letInnerDef( fold.accuName( i ) );
			for (int i = 0; i < fold.eltCount(); i++)
				letInnerDef( fold.eltName( i ) );
			letInnerDef( fold.indexName() );

			for (int i = 0; i < fold.accuCount(); i++)
				addToClosure( _closure, fold.accuStep( i ) );

			unletInnerDef( fold.indexName() );
			letDict().unlet( fold.eltCount() );
			letInnerDef( fold.countName() );

			addToClosure( _closure, fold.merge() );

			unletInnerDef( fold.countName() );
			letDict().unlet( fold.accuCount() );

			addToClosure( _closure, fold.whenEmpty() );
		}

		else {
			addToClosure( _closure, _node.arguments() );
		}
	}

	private void addToClosureWithInnerDefs( Map<String, LetEntry> _closure, ExpressionNode _node, String... _names )
	{
		for (int i = 0; i < _names.length; i++) {
			letInnerDef( _names[ i ] );
		}
		try {
			addToClosure( _closure, _node );
		}
		finally {
			for (int i = _names.length - 1; i >= 0; i--) {
				unletInnerDef( _names[ i ] );
			}
		}
	}

	private void letInnerDef( String _name )
	{
		if (_name != null) letDict().let( _name, null, INNER_DEF );
	}

	private void unletInnerDef( String _name )
	{
		if (_name != null) letDict().unlet( _name );
	}


	final void addClosureToLetDict( Iterable<LetEntry> _closure, int _leadingParamSize )
	{
		int iArg = 1 + _leadingParamSize; // 0 is "this"
		for (LetEntry entry : _closure) {
			if (isArray( entry )) {
				letDict().let( entry.name, entry.type, new LocalArrayRef( iArg ) );
				iArg++;
			}
			else {
				letDict().let( entry.name, entry.type, new LocalValueRef( iArg ) );
				iArg += section().engineCompiler().typeCompiler( entry.type ).type().getSize();
			}
		}
	}

	final void addClosureToLetDict( Iterable<LetEntry> _closure )
	{
		addClosureToLetDict( _closure, 0 );
	}


	final void compileClosure( Iterable<LetEntry> _closure ) throws CompilerException
	{
		for (LetEntry entry : _closure) {
			expressionCompiler( entry.type ).compileLetValue( entry.name, entry.value );
		}
	}

	final void compileCalleeAndClosure( Iterable<LetEntry> _closure ) throws CompilerException
	{
		mv().visitVarInsn( Opcodes.ALOAD, objectInContext() );
		compileClosure( _closure );
	}


	private int localsOffset = 0;

	protected final int localsOffset()
	{
		return this.localsOffset;
	}

	protected final void incLocalsOffset( int _by )
	{
		this.localsOffset += _by;
	}

	protected final void resetLocalsTo( int _to )
	{
		this.localsOffset = _to;
	}

	protected final int newLocal( int _size )
	{
		final int local = localsOffset();
		incLocalsOffset( _size );
		return local;
	}


	protected final void compileTableSwitch( int[] _keys, final TableSwitchGenerator _generator )
			throws CompilerException
	{
		try {
			mv().tableSwitch( _keys, new org.objectweb.asm.commons.TableSwitchGenerator()
			{

				public void generateCase( int _key, Label _end )
				{
					try {
						_generator.generateCase( _key, _end );
					}
					catch (CompilerException e) {
						throw new InnerException( e );
					}
				}

				public void generateDefault()
				{
					try {
						_generator.generateDefault();
					}
					catch (CompilerException e) {
						throw new InnerException( e );
					}
				}

			} );
		}
		catch (InnerException e) {
			throw (CompilerException) e.getCause();
		}

	}

	protected static abstract class TableSwitchGenerator
	{

		/**
		 * Generates the code for a switch case.
		 * 
		 * @param key the switch case key.
		 * @param end a label that corresponds to the end of the switch statement.
		 */
		protected abstract void generateCase( int key, Label end ) throws CompilerException;

		/**
		 * Generates the code for the default switch case.
		 */
		@SuppressWarnings( "unused" )
		protected void generateDefault() throws CompilerException
		{
			// fall through
		}

	}

	private static final class InnerException extends RuntimeException
	{

		public InnerException( Throwable _cause )
		{
			super( _cause );
		}

	}


	private Set<DelayedLet> trackedSetsOfOuterLets = null;
	private int letTrackingNestingLevel = 0;

	final Object beginTrackingSetsOfOuterLets()
	{
		final Object oldState = this.trackedSetsOfOuterLets;
		this.trackedSetsOfOuterLets = New.set();
		this.letTrackingNestingLevel++;
		return oldState;
	}

	@SuppressWarnings( "unchecked" )
	final Set<DelayedLet> endTrackingSetsOfOuterLets( Object _oldState )
	{
		final Set<DelayedLet> currentState = this.trackedSetsOfOuterLets;
		this.letTrackingNestingLevel--;
		this.trackedSetsOfOuterLets = (Set<DelayedLet>) _oldState;
		return currentState;
	}

	final Set<DelayedLet> trackedSetsOfOuterLets()
	{
		return Collections.unmodifiableSet( this.trackedSetsOfOuterLets );
	}

	final void trackSetOfLet( DelayedLet _let )
	{
		if (_let.nestingLevel < this.letTrackingNestingLevel) {
			this.trackedSetsOfOuterLets.add( _let );
		}
	}

	final int letTrackingNestingLevel()
	{
		return this.letTrackingNestingLevel;
	}


	protected final static boolean isArray( ExpressionNode _node )
	{
		if (_node instanceof ExpressionNodeForArrayReference) {
			return true;
		}
		else if (_node instanceof ExpressionNodeForMakeArray) {
			return true;
		}
		else {
			return false;
		}
	}

	protected final static boolean isArray( LetDictionary.LetEntry _e )
	{
		if (_e.value instanceof DelayedLet) {
			return ((DelayedLet) _e.value).isArray();
		}
		else if (_e.value instanceof LocalRef) {
			return ((LocalRef) _e.value).isArray();
		}
		else if (_e.value instanceof ExpressionNode) {
			return isArray( (ExpressionNode) _e.value );
		}
		else {
			return false;
		}
	}


	// LATER Might convert constructors to static getters reusing values.

	static abstract class LocalRef
	{
		final int offset;

		public LocalRef( int _offset )
		{
			this.offset = _offset;
		}

		public abstract boolean isArray();
	}

	static final class LocalValueRef extends LocalRef
	{

		public LocalValueRef( int _offset )
		{
			super( _offset );
		}

		@Override
		public String toString()
		{
			return "local_val(" + this.offset + ")";
		}

		@Override
		public boolean isArray()
		{
			return false;
		}

	}

	static final class LocalArrayRef extends LocalRef
	{

		public LocalArrayRef( int _offset )
		{
			super( _offset );
		}

		@Override
		public String toString()
		{
			return "local_array(" + this.offset + ")";
		}

		@Override
		public boolean isArray()
		{
			return true;
		}

	}

	static interface GeneratedRef
	{
		void compile( ExpressionCompiler _exp ) throws CompilerException;
	}

}
