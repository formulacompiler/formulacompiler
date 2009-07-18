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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.XMLConstants;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.loader.IOUtil;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.loader.NumberFormat;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.loader.XmlParser;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.xml.XmlNode;

/**
 * @author Igor Didyuk
 */
final class StylesheetParser extends XmlParser implements Stylesheet
{
	public static final class Style
	{
		final NumberFormat numberFormat;
		final boolean applyNumberFormat;
		final int baseStyleIndex;

		private Style( int _baseIndex, NumberFormat _numberFormat, boolean _applyNumberFormat )
		{
			this.numberFormat = _numberFormat;
			this.applyNumberFormat = _applyNumberFormat;
			this.baseStyleIndex = _baseIndex;
		}

		private Style( Style _baseStyle, int _baseIndex, NumberFormat _numberFormat, Boolean _applyNumberFormat )
		{
			this( _baseIndex, _numberFormat == null ? _baseStyle.numberFormat : _numberFormat,
					_applyNumberFormat == null ? _baseStyle.applyNumberFormat : _applyNumberFormat.booleanValue() );
		}

		public String getFormat()
		{
			return this.numberFormat.getFormat();
		}

		public boolean isDate()
		{
			return this.numberFormat.isDate();
		}

		public boolean isTime()
		{
			return this.numberFormat.isTime();
		}

		public String toString()
		{
			return this.numberFormat.toString();
		}
	}

	private static final int DEFAULT_BASE_STYLE_ID = 0;
	private static final int DEFAULT_DATE_NF = 14;
	private static final int DEFAULT_TIME_NF = 21;
	private static final int DEFAULT_DATETIME_NF = 22;

	private final byte[] source;

	private final List<Style> baseStyles = New.list();
	private final List<Style> styles = New.list();
	private final Map<String, Integer> namedStyles = New.map();

	private SheetStyle sheet = null;
	private final List<ColumnStyle> columns = New.list();
	private final Map<Integer, RowStyle> rowStyles = New.map();

	private final List<XmlNode> xmlCellStyleXfs = New.list();
	private final List<XmlNode> xmlCellXfs = New.list();
	private final List<XmlNode> xmlCellStyles = New.list();

	StylesheetParser( final byte[] _input ) throws XMLStreamException
	{
		super( new ByteArrayInputStream( _input ) );
		this.source = _input;

		Map<String, NumberFormat> numberFormats = null;

		find( XMLConstants.Main.STYLESHEET, 0 );
		final int rootContext = getContext();
		StartElement se;
		while ((se = findAny( rootContext )) != null) {
			if (se.getName().equals( XMLConstants.Main.NUMBER_FORMATS ))
				numberFormats = parseNumberFormats();
			else if (se.getName().equals( XMLConstants.Main.CELL_STYLE_FORMATS ))
				parseBaseStyles( numberFormats );
			else if (se.getName().equals( XMLConstants.Main.CELL_FORMATS ))
				parseCellStyles( numberFormats, this.baseStyles );
			else if (se.getName().equals( XMLConstants.Main.NAMED_STYLES )) {
				parseNamedCellStyles();
			}
		}
	}

	StylesheetParser( final InputStream _input ) throws IOException, XMLStreamException
	{
		this( IOUtil.readBytes( _input ) );
	}

	private Map<String, NumberFormat> parseNumberFormats() throws XMLStreamException
	{
		final Map<String, NumberFormat> formats = New.map();

		final int nfContext = getContext();
		StartElement se;
		while ((se = find( XMLConstants.Main.NUMBER_FORMAT, nfContext )) != null) {
			final String id = se.getAttributeByName( XMLConstants.Main.NUMBER_FORMAT_ID ).getValue();
			final String code = se.getAttributeByName( XMLConstants.Main.NUMBER_FORMAT_CODE ).getValue();
			formats.put( id, new NumberFormat( code ) );
		}

		return formats;
	}

