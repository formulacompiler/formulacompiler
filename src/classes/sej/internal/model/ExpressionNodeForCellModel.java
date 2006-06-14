/*
 * Copyright � 2006 by Abacus Research AG, Switzerland.
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

import java.io.IOException;

import sej.describable.DescriptionBuilder;
import sej.expressions.ExpressionNode;


public class ExpressionNodeForCellModel extends ExpressionNode
{
	private CellModel cellModel;


	public ExpressionNodeForCellModel(CellModel _cellModel)
	{
		super();
		setCellModel( _cellModel );
	}


	public CellModel getCellModel()
	{
		return this.cellModel;
	}


	public void setCellModel( CellModel _cellModel )
	{
		if (_cellModel == this.cellModel) return;
		this.cellModel = _cellModel;
	}


	@Override
	public ExpressionNode cloneWithoutArguments()
	{
		return new ExpressionNodeForCellModel( this.cellModel );
	}


	@Override
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		if (null == this.cellModel) {
			_to.append( "#NULL" );
		}
		else {
			_to.append( this.cellModel.toString() );
		}
	}


}