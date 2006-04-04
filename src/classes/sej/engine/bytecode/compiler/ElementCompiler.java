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

import sej.ModelError;
import sej.engine.compiler.model.CellModel;
import sej.engine.compiler.model.SectionModel;

final class ElementCompiler extends AbstractElementVisitor
{


	ElementCompiler(ByteCodeCompiler _compiler)
	{
		super( _compiler );
	}
	
	
	@Override
	public boolean visit( SectionModel _section ) throws ModelError
	{
		final boolean result = super.visit( _section );
		getSection().beginCompilation();
		return result;
	}
	
	
	@Override
	public boolean visited( SectionModel _section )
	{
		getSection().endCompilation();
		return super.visited( _section );
	}


	@Override
	protected ByteCodeSectionCompiler accessSubSection( SectionModel _section )
	{
		final ByteCodeSectionCompiler subCompiler = getSection().getSubSectionCompiler( _section );
		getSection().compileAccessTo( subCompiler );
		return subCompiler;
	}


	@Override
	protected void visitTargetCell( CellModel _cell ) throws ModelError
	{
		getSection().getCellComputation( _cell ).compile();
	}


}
