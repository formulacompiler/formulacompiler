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

import org.formulacompiler.compiler.Function;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
final class RuleDef extends AbstractDef implements Comparable<RuleDef>
{
	final Function fun;

	public RuleDef( Function _function )
	{
		this.fun = _function;
	}

	public int compareTo( RuleDef _o )
	{
		if (this == _o) return 0;
		int r = this.fun.compareTo( _o.fun );
		if (r == 0) r = this.params.size() < _o.params.size() ? -1 : +1;
		return r;
	}

}
