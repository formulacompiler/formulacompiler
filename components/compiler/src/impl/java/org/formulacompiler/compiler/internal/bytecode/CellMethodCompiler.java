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

import java.lang.reflect.Method;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;


final class CellMethodCompiler extends NullaryValueMethodCompiler
{
	private final CellComputation cellComputation;


	CellMethodCompiler( CellComputation _computation )
	{
		super( _computation.getSection(), 0, _computation.getMethodName(), _computation.getCell().getDataType() );
		this.cellComputation = _computation;
	}


	@Override
	protected void compileBody() throws CompilerException
	{
		final CellModel cell = this.cellComputation.getCell();

		if (cell.isOutput()) {
			compileOutputGetter();
		}

		if (cell.isInput()) {
			if (shouldCache( cell )) {
				compileCacheBegin();
				compileInput( cell.getCallChainToCall() );
				compileCacheEnd();
			}
			else {
				compileInput( cell.getCallChainToCall() );
			}
		}
		else {
			final ExpressionNode cellExpr = cell.getExpression();
			final ExpressionCompiler ec = expressionCompiler();
			if (null != cellExpr) {
				if (shouldCache( cell )) {
					compileCacheBegin();
					compileExpression( cellExpr );
					compileCacheEnd();
				}
				else {
					compileExpression( cellExpr );
				}
			}
			else {
				final Object constantValue = cell.getConstantValue();
				ec.compileConst( constantValue );
			}
		}
	}


	private final void compileInput( CallFrame _callChainToCall ) throws CompilerException
	{
		compileInputGetterCall( _callChainToCall );
		expressionCompiler().compileConversionFromResultOf( _callChainToCall.getMethod() );
	}


	private final boolean shouldCache( CellModel _cell )
	{
		return section().engineCompiler().isFullyCaching() && _cell.isCachingCandidate();
	}


	private String cachedIndicatorName = "h$" + methodName();
	private String cacheName = "c$" + methodName();
	private Label skipCachedComputation;


	private final void compileCacheBegin()
	{
		// private boolean h$<x>
		cw().visitField( Opcodes.ACC_PRIVATE, this.cachedIndicatorName, Type.BOOLEAN_TYPE.getDescriptor(), null, null )
				.visitEnd();

		// private <type> c$<x>
		cw().visitField( Opcodes.ACC_PRIVATE, this.cacheName, typeCompiler().typeDescriptor(), null, null ).visitEnd();

		// if (!h$<x>) {
		this.skipCachedComputation = mv().newLabel();
		mv().loadThis();
		mv().getField( classType(), this.cachedIndicatorName, Type.BOOLEAN_TYPE );
		mv().visitJumpInsn( Opcodes.IFNE, this.skipCachedComputation );

		// c$<x> = ...
		mv().loadThis();
	}


	private final void compileCacheEnd()
	{
		final String cachedIndicatorName = "h$" + methodName();
		final String cacheName = "c$" + methodName();

		// this and computed value is on stack, so
		// c$<x> = <value>;
		mv().putField( classType(), cacheName, typeCompiler().type() );

		// h$<x> = true;
		mv().loadThis();
		mv().push( true );
		mv().putField( classType(), cachedIndicatorName, Type.BOOLEAN_TYPE );

		// }
		// return c$<x>;
		mv().mark( this.skipCachedComputation );
		mv().loadThis();
		mv().getField( classType(), cacheName, typeCompiler().type() );

		// In reset(), do:
		if (section().hasReset()) {
			// h$<x> = false;
			GeneratorAdapter r = section().resetter();
			r.loadThis();
			r.push( false );
			r.putField( classType(), cachedIndicatorName, Type.BOOLEAN_TYPE );
		}
	}


	private final void compileOutputGetter() throws CompilerException
	{
		final CellModel cell = this.cellComputation.getCell();
		for (CallFrame callFrame : cell.getCallsToImplement()) {

			if (callFrame.getHead() != callFrame) throw new IllegalArgumentException();

			final Method method = callFrame.getMethod();
			if (0 == callFrame.getArgs().length) {
				compileOutputMethod( cell, method.getName(), method );
			}
			else {
				OutputDistributorCompiler dist = this.section().getOutputDistributorFor( method );
				final String caseName = dist.compileCase( callFrame );
				compileOutputMethod( cell, caseName, method );
			}
		}
	}


	private final void compileOutputMethod( CellModel _cell, String _name, Method _method ) throws CompilerException
	{
		final Type returnType = Type.getReturnType( _method );
		final String sig = "()" + returnType.getDescriptor();
		new OutputMethodCompiler( section(), _name, sig, this.cellComputation, _method ).compile();
	}


	private final Type classType()
	{
		return section().classType();
	}

}