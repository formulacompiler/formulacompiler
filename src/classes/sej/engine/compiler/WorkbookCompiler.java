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
package sej.engine.compiler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import sej.CallFrame;
import sej.Compiler;
import sej.ModelError;
import sej.Orientation;
import sej.Spreadsheet;
import sej.engine.compiler.definition.EngineDefinition;
import sej.model.Workbook;


public abstract class WorkbookCompiler implements Compiler
{
	protected final EngineDefinition definition;
	protected final Class inputs;
	protected final Class outputs;


	public WorkbookCompiler(Spreadsheet _model, Class _inputs, Class _outputs)
	{
		super();
		this.definition = new EngineDefinition( (Workbook) _model );
		this.inputs = _inputs;
		this.outputs = _outputs;
	}


	public final EngineDefinition getDefinition()
	{
		return this.definition;
	}


	public Spreadsheet getModel()
	{
		return getDefinition().getWorkbook();
	}


	public Compiler.Section getRoot()
	{
		return this.definition.getRoot();
	}


	public void defineInputCell( Spreadsheet.Cell _cell, CallFrame _callChain ) throws ModelError
	{
		this.definition.defineInputCell( _cell, _callChain );
	}


	public void defineOutputCell( Spreadsheet.Cell _cell, CallFrame _call ) throws ModelError
	{
		this.definition.defineOutputCell( _cell, _call );
	}
	
	
	public Compiler.Section defineRepeatingSection( Spreadsheet.Range _range, Orientation _orientation,
			CallFrame _inputCallChainReturningIterable, CallFrame _outputCallToImplementIterable ) throws ModelError
	{
		return this.definition.defineRepeatingSection( _range, _orientation, _inputCallChainReturningIterable, _outputCallToImplementIterable );
	}


	/**
	 * Serializes the a compiled engine to a stream for later loading by {@code loadFrom()}.
	 * 
	 * @param _output is any output stream. You can save engine serializations anywhere, in files,
	 *           databases, wherever.
	 * @throws IOException
	 * @throws ModelError
	 */
	public void saveTo( OutputStream _output ) throws IOException, ModelError
	{
		final DataOutputStream dataStream = new DataOutputStream( _output );
		try {
			dataStream.writeInt( getSerializationIdentifier() );
			compileEngineTo( dataStream );
		}
		finally {
			dataStream.close();
		}
	}


}
