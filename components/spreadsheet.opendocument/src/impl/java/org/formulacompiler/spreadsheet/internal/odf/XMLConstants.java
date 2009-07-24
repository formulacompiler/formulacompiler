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

package org.formulacompiler.spreadsheet.internal.odf;

import javax.xml.namespace.QName;

/**
 * @author Vladimir Korenev
 */
public interface XMLConstants
{
	public interface Office
	{
		String XMLNS = "urn:oasis:names:tc:opendocument:xmlns:office:1.0";
		String PREFIX = "office";
		QName DOCUMENT_CONTENT = new QName( XMLNS, "document-content", PREFIX );
		QName DOCUMENT_STYLES = new QName( XMLNS, "document-styles", PREFIX );
		QName STYLES = new QName( XMLNS, "styles", PREFIX );
		QName BODY = new QName( XMLNS, "body", PREFIX );
		QName SPREADSHEET = new QName( XMLNS, "spreadsheet", PREFIX );
		QName VALUE_TYPE = new QName( XMLNS, "value-type", PREFIX );
		QName VALUE = new QName( XMLNS, "value", PREFIX );
		QName BOOLEAN_VALUE = new QName( XMLNS, "boolean-value", PREFIX );
		QName DATE_VALUE = new QName( XMLNS, "date-value", PREFIX );
		QName TIME_VALUE = new QName( XMLNS, "time-value", PREFIX );
		QName STRING_VALUE = new QName( XMLNS, "string-value", PREFIX );
		QName ANNOTATION = new QName( XMLNS, "annotation", PREFIX );
		QName DOCUMENT_META = new QName( XMLNS, "document-meta", PREFIX );
		QName META = new QName( XMLNS, "meta", PREFIX );
	}

	public interface Style
	{
		String XMLNS = "urn:oasis:names:tc:opendocument:xmlns:style:1.0";
		String PREFIX = "style";
		QName FAMILY = new QName( XMLNS, "family", PREFIX );
		QName NAME = new QName( XMLNS, "name", PREFIX );
		QName STYLE = new QName( XMLNS, "style", PREFIX );
	}

	public interface Table
	{
		String XMLNS = "urn:oasis:names:tc:opendocument:xmlns:table:1.0";
		String PREFIX = "table";
		QName NUMBER_ROWS_REPEATED = new QName( XMLNS, "number-rows-repeated", PREFIX );
		QName NUMBER_COLUMNS_REPEATED = new QName( XMLNS, "number-columns-repeated", PREFIX );
		QName FORMULA = new QName( XMLNS, "formula", PREFIX );
		QName NAME = new QName( XMLNS, "name", PREFIX );
		QName NAMED_RANGE = new QName( XMLNS, "named-range", PREFIX );
		QName NAMED_EXPRESSIONS = new QName( XMLNS, "named-expressions", PREFIX );
		QName CELL_RANGE_ADDRESS = new QName( XMLNS, "cell-range-address", PREFIX );
		QName BASE_CELL_ADDRESS = new QName( XMLNS, "base-cell-address", PREFIX );
		QName COVERED_TABLE_CELL = new QName( XMLNS, "covered-table-cell", PREFIX );
		QName TABLE_CELL = new QName( XMLNS, "table-cell", PREFIX );
		QName TABLE_COLUMN = new QName( XMLNS, "table-column", PREFIX );
		QName TABLE_ROW = new QName( XMLNS, "table-row", PREFIX );
		QName TABLE = new QName( XMLNS, "table", PREFIX );
		QName STYLE_NAME = new QName( XMLNS, "style-name", PREFIX );
	}

	public interface Text
	{
		String XMLNS = "urn:oasis:names:tc:opendocument:xmlns:text:1.0";
		String PREFIX = "text";
		QName P = new QName( XMLNS, "p", PREFIX );
		QName S = new QName( XMLNS, "s", PREFIX );
		QName C = new QName( XMLNS, "c", PREFIX );
		QName TAB = new QName( XMLNS, "tab", PREFIX );
	}

	public interface Manifest
	{
		String XMLNS = "urn:oasis:names:tc:opendocument:xmlns:manifest:1.0";
		String PREFIX = "manifest";
		QName FILE_ENTRY = new QName( XMLNS, "file-entry", PREFIX );
		QName FULL_PATH = new QName( XMLNS, "full-path", PREFIX );
		QName MEDIA_TYPE = new QName( XMLNS, "media-type", PREFIX );
	}

	public interface Meta
	{
		String XMLNS = "urn:oasis:names:tc:opendocument:xmlns:meta:1.0";
		String PREFIX = "meta";
		QName GENERATOR = new QName( XMLNS, "generator", PREFIX );
		QName CREATION_DATE = new QName( XMLNS, "creation-date", PREFIX );
	}

}
