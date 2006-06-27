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
package sej.runtime;

import java.io.IOException;
import java.io.InputStream;

import sej.internal.bytecode.runtime.ByteCodeEngineLoader;


/**
 * Static class defining factory methods for run-time-only elements of SEJ. This class is extended
 * by {@link sej.SEJ} which provides factory methods for compile-time elements.
 * 
 * @author peo
 */
public class SEJRuntime
{

	/**
	 * Not supposed to be instantiated!
	 */
	protected SEJRuntime()
	{
		super();
	}

	/**
	 * Returns a new engine deserialized by a registered engine loader (see {@code register()}) - it
	 * must have been saved using {@link sej.SaveableEngine#saveTo(java.io.OutputStream)}.
	 * 
	 * @param _stream is an input stream which must support the {@link InputStream#mark(int)}
	 *           operation.
	 * @return The loaded engine.
	 * 
	 * @throws IOException
	 * @throws EngineException
	 */
	public static Engine loadEngine( InputStream _stream ) throws IOException, EngineException
	{
		if (!_stream.markSupported()) throw new IllegalArgumentException( "mark() is not supported by input stream" );
		return new ByteCodeEngineLoader().loadEngineData( _stream );
	}

}
