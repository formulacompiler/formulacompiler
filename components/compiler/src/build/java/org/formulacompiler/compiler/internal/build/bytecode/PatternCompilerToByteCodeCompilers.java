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
import java.math.BigDecimal;

import org.formulacompiler.compiler.internal.DescriptionBuilder;
import org.formulacompiler.compiler.internal.IOUtil;
import org.formulacompiler.compiler.internal.build.bytecode.ByteCodeCompilerGenerator.TemplateMethodGenerator;
import org.formulacompiler.compiler.internal.templates.ExpressionTemplatesForAll;
import org.formulacompiler.compiler.internal.templates.ExpressionTemplatesForDoubles;
import org.formulacompiler.compiler.internal.templates.ExpressionTemplatesForNumbers;
import org.formulacompiler.compiler.internal.templates.ExpressionTemplatesForPrecisionBigDecimals;
import org.formulacompiler.compiler.internal.templates.ExpressionTemplatesForScaledBigDecimals;
import org.formulacompiler.compiler.internal.templates.ExpressionTemplatesForScaledLongs;
import org.formulacompiler.compiler.internal.templates.ExpressionTemplatesForStrings;
import org.objectweb.asm.Type;


class PatternCompilerToByteCodeCompilers
{

	public void run() throws IOException
	{
		final File p = new File( "temp/impl/java-"
				+ IOUtil.jdkVersionSuffix() + "/org/formulacompiler/compiler/internal/bytecode" );
		p.mkdirs();

		new ByteCodeCompilerGenerator( ExpressionTemplatesForAll.class, "All" ).generate( p );
		new ByteCodeCompilerGenerator( ExpressionTemplatesForNumbers.class, "Numbers", new Numeric( Type
				.getType( Number.class ) ) ).generate( p );
		new ByteCodeCompilerGenerator( ExpressionTemplatesForDoubles.class, "Doubles", new Numeric( Type.DOUBLE_TYPE ) )
				.generate( p );
		new ByteCodeCompilerGenerator( ExpressionTemplatesForPrecisionBigDecimals.class, "PrecisionBigDecimals",
				new Adjusted( Type.getType( BigDecimal.class ) ) ).generate( p );
		new ByteCodeCompilerGenerator( ExpressionTemplatesForScaledBigDecimals.class, "ScaledBigDecimals", new Adjusted(
				Type.getType( BigDecimal.class ) ) ).generate( p );
		new ByteCodeCompilerGenerator( ExpressionTemplatesForScaledLongs.class, "ScaledLongs", new Numeric(
				Type.LONG_TYPE ) ).generate( p );
		new ByteCodeCompilerGenerator( ExpressionTemplatesForStrings.class, "Strings", new Strings() ).generate( p );
	}


	private static abstract class AbstractDual extends ByteCodeCompilerGenerator.Customization
	{

		protected abstract Type ownType();

		@Override
		protected final void genCompilationOfArgNode( DescriptionBuilder _cb, TemplateMethodGenerator _generator,
				int _paramIdx, Type _paramType )
		{
			if (_paramType.equals( ownType() )) {
				super.genCompilationOfArgNode( _cb, _generator, _paramIdx, _paramType );
			}
			else {
				genAccessAs( _cb, _generator, _paramIdx, _paramType );
			}
		}

		protected abstract void genAccessAs( DescriptionBuilder _cb, TemplateMethodGenerator _generator, int _paramIdx,
				Type _paramType );

		@Override
		protected final void genCompilationOfFinalValueConversion( DescriptionBuilder _cb,
				TemplateMethodGenerator _generator, Type _returnType )
		{
			if (_returnType.equals( ownType() )) {
				super.genCompilationOfFinalValueConversion( _cb, _generator, _returnType );
			}
			else {
				genReturnAs( _cb, _returnType );
			}
		}

		protected void genReturnAs( DescriptionBuilder _cb, Type _type )
		{
			if (Type.INT_TYPE.equals( _type )) {
				_cb.appendLine( "compileConversionFromInt();" );
			}
			else {
				_cb.append( "compileConversionFrom( " ).append( typeToJavaConst( _type ) ).appendLine( " );" );
			}
		}

