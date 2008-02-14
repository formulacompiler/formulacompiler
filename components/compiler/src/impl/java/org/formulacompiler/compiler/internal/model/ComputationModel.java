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

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.AbstractYamlizable;
import org.formulacompiler.compiler.internal.Util;
import org.formulacompiler.compiler.internal.YamlBuilder;
import org.formulacompiler.runtime.internal.Environment;


public class ComputationModel extends AbstractYamlizable
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
	public void yamlTo( YamlBuilder _to )
	{
		this.root.yamlTo( _to );
	}


}
