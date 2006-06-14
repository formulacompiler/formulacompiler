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
package sej.internal.spreadsheet.binding;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import sej.CallFrame;
import sej.CompilerError;
import sej.Orientation;
import sej.Resettable;
import sej.Spreadsheet;
import sej.internal.Util;
import sej.internal.spreadsheet.CellIndex;
import sej.internal.spreadsheet.CellRange;

public class SectionBinding extends ElementBinding implements Comparable<SectionBinding>
{
	private final WorkbookBinding workbook;
	private final CallFrame callChainToCall;
	private final CallFrame callToImplement;
	private final CellRange range;
	private final Orientation orientation;
	private final Class inputClass;
	private final Class outputClass;
	private final Map<CellIndex, InputCellBinding> inputs = new HashMap<CellIndex, InputCellBinding>();
	private final Map<CallFrame, OutputCellBinding> outputs = new HashMap<CallFrame, OutputCellBinding>();
	private final SortedSet<SectionBinding> sections = new TreeSet<SectionBinding>();


	private SectionBinding(SectionBinding _space, CallFrame _callChainToCall, Class _inputClass,
			CallFrame _callToImplement, Class _outputClass, CellRange _range, Orientation _orientation)
			throws CompilerError
	{
		super( _space );
		this.workbook = _space.getWorkbook();
		this.callChainToCall = _callChainToCall;
		this.callToImplement = _callToImplement;
		this.range = _range;
		this.orientation = _orientation;
		this.inputClass = _inputClass;
		this.outputClass = _outputClass;

		if (!_space.contains( _range.getFrom() ) || !_space.contains( _range.getTo() )) {
			notInSection( toString(), _range );
		}
	}


	public SectionBinding(WorkbookBinding _workbook, Class _inputClass, Class _outputClass)
	{
		super( null );
		this.workbook = _workbook;
		this.callChainToCall = null;
		this.callToImplement = null;
		this.range = CellRange.getEntireSheet( _workbook.getWorkbook() );
		this.orientation = Orientation.VERTICAL;
		this.inputClass = _inputClass;
		this.outputClass = _outputClass;
	}


