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

import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.formulacompiler.compiler.internal.expressions.parser.ExpressionParser;
import org.formulacompiler.compiler.internal.expressions.parser.GeneratedExpressionParserConstants;
import org.formulacompiler.spreadsheet.internal.CellRange;
import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;
import org.formulacompiler.spreadsheet.internal.odf.XMLConstants;
import org.formulacompiler.spreadsheet.internal.odf.xml.stream.ElementHandler;
import org.formulacompiler.spreadsheet.internal.odf.xml.stream.ElementListener;
import org.formulacompiler.spreadsheet.internal.parser.SpreadsheetExpressionParserA1ODF;

/**
 * @author Vladimir Korenev
 */
class NamedRangeParser extends ElementHandler
{
	private final SpreadsheetImpl spreadsheet;

	public NamedRangeParser( SpreadsheetImpl _spreadsheet )
	{
		this.spreadsheet = _spreadsheet;
	}

	@Override
	public void elementStarted( final StartElement _startElement, final Map<QName, ElementListener> _handlers )
	{
		final Attribute nameAttribute = _startElement.getAttributeByName( XMLConstants.Table.NAME );
		final String name = nameAttribute.getValue();

		final Attribute rangeAttribute = _startElement.getAttributeByName( XMLConstants.Table.CELL_RANGE_ADDRESS );
		final String cellRangeAddress = rangeAttribute.getValue();
		final ExpressionParser parser = new SpreadsheetExpressionParserA1ODF( cellRangeAddress, this.spreadsheet );
		parser.token_source.SwitchTo( GeneratedExpressionParserConstants.IN_ODF_CELL_REF );
		try {
			final CellRange cellRange = (CellRange) parser.rangeOrCellRefODF();
			this.spreadsheet.defineModelRangeName( name, cellRange );
		}
		catch (org.formulacompiler.compiler.internal.expressions.parser.ParseException e) {
			throw new RuntimeException( e );
		}
	}
}
