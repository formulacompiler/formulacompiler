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
package sej;

import java.util.Date;

public interface SpreadsheetBuilder
{
	public Spreadsheet getSpreadsheet();
	public void newPage();
	public void newRow();
	public void newCell( ExprNode _expr );
	public void newCell( Number _const );
	public void newCell( String _const );
	public void newCell( Date _const );
	public void newCell( boolean _const );
	public void nameCell( String _string );
	public CellRef currentCell();
	public ExprNode ref( CellRef _cell );
	public ExprNode cst( Object _constantValue );
	public ExprNode op( Operator _op, ExprNode... _args );
	public ExprNode fun( Function _fun, ExprNode... _args );
	public ExprNode agg( Aggregator _fun, ExprNode... _args );
	public ExprNode iff( ExprNode _test, ExprNode _ifTrue, ExprNode _ifFalse );
	
	public static interface ExprNode
	{
		// opaque
	}
	
	public static interface CellRef 
	{
		// opaque
	}
}
