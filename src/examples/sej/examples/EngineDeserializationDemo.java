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
package sej.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import sej.Engine;
import sej.EngineFactory;
import sej.engine.standard.StandardEngineFactory;

/**
 * Shows how an engine serialized by a 1.5 configuration application can be loaded into and used by
 * a 1.4 server.
 * 
 * @author peo
 */
public class EngineDeserializationDemo
{

	static {
		StandardEngineFactory.register();
	}


	public static void main( String[] args ) throws IOException, ClassNotFoundException, InstantiationException,
			IllegalAccessException
	{
		// Instantiate an engine from the serialized form.
		File engineSerializationFile = new File( "/temp/Engine.ser" );
		InputStream inStream = new FileInputStream( engineSerializationFile );
		Engine engine = EngineFactory.loadFrom( inStream );

		// Compute an actual output value for a given set of actual input values.
		Inputs inputs = new Inputs( 4, 40 );
		Outputs outputs = (Outputs) engine.newComputation( inputs );
		double result = outputs.getResult();

		System.out.printf( "Result is: %f", new Object[] { result } );
	}

}
