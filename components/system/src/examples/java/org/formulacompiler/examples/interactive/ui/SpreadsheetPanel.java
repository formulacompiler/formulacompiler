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

import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileFilter;

import org.formulacompiler.examples.interactive.controller.MainWindowController;
import org.formulacompiler.spreadsheet.SpreadsheetException;


public class SpreadsheetPanel extends BottomButtonsPanel
{
	private MainWindowController controller;

	private JLabel captionLabel;
	private JScrollPane scrollPane;
	private JTable table;
	private JButton openFileButton;


	public MainWindowController getController()
	{
		return this.controller;
	}


	public void setController( MainWindowController _controller )
	{
		this.controller = _controller;
		if (null != _controller) {
			getTable().setModel( new SpreadsheetTableModel( _controller.getSpreadsheetModel() ) );
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
		this.add( getScrollPane(), java.awt.BorderLayout.CENTER );
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
			captionLabel.setText( "Spreadsheet Cells" );
		}
		return captionLabel;
	}


	/**
	 * This method initializes spreadsheetScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	@SuppressWarnings( "unqualified-field-access" )
	private JScrollPane getScrollPane()
	{
		if (scrollPane == null) {
			scrollPane = new JScrollPane();
			scrollPane.setViewportView( getTable() );
		}
		return scrollPane;
	}


	/**
	 * This method initializes spreadsheetTable
	 * 
	 * @return javax.swing.JTable
	 */
	@SuppressWarnings( "unqualified-field-access" )
	JTable getTable()
	{
		if (table == null) {
			table = new JTable();
			table.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
			table.setCellSelectionEnabled( true );
		}
		return table;
	}


	@Override
	protected void initializeButtonsPanel()
	{
		getButtonsPanel().add( getOpenFileButton() );
	}


	/**
	 * This method initializes openSpreadsheetFileButton
	 * 
	 * @return javax.swing.JButton
	 */
	@SuppressWarnings( "unqualified-field-access" )
	private JButton getOpenFileButton()
	{
		if (openFileButton == null) {
			openFileButton = new JButton();
			openFileButton.setText( "Open spreadsheet file..." );
			openFileButton.addActionListener( new java.awt.event.ActionListener()
			{

				public void actionPerformed( java.awt.event.ActionEvent e )
				{
					final JFileChooser fc = new JFileChooser();
					fc.setDialogTitle( "Open Spreadsheet File" );
					fc.setFileFilter( new FileFilter()
					{

						@Override
						public boolean accept( File _f )
						{
							return _f.isDirectory() || _f.getName().toLowerCase().endsWith( ".xls" );
						}

						@Override
						public String getDescription()
						{
							return "Excel Files (*.xls)";
						}

					} );
					final int retval = fc.showOpenDialog( SpreadsheetPanel.this );
					if (retval == JFileChooser.APPROVE_OPTION) {
						final File file = fc.getSelectedFile();
						try {
							getController().loadSpreadsheetFrom( file.getAbsolutePath() );
						}
						catch (SpreadsheetException ex) {
							throw new RuntimeException( ex );
						}
					}
				}

			} );
		}
		return openFileButton;
	}


}
