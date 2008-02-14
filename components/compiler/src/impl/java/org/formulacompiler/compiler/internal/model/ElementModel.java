/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * This file is part of the Abacus Formula Compiler (AFC).
 *
 * For commercial licensing, please contact sales(at)formulacompiler.com.
 *
 * AFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AFC.  If not, see <http://www.gnu.org/licenses/>.
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
