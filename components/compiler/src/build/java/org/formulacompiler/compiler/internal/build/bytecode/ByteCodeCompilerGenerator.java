/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
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

package org.formulacompiler.compiler.internal.build.bytecode;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.formulacompiler.compiler.internal.DescriptionBuilder;
import org.formulacompiler.compiler.internal.IOUtil;
import org.formulacompiler.runtime.New;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.AbstractVisitor;


@SuppressWarnings( "unqualified-field-access" )
final class ByteCodeCompilerGenerator extends AbstractGenerator
{
	static final int INIT_BACKING_VAR_ON_ACCESS = -1;

	final Customization customization;

	public ByteCodeCompilerGenerator( Class _cls, String _typeName, String _superName, Customization _customization )
			throws IOException
	{
		super( _cls, _typeName, _superName );
		this.customization = _customization;
	}

	public ByteCodeCompilerGenerator( Class _cls, String _typeName, Customization _customization ) throws IOException
	{
		this( _cls, _typeName, "ExpressionCompilerFor" + _typeName + "_Base", _customization );
	}

	public ByteCodeCompilerGenerator( Class _cls, String _typeName ) throws IOException
	{
		this( _cls, _typeName, new Customization() );
	}


	public void generate( File _outputPath ) throws IOException
	{
		final String clsName = "ExpressionCompilerFor" + typeName + "_Generated";

		if (verbose) System.out.println( "Generating " + clsName + "..." );

		DescriptionBuilder cb = classBuilder;
		cb.appendLine( "/**" );
		cb.append( " * DO NOT MODIFY! This file is generated automatically from " ).append( this.cls.getName() )
				.appendLine( "." );
		cb.append( " * Generated using jdk-" ).append( System.getProperty( "java.version" ) ).appendLine( "." );
		cb.appendLine( " */" );
		cb.appendLine( "package org.formulacompiler.compiler.internal.bytecode;" );
		cb.newLine();
		cb.appendLine( "import org.objectweb.asm.Label;" );
		cb.appendLine( "import org.objectweb.asm.Opcodes;" );
		cb.appendLine( "import org.objectweb.asm.Type;" );
		cb.appendLine( "import org.objectweb.asm.commons.GeneratorAdapter;" );
		cb.newLine();
		cb.appendLine( "import org.formulacompiler.runtime.ComputationMode;" );
		cb.appendLine( "import org.formulacompiler.compiler.CompilerException;" );
		cb.appendLine( "import org.formulacompiler.compiler.Operator;" );
		cb.appendLine( "import org.formulacompiler.compiler.NumericType;" );
		cb.appendLine( "import org.formulacompiler.compiler.internal.expressions.ExpressionNode;" );
		cb.appendLine( "import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;" );
		cb.newLine();
		cb.newLine();
		cb.appendLine( "@SuppressWarnings(\"unused\")" );
		cb.appendLine( "abstract class " + clsName + " extends " + superName );
		cb.appendLine( "{" );
		cb.indent();

		cb.newLine();
		cb.append( "public " ).append( clsName ).append( "(MethodCompiler _methodCompiler" );
		customization.extendConstructorFormals( cb );
		cb.appendLine( ")" );
		cb.appendLine( "{" );
		cb.indent();
		cb.append( "super( _methodCompiler" );
		customization.extendConstructorArgumentsForSuper( cb );
		cb.appendLine( " );" );
		cb.outdent();
		cb.appendLine( "}" );

		genMethods();
		genOperatorDispatch();
		genFunctionDispatch();

		cb.outdent();
		cb.newLine();
		cb.appendLine( "}" );

		IOUtil.writeStringToIfNotUpToDateWithMessage( cb.toString(), new File( _outputPath, clsName + ".java" ) );

		if (verbose) {
			System.out.println( "Done generating " + clsName + "." );
			System.out.println();
		}
	}

