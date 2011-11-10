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

package org.formulacompiler.spreadsheet.internal.binding;


import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.AbstractDescribable;
import org.formulacompiler.compiler.internal.DescriptionBuilder;
import org.formulacompiler.spreadsheet.Orientation;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.CellRange;


/**
 * Subsections are sorted.
 * <p/>
 * Note: this class has a natural ordering that is inconsistent with equals.
 *
 * @author peo
 */
public class SubSectionBinding extends SectionBinding implements Comparable<SubSectionBinding>
{
	private final SectionBinding section;
	private final CallFrame callChainToCall;
	private final CallFrame callToImplement;
	private final CellRange range;

	public SubSectionBinding( final SectionBinding _space, final CallFrame _callChainToCall, final Class _inputClass, final CallFrame _callToImplement, final Class _outputClass, final CellRange _range, final Orientation _orientation ) throws CompilerException
	{
		super( _space.getWorkbook(), _inputClass, _outputClass, _orientation );
		this.section = _space;
		this.callChainToCall = _callChainToCall;
		this.callToImplement = _callToImplement;
		this.range = _range;

		_space.checkChildInSection( this, _range );
	}


	@Override
	void checkChildInSection( ElementBinding _child, CellRange _childRange ) throws SpreadsheetException.NotInSection
	{
		if (!contains( _childRange.getFrom() ) || !contains( _childRange.getTo() )) {
			throw new SpreadsheetException.NotInSection( _child.toString(), _childRange.getShortName(), toString(), getRange().getShortName() );
		}
	}


	public SectionBinding getSection()
	{
		return this.section;
	}


	public CallFrame getCallChainToCall()
	{
		return this.callChainToCall;
	}


	public CallFrame getCallToImplement()
	{
		return this.callToImplement;
	}


	public CellRange getRange()
	{
		return this.range;
	}


	public int compareTo( SubSectionBinding _other )
	{
		if (this == _other) return 0;

		final int thisFrom = this.getRange().getFrom().getIndex( this.getOrientation() );
		final int otherFrom = _other.getRange().getFrom().getIndex( _other.getOrientation() );

		if (thisFrom < otherFrom) return -1;
		if (thisFrom > otherFrom) return +1;
		return 0;
	}


	@Override
	public boolean contains( CellIndex _cellIndex )
	{
		return getRange().contains( _cellIndex );
	}


	@Override
	public CellRange[] tiling( final CellRange _range )
	{
		return _range.tilingAround( this.range, getOrientation() );
	}


	public CellRange getPrototypeRange( CellRange _range ) throws CompilerException
	{
		final CellIndex from = _range.getFrom();
		final CellIndex to = _range.getTo();

		final int wantFrom = this.range.getFrom().getIndex( getOrientation() );
		final int wantTo = this.range.getTo().getIndex( getOrientation() );
		final int isFrom = from.getIndex( getOrientation() );
		final int isTo = to.getIndex( getOrientation() );

		if ((isFrom != wantFrom) || (isTo != wantTo)) {
			throw new SpreadsheetException.SectionExtentNotCovered( _range.getShortName(), this.toString(), getOrientation() );
		}
		if (!contains( _range.getFrom() ) || !contains( _range.getTo() )) {
			throw new SpreadsheetException.NotInSection( null, _range.getShortName(), this.toString(), this.getRange()
					.getShortName() );
		}

		if (isTo > isFrom) {
			return CellRange.getCellRange( from, to.setIndex( getOrientation(), isFrom ) );
		}
		else {
			return _range;
		}
	}


	@Override
	Iterable<CellInstance> getCellInstances()
	{
		return getRange().getCellInstances();
	}


	@Override
	public void describeTo( DescriptionBuilder _to )
	{
		getRange().describeTo( _to );
		if (this.callChainToCall != null) {
			_to.append( " (which iterates " );
			((AbstractDescribable) this.callChainToCall).describeTo( _to );
			_to.append( ")" );
		}
	}
}
