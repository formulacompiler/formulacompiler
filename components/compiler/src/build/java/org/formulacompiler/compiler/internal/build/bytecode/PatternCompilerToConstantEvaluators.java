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
			cb.append( "public " ).append( _generator.typeName ).appendLine( "( NumericType _type, ComputationMode _mode, Environment _env ) {" );
			cb.indent();
			cb.appendLine( "super( _type, _mode, _env );" );
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
			cb.append( "public " ).append( _generator.typeName ).appendLine( "( NumericType _type, ComputationMode _mode, Environment _env ) {" );
			cb.indent();
			cb.appendLine( "super( _type, _mode, _env );" );
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
					"( org.formulacompiler.compiler.internal.AbstractLongType _type, ComputationMode _mode, Environment _env ) {" );
			cb.indent();
			cb.appendLine( "super( _type, _mode, _env );" );
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