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

package org.formulacompiler.compiler.internal.bytecode;

import org.formulacompiler.compiler.internal.bytecode.MethodCompiler.LocalRef;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;

final class DelayedLet
{
	final String name;
	final ExpressionNode node;
	final LocalRef local;
	final int nestingLevel;

	public DelayedLet( String _name, LocalRef _local, ExpressionNode _node, int _nesting )
	{
		super();
		this.name = _name;
		this.local = _local;
		this.node = _node;
		this.nestingLevel = _nesting;
	}

	public boolean isArray()
	{
		return this.local.isArray();
	}

	@Override
	public String toString()
	{
		return this.node.toString() + " @ " + String.valueOf( this.local );
	}

}