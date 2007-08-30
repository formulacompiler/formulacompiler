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
package org.formulacompiler.compiler.internal.model;

import java.io.IOException;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.Util;
import org.formulacompiler.describable.AbstractDescribable;
import org.formulacompiler.describable.DescriptionBuilder;
import org.formulacompiler.runtime.internal.Environment;


public class ComputationModel extends AbstractDescribable
{
	public static final String ROOTNAME = "_ROOT_";

	private final SectionModel root;
	private final Environment env;


	public ComputationModel( Class _inputClass, Class _outputClass, Environment _env )
	{
		if (null == _env) throw new IllegalArgumentException();
		this.root = new SectionModel( this, ROOTNAME, _inputClass, _outputClass );
		this.env = _env;
	}

	/**
	 * For tests only! 
	 */
	public ComputationModel( Class _inputClass, Class _outputClass )
	{
		this( _inputClass, _outputClass, Environment.DEFAULT );
		Util.assertTesting();
	}


	public SectionModel getRoot()
	{
		return this.root;
	}


	public Class getInputClass()
	{
		return getRoot().getInputClass();
	}


	public Class getOutputClass()
	{
		return getRoot().getOutputClass();
	}


	public Environment getEnvironment()
	{
		return this.env;
	}


	public boolean traverse( ComputationModelVisitor _visitor ) throws CompilerException
	{
		return _visitor.visit( this ) && this.root.traverse( _visitor ) && _visitor.visited( this );
	}


	@Override
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		this.root.describeTo( _to );
	}


}
