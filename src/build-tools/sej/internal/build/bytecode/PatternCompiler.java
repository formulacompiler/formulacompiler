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
import java.math.BigDecimal;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import sej.describable.DescriptionBuilder;
import sej.internal.build.bytecode.ByteCodeCompilerGenerator.TemplateMethodGenerator;
import sej.internal.templates.ExpressionTemplatesForAll;
import sej.internal.templates.ExpressionTemplatesForBigDecimals;
import sej.internal.templates.ExpressionTemplatesForDoubles;
import sej.internal.templates.ExpressionTemplatesForNumbers;
import sej.internal.templates.ExpressionTemplatesForScaledLongs;
import sej.internal.templates.ExpressionTemplatesForStrings;

public final class PatternCompiler
{
	private static final Type STRING_TYPE = Type.getType( String.class );


	public static void main( String[] args ) throws Exception
	{
		try {
			new PatternCompiler().run();
		}
		catch (Throwable t) {
			System.out.println( "ERROR: " + t.getMessage() );
		}
	}


	private void run() throws IOException
	{
		final File p = new File( "src/classes-gen/sej/internal/bytecode/compiler" );
		p.mkdirs();

		new ByteCodeCompilerGenerator( this, ExpressionTemplatesForAll.class, "All" ).generate( p );
		new ByteCodeCompilerGenerator( this, ExpressionTemplatesForNumbers.class, "Numbers", new Numeric( Type
				.getType( Number.class ) ) ).generate( p );
		new ByteCodeCompilerGenerator( this, ExpressionTemplatesForDoubles.class, "Doubles", new Numeric(
				Type.DOUBLE_TYPE ) ).generate( p );
		new ByteCodeCompilerGenerator( this, ExpressionTemplatesForScaledLongs.class, "ScaledLongs", new Numeric(
				Type.LONG_TYPE ) ).generate( p );
		new ByteCodeCompilerGenerator( this, ExpressionTemplatesForBigDecimals.class, "BigDecimals", new Adjusted( Type
				.getType( BigDecimal.class ) ) ).generate( p );
		new ByteCodeCompilerGenerator( this, ExpressionTemplatesForStrings.class, "Strings", new Strings() ).generate( p );
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
			if (_paramType.equals( STRING_TYPE )) return "String.class";
			else if (_paramType.equals( Type.INT_TYPE )) return "Integer.TYPE";
			else if (_paramType.equals( Type.BOOLEAN_TYPE )) return "Boolean.TYPE";
			else throw new IllegalArgumentException( "The type "
					+ _paramType.toString() + " is not supported by automatic conversion." );
		}

	}


	private static class Numeric extends AbstractDual
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
			if (_paramType.equals( STRING_TYPE )) {
				genStr( _cb, _generator );
				_cb.append( "str.compile( _" ).append( _generator.paramName( _paramIdx ) ).appendLine( " );" );
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


	private static class Adjusted extends Numeric
	{

		public Adjusted(Type _ownType)
		{
			super( _ownType );
		}

		@Override
		protected void genValueAdjustment( DescriptionBuilder _cb, TemplateMethodGenerator _generator )
		{
			if (!hasAnnotation( _generator.mtdNode, "Lsej/internal/templates/ReturnsAdjustedValue;" )) {
				_cb.appendLine( "compileValueAdjustment();" );
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

	}


	private static class Strings extends AbstractDual
	{

		@Override
		protected Type ownType()
		{
			return STRING_TYPE;
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
