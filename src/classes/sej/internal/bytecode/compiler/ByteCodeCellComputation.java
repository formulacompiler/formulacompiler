/*
 * Copyright � 2006 by Abacus Research AG, Switzerland.
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

import java.math.BigDecimal;
import java.util.Date;

import sej.CallFrame;
import sej.CompilerError;
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
		this.methodName = _section.getNewGetterName();
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

	
	void validate() throws CompilerError
	{
		validateInputType();
		validateOutputTypes();
	}

	private void validateInputType() throws CompilerError
	{
		if (this.cell.isInput()) {
			validateReturnTypeOf( this.cell.getCallChainToCall(), "input" );
		}
	}

	private void validateOutputTypes() throws CompilerError
	{
		for (CallFrame frame : this.cell.getCallsToImplement()) {
			if (frame.getHead() != frame) {
				throw new CompilerError.UnsupportedDataType( "The output method " + frame + " cannot be chained." );
			}
			validateReturnTypeOf( frame, "output" );
		}
	}

	private void validateReturnTypeOf( CallFrame _frame, String _usage ) throws CompilerError
	{
		final Class returnType = _frame.getMethod().getReturnType();
		if (Double.TYPE == returnType) return;
		if (Long.TYPE == returnType) return;
		if (Boolean.TYPE == returnType) return;
		if (Date.class == returnType) return;
		if (BigDecimal.class == returnType) return;
		throw new CompilerError.UnsupportedDataType( "The "
				+ _usage + " method " + _frame + " has an unsupported return type " + returnType );
	}


	void compile() throws CompilerError
	{
		new ByteCodeCellCompiler( this ).compile();
	}

}