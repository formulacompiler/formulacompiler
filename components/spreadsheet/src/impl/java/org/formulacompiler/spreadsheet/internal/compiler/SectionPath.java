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
package org.formulacompiler.spreadsheet.internal.compiler;

import java.util.Collection;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForParentSectionModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForSubSectionModel;
import org.formulacompiler.compiler.internal.model.SectionModel;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.binding.SectionBinding;

final class SectionPath
{
	private ExpressionNode rootNode;
	private ExpressionNode targetNode;
	private SectionModelCompiler targetSectionCompiler;


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


	public void stepInto( SectionBinding _sectionDef )
	{
		SectionModelCompiler sectionCompiler = this.targetSectionCompiler.getOrCreateSectionCompiler( _sectionDef );
		SectionModel sectionModel = sectionCompiler.getSectionModel();
		ExpressionNode step = new ExpressionNodeForSubSectionModel( sectionModel );
		add( step );
		this.targetSectionCompiler = sectionCompiler;
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
			this.targetNode.arguments().add( _step );
			this.targetNode = _step;
		}
	}


	public void buildStepsInto( CellIndex _cellIndex )
	{
		SectionBinding section;
		while (null != (section = this.targetSectionCompiler.getSectionDef().getContainingSection( _cellIndex ))) {
			stepInto( section );
		}
	}


	/**
	 * For the moment, we don't allow stepping back into subsections. That would require knowledge
	 * about whether we're aggregating.
	 * 
	 * @see SpreadsheetToModelCompilerTest#testBandCellWithSumOverOuterBand()
	 */
	public void stepOutTo( CellIndex _cellIndex ) throws CompilerException
	{
		while (!this.targetSectionCompiler.getSectionDef().contains( _cellIndex )) {
			stepOut();
		}
		final SectionBinding containingSection = this.targetSectionCompiler.getSectionDef().getContainingSection(
				_cellIndex );
		if (null != containingSection) {
			throw new CompilerException.ReferenceToOuterInnerCell();
		}
	}


	public ExpressionNode wrapAround( ExpressionNode _innerNode )
	{
		getTargetNode().arguments().add( _innerNode );
		return getRootNode();
	}

	public ExpressionNode wrapAround( Collection<ExpressionNode> _innerNodes )
	{
		getTargetNode().arguments().addAll( _innerNodes );
		return getRootNode();
	}


}
