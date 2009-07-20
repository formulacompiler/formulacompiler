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
	@SuppressWarnings( "unqualified-field-access" )
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
