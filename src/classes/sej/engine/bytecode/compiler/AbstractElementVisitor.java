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
package sej.engine.bytecode.compiler;

import org.objectweb.asm.ClassWriter;

import sej.ModelError;
import sej.engine.compiler.model.AbstractEngineModelVisitor;
import sej.engine.compiler.model.CellModel;
import sej.engine.compiler.model.SectionModel;

abstract class AbstractElementVisitor extends AbstractEngineModelVisitor
{
	private final ByteCodeCompiler compiler;
	private ByteCodeSectionCompiler section;
	

	AbstractElementVisitor(ByteCodeCompiler _compiler)
	{
		this.compiler = _compiler;
	}


	ByteCodeCompiler getCompiler()
	{
		return this.compiler;
	}


	ByteCodeSectionCompiler getSection()
	{
		return this.section;
	}
	
	
	ClassWriter cw()
	{
		return getSection().cw();
	}
	

	@Override
	public boolean visit( SectionModel _section ) throws ModelError
	{
		if (null == this.section) {
			this.section = getCompiler().getRootSectionCompiler();
		}
		else {
			this.section = accessSubSection( _section );
		}
		assert null != this.section;
		return true;
	}


	@Override
	public boolean visited( SectionModel _section )
	{
		this.section = this.section.getParentSectionCompiler();
		return true;
	}


	protected abstract ByteCodeSectionCompiler accessSubSection( SectionModel _section );

	
	@Override
	public boolean visit( CellModel _cell ) throws ModelError
	{
		if (_cell.isInput() || _cell.isOutput() || 2 <= _cell.getReferenceCount()) {
			visitTargetCell( _cell );
		}
		return true;
	}


	protected abstract void visitTargetCell( CellModel _cell ) throws ModelError;


}
