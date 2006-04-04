package sej;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;

import sej.Spreadsheet.Cell;


public class EngineBuilder
{
	private final SectionContext root;
	private Spreadsheet spreadsheet;
	private Compiler compiler;
	SectionContext current;


	public EngineBuilder(Class _inputs, Class _outputs)
	{
		super();
		this.root = new SectionContext( _inputs, _outputs );
		this.current = this.root;
	}


	public Spreadsheet getSpreadsheet()
	{
		return this.spreadsheet;
	}


	public void setSpreadsheet( Spreadsheet _spreadsheet )
	{
		if (null != this.spreadsheet) throw new IllegalStateException( "Spreadsheet already set" );
		this.spreadsheet = _spreadsheet;
	}


	public void loadSpreadsheet( String _filePath ) throws IOException
	{
		setSpreadsheet( SpreadsheetLoader.loadFromFile( _filePath ) );
	}


	public Compiler getCompiler()
	{
		if (null == this.compiler) {
			setCompiler( CompilerFactory.newDefaultCompiler( getSpreadsheet(), this.root.inputs, this.root.outputs ) );
		}
		return this.compiler;
	}


	public void setCompiler( Compiler _compiler )
	{
		if (null != this.compiler) throw new IllegalStateException( "Compiler already set" );
		this.compiler = _compiler;
		this.root.compiler = _compiler.getRoot();
	}


	public Engine buildEngine() throws ModelError
	{
		return getCompiler().compileNewEngine();
	}


	public void saveEngine( OutputStream _stream ) throws IOException, ModelError
	{
		getCompiler().saveTo( _stream );
	}


	public void bindCellsByName() throws ModelError
	{
		bindInputCellsByName();
		bindOutputCellsByName();
	}


	public void bindInputCellsByName() throws ModelError
	{
		bindCellsByName( this.current.inputs, new Binder()
		{

			public void bind( Cell _cell, CallFrame _frame ) throws ModelError
			{
				EngineBuilder.this.current.compiler.defineInputCell( _cell, _frame );
			}

		} );
	}


	public void bindOutputCellsByName() throws ModelError
	{
		bindCellsByName( this.current.outputs, new Binder()
		{

			public void bind( Cell _cell, CallFrame _frame ) throws ModelError
			{
				EngineBuilder.this.current.compiler.defineOutputCell( _cell, _frame );
			}

		} );
	}


	private void bindCellsByName( Class _class, Binder _binder ) throws ModelError
	{
		getCompiler();
		final Method[] methods = _class.getMethods();
		for (Spreadsheet.NameDefinition nameDef : this.spreadsheet.getDefinedNames()) {
			if (nameDef instanceof Spreadsheet.CellNameDefinition) {
				final Spreadsheet.CellNameDefinition cellDef = (Spreadsheet.CellNameDefinition) nameDef;
				final Spreadsheet.Cell cell = cellDef.getCell();
				final String cellName = cellDef.getName();
				final String methodName = "get" + cellName;
				final Method method = findMethod( methods, methodName );
				if (null != method) {
					_binder.bind( cell, new CallFrame( method ) );
				}
			}
		}
	}


	private Method findMethod( Method[] _methods, String _methodName )
	{
		for (Method method : _methods) {
			if (method.getName().equalsIgnoreCase( _methodName )) {
				if (0 == method.getParameterTypes().length) {
					return method;
				}
			}
		}
		return null;
	}


	private static interface Binder
	{
		void bind( Cell _cell, CallFrame _frame ) throws ModelError;
	}


	public static final class SectionContext
	{
		final Class inputs;
		final Class outputs;
		Compiler.Section compiler;

		public SectionContext(Class _inputs, Class _outputs)
		{
			super();
			this.inputs = _inputs;
			this.outputs = _outputs;
		}
	}

}
