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
