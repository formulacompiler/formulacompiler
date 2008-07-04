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
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForSwitch;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForSwitchCase;
import org.formulacompiler.compiler.internal.expressions.LetDictionary.LetEntry;
import org.objectweb.asm.Label;

final class HelperCompilerForSwitch extends HelperCompiler
{

	public HelperCompilerForSwitch( SectionCompiler _section, ExpressionNodeForSwitch _node, Iterable<LetEntry> _closure )
	{
		super( _section, _node, _closure );
	}


	@Override
	protected void compileBody() throws CompilerException
	{
		final ExpressionNodeForSwitch switchNode = (ExpressionNodeForSwitch) node();
		numericCompiler().compileInt( switchNode.selector() );

		int nCases = switchNode.numberOfCases();
		if (nCases > 0) {
			final int[] switchValues = new int[ nCases ];
			final ExpressionNodeForSwitchCase[] switchValueCases = new ExpressionNodeForSwitchCase[ nCases ];
			int iSwitchValue = 0;
			for (ExpressionNodeForSwitchCase caze : switchNode.cases()) {
				switchValueCases[ iSwitchValue ] = caze;
				switchValues[ iSwitchValue ] = caze.caseValue();
				iSwitchValue++;
			}

			compileTableSwitch( switchValues, new TableSwitchGenerator()
			{
				private final int valReturn = expressionCompiler().typeCompiler().returnOpcode();
				private int switchIndex = 0;

				@Override
				protected void generateCase( int _key, Label _end ) throws CompilerException
				{
					final ExpressionNodeForSwitchCase caze = switchValueCases[ this.switchIndex++ ];
					if (null != caze) {
						compileExpression( caze.value() );
						mv().visitInsn( this.valReturn );
					}
				}

			} );
		}

		compileExpression( switchNode.defaultValue() );
	}

}
