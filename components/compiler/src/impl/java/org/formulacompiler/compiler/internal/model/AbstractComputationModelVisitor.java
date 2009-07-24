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

package org.formulacompiler.compiler.internal.model;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.InnerExpressionException;


@SuppressWarnings( "unused" )
public abstract class AbstractComputationModelVisitor implements ComputationModelVisitor
{

	public final boolean visit( ComputationModel _model ) throws CompilerException
	{
		return visitModel( _model );
	}

	protected boolean visitModel( ComputationModel _model ) throws CompilerException
	{
		return true;
	}


	public final boolean visited( ComputationModel _model ) throws CompilerException
	{
		return visitedModel( _model );
	}

	protected boolean visitedModel( ComputationModel _model ) throws CompilerException
	{
		return true;
	}


	public final boolean visit( SectionModel _section ) throws CompilerException
	{
		try {
			return visitSection( _section );
		}
		catch (CompilerException e) {
			e.addMessageContext( "\nSection containing expression is " + _section.getFullName() + "." );
			throw e;
		}
	}

	protected boolean visitSection( SectionModel _section ) throws CompilerException
	{
		return true;
	}


	public boolean visited( SectionModel _section ) throws CompilerException
	{
		try {
			return visitedSection( _section );
		}
		catch (CompilerException e) {
			e.addMessageContext( "\nSection containing expression is " + _section.getFullName() + "." );
			throw e;
		}
	}

	protected boolean visitedSection( SectionModel _section ) throws CompilerException
	{
		return true;
	}


	public final boolean visit( CellModel _cell ) throws CompilerException
	{
		try {
			try {
				return visitCell( _cell );
			}
			catch (InnerExpressionException e) {
				final CompilerException cause = e.getCause();
				final ExpressionNode errorNode = e.getErrorNode();
				if (null != errorNode) {
					cause.addMessageContext( errorNode.getContext( errorNode ) );
				}
				else {
					final ExpressionNode cellExpr = _cell.getExpression();
					if (null != cellExpr) {
						cause.addMessageContext( cellExpr.getContext( null ) );
					}
					else {
						cause.addMessageContext( "\nCell containing expression is " + _cell.getFullName() + "." );
					}
				}
				throw cause;
			}
			catch (CompilerException e) {
				e.addMessageContext( "\nCell containing expression is " + _cell.getFullName() + "." );
				throw e;
			}
		}
		catch (CompilerException e) {
			e.addMessageContext( "\nReferenced by cell " + _cell.getFullName() + "." );
			throw e;
		}
	}

	protected boolean visitCell( CellModel _cell ) throws CompilerException
	{
		return true;
	}


}
