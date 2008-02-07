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

import org.formulacompiler.compiler.internal.DescriptionBuilder;
import org.formulacompiler.compiler.internal.build.bytecode.ConstantEvaluatorGenerator.AbstractMethodEvaluatorGenerator;
import org.formulacompiler.compiler.internal.templates.ExpressionTemplatesForDoubles;
import org.formulacompiler.compiler.internal.templates.ExpressionTemplatesForNumbers;
import org.formulacompiler.compiler.internal.templates.ExpressionTemplatesForPrecisionBigDecimals;
import org.formulacompiler.compiler.internal.templates.ExpressionTemplatesForScaledBigDecimals;
import org.formulacompiler.compiler.internal.templates.ExpressionTemplatesForScaledLongs;
import org.formulacompiler.compiler.internal.templates.ExpressionTemplatesForStrings;


class PatternCompilerToConstantEvaluators
{

	public void run() throws IOException
	{
		final File p = new File( "temp/impl/java/org/formulacompiler/compiler/internal/model/interpreter" );
		p.mkdirs();

		new ConstantEvaluatorGenerator( ExpressionTemplatesForNumbers.class, "InterpretedNumericType_Generated",
				"InterpretedNumericType_Base" ).generate( p );
		new ConstantEvaluatorGenerator( ExpressionTemplatesForStrings.class, "InterpretedNumericType_GeneratedStrings",
				"InterpretedNumericType_Generated" ).generate( p );

		new ConstantEvaluatorGenerator( ExpressionTemplatesForDoubles.class, "InterpretedDoubleType_Generated",
				"InterpretedDoubleType_Base" ).generate( p );
		new ConstantEvaluatorGenerator( ExpressionTemplatesForPrecisionBigDecimals.class,
				"InterpretedPrecisionBigDecimalType_Generated", "InterpretedPrecisionBigDecimalType_Base",
				new PrecisionBigDecimalCustomization() ).generate( p );
		new ConstantEvaluatorGenerator( ExpressionTemplatesForScaledBigDecimals.class,
				"InterpretedScaledBigDecimalType_Generated", "InterpretedScaledBigDecimalType_Base",
				new ScaledBigDecimalCustomization() ).generate( p );
		new ConstantEvaluatorGenerator( ExpressionTemplatesForScaledLongs.class, "InterpretedScaledLongType_Generated",
				"InterpretedScaledLongType_Base", new ScaledLongCustomization() ).generate( p );
	}

	private static class PrecisionBigDecimalCustomization extends ConstantEvaluatorGenerator.Customization
	{

		@Override
		protected void genConstructor( DescriptionBuilder _cb, ConstantEvaluatorGenerator _generator )
		{
			final DescriptionBuilder cb = _cb;

			cb.append( "private final " ).append( _generator.clsName ).appendLine( " template;" );

			cb.newLine();
			cb.append( "public " ).append( _generator.typeName ).appendLine( "( NumericType _type, Environment _env ) {" );
			cb.indent();
			cb.appendLine( "super( _type, _env );" );
			cb.append( "this.template = new " ).append( _generator.clsName ).appendLine( "( _type.mathContext(), _env );" );
			cb.outdent();
			cb.appendLine( "}" );
		}

		@Override
		protected String templateName()
		{
			return "this.template";
		}

		@Override
		protected String genValueAdjustment( DescriptionBuilder _cb, AbstractMethodEvaluatorGenerator _generator )
		{
			if (!PatternCompiler.hasAnnotation( _generator.mtdNode, PatternCompiler.RETURNS_ADJUSTED_VALUE_DESC )) {
				_cb.append( "adjustReturnedValue( " );
				return " )";
			}
			return "";
		}

	}

	private static class ScaledBigDecimalCustomization extends ConstantEvaluatorGenerator.Customization
	{

		@Override
		protected void genConstructor( DescriptionBuilder _cb, ConstantEvaluatorGenerator _generator )
		{
			final DescriptionBuilder cb = _cb;

			cb.append( "private final " ).append( _generator.clsName ).appendLine( " template;" );

			cb.newLine();
			cb.append( "public " ).append( _generator.typeName ).appendLine( "( NumericType _type, Environment _env ) {" );
			cb.indent();
			cb.appendLine( "super( _type, _env );" );
			cb.append( "this.template = new " ).append( _generator.clsName ).appendLine(
					"( _type.scale(), _type.roundingMode(), _env );" );
			cb.outdent();
			cb.appendLine( "}" );
		}

		@Override
		protected String templateName()
		{
			return "this.template";
		}

		@Override
		protected String genValueAdjustment( DescriptionBuilder _cb, AbstractMethodEvaluatorGenerator _generator )
		{
			if (!PatternCompiler.hasAnnotation( _generator.mtdNode, PatternCompiler.RETURNS_ADJUSTED_VALUE_DESC )) {
				_cb.append( "adjustReturnedValue( " );
				return " )";
			}
			return "";
		}

	}

	private static class ScaledLongCustomization extends ConstantEvaluatorGenerator.Customization
	{

		@Override
		protected void genConstructor( DescriptionBuilder _cb, ConstantEvaluatorGenerator _generator )
		{
			final DescriptionBuilder cb = _cb;

			cb.append( "private final " ).append( _generator.clsName ).appendLine( " template;" );

			cb.newLine();
			cb.append( "public " ).append( _generator.typeName ).appendLine(
					"( org.formulacompiler.compiler.internal.AbstractLongType _type, Environment _env ) {" );
			cb.indent();
			cb.appendLine( "super( _type, _env );" );
			cb.append( "this.template = new " ).append( _generator.clsName ).appendLine( "( getContext(), _env );" );
			cb.outdent();
			cb.appendLine( "}" );
		}

		@Override
		protected String templateName()
		{
			return "this.template";
		}

	}

}