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
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;

import org.formulacompiler.compiler.internal.LocalDate;
import org.formulacompiler.runtime.internal.RuntimeDouble_v2;
import org.formulacompiler.spreadsheet.internal.CellRefFormat;
import org.formulacompiler.spreadsheet.internal.CellWithConstant;
import org.formulacompiler.spreadsheet.internal.CellWithLazilyParsedExpression;
import org.formulacompiler.spreadsheet.internal.RowImpl;
import org.formulacompiler.spreadsheet.internal.odf.ValueTypes;
import org.formulacompiler.spreadsheet.internal.odf.XMLConstants;
import org.formulacompiler.spreadsheet.internal.odf.xml.DataTypeUtil;
import org.formulacompiler.spreadsheet.internal.parser.LazySpreadsheetExpressionParser;

/**
 * @author Vladimir Korenev
 */
class CellParser extends ElementParser
{
	private final RowImpl row;
	private TableCell tableCell;

	public CellParser( RowImpl _row )
	{
		this.row = _row;
	}

	@Override
	protected void elementStarted( final StartElement _startElement )
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
		addElementParser( XMLConstants.Text.P, new ElementParser()
		{
			StringBuilder stringBuilder;

			@Override
			protected void elementStarted( final StartElement _startElement )
			{
				this.stringBuilder = new StringBuilder();
				addElementParser( XMLConstants.Text.S, new ElementParser()
				{
					@SuppressWarnings( "unqualified-field-access" )
					@Override
					protected void elementStarted( final StartElement _startElement )
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
				addElementParser( XMLConstants.Text.TAB, new ElementParser()
				{
					@SuppressWarnings( "unqualified-field-access" )
					@Override
					protected void elementStarted( final StartElement _startElement )
					{
						stringBuilder.append( "\t" );
					}
				} );
			}

			@Override
			protected void elementEnded( final EndElement _endElement )
			{
				CellParser.this.tableCell.paragraphs.add( this.stringBuilder.toString() );
				this.stringBuilder = null;
			}

			@Override
			protected void processCharacters( final Characters _characters )
			{
				final String data = _characters.getData();
				this.stringBuilder.append( data );
			}
		} );
		addElementParser( XMLConstants.Office.ANNOTATION, new ElementParser()
		{
			// Skip annotations
		} );
	}

	@Override
	protected void elementEnded( final EndElement _endElement )
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
			final String formula = this.tableCell.formula;
			if (formula != null) {
				final String expression = formula.startsWith( "oooc:=" ) ? formula.substring( 6 ) : formula;
				if ("\"\"".equals( expression )) {
					// Replace ="" by empty string constant.
					new CellWithConstant( this.row, "" );
				}
				else {
					final CellWithLazilyParsedExpression exprCell = new CellWithLazilyParsedExpression( this.row );
					exprCell.setExpressionParser( new LazySpreadsheetExpressionParser( exprCell, expression,
							CellRefFormat.A1_ODF ) );
				}
			}
			else {
				final String value = this.tableCell.value;
				final String valueType = this.tableCell.valueType;
				if (ValueTypes.FLOAT.equals( valueType )
						|| ValueTypes.PERCENTAGE.equals( valueType ) || ValueTypes.CURRENCY.equals( valueType )) {
					new CellWithConstant( this.row, new Double( value ) );
				}
				else if (ValueTypes.BOOLEAN.equals( valueType )) {
					final boolean booleanValue = Boolean.parseBoolean( this.tableCell.booleanValue );
					new CellWithConstant( this.row, booleanValue );
				}
				else if (ValueTypes.DATE.equals( valueType )) {
					final Date date = DataTypeUtil.dateFromXmlFormat( this.tableCell.dateValue, DataTypeUtil.GMT_TIME_ZONE );
					final double dateNum = RuntimeDouble_v2.dateToNum( date, DataTypeUtil.GMT_TIME_ZONE );
					final LocalDate localDate = new LocalDate( dateNum );
					new CellWithConstant( this.row, localDate );
				}
				else if (ValueTypes.TIME.equals( valueType )) {
					final long durationInMillis = DataTypeUtil.durationFromXmlFormat( this.tableCell.timeValue );
					final LocalDate localDate = new LocalDate( RuntimeDouble_v2.msToNum( durationInMillis ) );
					new CellWithConstant( this.row, localDate );
				}
				else {
					final String stringValue;
					if (value != null) {
						stringValue = value;
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
						new CellWithConstant( this.row, stringValue );
					}
					else {
						this.row.getCellList().add( null );
					}
				}
			}
		}
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
}