	private static NumberFormat getNumberFormatCode( Map<String, NumberFormat> _numberFormats, Attribute _numberFormatId )
	{
		if (_numberFormatId == null)
			return null;
		if (_numberFormats != null) {
			final NumberFormat numberFormat = _numberFormats.get( _numberFormatId.getValue() );
			if (numberFormat != null)
				return numberFormat;
		}
		return NumberFormat.getPredefinedNumberFormat( _numberFormatId.getValue() );
	}

	private static boolean getApplyNumberFormat( Attribute _applyNumberFormat )
	{
		return XMLConstants.TRUE.equals( _applyNumberFormat.getValue() );
	}

	private void parseBaseStyles( Map<String, NumberFormat> _numberFormats ) throws XMLStreamException
	{
		final int context = getContext();
		StartElement se;
		while ((se = find( XMLConstants.Main.FORMAT, context )) != null) {
			this.xmlCellStyleXfs.add( readEvents( se ) );

			final Attribute numberFormatId = se.getAttributeByName( XMLConstants.Main.NUMBER_FORMAT_ID );
			final Attribute applyNumberFormat = se.getAttributeByName( XMLConstants.Main.FORMAT_APPLY_NUMBER_FORMAT );

			this.baseStyles.add( new Style( -1, getNumberFormatCode( _numberFormats, numberFormatId ),
					applyNumberFormat != null && getApplyNumberFormat( applyNumberFormat ) ) );
		}
	}

	private void parseCellStyles( Map<String, NumberFormat> _numberFormats, List<Style> _baseStyles ) throws XMLStreamException
	{
		final int context = getContext();
		StartElement se;
		while ((se = find( XMLConstants.Main.FORMAT, context )) != null) {
			this.xmlCellXfs.add( readEvents( se ) );

			final Attribute baseStyleId = se.getAttributeByName( XMLConstants.Main.FORMAT_STYLE_ID );
			final int baseStyleIdValue = baseStyleId == null ? DEFAULT_BASE_STYLE_ID : Integer.parseInt( baseStyleId.getValue() );
			final Style base = baseStyleId != null ? _baseStyles.get( baseStyleIdValue ) : null;

			final Attribute numberFormatId = se.getAttributeByName( XMLConstants.Main.NUMBER_FORMAT_ID );
			final Attribute applyNumberFormat = se.getAttributeByName( XMLConstants.Main.FORMAT_APPLY_NUMBER_FORMAT );
			final NumberFormat numberFormat = getNumberFormatCode( _numberFormats, numberFormatId );
			final Style style;
			if (base == null)
				style = new Style( 0, numberFormat,
						applyNumberFormat != null && getApplyNumberFormat( applyNumberFormat ) );
			else
				style = new Style( base, baseStyleIdValue, numberFormat,
						applyNumberFormat != null ? getApplyNumberFormat( applyNumberFormat ) : null );
			this.styles.add( style );
		}
	}

	private void parseNamedCellStyles() throws XMLStreamException
	{
		final int context = getContext();
		StartElement se;
		while ((se = find( XMLConstants.Main.NAMED_STYLE, context )) != null) {
			this.xmlCellStyles.add( readEvents( se ) );

			final Attribute styleId = se.getAttributeByName( XMLConstants.Main.FORMAT_STYLE_ID );
			final Attribute name = se.getAttributeByName( XMLConstants.Main.NAMED_STYLE_NAME );
			this.namedStyles.put( name.getValue(), new Integer( styleId.getValue() ) );
		}
	}

	public Style getStyle( final int _index )
	{
		return this.styles.get( _index );
	}

	private int createBaseStyle()
	{
		final Style baseStyle = new Style( -1, NumberFormat.getPredefinedNumberFormat( DEFAULT_BASE_STYLE_ID ), false );
		this.baseStyles.add( baseStyle );

		this.xmlCellStyleXfs.add( this.xmlCellStyleXfs.get( 0 ) );

		return this.xmlCellStyleXfs.size() - 1;
	}

