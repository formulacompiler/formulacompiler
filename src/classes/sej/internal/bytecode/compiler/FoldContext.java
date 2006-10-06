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

import org.objectweb.asm.Type;

import sej.internal.expressions.ExpressionNodeForFold;

final class FoldContext
{
	final ExpressionNodeForFold node;
	final Type accumulatorType;
	final SectionCompiler section;
	final int localThis;
	
	
	public FoldContext(ExpressionNodeForFold _node, SectionCompiler _section)
	{
		super();
		this.node = _node;
		this.section = _section;
		this.localThis = 0; // this

		// FIXME StringBuffer etc.
		this.accumulatorType = _section.engineCompiler().typeCompiler( _node.getDataType() ).type();
	}
	
	
	public FoldContext(FoldContext _context, SectionCompiler _section, int _sectionObject)
	{
		super();
		this.node = _context.node;
		this.accumulatorType = _context.accumulatorType;
		this.section = _section;
		this.localThis = _sectionObject;
	}
	

}
