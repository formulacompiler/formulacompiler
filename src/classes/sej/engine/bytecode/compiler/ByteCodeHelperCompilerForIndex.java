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
package sej.engine.bytecode.compiler;

import java.util.List;

import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import sej.ModelError;
import sej.engine.compiler.model.ExpressionNodeForRangeValue;
import sej.expressions.ExpressionNode;
import sej.expressions.ExpressionNodeForConstantValue;
import sej.expressions.ExpressionNodeForFunction;

class ByteCodeHelperCompilerForIndex extends ByteCodeHelperCompiler
{
	private final ExpressionNodeForFunction node;


	public ByteCodeHelperCompilerForIndex(ByteCodeSectionCompiler _section, ExpressionNodeForFunction _node) 
	{
		super( _section );
		this.node = _node;
	}


	@Override
	protected void compileBody() throws ModelError
	{
		final List<ExpressionNode> args = this.node.getArguments();
		final ExpressionNode firstArg = args.get( 0 );
		if (firstArg instanceof ExpressionNodeForRangeValue) {
			ExpressionNodeForRangeValue rangeNode = (ExpressionNodeForRangeValue) firstArg;
			switch (args.size()) {

			case 2:
				compileOneDimensionalIndexFunction( rangeNode, args.get( 1 ) );
				break;

			case 3:
				if (isNull( args.get( 1 ) )) {
					compileOneDimensionalIndexFunction( rangeNode, args.get( 2 ) );
				}
				else if (isNull( args.get( 2 ) )) {
					compileOneDimensionalIndexFunction( rangeNode, args.get( 1 ) );
				}
				else {
					unsupported( this.node ); // TODO two-dim index
				}
				break;

			default:
				unsupported( this.node );
			}
		}
		else {
			unsupported( this.node );
		}
	}


	private void compileOneDimensionalIndexFunction( ExpressionNodeForRangeValue _rangeNode, ExpressionNode _node )
	{
		final List<ExpressionNode> vals = _rangeNode.getArguments();
		final String arrayFieldName = getMethodName() + "_Consts";

		compileStaticArrayField( vals, arrayFieldName );


		// gen switch
		// gen lookup
	}


	private void compileStaticArrayField( final List<ExpressionNode> _vals, final String _name )
	{
		final int n = _vals.size();

		// private final static double[] xy
		final FieldVisitor fv = cw().visitField( Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_STATIC, _name,
				"[D", null, null );
		fv.visitEnd();

		// TODO Finish compileStaticArrayField
		// ... new double[ n ]
		final MethodVisitor ci = null; // TODO getSection().classInit();
		ci.visitIntInsn( Opcodes.BIPUSH, n );
		ci.visitIntInsn( Opcodes.NEWARRAY, n );

		// ... { c1, c2, ... }
		int i = 0;
		for (ExpressionNode val : _vals) {
			ci.visitInsn( Opcodes.DUP );
			ci.visitIntInsn( Opcodes.BIPUSH, i++ );
			if (val instanceof ExpressionNodeForConstantValue) {
				ExpressionNodeForConstantValue constVal = (ExpressionNodeForConstantValue) val;
				// ci.visitLdcInsn( Util.valueToDoubleOrZero( constVal.getValue() ) );
			}
			else {
				ci.visitInsn( Opcodes.DCONST_0 );
			}
			ci.visitInsn( Opcodes.DASTORE );
		}

		// ... xy *=* new double[] { ... }
		ci.visitFieldInsn( Opcodes.PUTSTATIC, getSection().engine.getInternalName(), _name, "[D" );
	}


}
