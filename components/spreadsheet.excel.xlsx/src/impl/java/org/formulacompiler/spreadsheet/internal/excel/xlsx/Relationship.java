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

package org.formulacompiler.spreadsheet.internal.excel.xlsx;


/**
 * @author Igor Didyuk
 */
public final class Relationship
{
	private static final char PATH_SEPARATOR = '/';
	private static final String RELATIONSHIP_DIR = "_rels";
	private static final String RELATIONSHIP_EXT = ".rels";

	private final String type;
	private final String target;

	public Relationship( String _type, String _targetAbsolute )
	{
		this.type = _type;
		this.target = _targetAbsolute;
	}

	public Relationship( String _type, String _targetRelative, String _parent )
	{
		this.type = _type;
		final int i = _parent.lastIndexOf( Relationship.PATH_SEPARATOR );
		if (i == -1)
			this.target = _targetRelative;
		else
			this.target = _parent.substring( 0, i + 1 ) + _targetRelative;
	}

	public String getType()
	{
		return this.type;
	}

	public String getTarget( String _relativeTo )
	{
		String path = this.target;
		if (_relativeTo != null) {
			if (!path.startsWith( _relativeTo ))
				throw new IllegalArgumentException( "Entity does not belong to the specified path." );
			path = path.substring( _relativeTo.length() );
		}
		if (path.charAt( 0 ) == Relationship.PATH_SEPARATOR)
			path = path.substring( 1 );
		return path;
	}

	public static String getRelationshipPath( final String _partName )
	{
		final int i = _partName.lastIndexOf( PATH_SEPARATOR );
		if (i == -1)
			return RELATIONSHIP_DIR + PATH_SEPARATOR + _partName + RELATIONSHIP_EXT;
		return _partName.substring( 0, i + 1 ) + RELATIONSHIP_DIR + _partName.substring( i ) + RELATIONSHIP_EXT;
	}

	public static String getRelationshipDir( final String _partName )
	{
		final int i = _partName.lastIndexOf( PATH_SEPARATOR );
		if (i == -1)
			return null;
		return _partName.substring( 0, i + 1 );
	}
}
