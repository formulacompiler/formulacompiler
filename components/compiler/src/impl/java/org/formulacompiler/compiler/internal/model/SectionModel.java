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

import java.util.List;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.Util;
import org.formulacompiler.compiler.internal.YamlBuilder;
import org.formulacompiler.runtime.New;


public class SectionModel extends ElementModel
{
	private final List<SectionModel> sections = New.list();
	private final List<CellModel> cells = New.list();
	private final Class inputClass;
	private final Class outputClass;


	public SectionModel( SectionModel _section, Object _source, String _name, Class _inputClass, Class _outputClass )
	{
		super( _section, _source, _name );
		this.inputClass = _inputClass;
		this.outputClass = _outputClass;
		_section.getSections().add( this );
	}

	/**
	 * For tests only.
	 */
	public SectionModel( SectionModel _section, Object _source, Class _inputClass, Class _outputClass )
	{
		super( _section, _source, null );
		Util.assertTesting();
		this.inputClass = _inputClass;
		this.outputClass = _outputClass;
		_section.getSections().add( this );
	}


	SectionModel( ComputationModel _engine, Object _source, Class _inputClass, Class _outputClass )
	{
		super( _engine, _source, null );
		this.inputClass = _inputClass;
		this.outputClass = _outputClass;
	}


	public List<SectionModel> getSections()
	{
		return this.sections;
	}


	public List<CellModel> getCells()
	{
		return this.cells;
	}


	public Class getInputClass()
	{
		return this.inputClass;
	}

	public Class getOutputClass()
	{
		return this.outputClass;
	}


	public boolean traverse( ComputationModelVisitor _visitor ) throws CompilerException
	{
		if (!_visitor.visit( this )) return false;
		for (CellModel cell : getCells()) {
			if (!_visitor.visit( cell )) return false;
		}
		for (SectionModel section : getSections()) {
			if (!section.traverse( _visitor )) return false;
		}
		if (!_visitor.visited( this )) return false;
		return true;
	}


	@Override
	public void yamlTo( YamlBuilder _to )
	{
		_to.nv( "source", getSource().toString() );
		if (getName() != null) _to.nv( "name", getName() );
		_to.ln( "cells" ).l( getCells() );
		_to.ln( "sections" ).l( getSections() );
	}

}
