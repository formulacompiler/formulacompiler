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
package sej.internal.build.bytecode;

import sej.describable.DescriptionBuilder;

final class DispatchBuilder extends DescriptionBuilder
{
	String pending;

	@Override
	public String toString()
	{
		closePending();
		return super.toString();
	}

	void closePending()
	{
		if (null != this.pending) {
			appendLine( this.pending );
			this.pending = null;
		}
	}

	private String lastDispatch = "";

	protected boolean genDispatchCase( String _enumName )
	{
		if (!this.lastDispatch.equals( _enumName )) {
			closePending();
			append( "case " ).append( _enumName ).appendLine( ":" );
			this.lastDispatch = _enumName;
			return true;
		}
		return false;
	}

	protected void genDispatchIf( String _ifCond )
	{
		if (null != _ifCond) {
			append( "if (" ).append( _ifCond ).appendLine( "()) {" );
			indent();
		}
	}

	protected void genDispatchEndIf( String _ifCond )
	{
		if (null != _ifCond) {
			outdent();
			appendLine( "}" );
		}
	}

}