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

package org.formulacompiler.spreadsheet.internal.excel.xlsx.loader.template;

import java.util.List;

import org.formulacompiler.spreadsheet.internal.excel.xlsx.xml.XmlNode;


/**
 * @author Igor Didyuk
 */
public interface Stylesheet
{

	public static final class SheetStyle
	{
		private final String defaultColWidth;
		private final String defaultRowHeight;

		public SheetStyle( String _defaultColWidth, String _defaultRowHeight )
		{
			this.defaultColWidth = _defaultColWidth;
			this.defaultRowHeight = _defaultRowHeight;
		}

		public String getDefaultColWidth()
		{
			return this.defaultColWidth;
		}

		public String getDefaultRowHeight()
		{
			return this.defaultRowHeight;
		}
	}

	public static final class ColumnStyle
	{
		private final String min;
		private final String max;
		private final String customWidth;
		private final String width;
		private final String bestFit;
		private final String style;

		public ColumnStyle( String _min, String _max, String _customWidth, String _width, String _bestFit, String _style )
		{
			super();
			this.min = _min;
			this.max = _max;
			this.customWidth = _customWidth;
			this.width = _width;
			this.bestFit = _bestFit;
			this.style = _style;
		}

		public String getMin()
		{
			return this.min;
		}

		public String getMax()
		{
			return this.max;
		}

		public String getCustomWidth()
		{
			return this.customWidth;
		}

		public String getWidth()
		{
			return this.width;
		}

		public String getBestFit()
		{
			return this.bestFit;
		}

		public String getStyle()
		{
			return this.style;
		}
	}

	public static final class RowStyle
	{
		private final String height;

		public RowStyle( String _height )
		{
			this.height = _height;
		}

		public String getHeight()
		{
			return this.height;
		}
	}

	int getStyleIndex( String _name, boolean _isDate, boolean _isTime );

	List<XmlNode> getCellStyleXfs();

	List<XmlNode> getCellXfs();

	List<XmlNode> getCellStyles();

	SheetStyle getSheetStyle();

	List<ColumnStyle> getColumns();

	RowStyle getRowStyle( String _name );

	byte[] getStylesheetSource();

}
