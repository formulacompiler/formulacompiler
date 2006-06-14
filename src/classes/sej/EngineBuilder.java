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