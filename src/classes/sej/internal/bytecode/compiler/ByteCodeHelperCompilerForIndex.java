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
package sej.internal.bytecode.compiler;

import java.util.List;

import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.CompilerException;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.expressions.ExpressionNodeForFunction;
import sej.internal.model.ExpressionNodeForRangeValue;

class ByteCodeHelperCompilerForIndex extends ByteCodeHelperCompiler
{
	private final ExpressionNodeForFunction node;


	ByteCodeHelperCompilerForIndex(ByteCodeSectionCompiler _section, ExpressionNodeForFunction _node)
	{
		super( _section );
		this.node = _node;
	}


	@Override
	protected void compileBody() throws CompilerException
	{
		final List<ExpressionNode> args = this.node.getArguments();
		final ExpressionNode firstArg = args.get( 0 );
		if (firstArg instanceof ExpressionNodeForRangeValue) {
			final ExpressionNodeForRangeValue rangeNode = (ExpressionNodeForRangeValue) firstArg;
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
						unsupported( this.node ); // LATER two-dim index
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


	private void compileOneDimensionalIndexFunction( ExpressionNodeForRangeValue _rangeNode, ExpressionNode _node ) throws CompilerException
	{
		final List<ExpressionNode> vals = _rangeNode.getArguments();
		final String arrayFieldName = methodName() + "_Consts";

		compileStaticArrayField( vals, arrayFieldName );


		// gen switch
		// gen lookup
	}


	private void compileStaticArrayField( final List<ExpressionNode> _vals, final String _name ) throws CompilerException
	{
		final int n = _vals.size();

		final ByteCodeNumericType num = section().numericType();
		final Type numType = num.type();
		final String arrayType = "[" + num.descriptor();
		
		// private final static double[] xy
		final FieldVisitor fv = cw().visitField( Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_STATIC, _name,
				arrayType, null, null );
		fv.visitEnd();

		// LATER Finish compileStaticArrayField
		// ... new double[ n ]
		final GeneratorAdapter ci = section().initializer();
		ci.push( n );
		ci.newArray( numType );

		// ... { c1, c2, ... }
		int i = 0;
		for (ExpressionNode val : _vals) {
			ci.visitInsn( Opcodes.DUP );
			ci.visitIntInsn( Opcodes.BIPUSH, i++ );
			if (val instanceof ExpressionNodeForConstantValue) {
				ExpressionNodeForConstantValue constVal = (ExpressionNodeForConstantValue) val;
				compileConst( constVal.getValue() );
			}
			else {
				num.compileZero( ci );
			}
			ci.arrayStore( numType );
		}

		// ... xy *=* new double[] { ... }
		ci.visitFieldInsn( Opcodes.PUTSTATIC, section().classInternalName(), _name, arrayType );
	}


}
