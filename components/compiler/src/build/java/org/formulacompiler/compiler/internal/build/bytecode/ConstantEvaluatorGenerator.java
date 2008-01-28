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
package org.formulacompiler.compiler.internal.build.bytecode;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.formulacompiler.compiler.internal.DescriptionBuilder;
import org.formulacompiler.compiler.internal.IOUtil;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;


@SuppressWarnings( "unqualified-field-access" )
final class ConstantEvaluatorGenerator extends AbstractGenerator
{
	final String clsName;
	final Customization customization;


	public ConstantEvaluatorGenerator( Class _template, String _typeName, String _superName, Customization _customization )
			throws IOException
	{
		super( _template, _typeName, _superName );
		this.clsName = cls.getSimpleName();
		this.customization = _customization;
	}

	public ConstantEvaluatorGenerator( Class _template, String _typeName, String _superName ) throws IOException
	{
		this( _template, _typeName, _superName, new Customization() );
	}


	public void generate( File _outputPath ) throws IOException
	{
		if (verbose) System.out.println( "Generating " + typeName + "..." );

		DescriptionBuilder cb = classBuilder;
		cb.appendLine( "/**" );
		cb.append( " * DO NOT MODIFY! This file is generated automatically from " ).append( this.cls.getName() )
				.appendLine( "." );
		cb.append( " * Generated using jdk-" ).append( System.getProperty( "java.version" ) ).appendLine( "." );
		cb.appendLine( " */" );
		cb.appendLine( "package org.formulacompiler.compiler.internal.model.interpreter;" );
		cb.newLine();
		cb.appendLine( "import org.formulacompiler.compiler.CompilerException;" );
		cb.appendLine( "import org.formulacompiler.compiler.Function;" );
		cb.appendLine( "import org.formulacompiler.compiler.NumericType;" );
		cb.appendLine( "import org.formulacompiler.compiler.Operator;" );
		cb.appendLine( "import org.formulacompiler.compiler.internal.expressions.ExpressionNode;" );
		cb.appendLine( "import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;" );
		cb.append( "import org.formulacompiler.compiler.internal.templates." ).append( clsName ).appendLine( ";" );
		cb.appendLine( "import org.formulacompiler.runtime.internal.Environment;" );
		cb.newLine();
		cb.newLine();
		cb.appendLine( "@SuppressWarnings(\"unused\")" );
		cb.appendLine( "abstract class " + typeName + " extends " + superName );
		cb.appendLine( "{" );
		cb.indent();

		customization.genConstructor( cb, this );

		genMethods();
		genOperatorDispatch();
		genFunctionDispatch();

		cb.outdent();
		cb.newLine();
		cb.appendLine( "}" );

		IOUtil.writeStringToIfNotUpToDateWithMessage( cb.toString(), new File( _outputPath, typeName + ".java" ) );

		if (verbose) {
			System.out.println( "Done generating " + typeName + "." );
			System.out.println();
		}
	}


	@Override
	protected final void genMethods( List _methods )
	{
		for (Object mtdObj : _methods) {
			final MethodNode mtdNode = (MethodNode) mtdObj;
			if (mtdNode.name.startsWith( "fun_" )) {
				new FunctionEvaluatorGenerator( mtdNode ).generate();
			}
			else if (mtdNode.name.startsWith( "op_" )) {
				new OperatorEvaluatorGenerator( mtdNode ).generate();
			}
		}
	}


	abstract class AbstractMethodEvaluatorGenerator extends AbstractMethodTemplateGenerator
	{

		public AbstractMethodEvaluatorGenerator( MethodNode _mtdNode )
		{
			super( _mtdNode );
		}

		protected final void generateArg( DispatchBuilder _db, int _iArg )
		{
			final Type argType = argTypes[ _iArg ];
			_db.append( "to_" ).append( convName( argType ) ).append( "( _args[ " ).append( _iArg ).append( " ] )" );
		}

		private final String convName( Type _argType )
		{
			switch (_argType.getSort()) {

				case Type.BOOLEAN:
					return "boolean";
				case Type.BYTE:
					return "byte";
				case Type.CHAR:
					return "char";
				case Type.DOUBLE:
					return "double";
				case Type.FLOAT:
					return "float";
				case Type.INT:
					return "int";
				case Type.LONG:
					return "long";
				case Type.SHORT:
					return "short";
				case Type.ARRAY:
					return "array";
				case Type.OBJECT:
					return convName( _argType.getInternalName() );
				default:
					throw new IllegalArgumentException( "Unsupported _argType " + _argType.toString() );
			}
		}

		private final String convName( String _internalName )
		{
			int p = _internalName.lastIndexOf( '/' );
			return _internalName.substring( p + 1 );
		}

	}


	final class OperatorEvaluatorGenerator extends AbstractMethodEvaluatorGenerator
	{

		public OperatorEvaluatorGenerator( MethodNode _mtdNode )
		{
			super( _mtdNode );
		}

