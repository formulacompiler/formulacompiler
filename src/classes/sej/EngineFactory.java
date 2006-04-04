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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;


/**
 * Factory for deserializing instances of the {@link Engine} class. Use an engine's
 * {@code register()} method to register it for use by {@code loadFrom()}.
 * 
 * @see sej.engine.standard.StandardEngineFactory
 * 
 * @author peo
 */
public abstract class EngineFactory
{
	private static Collection<EngineFactory> factories = new ArrayList<EngineFactory>();


	/**
	 * Registers an engine factory to be used by {@code loadFrom()}. It is ususally easier to call
	 * the engine's {@code register()} method instead.
	 * 
	 * @param _factory is an instance of an engine factory.
	 */
	public static void registerFactory( EngineFactory _factory )
	{
		factories.add( _factory );
	}


	/**
	 * Returns a new engine deserialized by a registered engine factory (see {@code register()}) -
	 * it must have been saved using {@code saveTo()}.
	 * 
	 * @return The new engine.
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static Engine loadFrom( InputStream _input ) throws IOException, ClassNotFoundException,
			InstantiationException, IllegalAccessException
	{
		int identifier;
		final DataInputStream dataStream = new DataInputStream( _input );
		try {
			identifier = dataStream.readInt();
			for (EngineFactory factory : factories) {
				if (factory.getSerializationIdentifier() == identifier) {
					return factory.newEngineFrom( dataStream );
				}
			}
			throw new NullPointerException( "EngineFactory failed to find a suitable registered factory" );
		}
		finally {
			dataStream.close();
		}
	}


	public abstract int getSerializationIdentifier();


	public abstract Engine newEngineFrom( DataInputStream _inBuf ) throws IOException, ClassNotFoundException,
			InstantiationException, IllegalAccessException;


}
