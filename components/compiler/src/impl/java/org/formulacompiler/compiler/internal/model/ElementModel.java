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

import java.util.Collection;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.internal.AbstractYamlizable;
import org.formulacompiler.runtime.New;


public abstract class ElementModel extends AbstractYamlizable
{
	private final ComputationModel engine;
	private final SectionModel section;
	private String name;
	private String originalName;
	private CallFrame callChainToCall;
	private final Collection<CallFrame> callsToImplement = New.collection();


	public ElementModel( SectionModel _section, String _name )
	{
		super();
		this.engine = _section.getEngine();
		this.section = _section;
		this.name = _name;
		this.originalName = _name;
	}


	ElementModel( ComputationModel _engine, String _name )
	{
		super();
		this.engine = _engine;
		this.section = null;
		this.name = _name;
		this.originalName = _name;
	}


	public ComputationModel getEngine()
	{
		return this.engine;
	}


	public SectionModel getSection()
	{
		return this.section;
	}

	public String getName()
	{
		return this.name;
	}

	protected void setName( String _name )
	{
		this.name = _name;
	}


	public final String getOriginalName()
	{
		return this.originalName;
	}

	public final void setOriginalName( String _originalName )
	{
		this.originalName = _originalName;
	}


	public boolean isInput()
	{
		return (null != this.callChainToCall);
	}

	public CallFrame getCallChainToCall()
	{
		return this.callChainToCall;
	}

	public void makeInput( CallFrame _callChainToCall )
	{
		this.callChainToCall = _callChainToCall;
		this.name = _callChainToCall.getHead().getMethod().getDeclaringClass().getSimpleName()
				+ "." + _callChainToCall.toString();
	}


	public boolean isOutput()
	{
		return (0 < this.callsToImplement.size());
	}

	public CallFrame[] getCallsToImplement()
	{
		return this.callsToImplement.toArray( new CallFrame[ this.callsToImplement.size() ] );
	}

	public void makeOutput( CallFrame _callToImplement )
	{
		this.callsToImplement.add( _callToImplement );
	}


	@Override
	public String toString()
	{
		return getName();
	}

}
