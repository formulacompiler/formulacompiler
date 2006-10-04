/*
 * Copyright � 2006 by Abacus Research AG, Switzerland.
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
package sej.internal.build.bytecode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.AbstractVisitor;

import sej.describable.DescriptionBuilder;

@SuppressWarnings("unqualified-field-access")
final class ByteCodeCompilerGenerator
{
	static final String IF_CLAUSE = "__if_";
	static final int IF_CLAUSE_LEN = IF_CLAUSE.length();
	static final int INIT_BACKING_VAR_ON_ACCESS = -1;

	final DescriptionBuilder classBuilder = new DescriptionBuilder();
	final DescriptionBuilder unaryOperatorDispatchBuilder = new DescriptionBuilder();
	final DescriptionBuilder binaryOperatorDispatchBuilder = new DescriptionBuilder();
	final DescriptionBuilder functionDispatchBuilder = new DescriptionBuilder();
	final String superName;
	final String typeName;
	final boolean adjustValues;
	final Class cls;
	final ClassNode clsNode;

	public ByteCodeCompilerGenerator(PatternCompiler _compiler, Class _cls, String _typeName, String _superName,
			boolean _adjustValues) throws IOException
	{
		this.superName = _superName;
		this.typeName = _typeName;
		this.adjustValues = _adjustValues;
		this.cls = _cls;
		this.clsNode = new ClassNode();
		new ClassReader( _cls.getCanonicalName() ).accept( clsNode, true );

		this.unaryOperatorDispatchBuilder.indent( 4 );
		this.binaryOperatorDispatchBuilder.indent( 4 );
		this.functionDispatchBuilder.indent( 3 );
	}

	public ByteCodeCompilerGenerator(PatternCompiler _compiler, Class _cls, String _typeName, boolean _adjustValues)
			throws IOException
	{
		this( _compiler, _cls, _typeName, "ExpressionCompilerFor" + _typeName + "_Base", _adjustValues );
	}

	public ByteCodeCompilerGenerator(PatternCompiler _compiler, Class _cls, String _typeName) throws IOException
	{
		this( _compiler, _cls, _typeName, false );
	}


	public void generate( File _outputPath ) throws IOException
	{
		final String clsName = "ExpressionCompilerFor" + typeName + "_Generated";

		System.out.println( "Generating " + clsName + "..." );

		DescriptionBuilder cb = classBuilder;
		cb.appendLine( "package sej.internal.bytecode.compiler;" );
		cb.newLine();
		cb.appendLine( "import org.objectweb.asm.Label;" );
		cb.appendLine( "import org.objectweb.asm.Opcodes;" );
		cb.appendLine( "import org.objectweb.asm.commons.GeneratorAdapter;" );
		cb.newLine();
		cb.appendLine( "import sej.CompilerException;" );
		cb.appendLine( "import sej.Operator;" );
		cb.appendLine( "import sej.NumericType;" );
		cb.appendLine( "import sej.internal.expressions.ExpressionNode;" );
		cb.appendLine( "import sej.internal.expressions.ExpressionNodeForFunction;" );
		cb.newLine();
		cb.newLine();
		cb.appendLine( "@SuppressWarnings(\"unused\")" );
		cb.appendLine( "abstract class " + clsName + " extends " + superName );
		cb.appendLine( "{" );
		cb.indent();

		cb.newLine();
		cb.append( "public " ).append( clsName )
				.appendLine( "(MethodCompiler _methodCompiler, NumericType _numericType)" );
		cb.appendLine( "{" );
		cb.indent();
		cb.appendLine( "super( _methodCompiler, _numericType );" );
		cb.outdent();
		cb.appendLine( "}" );

		genMethods();
		genOperatorDispatch();
		genFunctionDispatch();

		cb.outdent();
		cb.newLine();
		cb.appendLine( "}" );

		Util.writeStringTo( cb.toString(), new File( _outputPath, clsName + ".java" ) );

		System.out.println( "Done generating " + clsName + "." );
		System.out.println();
	}


	private void genMethods()
	{
		for (Object mtdObj : clsNode.methods) {
			final MethodNode mtdNode = (MethodNode) mtdObj;
			if (mtdNode.name.startsWith( "op_" )) {
				new OperatorTemplateGenerator( mtdNode ).generate();
			}
			else if (mtdNode.name.startsWith( "fun_" )) {
				new FunctionTemplateGenerator( mtdNode ).generate();
			}
			else if (mtdNode.name.startsWith( "util_" )) {
				new UtilTemplateGenerator( mtdNode ).generate();
			}
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

		cb.appendLine( "switch (_node.getFunction()) {" );
		cb.indent();

		cb.appendUnindented( this.functionDispatchBuilder.toString() );

		cb.outdent();
		cb.appendLine( "}" );

		cb.appendLine( "super.compileFunction( _node );" );
		cb.outdent();
		cb.appendLine( "}" );
	}


	private String lastDispatch = "";
	private Object lastDispatcher = null;

	protected void genDispatchCase( DescriptionBuilder _dispatcher, String _enumName )
	{
		if (lastDispatcher != _dispatcher || !lastDispatch.equals( _enumName )) {
			_dispatcher.append( "case " ).append( _enumName ).appendLine( ":" );
			lastDispatch = _enumName;
			lastDispatcher = _dispatcher;
		}
	}

	protected void genDispatchIf( DescriptionBuilder _dispatcher, String _ifCond )
	{
		if (null != _ifCond) {
			_dispatcher.append( "if (" ).append( _ifCond ).appendLine( "()) {" );
			_dispatcher.indent();
		}
	}

	protected void genDispatchEndIf( DescriptionBuilder _dispatcher, String _ifCond )
	{
		if (null != _ifCond) {
			_dispatcher.outdent();
			_dispatcher.appendLine( "}" );
		}
	}


	abstract class TemplateMethodGenerator
	{
		final MethodNode mtdNode;
		final Type[] argTypes;
		final int cardinality;
		final int firstVarInLocals;
		final List<Label> labels = new ArrayList<Label>();
		final String enumName;
		final String ifCond;

		int sizeOfLocals;
		int sizeOfVarsInLocals;
		int[] backingVars;

		public TemplateMethodGenerator(MethodNode _mtdNode)
		{
			this.mtdNode = _mtdNode;
			this.argTypes = Type.getArgumentTypes( mtdNode.desc );
			this.cardinality = argTypes.length;
			this.sizeOfLocals = mtdNode.maxLocals;
			this.firstVarInLocals = 1 + totalSizeOf( argTypes ); // add 1 for "this"
			this.sizeOfVarsInLocals = sizeOfLocals - firstVarInLocals;
			this.backingVars = new int[ cardinality ];
			// split name
			final String n = _mtdNode.name;
			final int p = n.indexOf( '_' );
			if (p < 0) {
				this.enumName = "";
				this.ifCond = "";
			}
			else {
				final String s = n.substring( p + 1 );
				final int pp = s.indexOf( IF_CLAUSE );
				if (pp < 0) {
					this.enumName = s;
					this.ifCond = null;
				}
				else {
					this.enumName = s.substring( 0, pp );
					this.ifCond = s.substring( pp + IF_CLAUSE_LEN );
				}
			}
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
			final DescriptionBuilder cb = classBuilder;
			cb.newLine();
			cb.append( visibility() ).append( " final void compile_" ).append( mtdNode.name );
			genParams();
			cb.appendLine( " throws CompilerException" );
			cb.appendLine( "{" );
			cb.indent();
			cb.appendLine( "final GeneratorAdapter mv = mv();" );

			scanForArgsNeedingBackingVars();
			genLabels();
			genInsns();
			if (adjustValues) genValueAdjustment();
			genLocalOffsetInc();

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
			for (Object insnObj : mtdNode.instructions) {
				if (insnObj instanceof JumpInsnNode) {
					final JumpInsnNode jumpNode = (JumpInsnNode) insnObj;
					final Label label = jumpNode.label;
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

		private void genInsns()
		{
			final Iterator insns = mtdNode.instructions.iterator();
			while (insns.hasNext()) {
				final AbstractInsnNode insnNode = (AbstractInsnNode) insns.next();
				switch (insnNode.getOpcode()) {

					case Opcodes.ILOAD:
					case Opcodes.DLOAD:
					case Opcodes.LLOAD:
					case Opcodes.ALOAD: {
						final VarInsnNode varNode = (VarInsnNode) insnNode;
						if (0 == varNode.var) {
							genLoadContextFieldInsn( (FieldInsnNode) insns.next() );
						}
						else if (varNode.var < firstVarInLocals) {
							final int paramIdx = paramIdxOfLocalAt( varNode.var );
							genLoadParamInsn( paramIdx );
						}
						else {
							genAbstractInsn( insnNode );
						}
						break;
					}

					case Opcodes.IRETURN:
					case Opcodes.DRETURN:
					case Opcodes.LRETURN:
					case Opcodes.ARETURN:
						return;

					default:
						genAbstractInsn( insnNode );
				}
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


		private void genLoadContextFieldInsn( FieldInsnNode _node )
		{
			classBuilder.append( "compile_" ).append( _node.name ).appendLine( "();" );
		}


		private void genLoadParamInsn( int _paramIdx )
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
			classBuilder.append( "compile( _" ).append( paramName( _paramIdx ) ).appendLine( " );" );
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
			// field
			// LookupSwitch
			// MultiANewArray
			// TableSwitch
			// Type
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
			startInsn( _node );
			classBuilder.append( ", " ).append( _node.incr ).append( ", " );
			genVar( _node.var );
			endInsn();
		}

		private void genInsn( JumpInsnNode _node )
		{
			startInsn( _node );
			classBuilder.append( ", l_" ).append( labels.indexOf( _node.label ) );
			endInsn();
		}

		private void genInsn( LabelNode _node )
		{
			classBuilder.append( "mv.visitLabel( l_" ).append( labels.indexOf( _node.label ) ).appendLine( " );" );
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
			startInsn( _node );
			classBuilder.append( ", \"" ).append( _node.owner ).append( "\", \"" ).append( _node.name ).append( "\", \"" )
					.append( _node.desc ).append( "\"" );
			endInsn();
		}

		private void genInsn( FieldInsnNode _node )
		{
			startInsn( _node );
			classBuilder.append( ", \"" ).append( _node.owner ).append( "\", \"" ).append( _node.name ).append( "\", \"" )
					.append( _node.desc ).append( "\"" );
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


		private void genVar( int _var )
		{
			classBuilder.append( _var - firstVarInLocals ).append( " + localsOffset()" );
		}


		private void genValueAdjustment()
		{
			if (!hasAnnotation( mtdNode, "Lsej/internal/templates/ReturnsAdjustedValue;" )) {
				classBuilder.appendLine( "compileValueAdjustment();" );
			}
		}

		private boolean hasAnnotation( MethodNode _mtdNode, String _className )
		{
			if (null != _mtdNode.invisibleAnnotations) {
				for (final Object annObj : _mtdNode.invisibleAnnotations) {
					final AnnotationNode annNode = (AnnotationNode) annObj;
					if (annNode.desc.equals( _className )) {
						return true;
					}
				}
			}
			return false;
		}


		private void genLocalOffsetInc()
		{
			if (sizeOfVarsInLocals > 0) {
				classBuilder.append( "incLocalsOffset( " ).append( sizeOfVarsInLocals ).appendLine( " );" );
			}
		}


	}


	class OperatorTemplateGenerator extends TemplateMethodGenerator
	{

		public OperatorTemplateGenerator(MethodNode _mtdNode)
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
				super.genFirstAccessToParamInsn( _paramIdx );
			}
		}


		@Override
		protected void genDispatch()
		{
			if (1 == cardinality) {
				final DescriptionBuilder db = unaryOperatorDispatchBuilder;
				genDispatch( db, "" );
			}
			else if (2 == cardinality) {
				final DescriptionBuilder db = binaryOperatorDispatchBuilder;
				genDispatch( db, " _secondArg " );
			}
			else throw new IllegalArgumentException( "Operator cannot have more than 2 arguments in "
					+ mtdNode.name + mtdNode.desc );
		}

		private void genDispatch( DescriptionBuilder _dispatcher, String _params )
		{
			genDispatchCase( _dispatcher, enumName );
			_dispatcher.indent();
			genDispatchIf( _dispatcher, ifCond );
			_dispatcher.append( "compile_" ).append( mtdNode.name ).append( "(" ).append( _params ).appendLine( ");" );
			_dispatcher.appendLine( "return;" );
			genDispatchEndIf( _dispatcher, ifCond );
			_dispatcher.outdent();
		}

	}


	class FunctionTemplateGenerator extends TemplateMethodGenerator
	{

		public FunctionTemplateGenerator(MethodNode _mtdNode)
		{
			super( _mtdNode );
		}


		@Override
		protected void genDispatch()
		{
			final DescriptionBuilder db = functionDispatchBuilder;
			genDispatchCase( db, enumName );
			db.indent();

			db.appendLine( "switch (_node.cardinality()) {" );
			db.indent();
			db.append( "case " ).append( cardinality ).appendLine( ":" );
			db.indent();
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
			db.outdent();
			db.outdent();
			db.appendLine( "}" );
			db.appendLine( "break;" );
			db.outdent();
		}
	}


	class UtilTemplateGenerator extends TemplateMethodGenerator
	{

		public UtilTemplateGenerator(MethodNode _mtdNode)
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
			int iArg = 1;
			if (iArg >= argTypes.length) {
				cb.append( "()" );
			}
			else {
				cb.append( "( " ).append( argTypes[ iArg ].getClassName() ).append( " _" ).append( paramName( iArg++ ) );
				while (iArg < argTypes.length) {
					cb.append( argTypes[ iArg ].getClassName() ).append( " _" ).append( paramName( iArg++ ) );
				}
				cb.append( " )" );
			}
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
				classBuilder.append( "mv.push( _" ).append( paramName( _paramIdx ) ).appendLine( " );" );
			}
		}


		@Override
		protected void genDispatch()
		{
			// No automatic dispatch for utils.
		}

	}


}