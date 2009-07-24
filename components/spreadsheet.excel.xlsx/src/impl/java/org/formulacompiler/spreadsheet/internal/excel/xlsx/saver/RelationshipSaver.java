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

package org.formulacompiler.spreadsheet.internal.excel.xlsx.saver;

import java.io.IOException;
import java.util.List;
import java.util.zip.ZipOutputStream;
import javax.xml.stream.XMLStreamException;

import org.formulacompiler.spreadsheet.internal.excel.xlsx.Relationship;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.XMLConstants;


/**
 * @author Igor Didyuk
 */
final class RelationshipSaver extends XmlWriter
{

	private final String root;

	RelationshipSaver( ZipOutputStream _outputStream, String _path ) throws XMLStreamException, IOException
	{
		super( _outputStream, Relationship.getRelationshipPath( _path ), XMLConstants.PackageRelationships.XMLNS );
		this.root = Relationship.getRelationshipDir( _path );
	}

	void write( final List<Relationship> _relationships ) throws XMLStreamException
	{
		writeStartElement( XMLConstants.PackageRelationships.RELATIONSHIPS );

		final int size = _relationships.size();
		for (int i = 0; i != size; i++) {
			final Relationship relationship = _relationships.get( i );
			writeStartElement( XMLConstants.PackageRelationships.RELATIONSHIP );
			writeAttribute( XMLConstants.PackageRelationships.ID, "rId" + String.valueOf( i + 1 ) );
			writeAttribute( XMLConstants.PackageRelationships.TYPE, relationship.getType() );
			writeAttribute( XMLConstants.PackageRelationships.TARGET, relationship.getTarget( this.root ) );
			writeEndElement( XMLConstants.PackageRelationships.RELATIONSHIP );
		}

		writeEndElement( XMLConstants.PackageRelationships.RELATIONSHIPS );
	}
}
