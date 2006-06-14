package sej;

import java.io.IOException;
import java.io.InputStream;

import sej.internal.bytecode.runtime.ByteCodeEngineLoader;

public class SEJRuntime
{
	
	/* 
	 * Package visible so there will be no subclasses.
	 */
	SEJRuntime()
	{
		super();
	}

	/**
	 * Returns a new engine deserialized by a registered engine loader (see {@code register()}) - it
	 * must have been saved using {@link SaveableEngine#saveTo(java.io.OutputStream)}.
	 * 
	 * @param _stream is an input stream which must support the {@link InputStream#mark(int)}
	 *           operation.
	 * @return The loaded engine.
	 * 
	 * @throws IOException
	 * @throws EngineError
	 */
	public static Engine loadEngine( InputStream _stream ) throws IOException, EngineError
	{
		if (!_stream.markSupported()) throw new IllegalArgumentException( "mark() is not supported by input stream" );
		return new ByteCodeEngineLoader().loadEngineData( _stream );
	}

}
