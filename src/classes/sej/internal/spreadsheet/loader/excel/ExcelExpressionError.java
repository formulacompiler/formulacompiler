/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
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
package sej.internal.spreadsheet.loader.excel;

public final class ExcelExpressionError extends ExcelLoaderError
{

	private static String addPositionInfoTo( String _message, String _source, int _atPosition )
	{
		final String sourceBeforeError = _source.substring( 0, _atPosition );
		final String sourceAfterError = _source.substring( _atPosition );

		StringBuilder result = new StringBuilder( _message );
		result.append( " in expression " ).append( sourceBeforeError ).append( " <<? " ).append( sourceAfterError ).append( "; error location indicated by <<?." );
		return result.toString();
	}


	public ExcelExpressionError(Exception _originalError, String _source, int _atPosition)
	{
		super( addPositionInfoTo( _originalError.getMessage(), _source, _atPosition ), _originalError );
	}

	public ExcelExpressionError(String _message, String _source, int _atPosition)
	{
		super( addPositionInfoTo( _message, _source, _atPosition ) );
	}

}
