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

package org.formulacompiler.examples.interactive.ui;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;

import org.formulacompiler.examples.interactive.controller.MainWindowController;
import org.formulacompiler.examples.interactive.controller.MainWindowController.CellListModel;


public class CellListPanel extends BottomButtonsPanel
{
	final JTable spreadsheetTable;
	private MainWindowController controller;
	CellListModel model;

	private JLabel captionLabel;
	private JTable table;
	private JButton addCellButton;
	private JButton removeCellButton;


	public CellListPanel( JTable _spreadsheetTable, String _caption )
	{
		super();
		this.spreadsheetTable = _spreadsheetTable;
		getCaptionLabel().setText( _caption );
	}


	public MainWindowController getController()
	{
		return this.controller;
	}


	public CellListModel getModel()
	{
		return this.model;
	}


	public void setControllerAndModel( MainWindowController _controller, CellListModel _model )
	{
		this.controller = _controller;
		this.model = _model;
		if (null != _model) {
			getTable().setModel( new CellListTableModel( _model ) );
		}
		else {
			getTable().setModel( null );
		}
	}


	@Override
	protected void initialize()
	{
		super.initialize();
		this.add( getCaptionLabel(), java.awt.BorderLayout.NORTH );
		this.add( getTable(), java.awt.BorderLayout.CENTER );
	}


	/**
	 * This method initializes jLabel
	 * 
	 * @return javax.swing.JLabel
	 */
	@SuppressWarnings( "unqualified-field-access" )
	private JLabel getCaptionLabel()
	{
		if (captionLabel == null) {
			captionLabel = new JLabel();
		}
		return captionLabel;
	}


	/**
	 * This method initializes inputTable
	 * 
	 * @return javax.swing.JTable
	 */
	@SuppressWarnings( "unqualified-field-access" )
	JTable getTable()
	{
		if (table == null) {
			table = new JTable();
		}
		return table;
	}


	@Override
	protected void initializeButtonsPanel()
	{
		getButtonsPanel().add( getAddCellButton() );
		getButtonsPanel().add( getRemoveCellButton() );
	}


	/**
	 * This method initializes addToInputsButton
	 * 
	 * @return javax.swing.JButton
	 */
	@SuppressWarnings( "unqualified-field-access" )
	private JButton getAddCellButton()
	{
		if (addCellButton == null) {
			addCellButton = new JButton();
			addCellButton.setText( "Add cell" );
			addCellButton.addActionListener( new java.awt.event.ActionListener()
			{
				public void actionPerformed( java.awt.event.ActionEvent e )
				{
					final int[] selectedColumns = spreadsheetTable.getSelectedColumns();
					final int[] selectedRows = spreadsheetTable.getSelectedRows();
					for (int iRow : selectedRows) {
						for (int iCol : selectedColumns) {
							if (null != model) model.add( iRow, iCol );
						}
					}
				}
			} );
		}
		return addCellButton;
	}


	@SuppressWarnings( "unqualified-field-access" )
	private JButton getRemoveCellButton()
	{
		if (removeCellButton == null) {
			removeCellButton = new JButton();
			removeCellButton.setText( "Remove cell" );
			removeCellButton.addActionListener( new java.awt.event.ActionListener()
			{
				public void actionPerformed( java.awt.event.ActionEvent e )
				{
					final int[] selectedRows = getTable().getSelectedRows();
					for (int iRow : selectedRows) {
						if (null != model) model.remove( iRow );
					}
				}
			} );
		}
		return removeCellButton;
	}


}