	@Override
	protected final void genMethod( MethodNode _mtdNode )
	{
		if (_mtdNode.name.startsWith( "fun_" )) {
			new FunctionTemplateGenerator( _mtdNode ).generate();
		}
		else if (_mtdNode.name.startsWith( "op_" )) {
			new OperatorTemplateGenerator( _mtdNode ).generate();
		}
		else if (_mtdNode.name.startsWith( "util_" )) {
			new UtilTemplateGenerator( _mtdNode ).generate();
		}
		else if (_mtdNode.name.startsWith( "utilOp_" )) {
			new UtilOperatorTemplateGenerator( _mtdNode ).generate();
		}
		else if (_mtdNode.name.startsWith( "utilFun_" )) {
			new UtilFunctionTemplateGenerator( _mtdNode ).generate();
		}
		else if (_mtdNode.name.equals( "scanArray" )) {
			new ScanArrayTemplateGenerator( _mtdNode ).generate();
		}
		else if (_mtdNode.name.equals( "scanArrayWithFirst" )) {
			new ScanArrayWithFirstTemplateGenerator( _mtdNode ).generate();
		}
	}


	private void genOperatorDispatch()
	{
		if (this.unaryOperatorDispatchBuilder.length() == 0) return;

		final DescriptionBuilder cb = classBuilder;
		cb.newLine();
		cb.appendLine( "@Override" );
		cb
				.appendLine( "protected void compileOperatorWithFirstArgOnStack( Operator _operator, ExpressionNode _secondArg ) throws CompilerException" );
		cb.appendLine( "{" );
		cb.indent();

		cb.appendLine( "if (null == _secondArg) {" );
		cb.indent();
		cb.appendLine( "switch (_operator) {" );
		cb.indent();

		cb.appendUnindented( this.unaryOperatorDispatchBuilder.toString() );

		cb.outdent();
		cb.appendLine( "}" );
		cb.outdent();
		cb.appendLine( "}" );
		cb.appendLine( "else {" );
		cb.indent();
		cb.appendLine( "switch (_operator) {" );
		cb.indent();

		cb.appendUnindented( this.binaryOperatorDispatchBuilder.toString() );

		cb.outdent();
		cb.appendLine( "}" );
		cb.outdent();
		cb.appendLine( "}" );

		cb.appendLine( "super.compileOperatorWithFirstArgOnStack( _operator, _secondArg );" );
		cb.outdent();
		cb.appendLine( "}" );
	}


	private void genFunctionDispatch()
	{
		if (this.functionDispatchBuilder.length() == 0) return;

		final DescriptionBuilder cb = classBuilder;
		cb.newLine();
		cb.appendLine( "@Override" );
		cb.appendLine( "protected void compileFunction( ExpressionNodeForFunction _node ) throws CompilerException" );
		cb.appendLine( "{" );
		cb.indent();

		cb.appendLine( "final int c = _node.cardinality();" );
		cb.appendLine( "switch (_node.getFunction()) {" );
		cb.indent();

		cb.appendUnindented( this.functionDispatchBuilder.toString() );

		cb.outdent();
		cb.appendLine( "}" );

		cb.appendLine( "super.compileFunction( _node );" );
		cb.outdent();
		cb.appendLine( "}" );
	}


	abstract class TemplateMethodGenerator extends AbstractMethodTemplateGenerator
	{
		final int firstVarInLocals;
		final List<Label> labels = New.list();
		final Iterator insns;
		final Map<String, Object> defs = New.map();

		int sizeOfLocals;
		int sizeOfVarsInLocals;
		int[] backingVars;

		public TemplateMethodGenerator( MethodNode _mtdNode )
		{
			super( _mtdNode );
			this.sizeOfLocals = mtdNode.maxLocals;
			this.firstVarInLocals = 1 + totalSizeOf( argTypes ); // add 1 for "this"
			this.sizeOfVarsInLocals = sizeOfLocals - firstVarInLocals;
			this.backingVars = new int[ cardinality ];
			this.insns = _mtdNode.instructions.iterator();
		}

		private final int totalSizeOf( Type[] _argTypes )
		{
			int result = 0;
			for (Type t : _argTypes)
				result += t.getSize();
			return result;
		}


		public void generate()
		{
			if (verbose) System.out.println( "  " + mtdNode.name );

			final DescriptionBuilder cb = classBuilder;
			cb.newLine();
			cb.append( visibility() ).append( " final void compile_" ).append( mtdNode.name );
			genParams();
			cb.appendLine( " throws CompilerException" );
			cb.appendLine( "{" );
			cb.indent();
			cb.appendLine( "final GeneratorAdapter mv = mv();" );
			cb.appendLine( "final int loc = localsOffset();" );
			genLocalOffsetInc();

			scanForArgsNeedingBackingVars();
			genLabels();
			genInsns();
			customization.genValueAdjustment( classBuilder, this );

			cb.outdent();
			cb.appendLine( "}" );

			genDispatch();
		}


