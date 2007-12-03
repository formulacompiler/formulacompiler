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

import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.formulacompiler.compiler.internal.expressions.parser.ExpressionParser;
import org.formulacompiler.compiler.internal.expressions.parser.GeneratedExpressionParserConstants;
import org.formulacompiler.spreadsheet.internal.Reference;
import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;
import org.formulacompiler.spreadsheet.internal.parser.SpreadsheetExpressionParserA1ODF;

/**
 * @author Vladimir Korenev
 */
class NamedRangeParser extends ElementParser
{
	private final SpreadsheetImpl spreadsheet;

	public NamedRangeParser( SpreadsheetImpl _spreadsheet )
	{
		this.spreadsheet = _spreadsheet;
	}

	@Override
	protected void elementStarted( final StartElement _startElement )
	{
		final Attribute nameAttribute = _startElement.getAttributeByName( XMLConstants.Table.NAME );
		final String name = nameAttribute.getValue();

		final Attribute rangeAttribute = _startElement.getAttributeByName( XMLConstants.Table.CELL_RANGE_ADDRESS );
		final String cellRangeAddress = rangeAttribute.getValue();
		final ExpressionParser parser = new SpreadsheetExpressionParserA1ODF( cellRangeAddress, this.spreadsheet );
		parser.token_source.SwitchTo( GeneratedExpressionParserConstants.IN_ODF_CELL_REF );
		try {
			final Reference reference = (Reference) parser.rangeOrCellRefODF();
			this.spreadsheet.addToNameMap( name, reference );
		}
		catch (org.formulacompiler.compiler.internal.expressions.parser.ParseException e) {
			throw new RuntimeException( e );
		}
	}
}
