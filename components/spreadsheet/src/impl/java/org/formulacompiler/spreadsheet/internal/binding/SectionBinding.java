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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.SortedSet;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.Util;
import org.formulacompiler.runtime.New;
import org.formulacompiler.runtime.Resettable;
import org.formulacompiler.spreadsheet.Orientation;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellRange;

public abstract class SectionBinding extends ElementBinding
{
	private final WorkbookBinding workbook;
	private final Orientation orientation;
	private final Class inputClass;
	private final Class outputClass;
	private final Map<CellIndex, InputCellBinding> inputs = New.map();
	private final Map<CallFrame, OutputCellBinding> outputs = New.map();
	private final SortedSet<SubSectionBinding> sections = New.sortedSet();


	SectionBinding( WorkbookBinding _workbook, Class _inputClass, Class _outputClass, Orientation _orientation )
	{
		this.workbook = _workbook;
		this.orientation = _orientation;
		this.inputClass = _inputClass;
		this.outputClass = _outputClass;
	}


	public WorkbookBinding getWorkbook()
	{
		return this.workbook;
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
	public SortedSet<SubSectionBinding> getSections()
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
					+ cellIndex.getShortName() + "' is already defined" );
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


	public SubSectionBinding defineRepeatingSection( Spreadsheet.Range _range, Orientation _orientation,
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
		final SubSectionBinding result = new SubSectionBinding( this, _inputCallChainReturningIterable, _inputClass,
				_outputCallToImplementIterable, _outputClass, cellRange, _orientation );
		this.sections.add( result );
		return result;
	}


	// ------------------------------------------------ Utils


	abstract void checkChildInSection( ElementBinding _child, CellRange _childRange ) throws SpreadsheetException.NotInSection;


	protected void validateAccessible( CallFrame _chain )
	{
		Util.validateCallable( getInputClass(), _chain.getHead().getMethod() );
		for (CallFrame frame : _chain.getFrames()) {
			Util.validateIsAccessible( frame.getMethod(), "Input" );
		}
	}

	protected void validateImplementable( CallFrame _chain )
	{
		if (_chain.getHead() != _chain) throw new IllegalArgumentException( "Cannot bind outputs to chains of calls" );
		Util.validateIsImplementable( _chain.getMethod(), "Output" );
	}


	protected void checkSection( CellRange _range, Orientation _orientation ) throws CompilerException
	{
		for (SubSectionBinding sub : this.getSections()) {
			if (sub.getRange().overlaps( _range, _orientation )) {
				throw new SpreadsheetException.SectionOverlap( "Section '"
						+ _range.getShortName() + "' overlaps '" + sub.toString() + "'" );
			}
		}
	}


	public abstract boolean contains( CellIndex _cellIndex );


	public SubSectionBinding getContainingSection( CellIndex _cellIndex )
	{
		for (SubSectionBinding section : getSections()) {
			if (section.contains( _cellIndex )) return section;
		}
		return null;
	}

	public SectionBinding getSectionFor( CellIndex _index )
	{
		final SubSectionBinding section = getContainingSection( _index );
		if (null == section) {
			return this;
		}
		else {
			return section.getSectionFor( _index );
		}
	}


	public abstract CellRange[] tiling( CellRange _range );


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
			for (SubSectionBinding sub : SectionBinding.this.sections) {
				if (sub.getCallToImplement() != null) {
					abstractMethods.remove( Util.nameAndSignatureOf( sub.getCallToImplement().getMethod() ) );
				}
			}
			if (abstractMethods.size() > 0) {
				final Method m = abstractMethods.values().iterator().next();
				throw new CompilerException.MethodNotImplemented( m );
			}
		}
	}
}
