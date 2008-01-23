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
package org.formulacompiler.spreadsheet.internal.binding;

import java.io.IOException;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.describable.Describable;
import org.formulacompiler.describable.DescriptionBuilder;
import org.formulacompiler.spreadsheet.internal.CellIndex;

public class OutputCellBinding extends CellBinding
{
	private final CallFrame callToImplement;

	public OutputCellBinding( SectionBinding _space, CallFrame _callToImplement, CellIndex _index )
			throws CompilerException
	{
		super( _space, _index );
		this.callToImplement = _callToImplement;
	}

	public CallFrame getCallToImplement()
	{
		return this.callToImplement;
	}

	@Override
	public CallFrame boundCall()
	{
		return this.callToImplement;
	}

	@Override
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		getIndex().describeTo( _to );
		_to.append( " implements " );
		((Describable) getCallToImplement()).describeTo( _to );
	}

}