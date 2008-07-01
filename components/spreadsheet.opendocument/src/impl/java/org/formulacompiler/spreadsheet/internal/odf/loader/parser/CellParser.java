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

package org.formulacompiler.spreadsheet.internal.odf.loader.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.formulacompiler.compiler.internal.Duration;
import org.formulacompiler.compiler.internal.LocalDate;
import org.formulacompiler.runtime.ComputationMode;
import org.formulacompiler.runtime.internal.RuntimeDouble_v2;
import org.formulacompiler.spreadsheet.SpreadsheetLoader;
import org.formulacompiler.spreadsheet.internal.CellRefFormat;
import org.formulacompiler.spreadsheet.internal.CellWithConstant;
import org.formulacompiler.spreadsheet.internal.CellWithLazilyParsedExpression;
import org.formulacompiler.spreadsheet.internal.RowImpl;
import org.formulacompiler.spreadsheet.internal.odf.ValueTypes;
import org.formulacompiler.spreadsheet.internal.odf.XMLConstants;
import org.formulacompiler.spreadsheet.internal.odf.xml.DataTypeUtil;
import org.formulacompiler.spreadsheet.internal.odf.xml.stream.ElementHandler;
import org.formulacompiler.spreadsheet.internal.odf.xml.stream.ElementListener;
import org.formulacompiler.spreadsheet.internal.odf.xml.stream.XMLEventListener;
import org.formulacompiler.spreadsheet.internal.parser.LazySpreadsheetExpressionParser;

/**
 * @author Vladimir Korenev
 */
class CellParser implements ElementListener
{
	private final RowImpl row;
	private final SpreadsheetLoader.Config config;
	private TableCell tableCell;

	public CellParser( RowImpl _row, SpreadsheetLoader.Config _config )
	{
		this.row = _row;
		this.config = _config;
	}

	public void elementStarted( final StartElement _startElement, final Map<QName, ElementListener> _handlers )
	{
		this.tableCell = new TableCell();
		{
			final Attribute attribute = _startElement.getAttributeByName( XMLConstants.Table.NUMBER_COLUMNS_REPEATED );
			if (attribute != null) {
				this.tableCell.numberColumnsRepeated = Integer.parseInt( attribute.getValue() );
			}
		}
		{
			final Attribute attribute = _startElement.getAttributeByName( XMLConstants.Office.VALUE_TYPE );
			if (attribute != null) {
				final String valueType = attribute.getValue();
				this.tableCell.valueType = valueType;
				if (ValueTypes.BOOLEAN.equals( valueType )) {
					final Attribute valueAttribute = _startElement.getAttributeByName( XMLConstants.Office.BOOLEAN_VALUE );
					if (valueAttribute != null) {
						this.tableCell.booleanValue = valueAttribute.getValue();
					}
				}
				else if (ValueTypes.DATE.equals( valueType )) {
					final Attribute valueAttribute = _startElement.getAttributeByName( XMLConstants.Office.DATE_VALUE );
					if (valueAttribute != null) {
						this.tableCell.dateValue = valueAttribute.getValue();
					}
				}
				else if (ValueTypes.TIME.equals( valueType )) {
					final Attribute valueAttribute = _startElement.getAttributeByName( XMLConstants.Office.TIME_VALUE );
					if (valueAttribute != null) {
						this.tableCell.timeValue = valueAttribute.getValue();
					}
				}
				else if (ValueTypes.STRING.equals( valueType )) {
					final Attribute valueAttribute = _startElement.getAttributeByName( XMLConstants.Office.STRING_VALUE );
					if (valueAttribute != null) {
						this.tableCell.stringValue = valueAttribute.getValue();
					}
				}
			}
		}
		{
			final Attribute attribute = _startElement.getAttributeByName( XMLConstants.Office.VALUE );
			if (attribute != null) {
				this.tableCell.value = attribute.getValue();
			}
		}
		{
			final Attribute attribute = _startElement.getAttributeByName( XMLConstants.Table.FORMULA );
			if (attribute != null) {
				this.tableCell.formula = attribute.getValue();
			}
		}
		_handlers.put( XMLConstants.Text.P, new ParagraphParser() );

		// Skip annotations
		_handlers.put( XMLConstants.Office.ANNOTATION, new ElementHandler() );
	}

	public void elementEnded( final EndElement _endElement )
	{
		try {
			createCell();
		}
		finally {
			this.tableCell = null;
		}
	}

