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

package org.formulacompiler.spreadsheet.internal.odf.saver.copying;

import java.util.Collections;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventWriter;

import org.formulacompiler.spreadsheet.internal.odf.XMLConstants;
import org.formulacompiler.spreadsheet.internal.odf.xml.stream.CopyingParser;
import org.formulacompiler.spreadsheet.internal.odf.xml.stream.ElementListener;


/**
 * @author Vladimir Korenev
 */
public class ManifestCopyingParser extends CopyingParser
{
	@Override
	protected Map<QName, ? extends ElementListener> getListeners( final XMLEventWriter _writer )
	{
		return Collections.singletonMap( XMLConstants.Manifest.FILE_ENTRY, new FileEntryCopyingHandler( _writer ) );
	}
}
