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

package org.formulacompiler.compiler.internal.bytecode;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.LetDictionary.LetEntry;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.GeneratorAdapter;


final class HelperCompilerForDatabaseMatch extends MethodCompiler
{
	private final ExpressionNode node;

	public HelperCompilerForDatabaseMatch( SectionCompiler _section, ExpressionNode _node, Iterable<LetEntry> _closure )
	{
		super( _section, 0, _section.newGetterName(), "(" + descriptorOf( _section, _closure ) + ")Z" );
		this.node = _node;
		addClosureToLetDict( _closure );
	}

	@Override
	protected void compileBody() throws CompilerException
	{
		final GeneratorAdapter mv = mv();
		final Label ifFalse = mv.newLabel();
		numericCompiler().compileTest( this.node, ifFalse );
		mv.push( true );
		mv.returnValue();
		mv.visitLabel( ifFalse );
		mv.push( false );
		mv.returnValue();
	}

}
