/*
 * Copyright (c) 2006 by Abacus Research AG, Switzerland.
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
package org.formulacompiler.runtime;


/**
 * Base class for all exceptions thrown by AFC. Augments the basic {@link Exception} class with
 * support for message context information. The idea is that handlers further up the chain can add
 * context information to the existing exception, then rethrow it. This makes the exception mutable,
 * but avoids having to wrap it repeatedly for no other reason that to provide a bit of source
 * location context.
 * 
 * @author peo
 */
public class FormulaCompilerException extends Exception
{
	private String messageContext;

	public FormulaCompilerException()
	{
		super();
	}

	public FormulaCompilerException(String _message, Throwable _cause)
	{
		super( _message, _cause );
	}

	public FormulaCompilerException(String _message)
	{
		super( _message );
	}

	public FormulaCompilerException(Throwable _cause)
	{
		super( _cause );
	}

	/**
	 * Override that appends the current message context to the message.
	 * 
	 * @return the message plus the context.
	 * 
	 * @see #getPureMessage()
	 * @see #getMessageContext()
	 */
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

	/**
	 * Returns the pure, unchanged message.
	 * 
	 * @see #getMessage()
	 */
	public String getPureMessage()
	{
		return super.getMessage();
	}

	/**
	 * Returns the current message context. It is automatically appended to the normal
	 * {@link #getMessage()}.
	 */
	public String getMessageContext()
	{
		return this.messageContext;
	}

	/**
	 * Sets the current message context.
	 * 
	 * @param _messageContext is the new message context.
	 * 
	 * @see #addMessageContext(String)
	 */
	public void setMessageContext( String _messageContext )
	{
		this.messageContext = _messageContext;
	}

	/**
	 * Appends to the current message context.
	 * 
	 * @param _messageContext is the context to append.
	 */
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
