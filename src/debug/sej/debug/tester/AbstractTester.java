package sej.debug.tester;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

import sej.CallFrame;
import sej.EngineBuilder;
import sej.NumericType;
import sej.Orientation;
import sej.SEJ;
import sej.SaveableEngine;
import sej.Spreadsheet.Cell;
import sej.Spreadsheet.CellNameDefinition;
import sej.Spreadsheet.NameDefinition;
import sej.Spreadsheet.Range;
import sej.Spreadsheet.RangeNameDefinition;
import sej.SpreadsheetBinder.Section;
import sej.internal.Debug;
import sej.runtime.Resettable;
import sej.util.New;

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
		this.builder = SEJ.newEngineBuilder();
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
		this.typeName = _type.getValueType().getSimpleName();
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
		final NameDefinition[] names = this.builder.getSpreadsheet().getDefinedNames();
		for (NameDefinition name : names) {
			if (pattern.matcher( name.getName() ).matches()) {
				if (name instanceof CellNameDefinition) {
					CellNameDefinition cellName = (CellNameDefinition) name;
					defineCell( cellName.getCell(), _type );
				}
				else if (name instanceof RangeNameDefinition) {
					RangeNameDefinition rangeName = (RangeNameDefinition) name;
					defineCell( rangeName.getRange().getTopLeft(), _type );
				}
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
		private final List<SectionDef> subs = New.newList();
		private final Inputs input;
		private final List<String> outputNames = New.newList();
		private int nextSectionIndex = 0;

		public SectionDef(SectionDef _parent, Range _range, Section _binder, Inputs _input) throws Exception
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
					final CallFrame getter = new CallFrame( Inputs.class.getMethod( "get" + typeName, Integer.TYPE ),
							this.input.addInput( castValue( _cell.getConstantValue() ) ) );
					this.binder.defineInputCell( _cell, getter );
					break;
				}
				case OUTPUT: {
					final CallFrame setter = new CallFrame( Outputs.class.getMethod( "get" + typeName, Integer.TYPE ), this
							.addOutput( _cell.describe() ) );
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
			final CallFrame getter = new CallFrame( Inputs.class.getMethod( "getSubs", Integer.TYPE ),
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
		private final List<Object> values = New.newList();
		private final List<Inputs[]> subs = New.newList();

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