		protected String visibility()
		{
			return "private";
		}


		protected void genParams()
		{
			final DescriptionBuilder cb = classBuilder;
			int iArg = firstArg();
			if (iArg >= argTypes.length) {
				cb.append( "()" );
			}
			else {
				cb.append( "( " ).append( "ExpressionNode _" ).append( paramName( iArg++ ) );
				while (iArg < argTypes.length) {
					cb.append( ", ExpressionNode _" ).append( paramName( iArg++ ) );
				}
				cb.append( " )" );
			}
		}


		protected int firstArg()
		{
			return 0;
		}


		protected abstract void genDispatch();


		private void genLabels()
		{
			final DescriptionBuilder cb = classBuilder;
			int iLabel = 0;
			for (Object insnObj : mtdNode.instructions.toArray()) {
				if (insnObj instanceof JumpInsnNode) {
					final JumpInsnNode jumpNode = (JumpInsnNode) insnObj;
					final Label label = jumpNode.label.getLabel();
					if (!labels.contains( label )) {
						labels.add( label );
						cb.append( "Label l_" ).append( iLabel++ ).appendLine( " = new Label();" );
					}
				}
			}
		}


		private void scanForArgsNeedingBackingVars()
		{
			final boolean[] seen = new boolean[ cardinality ];
			final Iterator insns = mtdNode.instructions.iterator();
			while (insns.hasNext()) {
				final AbstractInsnNode insnNode = (AbstractInsnNode) insns.next();
				switch (insnNode.getOpcode()) {
					case Opcodes.ILOAD:
					case Opcodes.DLOAD:
					case Opcodes.FLOAD:
					case Opcodes.LLOAD:
					case Opcodes.ALOAD:
						final VarInsnNode varNode = (VarInsnNode) insnNode;
						if (0 < varNode.var && varNode.var < firstVarInLocals) {
							final int paramIdx = paramIdxOfLocalAt( varNode.var );
							if (seen[ paramIdx ]) {
								backingVars[ paramIdx ] = INIT_BACKING_VAR_ON_ACCESS;
							}
							else {
								seen[ paramIdx ] = true;
							}
						}
						break;
				}
			}
		}


		private AbstractInsnNode next;

		private void genInsns()
		{
			// Loop through iterator with look-ahead of 1.
			// Assumes at least one element is present (RETURN).
			assert insns.hasNext();

			this.next = (AbstractInsnNode) insns.next();
			while (null != this.next) {
				final AbstractInsnNode insnNode = nextInsn();

				skipInsn();
				switch (insnNode.getOpcode()) {

					case Opcodes.ILOAD:
					case Opcodes.DLOAD:
					case Opcodes.FLOAD:
					case Opcodes.LLOAD:
					case Opcodes.ALOAD: {
						final VarInsnNode varNode = (VarInsnNode) insnNode;
						if (0 == varNode.var) {
							genThisInsn();
						}
						else if (varNode.var < firstVarInLocals) {
							final int paramIdx = paramIdxOfLocalAt( varNode.var );
							genLoadParamInsn( paramIdx, insnNode.getOpcode() );
						}
						else {
							genAbstractInsn( insnNode );
						}
						break;
					}

					case Opcodes.IRETURN:
					case Opcodes.DRETURN:
					case Opcodes.FRETURN:
					case Opcodes.LRETURN:
					case Opcodes.ARETURN:
					case Opcodes.RETURN: {
						while (insns.hasNext()) {
							skipInsn();
							if (isReturn( nextInsn() )) {
								throw new IllegalArgumentException( "The method "
										+ mtdNode.name
										+ " has multiple returns. Use an 'extract local variable' refactoring to fix this." );
							}
						}
						genFinalValueConversion();
						return;
					}

					default:
						genAbstractInsn( insnNode );
				}
			}
		}

		protected void genFinalValueConversion()
		{
			// Overridable
		}

		protected AbstractInsnNode nextInsn()
		{
			return this.next;
		}

