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


/**
 * Base class for all exceptions thrown by the run-time engine support of SEJ.
 * 
 * @author peo
 */
public class EngineException extends SEJException
{

	public EngineException(String _message, Throwable _cause)
	{
		super( _message, _cause );
	}

	public EngineException(String _message)
	{
		super( _message );
	}

	public EngineException(Throwable _cause)
	{
		super( _cause );
	}


	/**
	 * SEJ could not find an appropriate engine loader for the stream you provided to
	 * {@link sej.runtime.SEJRuntime#loadEngine(java.io.InputStream)}.
	 * 
	 * @author peo
	 */
	public static final class UnsupportedSerializationFormat extends EngineException
	{

		public UnsupportedSerializationFormat()
		{
			super( "Don't know how to load an engine -- you probably forgot to register a loader" );
		}

	}

}