		public void generate()
		{
			if (verbose) System.out.println( "  " + mtdNode.name );

			final DispatchBuilder db = (cardinality == 1) ? unaryOperatorDispatchBuilder : binaryOperatorDispatchBuilder;
			db.genDispatchCase( enumName );
			db.indent();
			db.genDispatchIf( ifCond );

			db.append( "return " );
			final String valueAdjustmentSuffix = customization.genValueAdjustment( db, this );
			db.append( "t." ).append( mtdNode.name );
			if (0 == cardinality) {
				db.append( "()" );
			}
			else {
				db.append( "( " );
				generateArg( db, 0 );
				for (int iArg = 1; iArg < cardinality; iArg++) {
					db.append( ", " );
					generateArg( db, iArg );
				}
				db.append( " )" );
			}
			db.append( valueAdjustmentSuffix ).appendLine( ";" );

			db.genDispatchEndIf( ifCond );
			db.outdent();
		}

	}


	final class FunctionEvaluatorGenerator extends AbstractMethodEvaluatorGenerator
	{

		public FunctionEvaluatorGenerator( MethodNode _mtdNode )
		{
			super( _mtdNode );
		}

		public void generate()
		{
			if (verbose) System.out.println( "  " + mtdNode.name );

			final DispatchBuilder db = functionDispatchBuilder;
			if (db.genDispatchCase( enumName )) {
				db.pending = "   break;";
			}
			db.indent();
			db.append( "if (" ).append( cardinality ).appendLine( " == c) {" );
			db.indent();
			db.genDispatchIf( ifCond );
			db.append( "return " );
			final String valueAdjustmentSuffix = customization.genValueAdjustment( db, this );
			db.append( "t." ).append( mtdNode.name );
			if (0 == cardinality) {
				db.append( "()" );
			}
			else {
				db.append( "( " );
				generateArg( db, 0 );
				for (int iArg = 1; iArg < cardinality; iArg++) {
					db.append( ", " );
					generateArg( db, iArg );
				}
				db.append( " )" );
			}
			db.append( valueAdjustmentSuffix ).appendLine( ";" );
			db.genDispatchEndIf( ifCond );
			db.outdent();
			db.appendLine( "}" );
			db.outdent();
		}

	}


	private final void genOperatorDispatch()
	{
		if (this.unaryOperatorDispatchBuilder.length() == 0) return;

		final DescriptionBuilder cb = classBuilder;
		cb.newLine();
		cb.appendLine( "@Override" );
		cb.appendLine( "public Object compute( Operator _operator, Object... _args ) throws InterpreterException" );
		cb.appendLine( "{" );
		cb.indent();

		cb.append( "final " ).append( clsName ).append( " t = " ).append( customization.templateName() ).appendLine( ";" );
		cb.appendLine( "final int c = _args.length;" );

		cb.appendLine( "if (1 == c) {" );
		cb.indent();
		cb.appendLine( "switch (_operator) {" );
		cb.indent();

		cb.appendUnindented( this.unaryOperatorDispatchBuilder.toString() );

		cb.outdent();
		cb.appendLine( "}" );
		cb.outdent();
		cb.appendLine( "}" );
		cb.appendLine( "else if (2 == c) {" );
		cb.indent();
		cb.appendLine( "switch (_operator) {" );
		cb.indent();

		cb.appendUnindented( this.binaryOperatorDispatchBuilder.toString() );

		cb.outdent();
		cb.appendLine( "}" );
		cb.outdent();
		cb.appendLine( "}" );

		cb.appendLine( "return super.compute( _operator, _args );" );
		cb.outdent();
		cb.appendLine( "}" );
	}


	private final void genFunctionDispatch()
	{
		if (this.functionDispatchBuilder.length() == 0) return;

		final DescriptionBuilder cb = classBuilder;
		cb.newLine();
		cb.appendLine( "@Override" );
		cb.appendLine( "public Object compute( Function _function, Object... _args ) throws InterpreterException" );
		cb.appendLine( "{" );
		cb.indent();

		cb.append( "final " ).append( clsName ).append( " t = " ).append( customization.templateName() ).appendLine( ";" );
		cb.appendLine( "final int c = _args.length;" );
		cb.appendLine( "switch (_function) {" );
		cb.indent();

		cb.appendUnindented( this.functionDispatchBuilder.toString() );

		cb.outdent();
		cb.appendLine( "}" );

		cb.appendLine( "return super.compute( _function, _args );" );
		cb.outdent();
		cb.appendLine( "}" );
	}


	static class Customization
	{

		protected void genConstructor( DescriptionBuilder _cb, ConstantEvaluatorGenerator _generator )
		{
			final DescriptionBuilder cb = _cb;
			cb.append( "private final " ).append( _generator.clsName ).appendLine( " template;" );
			cb.newLine();
			cb.append( "public " ).append( _generator.typeName ).appendLine( "( NumericType _type, Environment _env ) {" );
			cb.indent();
			cb.appendLine( "super( _type, _env );" );
			cb.append( "this.template = new " ).append( _generator.clsName ).appendLine( "( _env );" );
			cb.outdent();
			cb.appendLine( "}" );
		}

		protected String templateName()
		{
			return "this.template";
		}

		protected String genValueAdjustment( DescriptionBuilder _cb, AbstractMethodEvaluatorGenerator _generator )
		{
			return "";
		}

	}

}
