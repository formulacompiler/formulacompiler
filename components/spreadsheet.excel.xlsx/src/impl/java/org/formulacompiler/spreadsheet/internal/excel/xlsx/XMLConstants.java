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

import javax.xml.namespace.QName;


/**
 * @author Igor Didyuk
 */
public interface XMLConstants
{
	public static interface Main
	{
		String XMLNS = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";

		QName WORKBOOK = new QName( XMLNS, "workbook" );
		QName SHEETS = new QName( XMLNS, "sheets" );
		QName SHEET = new QName( XMLNS, "sheet" );
		QName DEFINED_NAMES = new QName( XMLNS, "definedNames" );
		QName DEFINED_NAME = new QName( XMLNS, "definedName" );

		QName WORKSHEET = new QName( XMLNS, "worksheet" );
		QName SHEET_FORMAT_PROPERTIES = new QName( XMLNS, "sheetFormatPr" );
		QName SHEET_FORMAT_DEFAULT_COL_WIDTH = new QName( "defaultColWidth" );
		QName SHEET_FORMAT_DEFAULT_ROW_HEIGHT = new QName( "defaultRowHeight" );
		QName SHEET_DATA = new QName( XMLNS, "sheetData" );
		QName COLS = new QName( XMLNS, "cols" );
		QName COL = new QName( XMLNS, "col" );
		QName COL_MIN = new QName( "min" );
		QName COL_MAX = new QName( "max" );
		QName COL_BEST_FIT = new QName( "bestFit" );
		QName COL_CUSTOM_WIDTH = new QName( "customWidth" );
		QName COL_WIDTH = new QName( "width" );
		QName COL_STYLE = new QName( "style" );
		QName ROW = new QName( XMLNS, "row" );
		QName ROW_CUSTOM_HEIGHT = new QName( "customHeight" );
		QName ROW_HEIGHT = new QName( "ht" );
		QName CELL = new QName( XMLNS, "c" );
		QName CELL_FORMULA = new QName( XMLNS, "f" );
		QName CELL_FORMULA_TYPE = new QName( "t" );
		QName CELL_FORMULA_SHARED_INDEX = new QName( "si" );
		QName CELL_VALUE = new QName( XMLNS, "v" );

		QName SST = new QName( XMLNS, "sst" );
		QName STRING_ITEM = new QName( XMLNS, "si" );
		QName TEXT = new QName( XMLNS, "t" );
		QName RICH_TEXT = new QName( XMLNS, "r" );

		QName STYLESHEET = new QName( XMLNS, "styleSheet" );
		QName FONTS = new QName( XMLNS, "fonts" );
		QName FONT = new QName( XMLNS, "font" );
		QName FILLS = new QName( XMLNS, "fills" );
		QName FILL = new QName( XMLNS, "fill" );
		QName BORDERS = new QName( XMLNS, "borders" );
		QName BORDER = new QName( XMLNS, "border" );
		QName NUMBER_FORMATS = new QName( XMLNS, "numFmts" );
		QName NUMBER_FORMAT = new QName( XMLNS, "numFmt" );
		QName NUMBER_FORMAT_ID = new QName( "numFmtId" );
		QName NUMBER_FORMAT_CODE = new QName( "formatCode" );
		QName CELL_STYLE_FORMATS = new QName( XMLNS, "cellStyleXfs" );
		QName CELL_FORMATS = new QName( XMLNS, "cellXfs" );
		QName FORMAT = new QName( XMLNS, "xf" );
		QName FORMAT_STYLE_ID = new QName( "xfId" );
		QName FORMAT_APPLY_NUMBER_FORMAT = new QName( "applyNumberFormat" );

		QName NAMED_STYLES = new QName( XMLNS, "cellStyles" );
		QName NAMED_STYLE = new QName( XMLNS, "cellStyle" );
		QName NAMED_STYLE_NAME = new QName( "name" );

		QName COUNT = new QName( "count" );
		QName UNIQUE_COUNT = new QName( "uniqueCount" );