		protected void skipInsn()
		{
			this.next = (this.insns.hasNext()) ? (AbstractInsnNode) this.insns.next() : null;
		}

		private boolean isReturn( AbstractInsnNode _insn )
		{
			switch (_insn.getOpcode()) {
				case Opcodes.IRETURN:
				case Opcodes.DRETURN:
				case Opcodes.FRETURN:
				case Opcodes.LRETURN:
				case Opcodes.ARETURN:
				case Opcodes.RETURN:
					return true;
				default:
					return false;
			}
		}


		private int paramIdxOfLocalAt( int _at )
		{
			int iPar = 0;
			int atPar = 1; // skip "this"
			while (atPar < _at) {
				atPar += argTypes[ iPar++ ].getSize();
			}
			return iPar;
		}


		protected void genThisInsn()
		{
			// Drop references to this. If followed by member access, compile as context
			// field access.
			if (next instanceof FieldInsnNode) {
				genLoadContextFieldInsn( (FieldInsnNode) next );
				skipInsn();
			}
		}

		private void genLoadContextFieldInsn( FieldInsnNode _node )
		{
			classBuilder.append( "compile_" ).append( _node.name ).appendLine( "();" );
		}


		private void genLoadParamInsn( int _paramIdx, int _opcode )
		{
			final DescriptionBuilder cb = classBuilder;
			int backingVar = backingVars[ _paramIdx ];
			if (backingVar == 0) {
				genFirstAccessToParamInsn( _paramIdx );
			}
			else {
				final Type type = argTypes[ _paramIdx ];
				if (backingVar == INIT_BACKING_VAR_ON_ACCESS) {
					backingVar = genBackingVar( _paramIdx );
					genFirstAccessToParamInsn( _paramIdx );
					final int dup = type.getSize() > 1 ? Opcodes.DUP2 : Opcodes.DUP;
					final int store = type.getOpcode( Opcodes.ISTORE );
					cb.append( "mv.visitInsn( Opcodes." ).append( AbstractVisitor.OPCODES[ dup ] ).appendLine( " );" );
					cb.append( "mv.visitVarInsn( Opcodes." ).append( AbstractVisitor.OPCODES[ store ] ).append( ", " );
					genVar( backingVar );
					cb.appendLine( " );" );
				}
				else {
					final int load = type.getOpcode( Opcodes.ILOAD );
					cb.append( "mv.visitVarInsn( Opcodes." ).append( AbstractVisitor.OPCODES[ load ] ).append( ", " );
					genVar( backingVar );
					cb.appendLine( " );" );
				}
			}
		}

		protected void genFirstAccessToParamInsn( int _paramIdx )
		{
			genOveridableCompilationOfArgNode( _paramIdx );
		}

		protected void genOveridableCompilationOfArgNode( int _paramIdx )
		{
			genCompilationOfArgNode( _paramIdx );
		}

		protected void genCompilationOfArgNode( int _paramIdx )
		{
			classBuilder.append( "compile( _" ).append( paramName( _paramIdx ) ).appendLine( " );" );
		}

		protected void genOwnMethodInsn( MethodInsnNode _node )
		{
			throw new IllegalArgumentException( "Cannot handle calls to own methods in this context." );
		}

		private int genBackingVar( int _paramIdx )
		{
			backingVars[ _paramIdx ] = sizeOfLocals;
			final int size = argTypes[ _paramIdx ].getSize();
			sizeOfLocals += size;
			sizeOfVarsInLocals += size;
			return backingVars[ _paramIdx ];
		}


		private void genAbstractInsn( AbstractInsnNode _insnNode )
		{
			if (_insnNode instanceof InsnNode) genInsn( (InsnNode) _insnNode );
			else if (_insnNode instanceof VarInsnNode) genInsn( (VarInsnNode) _insnNode );
			else if (_insnNode instanceof IincInsnNode) genInsn( (IincInsnNode) _insnNode );
			else if (_insnNode instanceof JumpInsnNode) genInsn( (JumpInsnNode) _insnNode );
			else if (_insnNode instanceof LabelNode) genInsn( (LabelNode) _insnNode );
			else if (_insnNode instanceof IntInsnNode) genInsn( (IntInsnNode) _insnNode );
			else if (_insnNode instanceof LdcInsnNode) genInsn( (LdcInsnNode) _insnNode );
			else if (_insnNode instanceof MethodInsnNode) genInsn( (MethodInsnNode) _insnNode );
			else if (_insnNode instanceof FieldInsnNode) genInsn( (FieldInsnNode) _insnNode );
			else if (_insnNode instanceof TypeInsnNode) genInsn( (TypeInsnNode) _insnNode );
			// LookupSwitch
			// MultiANewArray
			// TableSwitch
		}

