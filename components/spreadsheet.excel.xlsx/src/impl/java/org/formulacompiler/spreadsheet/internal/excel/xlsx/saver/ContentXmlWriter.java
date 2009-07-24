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
import java.util.zip.ZipOutputStream;
import javax.xml.stream.XMLStreamException;

import org.formulacompiler.spreadsheet.internal.excel.xlsx.Relationship;


/**
 * @author Igor Didyuk
 */
abstract class ContentXmlWriter extends XmlWriter implements ContentProvider
{

	static final class Part
	{
		private static final String SPREADSHEET_ML_PREFIX = "application/vnd.openxmlformats-officedocument.spreadsheetml.";

		private final String partName;
		private final String contentType;

		Part( String _partName, String _contentType )
		{
			this.partName = _partName;
			this.contentType = _contentType;
		}

		String getPartName()
		{
			return this.partName;
		}

		String getContentType()
		{
			return SPREADSHEET_ML_PREFIX + this.contentType;
		}
	}

	static final class Metadata
	{
		private final Part contentPart;
		private final Relationship relationship;

		Metadata( String _path, String _contentType, String _relationshipNamespace, String _relationshipType )
		{
			this.contentPart = new Part( "/" + _path, _contentType );
			this.relationship = new Relationship( _relationshipNamespace + "/" + _relationshipType, _path );
		}

		Part getContentPart()
		{
			return this.contentPart;
		}

		Relationship getRelationship()
		{
			return this.relationship;
		}
	}

	ContentXmlWriter( ZipOutputStream _outputStream, String _path, String... _namespaces ) throws XMLStreamException, IOException
	{
		super( _outputStream, _path, _namespaces );
	}
}
