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

package org.formulacompiler.spreadsheet.internal.excel.xlsx.loader.template;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.formulacompiler.spreadsheet.internal.excel.xlsx.XMLConstants;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.loader.PackageLoader;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.loader.XmlParser;

/**
 * @author Igor Didyuk
 */
final class WorksheetParser extends XmlParser
{
	private final StylesheetParser stylesheet;

	WorksheetParser( final PackageLoader _loader, final String _entryPath, final StylesheetParser _stylesheet ) throws XMLStreamException
	{
		super( _loader.getEntry( _entryPath ) );
		this.stylesheet = _stylesheet;
	}

	void parse() throws XMLStreamException
	{
		find( XMLConstants.WORKSHEET_ROOT );
		final int rootContext = getContext();
		StartElement se;
		while ((se = findAny( rootContext )) != null) {
			if (se.getName().equals( XMLConstants.Main.SHEET_FORMAT_PROPERTIES )) {
				final Stylesheet.SheetStyle sheet = parseSheetFormatPr( se );
				this.stylesheet.setSheetStyle( sheet );
				continue;
			}
			if (se.getName().equals( XMLConstants.Main.COLS )) {
				final int colsContext = getContext();
				while ((se = find( XMLConstants.Main.COL, colsContext )) != null) {
					final Stylesheet.ColumnStyle column = parseCol( se );
					this.stylesheet.addColumn( column );
				}
				continue;
			}
			if (se.getName().equals( XMLConstants.Main.SHEET_DATA )) {
				final int dataContext = getContext();
				while ((se = find( XMLConstants.Main.ROW, dataContext )) != null) {
					final Stylesheet.RowStyle rowStyle = parseRow( se );
					final int rowContext = getContext();
					if ((se = find( XMLConstants.Main.CELL, rowContext )) != null) {
						final Attribute styleId = se.getAttributeByName( XMLConstants.Main.CELL_STYLE );
						if (styleId != null)
							this.stylesheet.setRowStyle( Integer.parseInt( styleId.getValue() ), rowStyle );
					}
				}
			}
		}
	}

	private Stylesheet.SheetStyle parseSheetFormatPr( final StartElement _sheetFormatPr )
	{
		final Attribute defaultColWidth = _sheetFormatPr.getAttributeByName( XMLConstants.Main.SHEET_FORMAT_DEFAULT_COL_WIDTH );
		final Attribute defaultRowHeight = _sheetFormatPr.getAttributeByName( XMLConstants.Main.SHEET_FORMAT_DEFAULT_ROW_HEIGHT );

		return new Stylesheet.SheetStyle( defaultColWidth == null ? null : defaultColWidth.getValue(),
				defaultRowHeight == null ? null : defaultRowHeight.getValue() );
	}

	private Stylesheet.ColumnStyle parseCol( final StartElement _col )
	{
		final String minValue = _col.getAttributeByName( XMLConstants.Main.COL_MIN ).getValue();
		final String maxValue = _col.getAttributeByName( XMLConstants.Main.COL_MAX ).getValue();

		final Attribute bestFit = _col.getAttributeByName( XMLConstants.Main.COL_BEST_FIT );
		final String bestFitValue = bestFit == null ? null : bestFit.getValue();

		final Attribute style = _col.getAttributeByName( XMLConstants.Main.COL_STYLE );
		final String styleValue = style == null ? null : style.getValue();

		final Attribute customWidth = _col.getAttributeByName( XMLConstants.Main.COL_CUSTOM_WIDTH );
		final String customWidthValue = customWidth == null ? null : customWidth.getValue();

		final Attribute width = _col.getAttributeByName( XMLConstants.Main.COL_WIDTH );
		final String widthValue = width == null ? null : width.getValue();

		return new Stylesheet.ColumnStyle( minValue, maxValue, customWidthValue, widthValue, bestFitValue, styleValue );
	}

	private Stylesheet.RowStyle parseRow( final StartElement _row )
	{
		final Attribute customHeight = _row.getAttributeByName( XMLConstants.Main.ROW_CUSTOM_HEIGHT );
		if (customHeight != null && XMLConstants.TRUE.equals( customHeight.getValue() )) {
			final Attribute height = _row.getAttributeByName( XMLConstants.Main.ROW_HEIGHT );
			if (height != null)
				return new Stylesheet.RowStyle( height.getValue() );
		}
		return null;
	}
}
