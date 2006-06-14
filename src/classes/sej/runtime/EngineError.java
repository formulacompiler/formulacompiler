package sej.runtime;

public class EngineError extends SEJError
{

	public EngineError(String _message, Throwable _cause)
	{
		super( _message, _cause );
	}

	public EngineError(String _message)
	{
		super( _message );
	}

	public EngineError(Throwable _cause)
	{
		super( _cause );
	}


	public static final class UnsupportedSerializationFormat extends EngineError
	{

		public UnsupportedSerializationFormat()
		{
			super( "Don't know how to load an engine -- you probably forgot to register a loader" );
		}

	}

}
