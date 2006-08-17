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
 * Base class for all exceptions thrown by SEJ.
 * 
 * @author peo
 */
public class SEJException extends Exception
{
	private String messageContext;

	public SEJException()
	{
		super();
	}

	public SEJException(String _message, Throwable _cause)
	{
		super( _message, _cause );
	}

	public SEJException(String _message)
	{
		super( _message );
	}

	public SEJException(Throwable _cause)
	{
		super( _cause );
	}

	@Override
	public String getMessage()
	{
		final String cx = getMessageContext();
		if (cx == null) {
			return super.getMessage();
		}
		else {
			return super.getMessage() + cx;
		}
	}
	
	public String getMessageContext()
	{
		return this.messageContext;
	}
	
	public void setMessageContext( String _messageContext )
	{
		this.messageContext = _messageContext;
	}

	public void addMessageContext( String _messageContext )
	{
		if (null == this.messageContext) {
			this.messageContext = _messageContext;
		}
		else {
			this.messageContext += _messageContext;
		}
	}

}
