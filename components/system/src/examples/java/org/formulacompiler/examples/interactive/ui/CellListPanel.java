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


	public CellListPanel(JTable _spreadsheetTable, String _caption)
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
	@SuppressWarnings("unqualified-field-access")
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
	@SuppressWarnings("unqualified-field-access") JTable getTable()
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
	@SuppressWarnings("unqualified-field-access")
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


	@SuppressWarnings("unqualified-field-access")
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
