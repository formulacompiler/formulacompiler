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

import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileFilter;

import sej.examples.interactive.controller.MainWindowController;
import sej.spreadsheet.SpreadsheetException;

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
	@SuppressWarnings("unqualified-field-access")
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
	@SuppressWarnings("unqualified-field-access")
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
	@SuppressWarnings("unqualified-field-access")
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
	@SuppressWarnings("unqualified-field-access")
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
					fc.setFileFilter( new FileFilter() {

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
						
					});
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
