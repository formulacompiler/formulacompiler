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

package org.formulacompiler.spreadsheet.internal.compiler;

import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ConstantExpressionCellListenerSupport;
import org.formulacompiler.compiler.internal.model.SectionModel;
import org.formulacompiler.runtime.internal.spreadsheet.CellInfoImpl;
import org.formulacompiler.runtime.internal.spreadsheet.SectionInfoImpl;
import org.formulacompiler.runtime.spreadsheet.CellAddress;
import org.formulacompiler.runtime.spreadsheet.CellInfo;
import org.formulacompiler.runtime.spreadsheet.RangeAddress;
import org.formulacompiler.runtime.spreadsheet.SectionInfo;
import org.formulacompiler.runtime.spreadsheet.SpreadsheetCellComputationEvent;
import org.formulacompiler.spreadsheet.ConstantExpressionOptimizationListener;


public class ConstantExpressionCellListenerSupportImpl implements ConstantExpressionCellListenerSupport
{
	private final ConstantExpressionOptimizationListener listener;

	public ConstantExpressionCellListenerSupportImpl( ConstantExpressionOptimizationListener _listener )
	{
		this.listener = _listener;
	}

	public void constantExpressionEliminated( final CellModel _cell, final Object _value )
	{
		final Object cellSource = _cell.getSource();
		if (cellSource instanceof CellAddress) {
			final CellAddress cellAddress = (CellAddress) cellSource;
			final CellInfo cellInfo = new CellInfoImpl( cellAddress, _cell.getName() );
			final SectionModel sectionModel = _cell.getSection();
			final Object sectionSource = sectionModel.getSource();
			final RangeAddress range = sectionSource instanceof RangeAddress ? (RangeAddress) sectionSource : null;
			final SectionInfo sectionInfo = new SectionInfoImpl( sectionModel.getName(), range, -1 );
			final boolean input = _cell.isInput();
			final boolean output = _cell.isOutput();
			final SpreadsheetCellComputationEvent event = new SpreadsheetCellComputationEvent( cellInfo, sectionInfo,
					_value, input, output );
			this.listener.constantCellCalculated( event );
		}
	}

}
