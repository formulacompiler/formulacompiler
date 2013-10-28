/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
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
import org.formulacompiler.compiler.internal.NumericTypeImpl;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.runtime.spreadsheet.CellAddress;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;


final class CellMethodCompiler extends NullaryValueMethodCompiler
{
	private final CellModel cell;


	CellMethodCompiler( SectionCompiler _section, CellModel _cell ) throws CompilerException
	{
		super( _section, 0, getMethodName( _section, _cell ), _cell.getDataType() );
		this.cell = _cell;
		validate();
	}

	private static String getMethodName( SectionCompiler _section, CellModel _cell )
	{
		if (_section.engineCompiler().getCompileToReadableCode()) {
			return _section.newGetterName( cellNameToIdent( _cell.getShortName() ) );
		}
		else {
			return _section.newGetterName();
		}
	}

	private static String cellNameToIdent( String _name )
	{
		String result = _name;
		final int posOfDot = result.indexOf( '.' );
		if (posOfDot >= 0) {
			result = result.substring( posOfDot + 1 );
		}
		if (result.endsWith( "()" )) {
			result = result.substring( 0, result.length() - 2 );
		}
		// Replace all non-word characters by underscores.
		result = result.replaceAll( "\\W", "_" );
		return result;
	}

	void validate() throws CompilerException
	{
		validateInputType();
		validateOutputTypes();
	}

	private void validateInputType() throws CompilerException
	{
		if (this.cell.isInput()) {
			((NumericTypeImpl) section().engineCompiler().getNumericType()).validateReturnTypeForCell( this.cell
					.getCallChainToCall().getMethod() );
		}
	}

	private void validateOutputTypes() throws CompilerException
	{
		for (CallFrame frame : this.cell.getCallsToImplement()) {
			if (frame.getHead() != frame) {
				throw new CompilerException.UnsupportedDataType( "The output method " + frame + " cannot be chained." );
			}
			((NumericTypeImpl) section().engineCompiler().getNumericType()).validateReturnTypeForCell( frame
					.getMethod() );
		}
	}

	@Override
	protected void compileBody() throws CompilerException
	{
		if (this.cell.isOutput()) {
			compileOutputGetter();
		}

		if (this.cell.isInput()) {
			if (shouldCache( this.cell )) {
				compileCacheBegin();
				compileInput( this.cell.getCallChainToCall() );
				compileCacheEnd();
			}
			else {
				compileInput( this.cell.getCallChainToCall() );
			}
		}
		else {
			final ExpressionNode cellExpr = this.cell.getExpression();
			final ExpressionCompiler ec = expressionCompiler();
			if (null != cellExpr) {
				if (shouldCache( this.cell )) {
					compileCacheBegin();
					compileExpression( cellExpr );
					compileCacheEnd();
				}
				else {
					compileExpression( cellExpr );
				}
			}
			else {
				final Object constantValue = this.cell.getConstantValue();
				ec.compileConst( constantValue );
			}
		}
	}


	private final void compileInput( CallFrame _callChainToCall ) throws CompilerException
	{
		compileInputGetterCall( _callChainToCall );
		expressionCompiler().compileConversionFromResultOf( _callChainToCall.getMethod() );

		if (section().isComputationListenerEnabled()) {
			final Object source = this.cell.getSource();
			if (source instanceof CellAddress) {
				final CellAddress cellAddress = (CellAddress) source;
				final String name = this.cell.getName();
				expressionCompiler().compileLogging( cellAddress, name, true, this.cell.isOutput() );
			}
		}
	}


	private final boolean shouldCache( CellModel _cell )
	{
		return section().engineCompiler().isFullyCaching() && _cell.isCachingCandidate();
	}


	private final String cachedIndicatorName = "h$" + methodName();
	private final String cacheName = "c$" + methodName();
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
			final GeneratorAdapter r = section().resetter();
			r.loadThis();
			r.push( false );
			r.putField( classType(), cachedIndicatorName, Type.BOOLEAN_TYPE );
		}
	}


	private final void compileOutputGetter() throws CompilerException
	{
		for (CallFrame callFrame : this.cell.getCallsToImplement()) {

			if (callFrame.getHead() != callFrame) throw new IllegalArgumentException();

			final Method method = callFrame.getMethod();
			if (0 == callFrame.getArgs().length) {
				compileOutputMethod( method.getName(), method );
			}
			else {
				final OutputDistributorCompiler dist = this.section().getOutputDistributorFor( method );
				final String caseName = dist.compileCase( callFrame );
				compileOutputMethod( caseName, method );
			}
		}
	}


	private final void compileOutputMethod( String _name, Method _method ) throws CompilerException
	{
		final Type returnType = Type.getReturnType( _method );
		final String sig = "()" + returnType.getDescriptor();
		new OutputMethodCompiler( section(), _name, sig, this, _method ).compile();
	}


	private final Type classType()
	{
		return section().classType();
	}

}