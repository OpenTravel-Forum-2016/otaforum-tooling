/**
 * Copyright (C) 2016 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opentravel.otm.forum2016;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Simple Swing frame that allows the user to choose their context directory.
 * 
 * @author S. Livezey
 */
public class ContextChooser extends JFrame {
	
	private static final long serialVersionUID = -4104138100403822821L;
	
	private JComboBox<String> contextCombo;
	private JButton okButton, cancelButton;
	
	private File projectFolder;
	private String selectedContext;
	private boolean okPressed;
	
	/**
	 * Constructor that specifies the project folder from which the user must select
	 * a context.
	 * 
	 * @param projectFolder  the root directory of the mock content workspace
	 * @throws HeadlessException
	 */
	public ContextChooser(File projectFolder) throws HeadlessException {
		super( "Choose Context" );
		this.projectFolder = projectFolder;
		init();
		
		this.addWindowListener( new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				synchronized (ContextChooser.this) {
					ContextChooser.this.notifyAll();
				}
			}
		});
	}
	
	/**
	 * Returns the value of the 'okPressed' field.
	 *
	 * @return boolean
	 */
	public boolean isOkPressed() {
		return okPressed;
	}
	
	/**
	 * Returns the context folder name that was selected by the user, if and
	 * only if the 'Ok' button was pressed.
	 *
	 * @return String
	 */
	public String getSelectedContext() {
		return okPressed ? selectedContext : null;
	}
	
	/**
	 * Event handler called when the user changes the selection of the context
	 * combo box.
	 */
	private void handleContextChanged() {
		selectedContext = (String) contextCombo.getSelectedItem();
		okButton.setEnabled( (selectedContext != null) );
	}

	/**
	 * Event handler called when the 'Ok' button has been pressed.
	 */
	private void handleOkPressed() {
		okPressed = true;
		dispatchEvent( new WindowEvent( this, WindowEvent.WINDOW_CLOSING ) );
	}

	/**
	 * Event handler called when the 'Cancel' button has been pressed.
	 */
	private void handleCancelPressed() {
		dispatchEvent( new WindowEvent( this, WindowEvent.WINDOW_CLOSING ) );
	}

	/**
	 * Initializes the visible controls of the frame.
	 */
	private void init() {
		Container contentPane = getContentPane();
		JPanel buttonPanel = new JPanel();
		GridBagConstraints gbc;
		
		contentPane.setLayout(new GridBagLayout());
		contentPane.add( new JLabel( "Please select your context folder from the list below:"),
				newGBC( 0, 0, 2, GridBagConstraints.LINE_START ) );
		
		contextCombo = new JComboBox<>();
		contextCombo.setModel( new DefaultComboBoxModel<>( getContextOptions() ));
		
		contentPane.add( new JLabel( "Context Folder:"), newGBC( 0, 1, 1, GridBagConstraints.LINE_START ) );
		gbc = newGBC( 1, 1, 1, GridBagConstraints.LINE_START );
		gbc.fill = GridBagConstraints.HORIZONTAL;
		contentPane.add( contextCombo, gbc );

		gbc = newGBC( 0, 2, 2, GridBagConstraints.CENTER );
		gbc.fill = GridBagConstraints.HORIZONTAL;
		contentPane.add( buttonPanel, gbc );
		
		okButton = new JButton( "Ok" );
		cancelButton = new JButton( "Cancel" );
		
		buttonPanel.setLayout( new FlowLayout( FlowLayout.RIGHT, 5, 5 ) );
		buttonPanel.add( okButton, gbc );
		buttonPanel.add( cancelButton, gbc );
		okButton.setEnabled( false );
		
		contextCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleContextChanged();
			}
		});
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleOkPressed();
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleCancelPressed();
			}
		});
		
		pack();
		setVisible( true );
		setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	}
	
	/**
	 * Convenience method for creating common <code>GridBagConstraints</code>.
	 */
	private GridBagConstraints newGBC(int gridX, int gridY, int gridWidth,
			int anchor) {
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = gridX;
		gbc.gridy = gridY;
		gbc.gridwidth = gridWidth;
		gbc.anchor = anchor;
		gbc.insets = new Insets( 5, 5, 5, 5 );
		return gbc;
	}
	
	/**
	 * Returns the array of selectable options for the context combo box.
	 * 
	 * @return String[]
	 */
	private String[] getContextOptions() {
		List<File> contextFolders = OTMProjectUtils.getContextFolders( projectFolder );
		List<String> options = new ArrayList<>();
		
		options.add( null );
		
		for (File contextFolder : contextFolders) {
			options.add( contextFolder.getName() );
		}
		return options.toArray( new String[ options.size() ] );
	}
	
}
