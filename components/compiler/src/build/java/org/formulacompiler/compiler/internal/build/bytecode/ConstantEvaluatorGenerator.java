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

package org.formulacompiler.compiler.internal.build.bytecode;

import java.io.File;
import java.io.IOException;

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
		cb.appendLine( "import org.formulacompiler.runtime.ComputationMode;" );
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
	protected final void genMethod( MethodNode _mtdNode )
	{
		if (_mtdNode.name.startsWith( "fun_" )) {
			new FunctionEvaluatorGenerator( _mtdNode ).generate();
		}
		else if (_mtdNode.name.startsWith( "op_" )) {
			new OperatorEvaluatorGenerator( _mtdNode ).generate();
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
			db.append( "if (" ).append( cardinality ).append( " == c" );
			if (this.computationMode != null) {
				db.append( " && ComputationMode." );
				db.append( this.computationMode );
				db.append( " == getComputationMode()" );
			}
			db.appendLine( " ) {" );
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
			cb.append( "public " ).append( _generator.typeName ).appendLine( "( NumericType _type, ComputationMode _mode, Environment _env ) {" );
			cb.indent();
			cb.appendLine( "super( _type, _mode, _env );" );
			cb.append( "this.template = new " ).append( _generator.clsName ).appendLine( "( _mode, _env );" );
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