		private void genInsn( InsnNode _node )
		{
			startInsn( _node );
			endInsn();
		}

		private void genInsn( VarInsnNode _node )
		{
			startInsn( _node );
			classBuilder.append( ", " );
			genVar( _node.var );
			endInsn();
		}

		private void genInsn( IincInsnNode _node )
		{
			final DescriptionBuilder cb = classBuilder;
			cb.append( "mv.visitIincInsn( " );
			genVar( _node.var );
			cb.append( ", " ).append( _node.incr );
			endInsn();
		}

		private void genInsn( JumpInsnNode _node )
		{
			startInsn( _node );
			classBuilder.append( ", l_" ).append( labels.indexOf( _node.label.getLabel() ) );
			endInsn();
		}

		private void genInsn( LabelNode _node )
		{
			classBuilder.append( "mv.visitLabel( l_" ).append( labels.indexOf( _node.getLabel() ) ).appendLine( " );" );
		}

		private void genInsn( IntInsnNode _node )
		{
			startInsn( _node );
			classBuilder.append( ", " ).append( _node.operand );
			endInsn();
		}

		private void genInsn( LdcInsnNode _node )
		{
			final DescriptionBuilder cb = classBuilder;
			cb.append( "mv.visitLdcInsn( " );
			ASMHelpers.appendConstant( cb, _node.cst );
			endInsn();
		}

		private void genInsn( MethodInsnNode _node )
		{
			if (_node.owner == clsNode.name) {
				genOwnMethodInsn( _node );
			}
			else {
				startInsn( _node );
				classBuilder.append( ", \"" ).append( _node.owner ).append( "\", \"" ).append( _node.name ).append(
						"\", \"" ).append( _node.desc ).append( "\"" );
				endInsn();
			}
		}

		private void genInsn( FieldInsnNode _node )
		{
			startInsn( _node );
			classBuilder.append( ", \"" ).append( _node.owner ).append( "\", \"" ).append( _node.name ).append( "\", \"" )
					.append( _node.desc ).append( "\"" );
			endInsn();
		}

		private void genInsn( TypeInsnNode _node )
		{
			startInsn( _node );
			classBuilder.append( ", \"" ).append( _node.desc ).append( "\"" );
			endInsn();
		}


		private void startInsn( AbstractInsnNode _node )
		{
			final String clsName = _node.getClass().getSimpleName();
			final String mtdName = clsName.substring( 0, clsName.length() - 4 ); // drop "...Node"
			classBuilder.append( "mv.visit" ).append( mtdName ).append( "( Opcodes." ).append(
					AbstractVisitor.OPCODES[ _node.getOpcode() ] );
		}

		private void endInsn()
		{
			classBuilder.appendLine( " );" );
		}


		protected char paramName( int _i )
		{
			return (char) ('a' + _i);
		}


		protected void genVar( int _var )
		{
			classBuilder.append( _var - firstVarInLocals ).append( " + loc" );
		}


		private void genLocalOffsetInc()
		{
			if (sizeOfVarsInLocals > 0) {
				classBuilder.append( "incLocalsOffset( " ).append( sizeOfVarsInLocals ).appendLine( " );" );
			}
		}


	}


	class FunctionTemplateGenerator extends TemplateMethodGenerator
	{

		public FunctionTemplateGenerator( MethodNode _mtdNode )
		{
			super( _mtdNode );
		}