	private int createNamedStyle( String _name )
	{
		final int baseStyleId = createBaseStyle();
		this.namedStyles.put( _name, baseStyleId );

		final XmlNode node = new XmlNode( XMLConstants.Main.NAMED_STYLE );
		node.addAttribute( XMLConstants.Main.FORMAT_STYLE_ID, Integer.toString( baseStyleId ) );
		node.addAttribute( XMLConstants.Main.NAMED_STYLE_NAME, _name );
		this.xmlCellStyles.add( node );

		return baseStyleId;
	}

	private int createCellStyle( int _baseStyleId, boolean _isDate, boolean _isTime )
	{
		final XmlNode node = this.xmlCellStyleXfs.get( _baseStyleId ).clone();
		final Style style;
		if (!_isDate && !_isTime) {
			style = new Style( _baseStyleId, NumberFormat.getPredefinedNumberFormat( "0" ), false );

			node.addAttribute( XMLConstants.Main.FORMAT_STYLE_ID, Integer.toString( _baseStyleId ) );
		}
		else {
			final int numberFormatId;
			if (_isDate)
				if (_isTime)
					numberFormatId = DEFAULT_DATETIME_NF;
				else
					numberFormatId = DEFAULT_DATE_NF;
			else
				numberFormatId = DEFAULT_TIME_NF;

			style = new Style( _baseStyleId, NumberFormat.getPredefinedNumberFormat( numberFormatId ), true );

			node.addAttribute( XMLConstants.Main.FORMAT_STYLE_ID, Integer.toString( _baseStyleId ) );
			node.addAttribute( XMLConstants.Main.FORMAT_APPLY_NUMBER_FORMAT, XMLConstants.TRUE );
			node.addAttribute( XMLConstants.Main.NUMBER_FORMAT_ID, Integer.toString( numberFormatId ) );
		}

		this.styles.add( style );
		this.xmlCellXfs.add( node );
		return this.xmlCellXfs.size() - 1;
	}

	private int getStyleIndex( int _baseStyleId, boolean _isDate, boolean _isTime )
	{
		for (int i = 0; i != this.styles.size(); i++) {
			Style style = this.styles.get( i );
			if (style.baseStyleIndex == _baseStyleId &&
					(style.applyNumberFormat || !_isDate && !_isTime) &&
					style.isDate() == _isDate && style.isTime() == _isTime)
				return i;
		}
		return createCellStyle( _baseStyleId, _isDate, _isTime );
	}

	public int getStyleIndex( String _name, boolean _isDate, boolean _isTime )
	{
		if (_name != null) {
			final Integer baseStyleId = this.namedStyles.get( _name );
			if (baseStyleId == null)
				return getStyleIndex( createNamedStyle( _name ), _isDate, _isTime );
			else
				return getStyleIndex( baseStyleId.intValue(), _isDate, _isTime );
		}
		else {
			if (!_isDate && !_isTime)
				return -1;
			return getStyleIndex( DEFAULT_BASE_STYLE_ID, _isDate, _isTime );
		}
	}

	void addColumn( ColumnStyle _column )
	{
		this.columns.add( _column );
	}

	void setSheetStyle( SheetStyle _sheet )
	{
		this.sheet = _sheet;
	}

	public List<XmlNode> getCellStyleXfs()
	{
		return this.xmlCellStyleXfs;
	}

	public List<XmlNode> getCellStyles()
	{
		return this.xmlCellStyles;
	}

	public List<XmlNode> getCellXfs()
	{
		return this.xmlCellXfs;
	}

	void setRowStyle( int _styleId, RowStyle _style )
	{
		final int baseStyleId = this.styles.get( _styleId ).baseStyleIndex;
		this.rowStyles.put( baseStyleId, _style );
	}

	public SheetStyle getSheetStyle()
	{
		return this.sheet;
	}

	public List<ColumnStyle> getColumns()
	{
		return this.columns;
	}

	public RowStyle getRowStyle( String _name )
	{
		final Integer baseStyleId = this.namedStyles.get( _name );
		if (baseStyleId == null)
			return null;

		return this.rowStyles.get( baseStyleId );
	}

	public byte[] getStylesheetSource()
	{
		return this.source;
	}
}
