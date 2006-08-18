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
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.TableSwitchGenerator;

import sej.CompilerException;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.expressions.ExpressionNodeForFunction;

class HelperCompilerForIndex extends HelperCompiler
{
	private final ExpressionNodeForFunction node;


	HelperCompilerForIndex(SectionCompiler _section, ExpressionNodeForFunction _node)
	{
		super( _section );
		this.node = _node;
	}


	@Override
	protected void compileBody() throws CompilerException
	{
		final List<ExpressionNode> args = this.node.arguments();
		if (args.size() > 0) {
			final ExpressionNode[] rangeElements = rangeElements( this.node, args.get( 0 ) );
			switch (this.node.cardinality()) {

				case 2:
					compileIndexFunction( rangeElements, args.get( 1 ), null );
					return;

				case 3:
					compileIndexFunction( rangeElements, args.get( 1 ), args.get( 2 ) );
					return;

			}
		}
		unsupported( this.node );
	}


	private void compileIndexFunction( final ExpressionNode[] _vals, ExpressionNode _row, ExpressionNode _col ) throws CompilerException
	{
		final NumericTypeCompiler num = section().numericType();
		final Type numType = num.type();
		final String arrayFieldName = methodName() + "_Consts";
		final String arrayType = "[" + num.descriptor();

		compileStaticArrayField( _vals, arrayFieldName );

		final GeneratorAdapter mv = mv();

		final int l_i = mv.newLocal( Type.INT_TYPE );
		if (isNull(_row)) {
			// final int i = <col> - 1;
			compileExpr( _col );
			num.compileIntFromNum( mv );
			mv.push( 1 );
			mv.visitInsn( Opcodes.ISUB );
		}
		else if (isNull(_col)) {
			// final int i = <row> - 1;
			compileExpr( _row );
			num.compileIntFromNum( mv );
			mv.push( 1 );
			mv.visitInsn( Opcodes.ISUB );
		}
		else {
			// final int i = (<row> - 1) * <num_cols>) + (<col> - 1);
			compileExpr( _row );
			num.compileIntFromNum( mv );
			mv.push( 1 );
			mv.visitInsn( Opcodes.ISUB );

			mv.push( range( this.node, this.node.arguments().get(0)).getNumberOfColumns() );
			mv.visitInsn( Opcodes.IMUL );
			
			compileExpr( _col );
			num.compileIntFromNum( mv );
			mv.push( 1 );
			mv.visitInsn( Opcodes.ISUB );

			mv.visitInsn( Opcodes.IADD );
		}
		mv.storeLocal( l_i );

		// gen switch
		final int[] nonConstValIdxs = new int[ _vals.length ];
		int nonConstValCnt = 0;
		for (int i = 0; i < _vals.length; i++) {
			final ExpressionNode val = _vals[ i ];
			if (!(val instanceof ExpressionNodeForConstantValue)) {
				nonConstValIdxs[ nonConstValCnt++ ] = i;
			}
		}
		if (nonConstValCnt > 0) {
			final int[] keys = new int[ nonConstValCnt ];
			System.arraycopy( nonConstValIdxs, 0, keys, 0, nonConstValCnt );

			mv.loadLocal( l_i );
			try {
				mv.tableSwitch( keys, new TableSwitchGenerator()
				{

					public void generateCase( int _key, Label _end )
					{
						final ExpressionNode val = _vals[ _key ];
						try {
							compileExpr( val );
							mv.visitInsn( num.returnOpcode() );
						}
						catch (CompilerException e) {
							throw new InnerException( e );
						}
					}

					public void generateDefault()
					{
						// fall through
					}

				} );
			}
			catch (InnerException e) {
				throw (CompilerException) e.getCause();
			}
		}

		// return (i >= 0 && i < getStaticIndex_Consts.length) ? getStaticIndex_Consts[ i ] : 0;
		final Label outOfRange = mv.newLabel();
		mv.loadLocal( l_i );
		mv.visitJumpInsn( Opcodes.IFLT, outOfRange );

		mv.loadLocal( l_i );
		mv.visitFieldInsn( Opcodes.GETSTATIC, section().classInternalName(), arrayFieldName, arrayType );
		mv.arrayLength();
		mv.visitJumpInsn( Opcodes.IF_ICMPGE, outOfRange );

		mv.visitFieldInsn( Opcodes.GETSTATIC, section().classInternalName(), arrayFieldName, arrayType );
		mv.loadLocal( l_i );
		mv.arrayLoad( numType );
		mv.visitInsn( num.returnOpcode() );

		mv.mark( outOfRange );

		num.compileZero( mv );
	}

	private void compileStaticArrayField( ExpressionNode[] _vals, String _name ) throws CompilerException
	{
		final int n = _vals.length;

		final NumericTypeCompiler num = section().numericType();
		final Type numType = num.type();
		final String arrayType = "[" + num.descriptor();

		// private final static double[] xy
		final FieldVisitor fv = cw().visitField( Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_STATIC, _name,
				arrayType, null, null );
		fv.visitEnd();

		// ... new double[ n ]
		final GeneratorAdapter ci = section().initializer();
		ci.push( n );
		ci.newArray( numType );

		// ... { c1, c2, ... }
		int i = 0;
		for (ExpressionNode val : _vals) {
			if (val instanceof ExpressionNodeForConstantValue) {
				ci.visitInsn( Opcodes.DUP );
				ci.visitIntInsn( Opcodes.BIPUSH, i++ );
				ExpressionNodeForConstantValue constVal = (ExpressionNodeForConstantValue) val;
				num.compileConst( ci, constVal.getValue() );
				ci.arrayStore( numType );
			}
		}

		// ... xy *=* new double[] { ... }
		ci.visitFieldInsn( Opcodes.PUTSTATIC, section().classInternalName(), _name, arrayType );
	}


	private boolean isNull( ExpressionNode _node )
	{
		if (_node == null) return true;
		if (_node instanceof ExpressionNodeForConstantValue) {
			ExpressionNodeForConstantValue constNode = (ExpressionNodeForConstantValue) _node;
			return (constNode.getValue() == null);
		}
		return false;
	}


	private static final class InnerException extends RuntimeException
	{

		public InnerException(Throwable _cause)
		{
			super( _cause );
		}

	}


}
