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

package org.formulacompiler.spreadsheet.internal;

import org.formulacompiler.compiler.internal.YamlBuilder;


public final class CellWithError extends CellInstance
{
	public static final String NA = "#N/A";
	public static final String NUM = "#NUM!";
	public static final String VALUE = "#VALUE!";
	public static final String REF = "#REF!";
	public static final String DIV0 = "#DIV/0!";

	public CellWithError( RowImpl _row, String _text )
	{
		super( _row );
		setValue( _text );
	}


	public String getError()
	{
		return (String) getValue();
	}


	@Override
	public void copyTo( final RowImpl _row )
	{
		new CellWithError( _row, getError() );
	}

	@Override
	public void yamlTo( YamlBuilder _to )
	{
		_to.vn( "err" ).v( getError() ).lf();
		super.yamlTo( _to );
	}


}
