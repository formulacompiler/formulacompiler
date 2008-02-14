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

package org.formulacompiler.spreadsheet;


/**
 * Represents the association of spreadsheet cells and ranges to methods of the application's input
 * and output types. Used as input to the spreadsheet compiler.
 * 
 * @author peo
 * 
 * @see SpreadsheetBinder#getBinding()
 * @see SpreadsheetToEngineCompiler.Config#binding
 */
public interface SpreadsheetBinding
{

	/**
	 * Returns the spreadsheet whose cells are bound by this instance.
	 * 
	 * @see SpreadsheetBinder.Config#spreadsheet
	 */
	Spreadsheet getSpreadsheet();


	/**
	 * Returns the type to which input cells and ranges were bound.
	 * 
	 * @see SpreadsheetBinder.Config#inputClass
	 */
	Class getInputClass();


	/**
	 * Returns the type to which output cells and ranges were bound.
	 * 
	 * @see SpreadsheetBinder.Config#outputClass
	 */
	Class getOutputClass();


}
