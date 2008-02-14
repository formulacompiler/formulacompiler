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

package org.formulacompiler.examples.interactive.ui;

import java.util.Collection;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.formulacompiler.examples.interactive.controller.ControllerListener;
import org.formulacompiler.runtime.New;


public abstract class AbstractTableModel implements TableModel
{
	protected final Collection<TableModelListener> listeners = New.collection();


	public void addTableModelListener( TableModelListener _l )
	{
		this.listeners.add( _l );
	}


	public void removeTableModelListener( TableModelListener _l )
	{
		this.listeners.remove( _l );
	}


	public void listenToController( Collection<ControllerListener> _listeners )
	{
		_listeners.add( new ControllerListener()
		{

			public void dataChanged()
			{
				AbstractTableModel.this.dataChanged();
			}

		} );
	}


	public void dataChanged()
	{
		for (TableModelListener l : this.listeners) {
			l.tableChanged( null );
		}

	}


}
