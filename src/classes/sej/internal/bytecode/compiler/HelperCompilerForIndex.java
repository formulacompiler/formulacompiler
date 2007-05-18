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
package sej.internal.bytecode.compiler;

import java.util.List;

import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.compiler.CompilerException;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.expressions.ExpressionNodeForFunction;
import sej.internal.expressions.LetDictionary.LetEntry;

class HelperCompilerForIndex extends HelperCompiler
{

	HelperCompilerForIndex(SectionCompiler _section, ExpressionNodeForFunction _node, Iterable<LetEntry> _closure)
	{
		super( _section, _node, _closure );
	}


	@Override
	protected void compileBody() throws CompilerException
	{
		final List<ExpressionNode> args = node().arguments();
		if (args.size() > 0) {
			final ExpressionNode[] rangeElements = arrayRefElements( node(), args.get( 0 ) );
			switch (node().cardinality()) {

				case 2:
					compileIndexFunction( rangeElements, args.get( 1 ), null );
					return;

				case 3:
					compileIndexFunction( rangeElements, args.get( 1 ), args.get( 2 ) );
					return;

			}
		}
		throw new CompilerException.UnsupportedExpression( "INDEX must have two or three arguments." );
	}


	private void compileIndexFunction( final ExpressionNode[] _vals, ExpressionNode _row, ExpressionNode _col )
			throws CompilerException
	{
		final ExpressionCompilerForNumbers numCompiler = numericCompiler();
		final ExpressionCompiler valCompiler = expressionCompiler();
		final Type valType = valCompiler.type();
		final String arrayFieldName = methodName() + "_Consts";
		final String arrayType = "[" + valCompiler.typeDescriptor();
		final boolean arrayPresent = isAnyConstant( _vals );

		if (arrayPresent) {
			compileStaticArrayField( _vals, arrayFieldName );
		}

		final GeneratorAdapter mv = mv();

		final int l_i = mv.newLocal( Type.INT_TYPE );
		if (isNull( _row )) {
			// final int i = <col> - 1;
			numCompiler.compile( _col );
			numCompiler.compileConversionToInt();
			mv.push( 1 );
			mv.visitInsn( Opcodes.ISUB );
		}
		else if (isNull( _col )) {
			// final int i = <row> - 1;
			numCompiler.compile( _row );
			numCompiler.compileConversionToInt();
			mv.push( 1 );
			mv.visitInsn( Opcodes.ISUB );
		}
		else {
			// final int i = (<row> - 1) * <num_cols>) + (<col> - 1);
			numCompiler.compile( _row );
			numCompiler.compileConversionToInt();
			mv.push( 1 );
			mv.visitInsn( Opcodes.ISUB );

			mv.push( arrayDescriptor( node(), node().arguments().get( 0 ) ).getNumberOfColumns() );
			mv.visitInsn( Opcodes.IMUL );

			numCompiler.compile( _col );
			numCompiler.compileConversionToInt();
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
			if (!isConstant( val )) {
				nonConstValIdxs[ nonConstValCnt++ ] = i;
			}
		}
		if (nonConstValCnt > 0) {
			final int[] keys = new int[ nonConstValCnt ];
			System.arraycopy( nonConstValIdxs, 0, keys, 0, nonConstValCnt );

			mv.loadLocal( l_i );
			compileTableSwitch( keys, new TableSwitchGenerator()
			{

				@Override
				protected void generateCase( int _key, Label _end ) throws CompilerException
				{
					final ExpressionNode val = _vals[ _key ];
					valCompiler.compile( val );
					mv.visitInsn( valCompiler.typeCompiler().returnOpcode() );
				}

			} );
		}

		if (arrayPresent) {
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
			mv.arrayLoad( valType );
			mv.visitInsn( valCompiler.typeCompiler().returnOpcode() );

			mv.mark( outOfRange );
		}
		valCompiler.compileZero();
	}


	private boolean isConstant( final ExpressionNode _node )
	{
		return (_node instanceof ExpressionNodeForConstantValue);
	}

	private boolean isAnyConstant( ExpressionNode[] _nodes )
	{
		for (ExpressionNode n : _nodes) {
			if (isConstant( n )) return true;
		}
		return false;
	}
	

	private void compileStaticArrayField( ExpressionNode[] _vals, String _name ) throws CompilerException
	{
		final int n = _vals.length;

		final ExpressionCompiler valCompiler = expressionCompiler();
		final TypeCompiler valTypeCompiler = valCompiler.typeCompiler();
		final Type valType = valCompiler.type();
		final String arrayType = "[" + valCompiler.typeDescriptor();

		// private final static double[] xy
		final FieldVisitor fv = cw().visitField( Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_STATIC, _name,
				arrayType, null, null );
		fv.visitEnd();

		// ... new double[ n ]
		final GeneratorAdapter ci = section().initializer();
		ci.push( n );
		ci.newArray( valType );

		// ... { c1, c2, ... }
		int i = 0;
		for (ExpressionNode val : _vals) {
			if (val instanceof ExpressionNodeForConstantValue) {
				ci.visitInsn( Opcodes.DUP );
				ci.visitIntInsn( Opcodes.BIPUSH, i );
				ExpressionNodeForConstantValue constVal = (ExpressionNodeForConstantValue) val;
				valTypeCompiler.compileConst( ci, constVal.value() );
				ci.arrayStore( valType );
			}
			i++;
		}

		// ... xy *=* new double[] { ... }
		ci.visitFieldInsn( Opcodes.PUTSTATIC, section().classInternalName(), _name, arrayType );
	}


	private boolean isNull( ExpressionNode _node )
	{
		if (_node == null) return true;
		if (_node instanceof ExpressionNodeForConstantValue) {
			ExpressionNodeForConstantValue constNode = (ExpressionNodeForConstantValue) _node;
			return (constNode.value() == null);
		}
		return false;
	}


}
