/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * This file is part of the Abacus Formula Compiler (AFC).
 *
 * For commercial licensing, please contact sales(at)formulacompiler.com.
 *
 * AFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AFC.  If not, see <http://www.gnu.org/licenses/>.
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

	public FormulaCompilerException( String _message, Throwable _cause )
	{
		super( _message, _cause );
	}

	public FormulaCompilerException( String _message )
	{
		super( _message );
	}

	public FormulaCompilerException( Throwable _cause )
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
