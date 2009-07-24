/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
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
	private final Object source;
	private String name;
	private CallFrame callChainToCall;
	private final Collection<CallFrame> callsToImplement = New.collection();


	public ElementModel( SectionModel _section, Object _source, String _name )
	{
		super();
		this.engine = _section.getEngine();
		this.section = _section;
		this.source = _source;
		this.name = _name;
	}


	ElementModel( ComputationModel _engine, Object _source, String _name )
	{
		super();
		this.engine = _engine;
		this.section = null;
		this.source = _source;
		this.name = _name;
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

	public void setName( String _name )
	{
		this.name = _name;
	}

	public Object getSource()
	{
		return this.source;
	}

	public String getSourceName()
	{
		final Object src = getSource();
		return (null == src) ? "" : src.toString();
	}

	public String getShortName()
	{
		if (this.name != null) return this.name;
		else return getSourceName();
	}

	public String getFullName()
	{
		final String src = getSourceName();
		if (null == this.name) return src;
		final StringBuilder sb = new StringBuilder( src );
		sb.append( "(" );
		sb.append( this.name );
		sb.append( ")" );
		return sb.toString();
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
		if (isInput()) {
			return this.callChainToCall.getHead().getMethod().getDeclaringClass().getSimpleName()
					+ "." + this.callChainToCall.toString();
		}
		return getFullName();
	}

}
