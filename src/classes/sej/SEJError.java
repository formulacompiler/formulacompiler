package sej;

public class SEJError extends Exception
{

	public SEJError()
	{
		super();
	}

	public SEJError(String _message, Throwable _cause)
	{
		super( _message, _cause );
	}

	public SEJError(String _message)
	{
		super( _message );
	}

	public SEJError(Throwable _cause)
	{
		super( _cause );
	}
	
}
