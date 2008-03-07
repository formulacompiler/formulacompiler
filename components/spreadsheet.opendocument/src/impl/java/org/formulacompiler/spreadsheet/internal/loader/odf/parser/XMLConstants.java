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

package org.formulacompiler.spreadsheet.internal.loader.odf.parser;

import javax.xml.namespace.QName;

/**
 * @author Vladimir Korenev
 */
public interface XMLConstants
{
	public interface Office
	{
		String XMLNS = "urn:oasis:names:tc:opendocument:xmlns:office:1.0";
		QName DOCUMENT_CONTENT = new QName( XMLNS, "document-content" );
		QName BODY = new QName( XMLNS, "body" );
		QName SPREADSHEET = new QName( XMLNS, "spreadsheet" );
		QName VALUE_TYPE = new QName( XMLNS, "value-type" );
		QName VALUE = new QName( XMLNS, "value" );
		QName BOOLEAN_VALUE = new QName( XMLNS, "boolean-value" );
		QName DATE_VALUE = new QName( XMLNS, "date-value" );
		QName TIME_VALUE = new QName( XMLNS, "time-value" );
		QName STRING_VALUE = new QName( XMLNS, "string-value" );
		QName ANNOTATION = new QName( XMLNS, "annotation" );
	}

	public interface Table
	{
		String XMLNS = "urn:oasis:names:tc:opendocument:xmlns:table:1.0";
		QName NUMBER_ROWS_REPEATED = new QName( XMLNS, "number-rows-repeated" );
		QName NUMBER_COLUMNS_REPEATED = new QName( XMLNS, "number-columns-repeated" );
		QName FORMULA = new QName( XMLNS, "formula" );
		QName NAME = new QName( XMLNS, "name" );
		QName NAMED_RANGE = new QName( XMLNS, "named-range" );
		QName NAMED_EXPRESSIONS = new QName( XMLNS, "named-expressions" );
		QName CELL_RANGE_ADDRESS = new QName( XMLNS, "cell-range-address" );
		QName BASE_CELL_ADDRESS = new QName( XMLNS, "base-cell-address" );
		QName COVERED_TABLE_CELL = new QName( XMLNS, "covered-table-cell" );
		QName TABLE_CELL = new QName( XMLNS, "table-cell" );
		QName TABLE_ROW = new QName( XMLNS, "table-row" );
		QName TABLE = new QName( XMLNS, "table" );
	}

	public interface Text
	{
		String XMLNS = "urn:oasis:names:tc:opendocument:xmlns:text:1.0";
		QName P = new QName( XMLNS, "p" );
		QName S = new QName( XMLNS, "s" );
		QName C = new QName( XMLNS, "c" );
		QName TAB = new QName( XMLNS, "tab" );
	}
}
