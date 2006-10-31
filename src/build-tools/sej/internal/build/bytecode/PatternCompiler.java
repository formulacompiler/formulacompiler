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

import org.objectweb.asm.Opcodes;
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
		new ByteCodeCompilerGenerator( this, ExpressionTemplatesForNumbers.class, "Numbers", new Numeric() ).generate( p );
		new ByteCodeCompilerGenerator( this, ExpressionTemplatesForDoubles.class, "Doubles", new Numeric() ).generate( p );
		new ByteCodeCompilerGenerator( this, ExpressionTemplatesForScaledLongs.class, "ScaledLongs", new Numeric() )
				.generate( p );
		new ByteCodeCompilerGenerator( this, ExpressionTemplatesForBigDecimals.class, "BigDecimals", new Adjusted() )
				.generate( p );
		new ByteCodeCompilerGenerator( this, ExpressionTemplatesForStrings.class, "Strings", new Strings() ).generate( p );
	}


	private static class Numeric extends ByteCodeCompilerGenerator.Customization
	{

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

	}


	private static class Adjusted extends Numeric
	{

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


	private static class Strings extends ByteCodeCompilerGenerator.Customization
	{

		@Override
		protected void genCompilationOfArgNode( DescriptionBuilder _cb, TemplateMethodGenerator _generator,
				int _paramIdx, int _opcode )
		{
			switch (_opcode) {
				case Opcodes.ILOAD:
					compileNumberAs( _cb, _generator, _paramIdx, "Int" );
					break;
				default:
					super.genCompilationOfArgNode( _cb, _generator, _paramIdx, _opcode );
			}
		}

		private static final String NUM_COMP = "num";
		
		private void compileNumberAs( DescriptionBuilder _cb, TemplateMethodGenerator _generator, int _paramIdx,
				String _string )
		{
			if (!_generator.defs.containsKey( NUM_COMP )) {
				_cb.appendLine( "final ExpressionCompilerForNumbers num = method().numericCompiler();" );
				_generator.defs.put( NUM_COMP, null );
			}
			_cb.append( "num.compile( _" ).append( _generator.paramName( _paramIdx ) ).appendLine( " );" );
			_cb.appendLine( "num.compileConversionToInt();" );
		}

	}


}
