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
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.XMLConstants;


/**
 * @author Igor Didyuk
 */
final class StylesheetParser extends XmlParser
{
	static final class Style
	{
		final NumberFormat numberFormat;
		final boolean applyNumberFormat;

		private Style( NumberFormat _numberFormat, boolean _applyNumberFormat )
		{
			this.numberFormat = _numberFormat;
			this.applyNumberFormat = _applyNumberFormat;
		}

		private Style( Style _base, NumberFormat _numberFormat, Boolean _applyNumberFormat )
		{
			this( _numberFormat == null ? _base.numberFormat : _numberFormat,
					_applyNumberFormat == null ? _base.applyNumberFormat : _applyNumberFormat.booleanValue() );
		}

		boolean isDate()
		{
			return this.numberFormat.isDate();
		}

		boolean isTime()
		{
			return this.numberFormat.isTime();
		}

		public String toString()
		{
			return this.numberFormat.toString();
		}
	}

	private final List<Style> styles = New.arrayList();

	StylesheetParser( final InputStream _input ) throws XMLStreamException
	{
		super( _input );

		Map<String, NumberFormat> numberFormats = null;
		List<Style> baseStyles = null;

		find( XMLConstants.Main.STYLESHEET, 0 );
		final int rootContext = getContext();
		StartElement se;
		while ((se = findAny( rootContext )) != null)
			if (se.getName().equals( XMLConstants.Main.NUMBER_FORMATS ))
				numberFormats = parseNumberFormats();
			else if (se.getName().equals( XMLConstants.Main.CELL_STYLE_FORMATS ))
				baseStyles = parseBaseStyles( numberFormats );
			else if (se.getName().equals( XMLConstants.Main.CELL_FORMATS ))
				parseCellStyles( numberFormats, baseStyles );
	}

	private Map<String, NumberFormat> parseNumberFormats() throws XMLStreamException
	{
		final Map<String, NumberFormat> formats = New.hashMap();

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

	private List<Style> parseBaseStyles( Map<String, NumberFormat> _numberFormats ) throws XMLStreamException
	{
		final List<Style> styles = New.arrayList();

		final int context = getContext();
		StartElement se;
		while ((se = find( XMLConstants.Main.FORMAT, context )) != null) {
			final Attribute numberFormatId = se.getAttributeByName( XMLConstants.Main.NUMBER_FORMAT_ID );
			final Attribute applyNumberFormat = se.getAttributeByName( XMLConstants.Main.FORMAT_APPLY_NUMBER_FORMAT );

			styles.add( new Style( getNumberFormatCode( _numberFormats, numberFormatId ),
					applyNumberFormat != null && getApplyNumberFormat( applyNumberFormat ) ) );
		}

		return styles;
	}

	private void parseCellStyles( Map<String, NumberFormat> _numberFormats, List<Style> _baseStyles ) throws XMLStreamException
	{
		final int context = getContext();
		StartElement se;
		while ((se = find( XMLConstants.Main.FORMAT, context )) != null) {
			final Attribute baseStyleId = se.getAttributeByName( XMLConstants.Main.FORMAT_STYLE_ID );
			final Style base = baseStyleId != null ? _baseStyles.get( Integer.parseInt( baseStyleId.getValue() ) ) : null;

			final Attribute numberFormatId = se.getAttributeByName( XMLConstants.Main.NUMBER_FORMAT_ID );
			final Attribute applyNumberFormat = se.getAttributeByName( XMLConstants.Main.FORMAT_APPLY_NUMBER_FORMAT );
			final NumberFormat numberFormat = getNumberFormatCode( _numberFormats, numberFormatId );
			final Style style;
			if (base == null)
				style = new Style( numberFormat,
						applyNumberFormat != null && getApplyNumberFormat( applyNumberFormat ) );
			else
				style = new Style( base, numberFormat,
						applyNumberFormat != null ? getApplyNumberFormat( applyNumberFormat ) : null );
			this.styles.add( style );
		}
	}

	Style getStyle( final int _index )
	{
		return this.styles.get( _index );
	}
}
