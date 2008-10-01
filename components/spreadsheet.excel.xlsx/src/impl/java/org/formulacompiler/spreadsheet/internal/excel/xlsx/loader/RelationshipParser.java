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

package org.formulacompiler.spreadsheet.internal.excel.xlsx.loader;

import java.io.InputStream;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.Relationship;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.XMLConstants;


/**
 * @author Igor Didyuk
 */
public final class RelationshipParser extends XmlParser
{
	private final Map<String, Relationship> relationships = New.hashMap();

	public RelationshipParser( final InputStream _input, final String _parent ) throws XMLStreamException
	{
		super( _input );

		StartElement se;
		while ((se = find( XMLConstants.RELATIONSHIP_PATH )) != null) {
			final String id = se.getAttributeByName( XMLConstants.PackageRelationships.ID ).getValue();
			final String type = se.getAttributeByName( XMLConstants.PackageRelationships.TYPE ).getValue();
			final String target = se.getAttributeByName( XMLConstants.PackageRelationships.TARGET ).getValue();
			this.relationships.put( id, new Relationship( type, target, _parent ) );
		}
	}

	public String findByType( final String _targetType )
	{
		for (Relationship relationship : this.relationships.values())
			if (relationship.getType().equals( _targetType ))
				return relationship.getTarget( null );
		return null;
	}

	public String findById( final String _id )
	{
		final Relationship relationship = this.relationships.get( _id );
		return relationship == null ? null : relationship.getTarget( null );
	}
}
