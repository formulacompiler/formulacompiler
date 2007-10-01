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

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JPanel;

public abstract class BottomButtonsPanel extends JPanel
{
	private JPanel buttonsPanel;
	
	
	public BottomButtonsPanel()
	{
		super();
		initialize();
	}


	protected void initialize()
	{
		this.setLayout( new BorderLayout() );
		this.add( getButtonsPanel(), java.awt.BorderLayout.SOUTH );
	}


	/**
	 * This method initializes spreadsheetButtonsPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	@SuppressWarnings("unqualified-field-access")
	protected JPanel getButtonsPanel()
	{
		if (buttonsPanel == null) {
			FlowLayout flowLayout1 = new FlowLayout();
			flowLayout1.setAlignment( java.awt.FlowLayout.LEFT );
			flowLayout1.setVgap( 5 );
			flowLayout1.setHgap( 5 );
			buttonsPanel = new JPanel();
			buttonsPanel.setLayout( flowLayout1 );
			initializeButtonsPanel();
		}
		return buttonsPanel;
	}


	protected abstract void initializeButtonsPanel();


}
