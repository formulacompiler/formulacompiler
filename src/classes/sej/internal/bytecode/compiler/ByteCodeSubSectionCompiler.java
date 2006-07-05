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

import sej.CompilerException;
import sej.internal.expressions.ExpressionNode;
import sej.internal.model.SectionModel;

final class ByteCodeSubSectionCompiler extends ByteCodeSectionCompiler
{
	private final ByteCodeSectionCompiler parentSectionCompiler;
	private final String arrayDescriptor;
	private final Type arrayType;
	private final String getterName;
	private final String getterDescriptor;


	ByteCodeSubSectionCompiler(ByteCodeSectionCompiler _parent, SectionModel _model)
	{
		super( _parent.engineCompiler(), _model, _parent.engineCompiler().newSubClassName() );
		this.parentSectionCompiler = _parent;
		this.arrayDescriptor = "[" + classDescriptor();
		this.arrayType = Type.getType( arrayDescriptor() );
		this.getterName = "get" + className();
		this.getterDescriptor = "()" + arrayDescriptor();
		_parent.addSubSectionCompiler( _model, this );
	}


	@Override
	ByteCodeSectionCompiler parentSectionCompiler()
	{
		return this.parentSectionCompiler;
	}

	String arrayDescriptor()
	{
		return this.arrayDescriptor;
	}

	Type arrayType()
	{
		return this.arrayType;
	}

	String getterName()
	{
		return this.getterName;
	}

	String getterDescriptor()
	{
		return this.getterDescriptor;
	}


	public ByteCodeSectionNumericMethodCompiler compileExpr( ExpressionNode _node ) throws CompilerException
	{
		ByteCodeHelperCompilerForSubExpr result = new ByteCodeHelperCompilerForSubExpr( this, _node );
		result.compile();
		return result;
	}

}
