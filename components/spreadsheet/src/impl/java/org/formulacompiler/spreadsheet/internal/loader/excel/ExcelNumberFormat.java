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
package org.formulacompiler.spreadsheet.internal.loader.excel;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

import org.formulacompiler.runtime.New;


public final class ExcelNumberFormat
{
	private static Map<String, NumberFormat> standardFormats = New.newMap();


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
