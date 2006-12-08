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
package sej.internal.model;

import sej.CompilerException;
import sej.internal.InnerExpressionException;
import sej.internal.expressions.ExpressionNode;
import sej.runtime.SEJException;

@SuppressWarnings("unused")
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
			e.addMessageContext( "\nSection containing expression is " + _section.getName() + "." );
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
			e.addMessageContext( "\nSection containing expression is " + _section.getName() + "." );
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
						cause.addMessageContext( "\nCell containing expression is " + _cell.getOriginalName() + "." );
					}
				}
				throw cause;
			}
			catch (CompilerException e) {
				e.addMessageContext( "\nCell containing expression is " + _cell.getOriginalName() + "." );
				throw e;
			}
		}
		catch (CompilerException e) {
			e.addMessageContext( "\nReferenced by cell " + _cell.getOriginalName() + "." );
			throw e;
		}
	}

	protected boolean visitCell( CellModel _cell ) throws CompilerException
	{
		return true;
	}


}