		@Override
		protected void genDispatch()
		{
			final DispatchBuilder db = functionDispatchBuilder;
			if (db.genDispatchCase( enumName )) {
				db.pending = "   break;";
			}
			db.indent();
			db.append( "if (" ).append( cardinality ).append( " == c" );
			if (this.computationMode != null) {
				db.append( " && ComputationMode." );
				db.append( this.computationMode );
				db.append( " == getComputationMode()" );
			}
			db.appendLine( " ) {" );
			db.indent();
			db.genDispatchIf( ifCond );
			db.append( "compile_" ).append( mtdNode.name );
			if (0 == cardinality) {
				db.appendLine( "();" );
			}
			else {
				db.append( "( _node.argument( 0 )" );
				for (int iArg = 1; iArg < cardinality; iArg++) {
					db.append( ", _node.argument( " ).append( iArg ).append( " )" );
				}
				db.appendLine( " );" );
			}
			db.appendLine( "return;" );
			db.genDispatchEndIf( ifCond );
			db.outdent();
			db.appendLine( "}" );
			db.outdent();
		}

		@Override
		protected void genOveridableCompilationOfArgNode( int _paramIdx )
		{
			customization.genCompilationOfArgNode( classBuilder, this, _paramIdx, argTypes[ _paramIdx ] );
		}

		@Override
		protected void genFinalValueConversion()
		{
			customization.genCompilationOfFinalValueConversion( classBuilder, this, returnType );
		}

	}


	abstract class ImplicitFirstArgTemplateGenerator extends TemplateMethodGenerator
	{

		public ImplicitFirstArgTemplateGenerator( MethodNode _mtdNode )
		{
			super( _mtdNode );
		}

		@Override
		protected int firstArg()
		{
			return 1;
		}

		boolean firstParamAccess = true;

		@Override
		protected void genFirstAccessToParamInsn( int _paramIdx )
		{
			if (firstParamAccess) {
				if (0 != _paramIdx) {
					throw new IllegalArgumentException( "Operator does not access first argument first in "
							+ mtdNode.name + mtdNode.desc );
				}
				firstParamAccess = false;
			}
			else {
				genFirstAccessToExplicitParamInsn( _paramIdx );
			}
		}

		protected void genFirstAccessToExplicitParamInsn( int _paramIdx )
		{
			super.genFirstAccessToParamInsn( _paramIdx );
		}

	}


	class OperatorTemplateGenerator extends ImplicitFirstArgTemplateGenerator
	{

		public OperatorTemplateGenerator( MethodNode _mtdNode )
		{
			super( _mtdNode );
		}

		@Override
		protected void genDispatch()
		{
			if (1 == cardinality) {
				final DispatchBuilder db = unaryOperatorDispatchBuilder;
				genDispatch( db, "" );
			}
			else if (2 == cardinality) {
				final DispatchBuilder db = binaryOperatorDispatchBuilder;
				genDispatch( db, " _secondArg " );
			}
			else throw new IllegalArgumentException( "Operator cannot have more than 2 arguments in "
					+ mtdNode.name + mtdNode.desc );
		}

		private void genDispatch( DispatchBuilder _dispatcher, String _params )
		{
			_dispatcher.genDispatchCase( enumName );
			_dispatcher.indent();
			_dispatcher.genDispatchIf( ifCond );
			_dispatcher.append( "compile_" ).append( mtdNode.name ).append( "(" ).append( _params ).appendLine( ");" );
			_dispatcher.appendLine( "return;" );
			_dispatcher.genDispatchEndIf( ifCond );
			_dispatcher.outdent();
		}

		@Override
		protected void genOveridableCompilationOfArgNode( int _paramIdx )
		{
			customization.genCompilationOfArgNode( classBuilder, this, _paramIdx, argTypes[ _paramIdx ] );
		}

		@Override
		protected void genFinalValueConversion()
		{
			customization.genCompilationOfFinalValueConversion( classBuilder, this, returnType );
		}

	}


	class UtilOperatorTemplateGenerator extends ImplicitFirstArgTemplateGenerator
	{

		public UtilOperatorTemplateGenerator( MethodNode _mtdNode )
		{
			super( _mtdNode );
		}

		@Override
		protected String visibility()
		{
			return "protected";
		}

		@Override
		protected void genDispatch()
		{
			// No automatic dispatch for utils.
		}

	}


	class UtilFunctionTemplateGenerator extends TemplateMethodGenerator
	{

		public UtilFunctionTemplateGenerator( MethodNode _mtdNode )
		{
			super( _mtdNode );
		}

		@Override
		protected String visibility()
		{
			return "protected";
		}

		@Override
		protected void genDispatch()
		{
			// No automatic dispatch for utils.
		}

	}