	public WorkbookBinding getWorkbook()
	{
		return this.workbook;
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


	public Class getInputClass()
	{
		return this.inputClass;
	}


	public Class getOutputClass()
	{
		return this.outputClass;
	}


	public SortedSet<SectionBinding> getSections()
	{
		return this.sections;
	}


	// ------------------------------------------------ Definition By Interface


	public void defineInputCell( Spreadsheet.Cell _cell, CallFrame _callChainToCall ) throws CompilerError
	{
		validateAccessible( _callChainToCall );
		final CellIndex cellIndex = (CellIndex) _cell;
		if (this.inputs.containsKey( cellIndex )) {
			throw new CompilerError.DuplicateDefinition( "Input cell '" + cellIndex.toString() + "' is already defined" );
		}
		final InputCellBinding def = new InputCellBinding( this, _callChainToCall, cellIndex );
		this.inputs.put( def.getIndex(), def );
		this.workbook.getInputs().put( cellIndex, def );
	}


	public void defineOutputCell( Spreadsheet.Cell _cell, CallFrame _call ) throws CompilerError
	{
		validateImplementable( _call );
		if (this.outputs.containsKey( _call )) {
			throw new CompilerError.DuplicateDefinition( "Output method '" + _call.toString() + "' is already defined" );
		}
		final CellIndex cellIndex = (CellIndex) _cell;
		final OutputCellBinding def = new OutputCellBinding( this, _call, cellIndex );
		this.outputs.put( _call, def );
		this.workbook.getOutputs().add( def );
	}


	public SectionBinding defineRepeatingSection( Spreadsheet.Range _range, Orientation _orientation,
			CallFrame _inputCallChainReturningIterable, Class _inputClass, CallFrame _outputCallToImplementIterable,
			Class _outputClass ) throws CompilerError
	{
		if (_inputClass == null) throw new IllegalArgumentException( "inputClass is null" );
		validateAccessible( _inputCallChainReturningIterable );
		Util.validateIsAccessible( _inputClass, "input class" );
		if (_outputCallToImplementIterable != null) {
			if (_outputClass == null) throw new IllegalArgumentException( "outputClass is null" );
			validateImplementable( _outputCallToImplementIterable );
			Util.validateIsImplementable( _outputClass, "output class" );
		}

		final CellRange cellRange = (CellRange) _range;
		checkSection( cellRange, _orientation );
		SectionBinding result = new SectionBinding( this, _inputCallChainReturningIterable, _inputClass,
				_outputCallToImplementIterable, _outputClass, cellRange, _orientation );
		this.sections.add( result );
		this.workbook.getSections().add( result );
		return result;
	}


	// ------------------------------------------------ Utils


	protected void validateAccessible( CallFrame _chain )
	{
		Util.validateCallable( getInputClass(), _chain.getHead().getMethod() );
		for (CallFrame frame : _chain.getFrames()) {
			Util.validateIsAccessible( frame.getMethod(), "input" );
		}
	}

	protected void validateImplementable( CallFrame _chain )
	{
		if (_chain.getHead() != _chain) throw new IllegalArgumentException( "Cannot bind outputs to chains of calls" );
		Util.validateIsImplementable( _chain.getMethod(), "output" );
	}


	protected void checkSection( CellRange _range, Orientation _orientation ) throws CompilerError
	{
		for (SectionBinding other : this.workbook.getSections()) {
			if (other.isInSection( this )) {
				if (other.getOrientation() != _orientation) {
					throw new CompilerError.SectionOrientation( "Section '"
							+ _range.toString() + "' has a different orientation than '" + other.toString() + "'" );
				}
				if (other.getRange().overlaps( _range, _orientation )) {
					throw new CompilerError.SectionOverlap( "Section '"
							+ _range.toString() + "' overlaps '" + other.toString() + "'" );
				}
			}
		}
	}


	public int compareTo( SectionBinding _other )
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


	public SectionBinding getContainingSection( CellIndex _cellIndex )
	{
		for (SectionBinding section : getSections()) {
			if (section.contains( _cellIndex )) return section;
		}
		return null;
	}


	public SectionBinding getSectionFor( CellIndex _index )
	{
		SectionBinding section = getContainingSection( _index );
		if (null == section) {
			return this;
		}
		else {
			return section.getSectionFor( _index );
		}
	}


	public CellRange getPrototypeRange( CellRange _range ) throws CompilerError
	{
		CellIndex from = _range.getFrom();
		CellIndex to = _range.getTo();

		int wantFrom = this.range.getFrom().getIndex( this.orientation );
		int wantTo = this.range.getTo().getIndex( this.orientation );
		int isFrom = from.getIndex( this.orientation );
		int isTo = to.getIndex( this.orientation );

		if ((isFrom != wantFrom) || (isTo != wantTo)) {
			throw new CompilerError.SectionExtentNotCovered( _range.toString(), this.toString(), this.getRange()
					.toString() );
		}
		if (!contains( _range.getFrom() ) || !contains( _range.getTo() )) {
			throw new CompilerError.NotInSection( null, _range.toString(), this.toString(), this.getRange().toString() );
		}

		if (isTo > isFrom) {
			return new CellRange( from, to.setIndex( this.orientation, isFrom ) );
		}
		else {
			return _range;
		}

	}


	public void validate() throws CompilerError
	{
		if (this.outputClass != null) {
			validateOutputIsFullyImplemented();
		}
		for (SectionBinding sub : this.sections) {
			sub.validate();
		}
	}


	private void validateOutputIsFullyImplemented() throws CompilerError
	{
		Map<String, Method> abstractMethods = Util.abstractMethodsOf( this.outputClass );
		if (abstractMethods.size() > 0) {
			if (Resettable.class.isAssignableFrom( this.outputClass )) {
				abstractMethods.remove( "reset()V" );
			}
			for (CallFrame cf : SectionBinding.this.outputs.keySet()) {
				abstractMethods.remove( Util.nameAndSignatureOf( cf.getMethod() ) );
			}
			if (abstractMethods.size() > 0) {
				final Method m = abstractMethods.values().iterator().next();
				throw new CompilerError.MethodNotImplemented( m );
			}
		}
	}

}