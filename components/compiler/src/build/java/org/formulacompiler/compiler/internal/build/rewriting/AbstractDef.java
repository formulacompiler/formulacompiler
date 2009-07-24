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

package org.formulacompiler.compiler.internal.build.rewriting;

import java.util.List;

import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.runtime.New;

abstract class AbstractDef
{
	final List<Param> params = New.list();
	ExpressionNode body = null;

	public void addParam( String _name, Param.Type _type )
	{
		this.params.add( new Param( _name, _type ) );
	}

	public void setBody( ExpressionNode _expr )
	{
		this.body = _expr;
	}

	static final class Param
	{
		final String name;
		final Type type;

		static enum Type {
			VALUE, LIST, ARRAY, SYMBOLIC
		};

		public Param( String _name, Type _type )
		{
			this.name = _name;
			this.type = _type;
		}

	}

}
