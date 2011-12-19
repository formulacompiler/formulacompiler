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

package org.formulacompiler.spreadsheet.internal.compiler;

import java.util.Collection;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForParentSectionModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForSubSectionModel;
import org.formulacompiler.compiler.internal.model.SectionModel;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.binding.SectionBinding;
import org.formulacompiler.spreadsheet.internal.binding.SubSectionBinding;

final class SectionPath
{
	private ExpressionNode rootNode;
	private ExpressionNode targetNode;
	private SectionModelCompiler targetSectionCompiler;


	public SectionPath( SectionModelCompiler _target )
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


	public void stepInto( SubSectionBinding _sectionDef )
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
		SubSectionBinding section;
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
