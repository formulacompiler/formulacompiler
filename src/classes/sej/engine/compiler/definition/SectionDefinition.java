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
package sej.engine.compiler.definition;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import sej.CallFrame;
import sej.Compiler;
import sej.ModelError;
import sej.Orientation;
import sej.Spreadsheet;
import sej.model.CellIndex;
import sej.model.CellRange;

public class SectionDefinition extends ElementDefinition implements Compiler.Section,
		Comparable<SectionDefinition>
{
	private final EngineDefinition engine;
	private final CallFrame callChainToCall;
	private final CallFrame callToImplement;
	private final CellRange range;
	private final Orientation orientation;
	private final Map<CellIndex, InputCellDefinition> inputs = new HashMap<CellIndex, InputCellDefinition>();
	private final Map<CallFrame, OutputCellDefinition> outputs = new HashMap<CallFrame, OutputCellDefinition>();
	private final SortedSet<SectionDefinition> sections = new TreeSet<SectionDefinition>();


	public SectionDefinition(SectionDefinition _space, CallFrame _callChainToCall, CallFrame _callToImplement,
			CellRange _range, Orientation _orientation) throws ModelError
	{
		super( _space );
		this.engine = _space.getEngine();
		this.callChainToCall = _callChainToCall;
		this.callToImplement = _callToImplement;
		this.range = _range;
		this.orientation = _orientation;

		if (!_space.contains( _range.getFrom() ) || !_space.contains( _range.getTo() )) {
			notInSection( toString(), _range );
		}
	}


	public SectionDefinition(EngineDefinition _engine)
	{
		super( null );
		this.engine = _engine;
		this.callChainToCall = null;
		this.callToImplement = null;
		this.range = CellRange.ENTIRE_SHEET;
		this.orientation = Orientation.VERTICAL;
	}


	public EngineDefinition getEngine()
	{
		return this.engine;
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


	public Orientation getOrientation()
	{
		return this.orientation;
	}


	public SortedSet<SectionDefinition> getSections()
	{
		return this.sections;
	}


	// ------------------------------------------------ Definition By Interface


	public void defineInputCell( Spreadsheet.Cell _cell, CallFrame _callChainToCall ) throws ModelError
	{
		final CellIndex cellIndex = (CellIndex) _cell;
		if (this.inputs.containsKey( cellIndex )) {
			throw new ModelError.DuplicateDefinition( "Input cell '" + cellIndex.toString() + "' is already defined" );
		}
		final InputCellDefinition def = new InputCellDefinition( this, _callChainToCall, cellIndex );
		this.inputs.put( def.getIndex(), def );
		this.engine.getInputs().put( cellIndex, def );
	}


	public void defineOutputCell( Spreadsheet.Cell _cell, CallFrame _call ) throws ModelError
	{
		if (this.outputs.containsKey( _call )) {
			throw new ModelError.DuplicateDefinition( "Output method '" + _call.toString() + "' is already defined" );
		}
		final CellIndex cellIndex = (CellIndex) _cell;
		final OutputCellDefinition def = new OutputCellDefinition( this, _call, cellIndex );
		this.outputs.put( _call, def );
		this.engine.getOutputs().add( def );
	}


	public Compiler.Section defineRepeatingSection( Spreadsheet.Range _range, Orientation _orientation,
			CallFrame _inputCallChainReturningIterable, CallFrame _outputCallToImplementIterable ) throws ModelError
	{
		final CellRange cellRange = (CellRange) _range;
		checkSection( cellRange, _orientation );
		SectionDefinition result = new SectionDefinition( this, _inputCallChainReturningIterable,
				_outputCallToImplementIterable, cellRange, _orientation );
		this.sections.add( result );
		this.engine.getSections().add( result );
		return result;
	}


	// ------------------------------------------------ Utils


	protected void checkSection( CellRange _range, Orientation _orientation ) throws ModelError
	{
		for (SectionDefinition other : this.engine.getSections()) {
			if (other.isInSection( this )) {
				if (other.getOrientation() != _orientation) {
					throw new ModelError.SectionOrientation( "Section '"
							+ _range.toString() + "' has a different orientation than '" + other.toString() + "'" );
				}
				if (other.getRange().overlaps( _range, _orientation )) {
					throw new ModelError.SectionOverlap( "Section '"
							+ _range.toString() + "' overlaps '" + other.toString() + "'" );
				}
			}
		}
	}


	public int compareTo( SectionDefinition _other )
	{
		int thisFrom = this.getRange().getFrom().getIndex( this.getOrientation() );
		int otherFrom = _other.getRange().getFrom().getIndex( _other.getOrientation() );

		if (thisFrom < otherFrom) return -1;
		if (thisFrom > otherFrom) return +1;
		return 0;
	}


	public boolean contains( CellIndex _cellIndex )
	{
		int from = getRange().getFrom().getIndex( getOrientation() );
		int to = getRange().getTo().getIndex( getOrientation() );
		int cellAt = _cellIndex.getIndex( getOrientation() );

		return (cellAt >= from) && (cellAt <= to);
	}


	public SectionDefinition getContainingSection( CellIndex _cellIndex )
	{
		for (SectionDefinition section : getSections()) {
			if (section.contains( _cellIndex )) return section;
		}
		return null;
	}


	public SectionDefinition getSectionFor( CellIndex _index )
	{
		SectionDefinition section = getContainingSection( _index );
		if (null == section) {
			return this;
		}
		else {
			return section.getSectionFor( _index );
		}
	}


	public CellRange getPrototypeRange( CellRange _range ) throws ModelError
	{
		CellIndex from = _range.getFrom();
		CellIndex to = _range.getTo();

		int wantFrom = this.range.getFrom().getIndex( this.orientation );
		int wantTo = this.range.getTo().getIndex( this.orientation );
		int isFrom = from.getIndex( this.orientation );
		int isTo = to.getIndex( this.orientation );

		if ((isFrom != wantFrom) || (isTo != wantTo)) {
			throw new ModelError.SectionExtentNotCovered( _range.toString(), this.toString(), this.getRange().toString() );
		}
		if (!contains( _range.getFrom() ) || !contains( _range.getTo() )) {
			throw new ModelError.NotInSection( null, _range.toString(), this.toString(), this.getRange().toString() );
		}

		if (isTo > isFrom) {
			return new CellRange( from, to.setIndex( this.orientation, isFrom ) );
		}
		else {
			return _range;
		}

	}
}