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
package sej.examples.interactive.ui;

import java.util.Collection;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import sej.examples.interactive.controller.ControllerListener;
import sej.runtime.New;

public abstract class AbstractTableModel implements TableModel
{
	protected final Collection<TableModelListener> listeners = New.newCollection();


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
