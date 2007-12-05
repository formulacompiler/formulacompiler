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

import org.formulacompiler.spreadsheet.internal.RowImpl;
import org.formulacompiler.spreadsheet.internal.SheetImpl;

/**
 * @author Vladimir Korenev
 */
class RowParser extends ElementParser
{
	private final SheetImpl sheet;

	public RowParser( SheetImpl _sheet )
	{
		this.sheet = _sheet;
	}

	@Override
	protected void elementStarted( final StartElement _startElement )
	{
		final int numberRowsRepeated;
		{
			final Attribute attribute = _startElement.getAttributeByName( XMLConstants.Table.NUMBER_ROWS_REPEATED );
			if (attribute != null) {
				numberRowsRepeated = Integer.parseInt( attribute.getValue() );
			}
			else {
				numberRowsRepeated = 1;
			}
		}
		final RowImpl row = createRow( numberRowsRepeated );
		final CellParser cellParser = new CellParser( row );
		addElementParser( XMLConstants.Table.TABLE_CELL, cellParser );
		addElementParser( XMLConstants.Table.COVERED_TABLE_CELL, cellParser );
	}

	private RowImpl createRow( int _numberRowsRepeated )
	{
		final RowImpl row = new RowImpl( this.sheet );
		for (int i = 1; i < _numberRowsRepeated; i++) {
			this.sheet.getRowList().add( row );
		}
		return row;
	}
}