	class UtilTemplateGenerator extends ImplicitFirstArgTemplateGenerator
	{

		public UtilTemplateGenerator( MethodNode _mtdNode )
		{
			super( _mtdNode );
		}

		@Override
		protected String visibility()
		{
			return "protected";
		}

		@Override
		protected void genParams()
		{
			final DescriptionBuilder cb = classBuilder;
			int iArg = firstArg();
			if (iArg >= argTypes.length) {
				cb.append( "()" );
			}
			else {
				cb.append( "( " ).append( argTypes[ iArg ].getClassName() ).append( " _" ).append( paramName( iArg++ ) );
				while (iArg < argTypes.length) {
					cb.append( ", " ).append( argTypes[ iArg ].getClassName() ).append( " _" ).append( paramName( iArg++ ) );
				}
				cb.append( " )" );
			}
		}

		@Override
		protected void genFirstAccessToExplicitParamInsn( int _paramIdx )
		{
			classBuilder.append( "mv.push( _" ).append( paramName( _paramIdx ) ).appendLine( " );" );
		}

		@Override
		protected void genDispatch()
		{
			// No automatic dispatch for utils.
		}

	}


	class ScanArrayTemplateGenerator extends UtilTemplateGenerator
	{

		public ScanArrayTemplateGenerator( MethodNode _mtdNode )
		{
			super( _mtdNode );
		}

		@Override
		protected void genParams()
		{
			classBuilder.append( "( ForEachElementCompilation _forElement )" );
		}

		@Override
		protected void genThisInsn()
		{
			classBuilder.append( "_forElement.compile( " );
			genVar( ((VarInsnNode) nextInsn()).var );
			classBuilder.appendLine( " );" );
			skipInsn(); // xLOAD elt
			if (!((MethodInsnNode) nextInsn()).name.equals( "scanElement" ))
				throw new IllegalArgumentException( "scanElement() expected" );
			skipInsn(); // call
		}

	}


	class ScanArrayWithFirstTemplateGenerator extends UtilTemplateGenerator
	{

		public ScanArrayWithFirstTemplateGenerator( MethodNode _mtdNode )
		{
			super( _mtdNode );
		}

		@Override
		protected void genParams()
		{
			classBuilder.append( "( ForEachElementWithFirstCompilation _forElement )" );
		}

		@Override
		protected void genThisInsn()
		{
			if (nextInsn() instanceof MethodInsnNode) {
				final String mtdName = ((MethodInsnNode) nextInsn()).name;
				skipInsn(); // call
				if (mtdName.equals( "isFirst" )) classBuilder.appendLine( "_forElement.compileIsFirst();" );
				else if (mtdName.equals( "haveFirst" )) classBuilder.appendLine( "_forElement.compileHaveFirst();" );
				else throw new IllegalArgumentException( "isFirst() or haveFirst() expected" );
			}
			else {
				final int var = ((VarInsnNode) nextInsn()).var;
				skipInsn(); // xLOAD elt
				final String mtdName = ((MethodInsnNode) nextInsn()).name;
				skipInsn(); // call
				if (mtdName.equals( "scanFirst" )) classBuilder.append( "_forElement.compileFirst( " );
				else if (mtdName.equals( "scanElement" )) classBuilder.append( "_forElement.compileElement( " );
				else throw new IllegalArgumentException( "isFirst() or haveFirst() expected" );
				genVar( var );
				classBuilder.appendLine( " );" );
			}
		}

	}


	static class Customization
	{

		protected void extendConstructorFormals( DescriptionBuilder _cb )
		{
			// Overridable
		}

		protected void extendConstructorArgumentsForSuper( DescriptionBuilder _cb )
		{
			// Overridable
		}

		protected void genValueAdjustment( DescriptionBuilder _cb, TemplateMethodGenerator _generator )
		{
			// Overridable
		}

		protected void genCompilationOfArgNode( DescriptionBuilder _cb, TemplateMethodGenerator _generator,
				int _paramIdx, Type _paramType )
		{
			_generator.genCompilationOfArgNode( _paramIdx );
		}

		protected void genCompilationOfFinalValueConversion( DescriptionBuilder _cb, TemplateMethodGenerator _generator,
				Type _returnType )
		{
			// Overridable
		}

	}

}