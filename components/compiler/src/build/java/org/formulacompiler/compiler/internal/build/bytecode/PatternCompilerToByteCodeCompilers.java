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
import java.math.BigDecimal;

import org.formulacompiler.compiler.internal.build.Util;
import org.formulacompiler.compiler.internal.build.bytecode.ByteCodeCompilerGenerator.TemplateMethodGenerator;
import org.formulacompiler.compiler.internal.templates.ExpressionTemplatesForAll;
import org.formulacompiler.compiler.internal.templates.ExpressionTemplatesForBigDecimals;
import org.formulacompiler.compiler.internal.templates.ExpressionTemplatesForDoubles;
import org.formulacompiler.compiler.internal.templates.ExpressionTemplatesForNumbers;
import org.formulacompiler.compiler.internal.templates.ExpressionTemplatesForScaledLongs;
import org.formulacompiler.compiler.internal.templates.ExpressionTemplatesForStrings;
import org.formulacompiler.describable.DescriptionBuilder;
import org.objectweb.asm.Type;


class PatternCompilerToByteCodeCompilers
{

	public void run() throws IOException
	{
		final File p = new File( "temp/impl/java-" + Util.jdkVersionSuffix() + "/org/formulacompiler/compiler/internal/bytecode" );
		p.mkdirs();

		new ByteCodeCompilerGenerator( ExpressionTemplatesForAll.class, "All" ).generate( p );
		new ByteCodeCompilerGenerator( ExpressionTemplatesForNumbers.class, "Numbers", new Numeric( Type
				.getType( Number.class ) ) ).generate( p );
		new ByteCodeCompilerGenerator( ExpressionTemplatesForDoubles.class, "Doubles", new Numeric( Type.DOUBLE_TYPE ) )
				.generate( p );
		new ByteCodeCompilerGenerator( ExpressionTemplatesForScaledLongs.class, "ScaledLongs", new Numeric(
				Type.LONG_TYPE ) ).generate( p );
		new ByteCodeCompilerGenerator( ExpressionTemplatesForBigDecimals.class, "BigDecimals", new Adjusted( Type
				.getType( BigDecimal.class ) ) ).generate( p );
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
			else if (_paramType.equals( Type.INT_TYPE )) return "Integer.TYPE";
			else if (_paramType.equals( Type.BOOLEAN_TYPE )) return "Boolean.TYPE";
			else throw new IllegalArgumentException( "The type "
					+ _paramType.toString() + " is not supported by automatic conversion." );
		}

	}


	private static class Numeric extends PatternCompilerToByteCodeCompilers.AbstractDual
	{
		private final Type ownType;

		public Numeric(Type _ownType)
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

		public Adjusted(Type _ownType)
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
			_cb.append( "num.compile( _" ).append( _generator.paramName( _paramIdx ) ).appendLine( " );" );
			if (Type.INT_TYPE.equals( _paramType )) {
				_cb.appendLine( "num.compileConversionToInt();" );
			}
			else {
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