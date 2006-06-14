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
package sej.internal.model.compiler;

import sej.CompilerError;
import sej.expressions.ExpressionNode;
import sej.internal.model.ExpressionNodeForParentSectionModel;
import sej.internal.model.ExpressionNodeForSubSectionModel;
import sej.internal.model.SectionModel;
import sej.internal.spreadsheet.CellIndex;
import sej.internal.spreadsheet.CellRange;
import sej.internal.spreadsheet.binding.SectionBinding;

final class SectionPath
{
	private ExpressionNode rootNode;
	private ExpressionNode targetNode;
	private SectionModelCompiler targetSectionCompiler;
	private CellRange targetRange;


	public SectionPath(SectionModelCompiler _target)
	{
		this.targetSectionCompiler = _target;
	}


	public ExpressionNode getRootNode()
	{
		return this.rootNode;
	}


	public ExpressionNode getTargetNode()
	{
		return this.targetNode;
	}


	public SectionModelCompiler getSectionCompiler()
	{
		return this.targetSectionCompiler;
	}


	public CellRange getTargetRange()
	{
		return this.targetRange;
	}


	public void setTargetRange( CellRange _targetRange )
	{
		this.targetRange = _targetRange;
	}


	public void stepInto( SectionBinding _sectionDef ) throws CompilerError
	{
		SectionModelCompiler sectionCompiler = this.targetSectionCompiler.getOrCreateSectionCompiler( _sectionDef );
		SectionModel sectionModel = sectionCompiler.getSectionModel();
		ExpressionNode step = new ExpressionNodeForSubSectionModel( sectionModel );
		add( step );
		this.targetSectionCompiler = sectionCompiler;
		if (null != this.targetRange) {
			this.targetRange = _sectionDef.getPrototypeRange( this.targetRange );
		}
	}


	public void stepOut()
	{
		SectionModel parentModel = this.targetSectionCompiler.getSectionModel().getSection();
		assert null != parentModel;
		SectionModelCompiler parentCompiler = this.targetSectionCompiler.getSection();
		ExpressionNode step = new ExpressionNodeForParentSectionModel( parentModel );
		add( step );
		this.targetSectionCompiler = parentCompiler;
	}


	private void add( ExpressionNode _step )
	{
		if (null == this.targetNode) {
			assert null == this.rootNode;
			this.rootNode = _step;
			this.targetNode = _step;
		}
		else {
			this.targetNode.getArguments().add( _step );
			this.targetNode = _step;
		}
	}


	public void buildStepsInto( CellIndex _cellIndex ) throws CompilerError
	{
		SectionBinding section;
		while (null != (section = this.targetSectionCompiler.getSectionDef().getContainingSection( _cellIndex ))) {
			stepInto( section );
		}
	}


	public void buildStepsTo( CellIndex _cellIndex ) throws CompilerError
	{
		while (!this.targetSectionCompiler.getSectionDef().contains( _cellIndex )) {
			stepOut();
		}
		buildStepsInto( _cellIndex );
	}


}