		protected final String typeToJavaConst( Type _paramType )
		{
			if (_paramType.equals( PatternCompiler.STRING_TYPE )) return "String.class";
			else if (_paramType.equals( PatternCompiler.NUMBER_TYPE )) return "Number.class";
			else if (_paramType.equals( Type.INT_TYPE )) return "Integer.TYPE";
			else if (_paramType.equals( Type.BOOLEAN_TYPE )) return "Boolean.TYPE";
			else throw new IllegalArgumentException( "The type "
					+ _paramType.toString() + " is not supported by automatic conversion." );
		}

	}


	private static class Numeric extends PatternCompilerToByteCodeCompilers.AbstractDual
	{
		private final Type ownType;

		public Numeric( Type _ownType )
		{
			super();
			this.ownType = _ownType;
		}

		@Override
		protected Type ownType()
		{
			return this.ownType;
		}

		@Override
		protected void extendConstructorFormals( DescriptionBuilder _cb )
		{
			_cb.append( ", NumericType _numericType" );
		}

		@Override
		protected void extendConstructorArgumentsForSuper( DescriptionBuilder _cb )
		{
			_cb.append( ", _numericType" );
		}

		@Override
		protected void genAccessAs( DescriptionBuilder _cb, TemplateMethodGenerator _generator, int _paramIdx,
				Type _paramType )
		{
			if (_paramType.equals( PatternCompiler.STRING_TYPE )) {
				genStr( _cb, _generator );
				_cb.append( "str.compile( _" ).append( _generator.paramName( _paramIdx ) ).appendLine( " );" );
			}
			else if (_paramType.getSort() == Type.ARRAY) {
				_cb.append( "compileArray( _" ).append( _generator.paramName( _paramIdx ) ).appendLine( " );" );
			}
			else {
				_cb.append( "compile( _" ).append( _generator.paramName( _paramIdx ) ).appendLine( " );" );
				_cb.append( "compileConversionTo( " ).append( typeToJavaConst( _paramType ) ).appendLine( " );" );
			}
		}

		private static final String STR_COMP = "str";

		private void genStr( DescriptionBuilder _cb, TemplateMethodGenerator _generator )
		{
			if (!_generator.defs.containsKey( STR_COMP )) {
				_cb.appendLine( "final ExpressionCompilerForStrings str = method().stringCompiler();" );
				_generator.defs.put( STR_COMP, null );
			}
		}

	}


	private static class Adjusted extends PatternCompilerToByteCodeCompilers.Numeric
	{

		public Adjusted( Type _ownType )
		{
			super( _ownType );
		}

		@Override
		protected void genValueAdjustment( DescriptionBuilder _cb, TemplateMethodGenerator _generator )
		{
			if (!PatternCompiler.hasAnnotation( _generator.mtdNode, PatternCompiler.RETURNS_ADJUSTED_VALUE_DESC )) {
				_cb.appendLine( "compileValueAdjustment();" );
			}
		}

	}


	private static class Strings extends PatternCompilerToByteCodeCompilers.AbstractDual
	{

		@Override
		protected Type ownType()
		{
			return PatternCompiler.STRING_TYPE;
		}

		@Override
		protected void genAccessAs( DescriptionBuilder _cb, TemplateMethodGenerator _generator, int _paramIdx,
				Type _paramType )
		{
			genNum( _cb, _generator );
			if (Type.INT_TYPE.equals( _paramType )) {
				_cb.append( "num.compileInt( _" ).append( _generator.paramName( _paramIdx ) ).appendLine( " );" );
			}
			else {
				_cb.append( "num.compile( _" ).append( _generator.paramName( _paramIdx ) ).appendLine( " );" );
				_cb.append( "num.compileConversionTo( " ).append( typeToJavaConst( _paramType ) ).appendLine( " );" );
			}
		}

		private static final String NUM_COMP = "num";

		private void genNum( DescriptionBuilder _cb, TemplateMethodGenerator _generator )
		{
			if (!_generator.defs.containsKey( NUM_COMP )) {
				_cb.appendLine( "final ExpressionCompilerForNumbers num = method().numericCompiler();" );
				_generator.defs.put( NUM_COMP, null );
			}
		}

	}

}