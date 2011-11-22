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

package org.formulacompiler.compiler.internal.bytecode;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.NumericTypeImpl;
import org.formulacompiler.compiler.internal.model.CellModel;


final class CellComputation
{
	private final SectionCompiler section;
	private final CellModel cell;
	private final String methodName;


	CellComputation( SectionCompiler _section, CellModel _cell )
	{
		super();
		this.section = _section;
		this.cell = _cell;
		if (_section.engineCompiler().getCompileToReadableCode()) {
			this.methodName = _section.newGetterName( cellNameToIdent( _cell.getShortName() ) );
		}
		else {
			this.methodName = _section.newGetterName();
		}
		_section.addCellComputation( _cell, this );
	}

	private static String cellNameToIdent( String _name )
	{
		String result = _name;
		final int posOfDot = result.indexOf( '.' );
		if (posOfDot >= 0) {
			result = result.substring( posOfDot + 1 );
		}
		if (result.endsWith( "()" )) {
			result = result.substring( 0, result.length() - 2 );
		}
		// Replace all non-word characters by underscores.
		result = result.replaceAll( "\\W", "_" );
		return result;
	}


	CellModel getCell()
	{
		return this.cell;
	}

	SectionCompiler getSection()
	{
		return this.section;
	}

	String getMethodName()
	{
		return this.methodName;
	}


	void validate() throws CompilerException
	{
		validateInputType();
		validateOutputTypes();
	}

	private void validateInputType() throws CompilerException
	{
		if (this.cell.isInput()) {
			((NumericTypeImpl) getSection().engineCompiler().getNumericType()).validateReturnTypeForCell( this.cell
					.getCallChainToCall().getMethod() );
		}
	}

	private void validateOutputTypes() throws CompilerException
	{
		for (CallFrame frame : this.cell.getCallsToImplement()) {
			if (frame.getHead() != frame) {
				throw new CompilerException.UnsupportedDataType( "The output method " + frame + " cannot be chained." );
			}
			((NumericTypeImpl) getSection().engineCompiler().getNumericType()).validateReturnTypeForCell( frame
					.getMethod() );
		}
	}

	void compile() throws CompilerException
	{
		new CellMethodCompiler( this ).compile();
	}

}
