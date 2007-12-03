/*
 * Copyright (c) 2006 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are prohibited, unless you have been explicitly granted
 * more rights by Abacus Research AG.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
