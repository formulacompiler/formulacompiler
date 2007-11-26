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
 * Runtime exception thrown by compiled computations for the {@code NA()} spreadsheet function,
 * which is normally displayed as {@code #N/A}. You can also throw this exception in input getters
 * yourself to signal an unavailable value to the computation. The spreadsheet functions
 * {@code ISNA()} and {@code ISERROR()} both trap this error.
 * 
 * @author peo
 * 
 * @see FormulaException
 */
public class NotAvailableException extends ComputationException
{

	public NotAvailableException()
	{
		super( "#N/A" );
	}

	public NotAvailableException( String _message, Throwable _cause )
	{
		super( _message, _cause );
	}

	public NotAvailableException( String _message )
	{
		super( _message );
	}

	public NotAvailableException( Throwable _cause )
	{
		super( _cause );
	}

}
