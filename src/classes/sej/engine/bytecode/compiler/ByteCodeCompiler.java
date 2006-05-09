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
package sej.engine.bytecode.compiler;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

import org.objectweb.asm.Type;

import sej.CompilerFactory;
import sej.Engine;
import sej.ModelError;
import sej.NumericType;
import sej.Settings;
import sej.Spreadsheet;
import sej.engine.bytecode.ByteCodeEngineFactory;
import sej.engine.bytecode.ByteCodeEngineLoader;
import sej.engine.compiler.WorkbookCompiler;
import sej.engine.compiler.model.EngineModel;


public class ByteCodeCompiler extends WorkbookCompiler
{
	static final Type ENGINE_INTF = Type.getType( Engine.class );
	static final String INPUTS_MEMBER_NAME = "inputs";
	static final Type MATH = Type.getType( Math.class );
	static final Type BIGDECIMAL = Type.getType( BigDecimal.class );

	private ByteCodeSectionCompiler root;


	public static void registerAsDefault()
	{
		ByteCodeEngineFactory.register();
		CompilerFactory.setDefaultFactory( new ByteCodeCompilerFactory() );
	}


	public ByteCodeCompiler(Spreadsheet _model, Class _inputs, Class _outputs, NumericType _numericType)
	{
		super( _model, _inputs, _outputs, _numericType );
	}


	public Engine compileNewEngine() throws ModelError
	{
		return newEngineFromClassBytes( compileNewEngineClassBytes() );
	}


	Class compileNewEngineClass() throws ModelError
	{
		return newEngineClassFromClassBytes( compileNewEngineClassBytes() );
	}


	byte[] compileNewEngineClassBytes() throws ModelError
	{
		return compileNewEngineClassBytes( compileEngineModel() );
	}


	Engine compileNewEngine( final EngineModel _model ) throws ModelError
	{
		return newEngineFromClassBytes( compileNewEngineClassBytes( _model ) );
	}


	byte[] compileNewEngineClassBytes( final EngineModel _model ) throws ModelError
	{
		this.root = new ByteCodeSectionCompiler( this, _model.getRoot() );

		_model.traverse( new ElementCreator( this ) );
		_model.traverse( new ElementCompiler( this ) );

		final byte[] classBytes = this.root.getClassBytes();

		if (Settings.isDebugCompilationEnabled()) dumpClassBytes( classBytes );

		return classBytes;
	}


	private void dumpClassBytes( byte[] _classBytes )
	{
		try {
			FileOutputStream stream = new FileOutputStream( "D:/Temp/GeneratedEngine.class" );
			try {
				stream.write( _classBytes );
			}
			finally {
				stream.close();
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}


	Class getInputs()
	{
		return this.inputs;
	}


	Class getOutputs()
	{
		return this.outputs;
	}


	public void compileEngineTo( DataOutputStream _stream ) throws IOException, ModelError
	{
		final byte[] classBytes = compileNewEngineClassBytes();
		_stream.writeInt( classBytes.length );
		_stream.write( classBytes );
	}


	public int getSerializationIdentifier()
	{
		return ByteCodeEngineFactory.serializationIdentifier;
	}


	Engine newEngineFromClassBytes( byte[] _classBytes )
	{
		try {
			return ByteCodeEngineLoader.newEngineFromClassBytes( _classBytes );
		}
		catch (InstantiationException e) {
			e.printStackTrace();
			assert false : "Could not instantiate the ByteCode class";
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
			assert false : "Could not load the ByteCode class";
		}
		return null;
	}


	Class newEngineClassFromClassBytes( byte[] _classBytes )
	{
		return ByteCodeEngineLoader.newEngineClassFromClassBytes( _classBytes );
	}


	ByteCodeSectionCompiler getRootSectionCompiler()
	{
		return this.root;
	}

}
