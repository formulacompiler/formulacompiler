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

package org.formulacompiler.spreadsheet.internal.odf.saver.writer;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;

import org.formulacompiler.compiler.internal.Duration;
import org.formulacompiler.compiler.internal.LocalDate;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.runtime.New;
import org.formulacompiler.runtime.internal.Runtime_v2;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.odf.StyleFamilies;
import org.formulacompiler.spreadsheet.internal.odf.ValueTypes;
import org.formulacompiler.spreadsheet.internal.odf.XMLConstants;
import org.formulacompiler.spreadsheet.internal.odf.saver.Style;
import org.formulacompiler.spreadsheet.internal.odf.saver.util.ExpressionFormatter;
import org.formulacompiler.spreadsheet.internal.odf.xml.DataTypeUtil;
import org.formulacompiler.spreadsheet.internal.odf.xml.stream.ElementWriter;

class CellWriter extends ElementWriter
{
	private static final String DEFAULT_STYLE = "Default";

	private final TimeZone timeZone;
	private final Set<Style> styles;

	public CellWriter( final XMLEventFactory _xmlEventFactory, final XMLEventWriter _xmlEventWriter,
			final TimeZone _timeZone, final Set<Style> _styles )
	{
		super( _xmlEventFactory, _xmlEventWriter, XMLConstants.Table.TABLE_CELL );
		this.timeZone = _timeZone;
		this.styles = _styles;
	}

	public void write( CellInstance _cell ) throws XMLStreamException, SpreadsheetException
	{
		final Map<QName, String> attributes = New.map();
		String str = null;
		if (_cell != null) {
			String styleName = _cell.getStyleName();
			if (styleName == null || "".equals( styleName )) {
				styleName = DEFAULT_STYLE;
			}
			attributes.put( XMLConstants.Table.STYLE_NAME, styleName );
			this.styles.add( new Style( styleName, StyleFamilies.TABLE_CELL ) );

			final ExpressionNode exp = _cell.getExpression();
			if (exp != null) {
				attributes.put( XMLConstants.Table.FORMULA, ExpressionFormatter.format( exp, _cell.getCellIndex() ) );
			}
			else {
				final Object constantValue = _cell.getValue();
				if (constantValue != null) {
					if (constantValue instanceof Boolean) {
						attributes.put( XMLConstants.Office.VALUE_TYPE, ValueTypes.BOOLEAN );
						attributes.put( XMLConstants.Office.BOOLEAN_VALUE, constantValue.toString() );
					}
					else if (constantValue instanceof Date) {
						attributes.put( XMLConstants.Office.VALUE_TYPE, ValueTypes.DATE );
						final String dateValue = DataTypeUtil.dateToXmlFormat( (Date) constantValue, this.timeZone );
						attributes.put( XMLConstants.Office.DATE_VALUE, dateValue );
					}
					else if (constantValue instanceof LocalDate) {
						attributes.put( XMLConstants.Office.VALUE_TYPE, ValueTypes.DATE );
						final LocalDate localDate = (LocalDate) constantValue;
						final Date date = Runtime_v2.dateFromDouble( localDate.doubleValue(), DataTypeUtil.GMT_TIME_ZONE );
						final String dateValue = DataTypeUtil.dateToXmlFormat( date, DataTypeUtil.GMT_TIME_ZONE );
						attributes.put( XMLConstants.Office.DATE_VALUE, dateValue );
					}
					else if (constantValue instanceof Duration) {
						attributes.put( XMLConstants.Office.VALUE_TYPE, ValueTypes.TIME );
						final Duration duration = (Duration) constantValue;
						final long milliseconds = duration.getMilliseconds();
						final String dateValue = DataTypeUtil.durationToXmlFormat( milliseconds );
						attributes.put( XMLConstants.Office.TIME_VALUE, dateValue );
					}
					else if (constantValue instanceof Number) {
						attributes.put( XMLConstants.Office.VALUE_TYPE, ValueTypes.FLOAT );
						attributes.put( XMLConstants.Office.VALUE, constantValue.toString() );
					}
					else if (constantValue instanceof String) {
						attributes.put( XMLConstants.Office.VALUE_TYPE, ValueTypes.STRING );
						str = (String) constantValue;
					}
				}
			}
		}
		startElement( attributes );
		if (str != null) {
			TextWriter textWriter = new TextWriter( getXmlEventFactory(), getXmlEventWriter() );
			textWriter.write( str );
		}
		endElement();
	}


}
