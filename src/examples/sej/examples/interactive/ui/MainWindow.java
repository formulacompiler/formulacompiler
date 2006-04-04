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

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import sej.ModelError;
import sej.examples.interactive.controller.MainWindowController;


public class MainWindow extends JFrame
{
	private MainWindowController controller = null;

	private JPanel jContentPane = null;
	private JSplitPane dataSplitPane = null;
	private SpreadsheetPanel spreadsheetPanel = null;
	private JSplitPane computationSplitPane = null;
	private CellListPanel inputsPanel = null;
	private CellListPanel outputsPanel = null;
	private JButton computeNowButton = null;


	/**
	 * This is the default constructor
	 */
	public MainWindow()
	{
		super();
		initialize();
	}


	public MainWindowController getController()
	{
		return this.controller;
	}


	public void setController( MainWindowController _controller )
	{
		this.controller = _controller;
		getSpreadsheetPanel().setController( _controller );
		if (null != _controller) {
			getInputsPanel().setControllerAndModel( _controller, _controller.getInputs() );
			getOutputsPanel().setControllerAndModel( _controller, _controller.getOutputs() );
		}
		else {
			getInputsPanel().setControllerAndModel( null, null );
			getOutputsPanel().setControllerAndModel( null, null );
		}
	}


	private void initialize()
	{
		this.setSize( 640, 480 );
		this.setContentPane( getJContentPane() );
		this.setTitle( "Spreadsheet Engine for Java (SEJ) Interactive Demo" );
		this.addWindowListener( new java.awt.event.WindowAdapter()
		{
			@Override
			public void windowClosing( java.awt.event.WindowEvent e )
			{
				System.exit( 0 );
			}
		} );
	}


	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	@SuppressWarnings("unqualified-field-access")
	private JPanel getJContentPane()
	{
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout( new BorderLayout() );
			jContentPane.add( getDataSplitPane(), java.awt.BorderLayout.CENTER );
		}
		return jContentPane;
	}


	/**
	 * This method initializes dataSplitPane
	 * 
	 * @return javax.swing.JSplitPane
	 */
	@SuppressWarnings("unqualified-field-access")
	private JSplitPane getDataSplitPane()
	{
		if (dataSplitPane == null) {
			dataSplitPane = new JSplitPane();
			dataSplitPane.setOrientation( JSplitPane.VERTICAL_SPLIT );
			dataSplitPane.setContinuousLayout( true );
			dataSplitPane.setResizeWeight( 0.5D );
			dataSplitPane.setDividerSize( 5 );
			dataSplitPane.setTopComponent( getSpreadsheetPanel() );
			dataSplitPane.setBottomComponent( getComputationSplitPane() );
		}
		return dataSplitPane;
	}


	/**
	 * This method initializes spreadsheetPanel
	 * 
	 * @return SpreadsheetPanel
	 */
	@SuppressWarnings("unqualified-field-access")
	private SpreadsheetPanel getSpreadsheetPanel()
	{
		if (spreadsheetPanel == null) {
			spreadsheetPanel = new SpreadsheetPanel();
		}
		return spreadsheetPanel;
	}


	/**
	 * This method initializes computationSplitPane
	 * 
	 * @return javax.swing.JSplitPane
	 */
	@SuppressWarnings("unqualified-field-access")
	private JSplitPane getComputationSplitPane()
	{
		if (computationSplitPane == null) {
			computationSplitPane = new JSplitPane();
			computationSplitPane.setResizeWeight( 0.5D );
			computationSplitPane.setDividerSize( 5 );
			computationSplitPane.setLeftComponent( getInputsPanel() );
			computationSplitPane.setRightComponent( getOutputsPanel() );
			computationSplitPane.setContinuousLayout( true );
		}
		return computationSplitPane;
	}


	/**
	 * This method initializes inputsPanel
	 * 
	 * @return CellListPanel
	 */
	@SuppressWarnings("unqualified-field-access")
	private CellListPanel getInputsPanel()
	{
		if (inputsPanel == null) {
			inputsPanel = new CellListPanel( getSpreadsheetPanel().getTable(), "Input cells" );
		}
		return inputsPanel;
	}


	/**
	 * This method initializes outputsPanel
	 * 
	 * @return CellListPanel
	 */
	@SuppressWarnings("unqualified-field-access")
	private CellListPanel getOutputsPanel()
	{
		if (outputsPanel == null) {
			outputsPanel = new CellListPanel( getSpreadsheetPanel().getTable(), "Output cells" );
			outputsPanel.getButtonsPanel().add( getComputeNowButton() );
		}
		return outputsPanel;
	}


	/**
	 * This method initializes computeNowButton
	 * 
	 * @return javax.swing.JButton
	 */
	@SuppressWarnings("unqualified-field-access")
	private JButton getComputeNowButton()
	{
		if (computeNowButton == null) {
			computeNowButton = new JButton();
			computeNowButton.setText( "Compute now" );
			computeNowButton.addActionListener( new java.awt.event.ActionListener()
			{
				public void actionPerformed( java.awt.event.ActionEvent e )
				{
					try {
						getController().computeNow();
					}
					catch (ModelError ex) {
						JOptionPane.showMessageDialog( getRootPane(), ex.getMessage() );
					}
					catch (NoSuchMethodException ex) {
						JOptionPane.showMessageDialog( getRootPane(), ex.getMessage() );
					}
				}
			} );
		}
		return computeNowButton;
	}


}
