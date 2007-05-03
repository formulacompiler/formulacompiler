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
package sej.spreadsheet;


/**
 * Represents the association of spreadsheet cells and ranges to methods of the application's input
 * and output types. Used as input to the spreadsheet compiler.
 * 
 * @author peo
 * 
 * @see SpreadsheetBinder#getBinding()
 * @see SpreadsheetCompiler.Config#binding
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
