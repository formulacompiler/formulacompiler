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
package org.formulacompiler.compiler.internal.logging;

import java.io.PrintStream;

public final class Logger
{
	private final String indent;
	private final int indentLength;
	private StringBuilder indentation = new StringBuilder();
	private String currentIndentation = "";
	private boolean indentPending = true;


	public Logger()
	{
		this( "\t" );
	}

	public Logger( final String _indent )
	{
		super();
		this.indent = _indent;
		this.indentLength = _indent.length();
	}


	void indent()
	{
		this.indentation.append( this.indent );
		this.currentIndentation = this.indentation.toString();
	}

	void outdent()
	{
		final int l = this.indentation.length();
		if (this.indentLength <= l) {
			this.indentation.setLength( l - this.indentLength );
			this.currentIndentation = this.indentation.toString();
		}
	}

	private void addIndentationIfPending()
	{
		if (this.indentPending) addIndentation();
	}

	private void addIndentation()
	{
		System.out.print( this.currentIndentation );
		this.indentPending = false;
	}


	Logger newLine()
	{
		System.out.print( '\n' );
		this.indentPending = true;
		return this;
	}

	PrintStream stream()
	{
		addIndentationIfPending();
		return System.out;
	}


}
