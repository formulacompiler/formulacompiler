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
package org.formulacompiler.spreadsheet;


/**
 * Utility interface that supports the creation of cell names from other cells' values.
 * 
 * @author peo
 */
public interface SpreadsheetNameCreator
{
	
	
	/**
	 * Configuration data for new instances of {@link SpreadsheetNameCreator}.
	 * 
	 * @author peo
	 */
	public static class Config
	{
		
		/**
		 * The sheet of the spreadsheet representation in which to name cells.
		 */
		public Spreadsheet.Sheet sheet;

		/**
		 * Validates the configuration.
		 * @throws IllegalArgumentException
		 */
		public void validate()
		{
			if (this.sheet == null) throw new IllegalArgumentException( "sheet is null" );
		}
		
	}


	/**
	 * Creates cell names from row titles. A row title is the constant string value of the leftmost
	 * cell of a row. This value is given as a cell name to the cell just to the right of it. The
	 * method only processes rows which have two leftmost cells and the leftmost one of them holds a
	 * constant string value.
	 */
	public void createCellNamesFromRowTitles();


	/**
	 * Factory interface for {@link org.formulacompiler.runtime.ImplementationLocator#getInstance(Class)}.
	 */
	public static interface Factory
	{
		/**
		 * Factory method.
		 */
		SpreadsheetNameCreator newInstance( Config _config );
	}

}
