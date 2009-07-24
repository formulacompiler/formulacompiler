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

package org.formulacompiler.compiler.internal.build.bytecode;

import org.formulacompiler.compiler.internal.DescriptionBuilder;

final class DispatchBuilder extends DescriptionBuilder
{
	String pending;

	@Override
	public String toString()
	{
		closePending();
		return super.toString();
	}

	void closePending()
	{
		if (null != this.pending) {
			appendLine( this.pending );
			this.pending = null;
		}
	}

	private String lastDispatch = "";

	protected boolean genDispatchCase( String _enumName )
	{
		if (!this.lastDispatch.equals( _enumName )) {
			closePending();
			append( "case " ).append( _enumName ).appendLine( ":" );
			this.lastDispatch = _enumName;
			return true;
		}
		return false;
	}

	protected void genDispatchIf( String _ifCond )
	{
		if (null != _ifCond) {
			append( "if (" ).append( _ifCond ).appendLine( "()) {" );
			indent();
		}
	}

	protected void genDispatchEndIf( String _ifCond )
	{
		if (null != _ifCond) {
			outdent();
			appendLine( "}" );
		}
	}

}