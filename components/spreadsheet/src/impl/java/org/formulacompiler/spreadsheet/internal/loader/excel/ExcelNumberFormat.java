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

package org.formulacompiler.spreadsheet.internal.loader.excel;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

import org.formulacompiler.runtime.New;


public final class ExcelNumberFormat
{
	private static Map<String, NumberFormat> standardFormats = New.map();


	static {
		standardFormats.put( "Percent", new DecimalFormat( "0%" ) );
	}


	public static NumberFormat getNumberFormatForExcelFormat( String _excelFormatString )
	{
		NumberFormat result = standardFormats.get( _excelFormatString );
		if (null == result) {

			/*
			 * Copied and adapated from jxl.biff.FormatRecord.getNumberFormat
			 */
			String fs = _excelFormatString;

			// Replace the Excel formatting characters with java equivalents
			fs = fs.replace( "E+", "E" );
			fs = fs.replace( "_)", "" );
			fs = fs.replace( "_", "" );
			fs = fs.replace( "[Red]", "" );
			fs = fs.replace( "\\", "" );

			int posOfFirstSemicolon = fs.indexOf( ';' );
			if (posOfFirstSemicolon >= 0) {
				int posOfSecondSemicolon = fs.indexOf( ';', posOfFirstSemicolon + 1 );
				if (posOfSecondSemicolon >= 0) {
					fs = fs.substring( 0, posOfSecondSemicolon - 1 );
				}
			}

			result = new DecimalFormat( fs );
		}
		return result;
	}
}