		QName NAME = new QName( "name" );
		QName SHEET_ID = new QName( "sheetId" );

		QName ROW_INDEX = new QName( "r" );
		QName CELL_REFERENCE = new QName( "r" );
		QName CELL_TYPE = new QName( "t" );
		QName CELL_STYLE = new QName( "s" );

		QName FONT_ID = new QName( "fontId" );
		QName FILL_ID = new QName( "fillId" );
		QName BORDER_ID = new QName( "borderId" );
	}

	public static interface PackageRelationships
	{
		String XMLNS = "http://schemas.openxmlformats.org/package/2006/relationships";

		QName RELATIONSHIPS = new QName( XMLNS, "Relationships" );
		QName RELATIONSHIP = new QName( XMLNS, "Relationship" );

		QName ID = new QName( "Id" );
		QName TYPE = new QName( "Type" );
		QName TARGET = new QName( "Target" );
	}

	public static interface DocumentRelationships
	{
		String XMLNS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
		QName ID = new QName( XMLNS, "id" );
	}

	public static interface ContentTypes
	{
		String XMLNS = "http://schemas.openxmlformats.org/package/2006/content-types";

		QName TYPES = new QName( XMLNS, "Types" );
		QName DEFAULT = new QName( XMLNS, "Default" );
		QName OVERRIDE = new QName( XMLNS, "Override" );

		QName EXTENSION = new QName( "Extension" );
		QName PART_NAME = new QName( "PartName" );
		QName CONTENT_TYPE = new QName( "ContentType" );
	}

	QName[] RELATIONSHIP_PATH = { PackageRelationships.RELATIONSHIPS, PackageRelationships.RELATIONSHIP };

	QName[] WORKBOOK_SHEETS_PATH = { Main.WORKBOOK, Main.SHEETS };
	QName[] WORKBOOK_DEFINED_NAMES_PATH = { Main.WORKBOOK, Main.DEFINED_NAMES };

	QName[] WORKSHEET_ROOT = { Main.WORKSHEET };
	QName[] COLS_PATH = { Main.WORKSHEET, Main.COLS };
	QName[] ROW_PATH = { Main.WORKSHEET, Main.SHEET_DATA, Main.ROW };

	QName[] STRING_ITEM_PATH = { Main.SST, Main.STRING_ITEM };

	QName[] NUMBER_FORMAT_PATH = { Main.STYLESHEET, Main.NUMBER_FORMATS, Main.NUMBER_FORMAT };
	QName[] CELL_STYLE_FORMAT_PATH = { Main.STYLESHEET, Main.CELL_STYLE_FORMATS, Main.FORMAT };
	QName[] CELL_FORMAT_PATH = { Main.STYLESHEET, Main.CELL_FORMATS, Main.FORMAT };

	String TRUE = "1";
	String FALSE = "0";

	String CELL_TYPE_NUMBER = "n";
	String CELL_TYPE_SHARED_STRING = "s";
	String CELL_TYPE_INLINE_STRING = "str";
	String CELL_TYPE_BOOLEAN = "b";
	String CELL_TYPE_ERROR = "e";

	String WORKSHEET_CONTENT_TYPE = "worksheet+xml";
	String WORKSHEET_RELATIONSHIP_TYPE = DocumentRelationships.XMLNS + "/worksheet";

	String SHARED_STRINGS_CONTENT_TYPE = "sharedStrings+xml";
	String SHARED_STRINGS_RELATIONSHIP_TYPE = DocumentRelationships.XMLNS + "/sharedStrings";

	String STYLESHEET_CONTENT_TYPE = "styles+xml";
	String STYLESHEET_RELATIONSHIP_TYPE = DocumentRelationships.XMLNS + "/styles";

	String WORKBOOK_CONTENT_TYPE = "sheet.main+xml";
	String WORKBOOK_RELATIONSHIP_TYPE = DocumentRelationships.XMLNS + "/officeDocument";
}
