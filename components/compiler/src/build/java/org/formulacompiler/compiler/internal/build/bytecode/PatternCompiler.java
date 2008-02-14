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
