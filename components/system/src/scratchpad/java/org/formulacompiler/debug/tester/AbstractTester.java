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
package org.formulacompiler.debug.tester;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.FormulaCompiler;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.compiler.internal.Debug;
import org.formulacompiler.runtime.New;
import org.formulacompiler.runtime.Resettable;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Orientation;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.Spreadsheet.Cell;
import org.formulacompiler.spreadsheet.Spreadsheet.Range;
import org.formulacompiler.spreadsheet.SpreadsheetBinder.Section;


public abstract class AbstractTester
{


	protected abstract String sourceFileName();
	protected abstract void define() throws Exception;


	// ------------------------------------------------ Internal stuff


	private EngineBuilder builder;
	private SectionDef root;
	private String typeName;
	private SaveableEngine engine;
	private Outputs computation;


	protected void run( String[] _args ) throws Exception
	{
		run();
	}


	private final void run() throws Exception
	{
		this.builder = SpreadsheetCompiler.newEngineBuilder();
		this.builder.loadSpreadsheet( sourceFileName() );
		this.builder.setInputClass( Inputs.class );
		this.builder.setOutputClass( Outputs.class );
		this.root = new SectionDef();

		define();

		this.engine = this.builder.compile();

		Debug.saveEngine( this.engine, "/temp/debug.jar" );

		this.computation = (Outputs) this.engine.getComputationFactory().newComputation( this.root.input );

		this.root.logOutputs( this.computation );
	}


	protected final void setNumericType( NumericType _type )
	{
		this.builder.setNumericType( _type );
		this.typeName = _type.valueType().getSimpleName();
	}


	protected final void defineInputsFromNames( String _pattern ) throws Exception
	{
		defineNames( _pattern, DefinitionType.INPUT );
	}

	protected final void defineOutputsFromNames( String _pattern ) throws Exception
	{
		defineNames( _pattern, DefinitionType.OUTPUT );
	}

	protected final void defineNames( String _pattern, DefinitionType _type ) throws Exception
	{
		final Pattern pattern = Pattern.compile( _pattern );
		final Map<String, Range> defs = this.builder.getSpreadsheet().getDefinedNames();
		for (Map.Entry<String, Range> def : defs.entrySet()) {
			if (pattern.matcher( def.getKey() ).matches()) {
				defineCell( def.getValue().getTopLeft(), _type );
			}
		}
	}

	protected final void defineCell( Cell _cell, DefinitionType _type ) throws Exception
	{
		final SectionDef parent = parentSectionFor( _cell );
		parent.defineCell( _cell, _type );
	}


	protected final void defineSection( String _rangeName ) throws Exception
	{
		final Range range = this.builder.getSpreadsheet().getRange( _rangeName );
		final SectionDef parent = parentSectionFor( range );
		parent.defineSection( range );
	}


	private final SectionDef parentSectionFor( Range _range )
	{
		return this.root.parentSectionFor( _range );
	}

	private final SectionDef parentSectionFor( Cell _cell )
	{
		return this.root.parentSectionFor( _cell );
	}


	private final class SectionDef
	{
		private final Section binder;
		private final Range range;
		private final List<SectionDef> subs = New.list();
		private final Inputs input;
		private final List<String> outputNames = New.list();
		private int nextSectionIndex = 0;

		public SectionDef( SectionDef _parent, Range _range, Section _binder, Inputs _input ) throws Exception
		{
			this.range = _range;
			this.binder = _binder;
			this.input = _input;
			_parent.subs.add( this );
		}

		public SectionDef() throws Exception
		{
			this.binder = AbstractTester.this.builder.getRootBinder();
			this.range = null;
			this.input = new Inputs();
		}

		public SectionDef parentSectionFor( Range _range )
		{
			for (SectionDef sub : this.subs) {
				if (sub.range.contains( _range )) {
					return sub.parentSectionFor( _range );
				}
			}
			return this;
		}

		public SectionDef parentSectionFor( Cell _cell )
		{
			for (SectionDef sub : this.subs) {
				if (sub.range.contains( _cell )) {
					return sub.parentSectionFor( _cell );
				}
			}
			return this;
		}

		public void defineCell( Cell _cell, DefinitionType _type ) throws Exception
		{
			final String typeName = (_cell.getConstantValue() instanceof String) ? "String" : AbstractTester.this.typeName;
			switch (_type) {
				case INPUT: {
					final CallFrame getter = FormulaCompiler.newCallFrame( Inputs.class.getMethod( "get" + typeName,
							Integer.TYPE ), this.input.addInput( castValue( _cell.getConstantValue() ) ) );
					this.binder.defineInputCell( _cell, getter );
					break;
				}
				case OUTPUT: {
					final CallFrame setter = FormulaCompiler.newCallFrame( Outputs.class.getMethod( "get" + typeName,
							Integer.TYPE ), this.addOutput( _cell.describe() ) );
					this.binder.defineOutputCell( _cell, setter );
					break;
				}
			}
		}

		private Object castValue( Object _value )
		{
			if (_value instanceof Number) {
				return AbstractTester.this.builder.getNumericType().valueOf( (Number) _value );
			}
			return _value;
		}

		private int addOutput( String _name )
		{
			this.outputNames.add( _name );
			return this.outputNames.size() - 1;
		}

		public void defineSection( Range _range ) throws Exception
		{
			final int subIndex = this.input.addSub();
			final CallFrame getter = FormulaCompiler.newCallFrame( Inputs.class.getMethod( "getSubs", Integer.TYPE ),
					this.nextSectionIndex++ );
			final Section sub = this.binder.defineRepeatingSection( _range, Orientation.VERTICAL, getter, Inputs.class,
					null, null );
			new SectionDef( this, _range, sub, this.input.subs.get( subIndex )[ 0 ] );
		}

		public void logOutputs( Outputs _computation )
		{
			for (int i = 0; i < this.outputNames.size(); i++) {
				final String name = this.outputNames.get( i );
				final BigDecimal bd = _computation.getBigDecimal( i );
				final String s = _computation.getString( i );
				final Object actual = (bd == null) ? s : bd;
				System.out.println( "Output " + name + " is: " + actual.toString() );
			}
		}

	}


	public static final class Inputs
	{
		private final List<Object> values = New.list();
		private final List<Inputs[]> subs = New.list();

		public int addSub()
		{
			Inputs[] arr = new Inputs[ 1 ];
			arr[ 0 ] = new Inputs();
			this.subs.add( arr );
			return this.subs.size() - 1;
		}

		public Inputs[] getSubs( int _sectionIndex )
		{
			return this.subs.get( _sectionIndex );
		}

		public int addInput( Object _value )
		{
			this.values.add( _value );
			return this.values.size() - 1;
		}

		public String getString( int _valueIndex )
		{
			return (String) this.values.get( _valueIndex );
		}

		public BigDecimal getBigDecimal( int _valueIndex )
		{
			return (BigDecimal) this.values.get( _valueIndex );
		}

	}

	public static abstract class Outputs implements Resettable
	{

		public String getString( int _valueIndex )
		{
			return null;
		}

		public BigDecimal getBigDecimal( int _valueIndex )
		{
			return null;
		}

	}


	private static enum DefinitionType {
		INPUT, OUTPUT;
	}

}
