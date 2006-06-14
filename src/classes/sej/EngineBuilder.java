package sej;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;

import sej.runtime.EngineError;



public interface EngineBuilder
{

	public NumericType getNumericType();

	public void setNumericType( NumericType _type );

	public Spreadsheet getSpreadsheet();

	public void setSpreadsheet( Spreadsheet _sheet );

	public void loadSpreadsheet( File _file ) throws FileNotFoundException, IOException, SpreadsheetError;

	public void loadSpreadsheet( String _fileName ) throws FileNotFoundException, IOException, SpreadsheetError;

	public Class getInputClass();

	public void setInputClass( Class _inputClass );

	public Class getOutputClass();

	public void setOutputClass( Class _outputClass );

	public Class getFactoryClass();

	public void setFactoryClass( Class _class );

	public Method getFactoryMethod();

	public void setFactoryMethod( Method _factoryMethod );

	public boolean areAnyNamesDefined();

	public void createCellNamesFromRowTitles();

	public SpreadsheetBinder.Section getRootBinder() throws CompilerError;

	public SpreadsheetByNameBinder getByNameBinder() throws CompilerError;

	public void bindAllByName() throws CompilerError;

	public SaveableEngine compile() throws CompilerError, EngineError;

}