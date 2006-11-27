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
package sej.internal.build.bytecode;

import java.io.File;
import java.io.IOException;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import sej.describable.DescriptionBuilder;
import sej.internal.build.Util;

@SuppressWarnings("unqualified-field-access")
final class ConstantEvaluatorGenerator extends AbstractGenerator
{
	final String clsName;
	final Customization customization;


	public ConstantEvaluatorGenerator(Class _template, String _typeName, String _superName, Customization _customization)
			throws IOException
	{
		super( _template, _typeName, _superName );
		this.clsName = cls.getSimpleName();
		this.customization = _customization;
	}

	public ConstantEvaluatorGenerator(Class _template, String _typeName, String _superName) throws IOException
	{
		this( _template, _typeName, _superName, new Customization() );
	}


	public void generate( File _outputPath ) throws IOException
	{
		System.out.println( "Generating " + typeName + "..." );

		DescriptionBuilder cb = classBuilder;
		cb.appendLine( "/**" );
		cb.append( " * DO NOT MODIFY! This file is generated automatically from " ).append( this.cls.getName() )
				.appendLine( "." );
		cb.append( " * Generated using jdk-" ).append( System.getProperty( "java.version" ) ).appendLine( "." );
		cb.appendLine( " */" );
		cb.appendLine( "package sej.internal.model.util;" );
		cb.newLine();
		cb.appendLine( "import sej.CompilerException;" );
		cb.appendLine( "import sej.Function;" );
		cb.appendLine( "import sej.NumericType;" );
		cb.appendLine( "import sej.internal.expressions.ExpressionNode;" );
		cb.appendLine( "import sej.internal.expressions.ExpressionNodeForFunction;" );
		cb.append( "import sej.internal.templates." ).append( clsName ).appendLine( ";" );
		cb.newLine();
		cb.newLine();
		cb.appendLine( "@SuppressWarnings(\"unused\")" );
		cb.appendLine( "abstract class " + typeName + " extends " + superName );
		cb.appendLine( "{" );
		cb.indent();

		customization.genConstructor( cb, this );

		genFunctions();
		genFunctionDispatch();

		cb.outdent();
		cb.newLine();
		cb.appendLine( "}" );

		Util.writeStringTo( cb.toString(), new File( _outputPath, typeName + ".java" ) );

		System.out.println( "Done generating " + typeName + "." );
		System.out.println();
	}


	private final void genFunctions()
	{
		for (Object mtdObj : clsNode.methods) {
			final MethodNode mtdNode = (MethodNode) mtdObj;
			if (mtdNode.name.startsWith( "fun_" )) {
				new FunctionTemplateGenerator( mtdNode ).generate();
			}
		}
	}


	final class FunctionTemplateGenerator extends AbstractMethodTemplateGenerator
	{

		public FunctionTemplateGenerator(MethodNode _mtdNode)
		{
			super( _mtdNode );
		}

		public void generate()
		{
			System.out.println( "  " + mtdNode.name );

			final DispatchBuilder db = functionDispatchBuilder;
			if (db.genDispatchCase( enumName )) {
				db.pending = "   break;";
			}
			db.indent();
			db.append( "if (" ).append( cardinality ).appendLine( " == c) {" );
			db.indent();
			db.append( "return " );
			final String valueAdjustmentSuffix = customization.genValueAdjustment( db, this );
			db.append( "t." ).append( mtdNode.name );
			if (0 == cardinality) {
				db.append( "()" );
			}
			else {
				db.append( "( " );
				generateArg( 0 );
				for (int iArg = 1; iArg < cardinality; iArg++) {
					db.append( ", " );
					generateArg( iArg );
				}
				db.append( " )" );
			}
			db.append( valueAdjustmentSuffix ).appendLine( ";" );
			db.outdent();
			db.appendLine( "}" );
			db.outdent();
		}

		private final void generateArg( int _iArg )
		{
			final DispatchBuilder db = functionDispatchBuilder;
			final Type argType = argTypes[ _iArg ];
			db.append( "to_" ).append( convName( argType ) ).append( "( _args[ " ).append( _iArg ).append( " ] )" );
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


	private final void genFunctionDispatch()
	{
		if (this.functionDispatchBuilder.length() == 0) return;

		final DescriptionBuilder cb = classBuilder;
		cb.newLine();
		cb.appendLine( "@Override" );
		cb.appendLine( "public Object compute( Function _function, Object... _args )" );
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
			cb.append( "private final static " ).append( _generator.clsName ).append( " TEMPLATE = new " ).append(
					_generator.clsName ).appendLine( "( );" );
			cb.newLine();
			cb.append( "public " ).append( _generator.typeName ).appendLine( "(NumericType _type) {" );
			cb.indent();
			cb.appendLine( "super( _type );" );
			cb.outdent();
			cb.appendLine( "}" );
		}

		protected String templateName()
		{
			return "TEMPLATE";
		}

		protected String genValueAdjustment( DescriptionBuilder _cb, FunctionTemplateGenerator _generator )
		{
			return "";
		}

	}

}