	private void createCell()
	{
		final int numberColumnsRepeated = this.tableCell.numberColumnsRepeated;
		for (int i = 0; i < numberColumnsRepeated; i++) {
			String formula = this.tableCell.formula;
			if (formula != null) {
				final String expression;
				if (formula.startsWith( "oooc:=" ))
					expression = formula.substring( 6 );
				else if (formula.startsWith( "=" )) {
					expression = formula.substring( 1 );
				}
				else {
					expression = formula;
				}
				if ("\"\"".equals( expression )) {
					// Replace ="" by empty string constant.
					new CellWithConstant( this.row, "" );
				}
				else {
					final CellWithLazilyParsedExpression exprCell = new CellWithLazilyParsedExpression( this.row );
					exprCell.setExpressionParser( new LazySpreadsheetExpressionParser( exprCell, expression,
							CellRefFormat.A1_ODF ) );
					if (this.config.loadAllCellValues) {
						final Object value = getValue();
						if (value != null) {
							exprCell.setValue( value );
						}
					}
				}
			}
			else {
				final Object value = getValue();
				if (value != null) {
					new CellWithConstant( this.row, value );
				}
				else {
					this.row.getCellList().add( null );
				}
			}
		}
	}

	private Object getValue()
	{
		final String cellValue = this.tableCell.value;
		final String cellValueType = this.tableCell.valueType;
		final Object value;
		if (ValueTypes.FLOAT.equals( cellValueType )
				|| ValueTypes.PERCENTAGE.equals( cellValueType ) || ValueTypes.CURRENCY.equals( cellValueType )) {
			value = Double.valueOf( cellValue );
		}
		else if (ValueTypes.BOOLEAN.equals( cellValueType )) {
			value = Boolean.valueOf( this.tableCell.booleanValue );
		}
		else if (ValueTypes.DATE.equals( cellValueType )) {
			final Date date = DataTypeUtil.dateFromXmlFormat( this.tableCell.dateValue, DataTypeUtil.GMT_TIME_ZONE );
			final double dateNum = RuntimeDouble_v2.dateToNum( date, DataTypeUtil.GMT_TIME_ZONE, ComputationMode.OPEN_OFFICE_CALC );
			value = new LocalDate( dateNum );
		}
		else if (ValueTypes.TIME.equals( cellValueType )) {
			final long durationInMillis = DataTypeUtil.durationFromXmlFormat( this.tableCell.timeValue );
			value = new Duration( durationInMillis );
		}
		else {
			final String stringValue;
			if (cellValue != null) {
				stringValue = cellValue;
			}
			else if (this.tableCell.stringValue != null) {
				stringValue = this.tableCell.stringValue;
			}
			else {
				final Iterator<String> textContentIterator = this.tableCell.paragraphs.iterator();
				if (textContentIterator.hasNext()) {
					final StringBuilder stringBuilder = new StringBuilder( textContentIterator.next() );
					while (textContentIterator.hasNext()) {
						stringBuilder.append( "\n" ).append( textContentIterator.next() );
					}
					stringValue = stringBuilder.toString();
				}
				else {
					stringValue = null;
				}
			}
			if (stringValue != null) {
				value = stringValue;
			}
			else {
				value = null;
			}
		}
		return value;
	}

	private class TableCell
	{
		public int numberColumnsRepeated = 1;
		public String valueType;
		public String value;
		public String booleanValue;
		public String dateValue;
		public String timeValue;
		public String stringValue;
		public String formula;
		public final Collection<String> paragraphs = new ArrayList<String>();
	}

	private class ParagraphParser implements ElementListener, XMLEventListener
	{
		StringBuilder stringBuilder;

		public void elementStarted( final StartElement _startElement, final Map<QName, ElementListener> _handlers )
		{
			this.stringBuilder = new StringBuilder();
			_handlers.put( XMLConstants.Text.S, new ElementHandler()
			{
				@SuppressWarnings( "unqualified-field-access" )
				@Override
				public void elementStarted( final StartElement _startElement, final Map<QName, ElementListener> _handlers )
				{
					final int count;
					final Attribute attribute = _startElement.getAttributeByName( XMLConstants.Text.C );
					if (attribute != null) {
						count = Integer.parseInt( attribute.getValue() );
					}
					else {
						count = 1;
					}
					for (int i = 0; i < count; i++) {
						stringBuilder.append( " " );
					}
				}
			} );
			_handlers.put( XMLConstants.Text.TAB, new ElementHandler()
			{
				@SuppressWarnings( "unqualified-field-access" )
				@Override
				public void elementStarted( final StartElement _startElement, final Map<QName, ElementListener> _handlers )
				{
					stringBuilder.append( "\t" );
				}
			} );
		}

		public void elementEnded( final EndElement _endElement )
		{
			CellParser.this.tableCell.paragraphs.add( this.stringBuilder.toString() );
			this.stringBuilder = null;
		}

		public void process( final XMLEvent _event )
		{
			if (_event.isCharacters()) {
				final String data = _event.asCharacters().getData();
				this.stringBuilder.append( data );
			}
		}
	}
}
