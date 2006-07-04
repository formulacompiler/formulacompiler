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
package sej.internal.bytecode.compiler;

import sej.CallFrame;
import sej.CompilerException;
import sej.internal.model.CellModel;


final class ByteCodeCellComputation
{
	private final ByteCodeSectionCompiler section;
	private final CellModel cell;
	private final String methodName;


	ByteCodeCellComputation(ByteCodeSectionCompiler _section, CellModel _cell)
	{
		super();
		this.section = _section;
		this.cell = _cell;
		this.methodName = _section.newGetterName();
		_section.addCellComputation( _cell, this );
	}


	CellModel getCell()
	{
		return this.cell;
	}

	ByteCodeSectionCompiler getSection()
	{
		return this.section;
	}

	String getMethodName()
	{
		return this.methodName;
	}

	
	void validate() throws CompilerException
	{
		validateInputType();
		validateOutputTypes();
	}

	private void validateInputType() throws CompilerException
	{
		if (this.cell.isInput()) {
			getSection().numericType().getNumericType().validateReturnTypeForCell( this.cell.getCallChainToCall().getMethod() );
		}
	}

	private void validateOutputTypes() throws CompilerException
	{
		for (CallFrame frame : this.cell.getCallsToImplement()) {
			if (frame.getHead() != frame) {
				throw new CompilerException.UnsupportedDataType( "The output method " + frame + " cannot be chained." );
			}
			getSection().numericType().getNumericType().validateReturnTypeForCell( frame.getMethod() );
		}
	}

	void compile() throws CompilerException
	{
		new ByteCodeCellCompiler( this ).compile();
	}

}
