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


import org.formulacompiler.compiler.internal.templates.ReturnsAdjustedValue;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;


public final class PatternCompiler
{
	static final Type STRING_TYPE = Type.getType( String.class );
	static final Type NUMBER_TYPE = Type.getType( Number.class );
	static final Type RETURNS_ADJUSTED_VALUE_TYPE = Type.getType( ReturnsAdjustedValue.class );
	static final String RETURNS_ADJUSTED_VALUE_DESC = RETURNS_ADJUSTED_VALUE_TYPE.getDescriptor();


	public static void main( String[] args ) throws Exception
	{
		AbstractGenerator.verbose = (args.length > 0);
		try {
			new PatternCompilerToByteCodeCompilers().run();
			new PatternCompilerToConstantEvaluators().run();
		}
		catch (Throwable t) {
			System.out.println( "ERROR: " + t.getMessage() );
		}
	}


	final static boolean hasAnnotation( MethodNode _mtdNode, String _className )
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
