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
package org.formulacompiler.spreadsheet.internal.binding;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.SortedSet;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.Util;
import org.formulacompiler.describable.DescriptionBuilder;
import org.formulacompiler.runtime.New;
import org.formulacompiler.runtime.Resettable;
import org.formulacompiler.spreadsheet.Orientation;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellRange;

/**
 * Subsections are sorted
 * 
 * @author peo
 */
public class SectionBinding extends ElementBinding implements Comparable<SectionBinding>
{
	private final WorkbookBinding workbook;
	private final CallFrame callChainToCall;
	private final CallFrame callToImplement;
	private final CellRange range;
	private final Orientation orientation;
	private final Class inputClass;
	private final Class outputClass;
	private final Map<CellIndex, InputCellBinding> inputs = New.newMap();
	private final Map<CallFrame, OutputCellBinding> outputs = New.newMap();
	private final SortedSet<SectionBinding> sections = New.newSortedSet();


	private SectionBinding(SectionBinding _space, CallFrame _callChainToCall, Class _inputClass,
			CallFrame _callToImplement, Class _outputClass, CellRange _range, Orientation _orientation)
			throws CompilerException
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


	/**
	 * Constructs the root binding of a workbook, which encompasses the entire workbook, but does not
	 * constitute a repeating section. Nevertheless, it has a default orientation, vertical, which
	 * determines the sort order of its subsections.
	 */
	public SectionBinding(WorkbookBinding _workbook, Class _inputClass, Class _outputClass)
	{
		super( null );
		this.workbook = _workbook;
		this.callChainToCall = null;
		this.callToImplement = null;
		this.range = CellRange.getEntireWorkbook( _workbook.getWorkbook() );
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


	/**
	 * The subsections are sorted to enable efficient splitting of aggregated ranges into the parts
	 * overlapping sections.
	 */
	public SortedSet<SectionBinding> getSections()
	{
		return this.sections;
	}


	// ------------------------------------------------ Definition By Interface


	public void defineInputCell( Spreadsheet.Cell _cell, CallFrame _callChainToCall ) throws CompilerException
	{
		validateAccessible( _callChainToCall );
		final CellIndex cellIndex = (CellIndex) _cell;
		if (this.inputs.containsKey( cellIndex )) {
			throw new CompilerException.DuplicateDefinition( "Input cell '"
					+ cellIndex.toString() + "' is already defined" );
		}
		final InputCellBinding def = new InputCellBinding( this, _callChainToCall, cellIndex );
		this.inputs.put( def.getIndex(), def );
		this.workbook.add( def );
	}


	public void defineOutputCell( Spreadsheet.Cell _cell, CallFrame _call ) throws CompilerException
	{
		validateImplementable( _call );
		if (this.outputs.containsKey( _call )) {
			throw new CompilerException.DuplicateDefinition( "Output method '" + _call.toString() + "' is already defined" );
		}
		final CellIndex cellIndex = (CellIndex) _cell;
		final OutputCellBinding def = new OutputCellBinding( this, _call, cellIndex );
		this.outputs.put( _call, def );
		this.workbook.add( def );
	}


	public SectionBinding defineRepeatingSection( Spreadsheet.Range _range, Orientation _orientation,
			CallFrame _inputCallChainReturningIterable, Class _inputClass, CallFrame _outputCallToImplementIterable,
			Class _outputClass ) throws CompilerException
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
		this.workbook.add(  result );
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


	protected void checkSection( CellRange _range, Orientation _orientation ) throws CompilerException
	{
		for (SectionBinding sub : this.getSections()) {
			if (sub.getRange().overlaps( _range, _orientation )) {
				throw new SpreadsheetException.SectionOverlap( "Section '"
						+ _range.toString() + "' overlaps '" + sub.toString() + "'" );
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
		return getRange().contains( _cellIndex );
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


	public CellRange getPrototypeRange( CellRange _range ) throws CompilerException
	{
		CellIndex from = _range.getFrom();
		CellIndex to = _range.getTo();

		int wantFrom = this.range.getFrom().getIndex( this.orientation );
		int wantTo = this.range.getTo().getIndex( this.orientation );
		int isFrom = from.getIndex( this.orientation );
		int isTo = to.getIndex( this.orientation );

		if ((isFrom != wantFrom) || (isTo != wantTo)) {
			throw new SpreadsheetException.SectionExtentNotCovered( _range.toString(), this.toString(), this.orientation );
		}
		if (!contains( _range.getFrom() ) || !contains( _range.getTo() )) {
			throw new SpreadsheetException.NotInSection( null, _range.toString(), this.toString(), this.getRange().toString() );
		}

		if (isTo > isFrom) {
			return new CellRange( from, to.setIndex( this.orientation, isFrom ) );
		}
		else {
			return _range;
		}

	}


	public void validate() throws CompilerException
	{
		if (this.outputClass != null) {
			validateOutputIsFullyImplemented();
		}
		for (SectionBinding sub : this.sections) {
			sub.validate();
		}
	}


	private void validateOutputIsFullyImplemented() throws CompilerException
	{
		Map<String, Method> abstractMethods = Util.abstractMethodsOf( this.outputClass );
		if (abstractMethods.size() > 0) {
			if (Resettable.class.isAssignableFrom( this.outputClass )) {
				abstractMethods.remove( "reset()V" );
			}
			for (CallFrame cf : SectionBinding.this.outputs.keySet()) {
				abstractMethods.remove( Util.nameAndSignatureOf( cf.getMethod() ) );
			}
			for (SectionBinding sub : SectionBinding.this.sections) {
				if (sub.callToImplement != null) {
					abstractMethods.remove( Util.nameAndSignatureOf( sub.callToImplement.getMethod() ) );
				}
			}
			if (abstractMethods.size() > 0) {
				final Method m = abstractMethods.values().iterator().next();
				throw new CompilerException.MethodNotImplemented( m );
			}
		}
	}


	@Override
	public void describeTo( DescriptionBuilder _to )
	{
		getRange().describeTo( _to );
		_to.append( " (which iterates " );
		getCallChainToCall().describeTo( _to );
		_to.append( ")" );
	}


}