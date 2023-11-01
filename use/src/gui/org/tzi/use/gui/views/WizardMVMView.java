/*
 * USE - UML based specification environment
 * Copyright (C) 1999-2004 Mark Richters, University of Bremen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundat	ion; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.tzi.use.gui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.tzi.use.config.Options;
import org.tzi.use.gui.main.MainWindow;
import org.tzi.use.gui.main.ModelBrowserSorting;
import org.tzi.use.gui.main.ModelBrowserSorting.SortChangeEvent;
import org.tzi.use.gui.main.ModelBrowserSorting.SortChangeListener;
import org.tzi.use.gui.main.ViewFrame;
import org.tzi.use.gui.util.ExtendedJTable;
import org.tzi.use.gui.views.diagrams.objectdiagram.NewObjectDiagram;
import org.tzi.use.gui.views.diagrams.objectdiagram.NewObjectDiagramView;
import org.tzi.use.gui.views.diagrams.objectdiagram.QualifierInputView;
import org.tzi.use.main.Session;
import org.tzi.use.parser.ocl.OCLCompiler;
import org.tzi.use.uml.mm.MAssociation;
import org.tzi.use.uml.mm.MAssociationEnd;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.mm.MClassInvariant;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.MMultiplicity;
import org.tzi.use.uml.ocl.expr.Evaluator;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.expr.MultiplicityViolationException;
import org.tzi.use.uml.ocl.value.BooleanValue;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.sys.MLink;
import org.tzi.use.uml.sys.MLinkEnd;
import org.tzi.use.uml.sys.MObject;
import org.tzi.use.uml.sys.MObjectState;
import org.tzi.use.uml.sys.MSystem;
import org.tzi.use.uml.sys.MSystemException;
import org.tzi.use.uml.sys.MSystemState;
import org.tzi.use.uml.sys.events.tags.SystemStateChangedEvent;
import org.tzi.use.uml.sys.soil.MAttributeAssignmentStatement;
import org.tzi.use.uml.sys.soil.MLinkDeletionStatement;
import org.tzi.use.uml.sys.soil.MLinkInsertionStatement;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;

/** 
 * A view for showing and changing object properties (attributes).
 *  
 * @author  Mark Richters
 */
@SuppressWarnings("serial")
public class WizardMVMView extends JPanel implements View {
//	private static final String NO_OBJECTS_AVAILABLE = "(No objects available.)";

	private static final String NAMEFRAMEMVMDIAGRAM = "MVM";
	private static final String NAMEFRAMEMVMWIZARD = "MVMWizard";
	private MainWindow fMainWindow;
	private Session fSession;
	private MSystem fSystem;
	private MObject fObject;

	//---
	private JFrame frame;
	private JPanel panel;

//	private DefaultListModel<MClass> modelClass;
	private DefaultListModel<String> modelObjects;

	private DefaultTableModel modelTabAttrs;
//	private DefaultListModel<MAssociation> modelAssocs;
//	private JTableHeader header;

	private JLabel lbClass;
	private JLabel lbObjects;
	private JLabel lbAttrs;
	private JLabel lbAssoc;
	private JLabel lbObj;
	private JLabel lbFrom;
	private JLabel lbTo;
	private JLabel lbFromClass;
	private JLabel lbToClass;
	private JLabel lbAclass;	
	private JLabel lbAobject;	
	private JLabel lbAmultiplicity;	
	private JLabel lbArole;	
//	private JLabel lbAresMultiplicity;
	private JLabel lbClassInvariants;
	private JLabel lbResClassInvariants;
	private JLabel lbCheckStructure;

	private JScrollPane scrollPaneClass;
	private JScrollPane scrollPaneObj;

	private JList<MClass> lClass;
	private JList<String> lObjects;
	private JList<MAssociation> lAssocs;
	private JList<String> lAttrs;

//	private JTable tabAttr;
//	private JScrollPane paneTabAttrs;

	private JTextField txNewObject;
	private JTextField txMultiOri;
	private JTextField txMultiDes;
	private JTextField txOriAssocRole;
	private JTextField txDesAssocRole;
	private String nomClass;
	private MClass oClass;
	private String nomObj;	
	private JComboBox<MClass> cmbClassOri;
	private JComboBox<MClass> cmbClassDes;
	private JComboBox<MObject> cmbObjectOri;
	private JComboBox<MObject> cmbObjectDes;
	private JComboBox<String> cmbMultiOri;
	private JComboBox<String> cmbMultiDes;

	private JButton btnCreateObject;
	private JButton btnNewObjectAuto;
	private JButton btnDeleteObject;
	private JButton btnSaveObject;
	private JButton btnCancelObject;
	private JButton btnInsertLinkAssoc;
	private JButton btnDeleteLink;	
	private JButton btnShowClassInvariants;	
	private JButton btnShowCheckStructure;

	private boolean bNewObj;
	private JTable fTable;
	private JScrollPane fTablePane;

	private TableModel fTableModel;

	private List<MAttribute> fAttributes;
	private String[] fValues;
	private Map<MAttribute, Value> fAttributeValueMap;
	private NewObjectDiagram odvAssoc;
	private String aMulti[] = new String[] { 
			"0", "1", "*" };
	private List<Future<EvalResult>> futures;
	private Color colorSoftGray;
	
	private JInternalFrame[] allframes;

	/**
	 * The table model.
	 */
	class TableModel extends AbstractTableModel implements SortChangeListener {
		final String[] columnNames = { "Attribute", "Value" };

		TableModel() {
			ModelBrowserSorting.getInstance().addSortChangeListener( this );
			update();
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public int getColumnCount() { 
			return 2; 
		}
		public int getRowCount() { 
			return fAttributes.size();
		}
		public Object getValueAt(int row, int col) { 
			if (col == 0 )
				return fAttributes.get(row);
			else
				return fValues[row];
		}
		public boolean isCellEditable(int row, int col) {
			return col == 1 && !fAttributes.get(row).isDerived();
		}

		public void setValueAt(Object value, int row, int col) {
			fValues[row] = value.toString();
			fireTableCellUpdated(row, col);
		}

		private void update() {
			// initialize table model
			if ( haveObject() ) {
				MObjectState objState = fObject.state(fSystem.state());
				fAttributeValueMap = objState.attributeValueMap();

				Collection<MAttribute> attributes = ModelBrowserSorting.getInstance().sortAttributes( fAttributeValueMap.keySet() );

				attributes = Collections2.filter(attributes, new Predicate<MAttribute>() {
					@Override
					public boolean apply(MAttribute input) {
						return !input.isDerived();
					}
				});

				fAttributes = Lists.newArrayList(attributes);
				fValues = new String[fAttributes.size()];
				for (int i = 0; i < fValues.length; i++) {
					fValues[i] = fAttributeValueMap.get(fAttributes.get(i)).toString();
				}
			} else {
				fAttributes = Collections.emptyList();
				fValues = new String[0];
			}
			fireTableDataChanged();
		}

		/**
		 * After the occurrence of an event the attribute list is updated.
		 */
		public void stateChanged( SortChangeEvent e ) {
			fAttributes = ModelBrowserSorting.getInstance()
					.sortAttributes( fAttributes );
			update();
		}
	}

	public WizardMVMView(MainWindow parent, Session session) {
		super(new BorderLayout());
		fMainWindow = parent;
		fSession = session;
		fSystem = session.system();
		fSystem.registerRequiresAllDerivedValues();
		fSystem.getEventBus().register(this);
		//		colorSoftGray=new Color(196,244,244);
		colorSoftGray=new Color(218,224,224);

		searchObjDiagramAssociated();

		bNewObj=false;

		frame = new JFrame("Prototipo MVM Wizard");
		frame.setSize(630, 660);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);

		panel = new JPanel();

		lAttrs = new JList<String>();
		lObjects = new JList<String>();

		frame.add(panel);

		panel.setLayout(null);

//		modelClass = new DefaultListModel<MClass>();
		modelObjects = new DefaultListModel<>();
//		modelAssocs = new DefaultListModel<MAssociation>();

		modelTabAttrs = new DefaultTableModel();
//		tabAttr = new JTable(modelTabAttrs);

		lbClass = new JLabel("Class");
		lbClass.setBounds(10, 15, 60, 25);
		panel.add(lbClass);

		lbObjects = new JLabel("Objects");
		lbObjects.setBounds(110, 15, 60, 25);
		panel.add(lbObjects);	   

		lbAttrs = new JLabel("Attributes");
		lbAttrs.setBounds(245, 15, 60, 25);
		panel.add(lbAttrs);			

		lClass = new JList<MClass>(loadListMClass());
		lClass.setBounds(10, 40, 90, 145);
		lClass.setBorder(BorderFactory.createLineBorder(Color.black));
		lClass.setSelectedIndex(0);
		oClass = lClass.getSelectedValue();
		nomClass = oClass.name();

		lClass.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				oClass = lClass.getSelectedValue();
				nomClass = oClass.name();
				lObjects.setModel(loadListObjects(nomClass));
				lObjects.setSelectedIndex(0);
				nomObj = (String) lObjects.getSelectedValue();
				selectObject( nomObj);
				txNewObject.setText(nomObj);
				txNewObject.setEnabled(false);
			}
		});

		scrollPaneClass = new JScrollPane();
		scrollPaneClass.setViewportView(lClass);
		scrollPaneClass.setBounds(10, 40, 90, 145);

		modelObjects=loadListObjects(nomClass);


		lObjects = new JList<String>( modelObjects );
		lObjects.setBounds(110, 40, 90, 110);
		lObjects.setBorder(BorderFactory.createLineBorder(Color.black));
		lObjects.setLayoutOrientation(JList.VERTICAL);
		lObjects.setSelectedIndex(0);
		nomObj = (String) lObjects.getSelectedValue();


		lObjects.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				oClass = lClass.getSelectedValue();
				nomClass = oClass.name();
				nomObj = (String) lObjects.getSelectedValue();
				selectObject( nomObj);
				txNewObject.setText(nomObj);
				txNewObject.setEnabled(false);
			}
		});
		scrollPaneObj = new JScrollPane();
		scrollPaneObj.setViewportView(lObjects);
		scrollPaneObj.setBounds(110, 40, 90, 110);

		btnNewObjectAuto = new JButton("+");
		btnNewObjectAuto.setBounds(110, 160, 90, 25);

		btnNewObjectAuto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newObjectAuto();
			}
		});
		panel.add(btnNewObjectAuto);

		fTableModel = new TableModel();
		fTable = new ExtendedJTable(fTableModel);
		fTable.setPreferredScrollableViewportSize(new Dimension(250, 70));

		fTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fTablePane = new JScrollPane(fTable);
		fTablePane.setBounds(210, 40, 180, 100);

		selectObject(nomObj);

		lbObj = new JLabel("Object");
		lbObj.setBounds(400, 15, 160, 25);
		panel.add(lbObj);	

		txNewObject = new JTextField(20);
		txNewObject.setBounds(400, 40, 100, 25);
		txNewObject.setEnabled(false);
		txNewObject.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) { //watch for key strokes
				if(txNewObject.getText().length() == 0 )
					btnSaveObject.setEnabled(false);
				else
				{
					btnSaveObject.setEnabled(true);
				}
			}
		});
		panel.add(txNewObject);

		nomObj = (String) lObjects.getSelectedValue();
		txNewObject.setText(nomObj);

		btnCreateObject = new JButton("New Object");
		btnCreateObject.setBounds(400, 70, 100, 25);
		btnCreateObject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				initNewObject();
			}
		});
		panel.add(btnCreateObject);

		btnSaveObject = new JButton("Save Object");
		btnSaveObject.setBounds(400, 100, 100, 25);

		btnSaveObject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nomObj = txNewObject.getText();
				oClass = lClass.getSelectedValue();
				saveObject(oClass, nomObj);
				bNewObj=false;
				txNewObject.setEnabled(false);
				selectObject(nomObj);
				cmbObjectOri.setModel(loadComboObjectMObject(cmbClassOri));
				cmbObjectDes.setModel(loadComboObjectMObject(cmbClassDes));
			}
		});

		panel.add(btnSaveObject);

		btnCancelObject = new JButton("Cancel Object");
		btnCancelObject.setBounds(400, 130, 100, 25);

		btnCancelObject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelObject(nomObj);
			}
		});
		panel.add(btnCancelObject);

		btnDeleteObject = new JButton("Delete Object");
		btnDeleteObject.setBounds(400, 160, 100, 25);
		btnDeleteObject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteObject(nomObj);
				cmbObjectOri.setModel(loadComboObjectMObject(cmbClassOri));
				cmbObjectDes.setModel(loadComboObjectMObject(cmbClassDes));
			}
		});
		panel.add(btnDeleteObject);

		lbAssoc = new JLabel("Assoc");
		lbAssoc.setBounds(10, 190, 160, 25);
		panel.add(lbAssoc);	

		lbFrom = new JLabel("From");
		lbFrom.setBounds(220, 190, 160, 25);
		panel.add(lbFrom);

		lbTo = new JLabel("To");
		lbTo.setBounds(350, 190, 160, 25);
		panel.add(lbTo);

		lAssocs = new JList<MAssociation>(loadListAssoc());

		lAssocs.setBounds(10, 215, 110, 85);
		lAssocs.setBorder(BorderFactory.createLineBorder(Color.black));
		lAssocs.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				MAssociation oAssoc = lAssocs.getSelectedValue();
				setComposAssoc(oAssoc);
			}
		});

		panel.add(lAssocs);

		lbAclass = new JLabel("Class");
		lbAclass.setBounds(150, 215, 100, 25);
		panel.add(lbAclass);

		lbAobject = new JLabel("Object");
		lbAobject.setBounds(150, 245, 100, 25);
		panel.add(lbAobject);	

		lbAmultiplicity = new JLabel("Multiplicity");
		lbAmultiplicity.setBounds(150, 275, 100, 25);
		panel.add(lbAmultiplicity);

		lbArole = new JLabel("Role");
		lbArole.setBounds(150, 305, 100, 25);
		panel.add(lbArole);

		cmbClassOri = new JComboBox<MClass>();

		cmbClassOri.setModel(loadComboClass());
		cmbClassOri.setBounds(220, 215, 120, 25);
		cmbClassOri.addActionListener (new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				cmbObjectOri.setModel(loadComboObjectMObject(cmbClassOri));
			}
		});
		cmbClassOri.setEnabled(false);
		cmbClassOri.setVisible(false);
		panel.add(cmbClassOri);

		Border blackline = BorderFactory.createLineBorder(Color.black);

		lbFromClass = new JLabel("");
		lbFromClass.setBounds(220, 215, 120, 25);
		lbFromClass.setBorder(blackline);
		lbFromClass.setBackground(colorSoftGray);
		lbFromClass.setHorizontalAlignment(SwingConstants.CENTER);
		lbFromClass.setOpaque(true);

		panel.add(lbFromClass);

		cmbClassDes = new JComboBox<MClass>();
		cmbClassDes.setModel(loadComboClass());
		cmbClassDes.setBounds(350, 215, 120, 25);
		cmbClassDes.addActionListener (new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				cmbObjectDes.setModel(loadComboObjectMObject(cmbClassDes));
			}
		});
		cmbClassDes.setEnabled(false);
		cmbClassDes.setVisible(false);
		panel.add(cmbClassDes);

		lbToClass = new JLabel("");
		lbToClass.setBounds(350, 215, 120, 25);
		lbToClass.setBorder(blackline);
		lbToClass.setBackground(colorSoftGray);
		lbToClass.setHorizontalAlignment(SwingConstants.CENTER);
		lbToClass.setOpaque(true);
		panel.add(lbToClass);

		cmbObjectOri = new JComboBox<MObject>();
		cmbObjectOri.setModel(loadComboObjectMObject(cmbClassOri));
		cmbObjectOri.setBounds(220, 245, 120, 25);
		panel.add(cmbObjectOri);

		cmbObjectDes = new JComboBox<MObject>();
		cmbObjectDes.setModel(loadComboObjectMObject(cmbClassDes));
		cmbObjectDes.setBounds(350, 245, 120, 25);
		panel.add(cmbObjectDes);

		cmbMultiOri = new JComboBox<String>(aMulti);
		cmbMultiOri.setBounds(220, 275, 120, 25);
		cmbMultiOri.setEditable(false);
		cmbMultiOri.setVisible(false);
		panel.add(cmbMultiOri);		

		txMultiOri = new JTextField(20);
		txMultiOri.setBounds(220, 275, 120, 25);
		txMultiOri.setBorder(blackline);
		txMultiOri.setEditable(false);
		txMultiOri.setBackground(colorSoftGray);
		txMultiOri.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(txMultiOri);	

		cmbMultiDes = new JComboBox<String>(aMulti);
		cmbMultiDes.setBounds(350, 275, 120, 25);
		cmbMultiDes.setEditable(false);
		cmbMultiDes.setVisible(false);
		panel.add(cmbMultiDes);		

		txMultiDes = new JTextField(20);
		txMultiDes.setBounds(350, 275, 120, 25);
		txMultiDes.setBorder(blackline);
		txMultiDes.setEditable(false);
		txMultiDes.setBackground(colorSoftGray);
		txMultiDes.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(txMultiDes);	

		txOriAssocRole = new JTextField(20);
		txOriAssocRole.setBounds(220, 305, 120, 25);
		txOriAssocRole.setBorder(blackline);
		txOriAssocRole.setEditable(false);
		txOriAssocRole.setBackground(colorSoftGray);
		txOriAssocRole.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(txOriAssocRole);	

		txDesAssocRole = new JTextField(20);
		txDesAssocRole.setBounds(350, 305, 120, 25);
		txDesAssocRole.setBorder(blackline);
		txDesAssocRole.setEditable(false);
		txDesAssocRole.setBackground(colorSoftGray);
		txDesAssocRole.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(txDesAssocRole);	

		btnInsertLinkAssoc = new JButton("Insert link");
		btnInsertLinkAssoc.setBounds(220, 340, 110, 25);
		btnInsertLinkAssoc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MAssociation oAssoc = lAssocs.getSelectedValue();
				insertLink(oAssoc) ;
			}
		});
		panel.add(btnInsertLinkAssoc);

		btnDeleteLink = new JButton("Delete link");
		btnDeleteLink.setBounds(350, 340, 120, 25);
		btnDeleteLink.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MAssociation oAssoc = lAssocs.getSelectedValue();
				deleteLink(oAssoc) ;
			}
		});

		panel.add(btnDeleteLink);

		//--
		lbClassInvariants = new JLabel("State invariants");
		lbClassInvariants.setBounds(220, 375, 120, 25);
		panel.add(lbClassInvariants);

		lbResClassInvariants = new JLabel("");
		lbResClassInvariants.setBounds(350, 375, 120, 25);
		lbResClassInvariants.setBorder(blackline);
		lbResClassInvariants.setVerticalAlignment(SwingConstants.CENTER);
		lbResClassInvariants.setHorizontalAlignment(SwingConstants.CENTER);
		lbResClassInvariants.setFont(new Font("Serif", Font.BOLD, 18));
		lbResClassInvariants.setVisible(false);
		panel.add(lbResClassInvariants);

		//btnShowClassInvariants
		btnShowClassInvariants = new JButton("OK");
		btnShowClassInvariants.setBounds(350, 375, 120, 25);
		btnShowClassInvariants.setVerticalAlignment(SwingConstants.CENTER);
		btnShowClassInvariants.setHorizontalAlignment(SwingConstants.CENTER);
		btnShowClassInvariants.setFont(new Font("Serif", Font.BOLD, 18));
		btnShowClassInvariants.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showClassInvariantsState();
			}
		});
		panel.add(btnShowClassInvariants);

		lbCheckStructure = new JLabel("Check Structure");
		lbCheckStructure.setBounds(220, 410, 120, 25);
		panel.add(lbCheckStructure);

		btnShowCheckStructure = new JButton("OK");
		btnShowCheckStructure.setBounds(350, 410, 120, 25);
		btnShowCheckStructure.setVerticalAlignment(SwingConstants.CENTER);
		btnShowCheckStructure.setHorizontalAlignment(SwingConstants.CENTER);
		btnShowCheckStructure.setFont(new Font("Serif", Font.BOLD, 18));
		btnShowCheckStructure.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//				showClassInvariantsState();
				checkStructure();
			}
		});
		panel.add(btnShowCheckStructure);

		//--

		//		panel.add(lClass);
		panel.add(scrollPaneClass);
		//		panel.add(lObjects);
		panel.add(scrollPaneObj);

		panel.add(lAttrs);		
		panel.add(fTablePane);	

		lClass.setSelectedIndex(0);
		nomClass = ((MClass) lClass.getSelectedValue()).name();
		lObjects.setModel(loadListObjects(nomClass));
		lObjects.setSelectedIndex(0);
		lAssocs.setSelectedIndex(0);
		MAssociation oAssoc = lAssocs.getSelectedValue();
		if (oAssoc!=null) {
			setComposAssoc(oAssoc);
		}
		
		setResClassInvariants();
		setResCheckStructure();
		add(panel);

		setSize(new Dimension(400, 300));

	}
	public void newObjectAuto() {


		int numObj = 1;
		String nomProposed = nomClass.toLowerCase() + numObj; 

		// Averiguar si existe objeto o no en base a una iteracion

		while(existObject(nomProposed)) {
			numObj+=1;
			nomProposed = nomClass.toLowerCase() + numObj; 
		}
		System.out.println("nomProposed [" + nomProposed+"]");
		oClass = lClass.getSelectedValue();

		bNewObj=true;
		saveObject(oClass, nomProposed);

		bNewObj=false;
		txNewObject.setEnabled(false);
		//Aqui1
		selectObject(nomProposed);
		nomObj = nomProposed;
		cmbObjectOri.setModel(loadComboObjectMObject(cmbClassOri));
		cmbObjectDes.setModel(loadComboObjectMObject(cmbClassDes));
	}
	public boolean existObject(String name) {
		boolean bRes=false;
		MSystemState state = fSystem.state();
		Set<MObject> allObjects = state.allObjects();

		for (MObject obj : allObjects) {
			if (obj.name().equals(name)) {
				bRes=true;
				return bRes;
			}
		}		

		return bRes;
	}

	public void setResCheckStructure() {
		boolean bRes = checkStructure();
		String sRes="OK";
		if (!bRes) sRes="KO";
		btnShowCheckStructure.setText(sRes);
		if(!bRes) {
			btnShowCheckStructure.setForeground(Color.white);
			btnShowCheckStructure.setBackground(Color.red);
		}else {
			btnShowCheckStructure.setForeground(Color.black);
			btnShowCheckStructure.setBackground(Color.green);
		}
	}

	public void setResClassInvariants() {
		boolean bRes = check_inv_state();
		String sRes="OK";
		if (!bRes) sRes="KO";
		lbResClassInvariants.setText(sRes);
		btnShowClassInvariants.setText(sRes);
		if(!bRes) {
			lbResClassInvariants.setForeground(Color.white);
			lbResClassInvariants.setBackground(Color.red);
			btnShowClassInvariants.setForeground(Color.white);
			btnShowClassInvariants.setBackground(Color.red);
		}else {
			lbResClassInvariants.setForeground(Color.black);
			lbResClassInvariants.setBackground(Color.green);
			btnShowClassInvariants.setForeground(Color.black);
			btnShowClassInvariants.setBackground(Color.green);
		}
		lbResClassInvariants.setOpaque(true);

	}
	public void showClassInvariantsState() {
		ClassInvariantView civ = new ClassInvariantView(fMainWindow,
				fSession.system());
		ViewFrame f = new ViewFrame("Class invariants", civ,
				"InvariantView.gif");
		civ.setViewFrame(f);
		JComponent c = (JComponent) f.getContentPane();
		c.setLayout(new BorderLayout());
		c.add(civ, BorderLayout.CENTER);
		fMainWindow.addNewViewFrame(f);
	}
	public boolean check_inv_state() {
		boolean bRes = false;

		MModel fModel = fSystem.model();
		int n = fModel.classInvariants().size();
		MClassInvariant[] fClassInvariants = new MClassInvariant[0];
		fClassInvariants = new MClassInvariant[n];
		System.arraycopy(fModel.classInvariants().toArray(), 0,
				fClassInvariants, 0, n);
		Arrays.sort(fClassInvariants);
		EvalResult[] fValues;
		fValues = new EvalResult[n];
		for (int i = 0; i < fValues.length; i++) {
			fValues[i] = null;
		}
		ExecutorService executor = Executors.newFixedThreadPool(Options.EVAL_NUMTHREADS);
		futures = new ArrayList<Future<EvalResult>>();
		ExecutorCompletionService<EvalResult> ecs = new ExecutorCompletionService<EvalResult>(executor);
		boolean violationLabel = false; 
		int numFailures = 0;
		boolean structureOK = true;
		for (int i = 0; i < fClassInvariants.length; i++) {
			if(!fClassInvariants[i].isActive()){
				continue;
			}
			MyEvaluatorCallable cb = new MyEvaluatorCallable(fSystem.state(), i, fClassInvariants[i]);
			futures.add(ecs.submit(cb));
		}

		for (int i = 0; i < fClassInvariants.length && !isCancelled(); i++) {
			if(!fClassInvariants[i].isActive()){
				continue;
			}
			try {
				EvalResult res;
				res = ecs.take().get();
				fValues[res.index] = res;

				boolean ok = false;
				// if v == null it is not considered as a failure, rather it is
				// a MultiplicityViolation and it is skipped as failure count
				boolean skip = false;
				if (res.result != null) {
					ok = res.result.isDefined() && ((BooleanValue)res.result).isTrue();
				} else {
					violationLabel = true;
					skip = true;
				}

				if (!skip && !ok)
					numFailures++;

			} catch (InterruptedException ex) {
				break;
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}

		for (Future<EvalResult> f : futures) {
			f.cancel(true);
		}

		//		structureOK = fSystem.state().checkStructure(new PrintWriter(new NullWriter()), false);

		boolean todosOk=true;
		for (EvalResult res : fValues) {
			Boolean boolRes=  ((BooleanValue)res.result).value();

			if (boolRes.equals(false)) todosOk=false;
			System.out.println("res.index ["+res.index+"]");
			System.out.println("Para invs res ["+fClassInvariants[res.index].name()+"] result ["+res.result+"]");
		}
		System.out.println("todosOk ["+todosOk+"]");
		executor.shutdown();

		return todosOk;

	}
	private boolean checkStructure() {
		StringWriter buffer = new StringWriter();
		PrintWriter out = new PrintWriter(buffer);

		//		boolean ok = fSession.system().state().checkStructure(new PrintWriter(new NullWriter()));
		boolean ok = fSession.system().state().checkStructure(out);
		//--
		boolean res=false;
		// check all associations
		boolean reportAllErrors = true;
		for (MAssociation assoc : fSystem.model().associations()) {
			StringWriter buffer2 = new StringWriter();
			PrintWriter out2 = new PrintWriter(buffer2);
			res = fSession.system().state().checkStructure(assoc, out2, reportAllErrors) ;
			System.out.println("Para assoc ["+assoc.name()+" -  ["+res+"] ["+buffer2+"]");
		}

		System.out.println("Total ["+ok+"]");
		System.out.println("Totalout ["+buffer+"]");
		return ok;
	}

	public void setFrameName(String name) {
		frame.setName(name);
	}
	private DefaultListModel<MClass> loadListMClass() {
		DefaultListModel<MClass> ldefLModel = new DefaultListModel<MClass>();
		for (MClass oClass : fSystem.model().classes()) {
			ldefLModel.addElement(oClass);
		}
		return ldefLModel;
	}

	private DefaultListModel<String> loadListObjects(String nomClass) {
		DefaultListModel<String> ldefLModel = new DefaultListModel<String>();
		MSystemState state = fSystem.state();
		Set<MObject> allObjects = state.allObjects();

		for (MObject obj : allObjects) {
			if (obj.cls().name().equals(nomClass)) {
				ldefLModel.addElement(obj.name());
			}
		}
		return ldefLModel;
	}
	private  DefaultComboBoxModel<MClass> loadComboClass() {
		DefaultComboBoxModel<MClass> cbm = new DefaultComboBoxModel<MClass>();

		for (MClass oClass : fSystem.model().classes()) {
			cbm.addElement(oClass);
		}
		return cbm;

	}

	private  DefaultComboBoxModel<MObject> loadComboObjectMObject(JComboBox<MClass> cmbClass) {
		DefaultComboBoxModel<MObject> cbm = new DefaultComboBoxModel<MObject>();
		MClass oClass = (MClass) cmbClass.getItemAt(cmbClass.getSelectedIndex());
		MSystemState state = fSystem.state();
		Set<MObject> allObjects = state.allObjects();

		for (MObject obj : allObjects) {
			if (obj.cls().name().equals(oClass.name())) {
				cbm.addElement(obj);
			}
		}

		return cbm;
	}

	private DefaultListModel<MAssociation> loadListAssoc() {
		DefaultListModel<MAssociation> ldefLModel = new DefaultListModel<MAssociation>();
		for (MAssociation oClass : fSystem.model().associations()) {
			ldefLModel.addElement(oClass);
		}
		return ldefLModel;
	}
	private void setComposAssoc(MAssociation oAssoc) {

		Set<MLink> links = fSystem.state().linksOfAssociation(oAssoc).links();
		// Si no hay links, se han de inicializar los combos en base a las AssociationEnds
		int nLink=0;
		if (links.size()==0) {
			for (MAssociationEnd ma:oAssoc.associationEnds()) {
				String className=ma.cls().name();
				switch(nLink){
				case 0:
					selectClasInCombo(cmbClassOri,className);
					lbFromClass.setText(className);
				case 1:
					selectClasInCombo(cmbClassDes,className);
					lbToClass.setText(className);
				default:
					// De momento no hacemos nada
					break;
				}
				nLink+=1;
			}
			return;
		}

		// Si hay links, se anliazan los links y se muestra el primero
		// Inicialmente supondremos que solo hay un link

		for (MLink oLink:links) {
			System.out.println(oLink.linkedObjects());
			if (nLink==0) {
				MObject oOri=null;
				MObject oDes=null;

				int nLinkEnd=0;
				for(MLinkEnd oMlinkEnd: oLink.linkEnds()) {
					System.out.println(oMlinkEnd.object().name());
					MAssociationEnd oMAssociationEnd=oMlinkEnd.associationEnd();
					System.out.println(oMAssociationEnd.name());

					MMultiplicity oMMultiplicity=oMAssociationEnd.multiplicity();

					System.out.println(oMMultiplicity.toString());
					switch(nLinkEnd){
					case 0:
						oOri=oMlinkEnd.object();
						txOriAssocRole.setText(oMAssociationEnd.name());
						cmbClassOri.setSelectedItem(oOri.cls());
						cmbObjectOri.setSelectedItem(oOri);
						cmbMultiOri.setSelectedItem(oMMultiplicity.toString());
						txMultiOri.setText(oMMultiplicity.toString());
						lbFromClass.setText(oOri.cls().name());
						break;
					case 1:
						oDes=oMlinkEnd.object();
						txDesAssocRole.setText(oMAssociationEnd.name());
						cmbClassDes.setSelectedItem(oDes.cls());
						cmbObjectDes.setSelectedItem(oDes);
						cmbMultiDes.setSelectedItem(oMMultiplicity.toString());
						txMultiDes.setText(oMMultiplicity.toString());
						lbToClass.setText(oDes.cls().name());
						break;
					default:
						// De momento no hacemos nada
						break;
					}
					nLinkEnd+=1;
				}

			}
		}
		System.out.println(oAssoc.name());

	}
	private void insertLink(MAssociation oAssoc) {
		MObject oOri = cmbObjectOri.getItemAt(cmbObjectOri.getSelectedIndex());
		MObject oDes = cmbObjectDes.getItemAt(cmbObjectDes.getSelectedIndex());
		MObject[] fParticipants = new MObject[] {oOri,oDes};
		insertLink(oAssoc, fParticipants);
	}

	private void insertLink(MAssociation association, MObject[] objects) {
		if (association.hasQualifiedEnds()) {
			QualifierInputView input = new QualifierInputView(fMainWindow, fSystem, association, objects);
			input.setLocationRelativeTo(this);
			input.setVisible(true);

		} else {
			try {
				fSystem.execute(new MLinkInsertionStatement(association, objects, Collections.<List<Value>>emptyList()));
			} catch (MSystemException e) {
				JOptionPane.showMessageDialog(
						fMainWindow, 
						e.getMessage(), 
						"Error", 
						JOptionPane.ERROR_MESSAGE);
			}
		}
		setResCheckStructure();
	}
	private void deleteLink(MAssociation oAssoc) {
		// Averiguar el link del que se trata
		MObject oOri = cmbObjectOri.getItemAt(cmbObjectOri.getSelectedIndex());
		MObject oDes = cmbObjectDes.getItemAt(cmbObjectDes.getSelectedIndex());

		MLink oLinkTOdel=null;
		Set<MLink> links = fSystem.state().linksOfAssociation(oAssoc).links();
		// Inicialmente suponderemos que solo hay un link

		for (MLink oLink:links) {
			System.out.println(oLink.linkedObjects());
			//Si se trata de los objetos seleccionados en la UI es el link buscado

			int nLinksObj = oLink.linkedObjects().size();
			MObject o1=null;
			MObject o2=null;
			for(int nLinkObj = 0; nLinkObj<nLinksObj;nLinkObj++) {
				switch(nLinkObj){
				case 0:
					o1=oLink.linkedObjects().get(nLinkObj);
					break;
				case 1:
					o2=oLink.linkedObjects().get(nLinkObj);
					break;
				default:
					break;
				}
			}
			if (o1!=null && o2!=null) {
				if (oOri.equals(o1) && oDes.equals(o2) || oOri.equals(o2) && oDes.equals(o1))
					oLinkTOdel = oLink;
				continue;
			}
		}

		if(oLinkTOdel!=null) {
			System.out.println("Borra ["+oLinkTOdel.toString()+"]");
			deleteLink(oLinkTOdel);
		}

	}
	private void deleteLink(MLink link) {
		try {
			fSystem.execute(
					new MLinkDeletionStatement(link));
		} catch (MSystemException e) {
			JOptionPane.showMessageDialog(
					fMainWindow, 
					e.getMessage(), 
					"Error", 
					JOptionPane.ERROR_MESSAGE);
		}
		// Localizar diagrama y ver si se puede actualizar
		for (NewObjectDiagramView odv: fMainWindow.getObjectDiagrams()) {
			if (odv.getName().equals(NAMEFRAMEMVMDIAGRAM)) {
				odv.repaint();
			}
		}
		setResCheckStructure();
	}

	private void searchObjDiagramAssociated() {
		odvAssoc = null; 
		for (NewObjectDiagramView odv: fMainWindow.getObjectDiagrams()) {
			if (odv.getName()!=null) {
				if (odv.getName().equals(NAMEFRAMEMVMDIAGRAM)) {
					odvAssoc = odv.getDiagram();
					return;
				}
			}
		}
	}

	private void createObjDiagram() {
		NewObjectDiagramView odv = new NewObjectDiagramView(fMainWindow, fSession.system());
		ViewFrame f = new ViewFrame("Object diagram", odv, "ObjectDiagram.gif");
		f.setName(NAMEFRAMEMVMDIAGRAM);
		odv.setName(NAMEFRAMEMVMDIAGRAM);

		int OBJECTS_LARGE_SYSTEM = 100;

		// Many objects. Ask user if all objects should be hidden
		if (fSession.system().state().allObjects().size() > OBJECTS_LARGE_SYSTEM) {

			int option = JOptionPane.showConfirmDialog(new JPanel(),
					"The current system state contains more then " + OBJECTS_LARGE_SYSTEM + " instances." +
							"This can slow down the object diagram.\r\nDo you want to start with an empty object diagram?",
							"Large system state", JOptionPane.YES_NO_OPTION);

			if (option == JOptionPane.YES_OPTION) {
				odv.getDiagram().hideAll();
			}
		}

		JComponent c = (JComponent) f.getContentPane();
		c.setLayout(new BorderLayout());
		c.add(odv, BorderLayout.CENTER);
		fMainWindow.addNewViewFrame(f);
		fMainWindow.getObjectDiagrams().add(odv);
		odvAssoc = odv.getDiagram();

		tile();
	}

	private void initNewObject() {
		txNewObject.setText("");
		txNewObject.setEnabled(true);
		bNewObj=true;
	}

	private void cancelObject(String nomObj) {
		selectObject(nomObj);
	}
	private boolean checkExistObjDiagram() {
		boolean existDiagram=false;
		// Ver frames
		JDesktopPane fDesk = fMainWindow.getFdesk();
		allframes = fDesk.getAllFrames();

		for (JInternalFrame ifr: allframes) {
			if (ifr.getName()!=null ) {
				if (ifr.getName().equals(NAMEFRAMEMVMDIAGRAM)) {
					existDiagram=true;
					continue;
				}	
			}
		}

		if (!existDiagram) {
			createObjDiagram();
		}
		return existDiagram;
	}
	private void createObject(MClass oClass, String nomObj) {

		checkExistObjDiagram();
		fMainWindow.createObject(oClass, nomObj);
		lObjects.setModel(loadListObjects(nomClass));

//		checkExistObjDiagram();
		odvAssoc.forceStartLayoutThread();
	}
	private void saveObject(MClass oClass, String nomObj) {
		// Verificamos existencia de ObjectDiagram MVM
//		boolean existDiagram=false;
		//aqui8
		checkExistObjDiagram();
		
		if (bNewObj) {
			// Hacer copia de fValues
			String[] fValuesAnt = new String[fAttributes.size()];
			for (int i = 0; i < fAttributes.size(); i++) {
				fValuesAnt[i] = fValues[i];
			}
			if (!existObject(nomObj)) {
				createObject(oClass, nomObj);
			}

			selectObject( nomObj);
			if (fValues.length == fValuesAnt.length) {
				for (int i = 0; i < fAttributes.size(); i++) {
					fValues[i] = fValuesAnt[i];
				}
			}else {
				for (int i = 0; i < fAttributes.size(); i++) {
					MAttribute attr = (MAttribute) fAttributes.get(i);
					// Aqui8
					attr.type();
					if (attr.type().isTypeOfInteger()) {
						fValues[i] = "1";
					}else if (attr.type().isTypeOfString()) {
						fValues[i] = "'x'";
					}
				}
			}
		}
		applyChanges();
		if (!checkExistObjDiagram()) {
			odvAssoc.forceStartLayoutThread();
		}
		setResClassInvariants();
		//		checkStructure();
		setResCheckStructure();

	}
	private void deleteObject(String nomObjDel) {
		int idx = selectObject(nomObjDel);
		MSystemState state = fSystem.state();
		// Localizar diagrama y ver si se puede actualizar
		for (NewObjectDiagramView odv: fMainWindow.getObjectDiagrams()) {
			if (odv.getName().equals(NAMEFRAMEMVMDIAGRAM)) {
				odv.getDiagram().deleteObject(fObject);
				odv.repaint();
				idx=idx-1;

			}
		}
		state.deleteObject(fObject);
		lObjects.setModel(loadListObjects(nomClass));
		if (idx<0) idx=0;
		if (idx>-1) {
			lObjects.setSelectedIndex(idx);
			nomObj = (String) lObjects.getSelectedValue();
		}
		setResClassInvariants();
		setResCheckStructure();
	}



	/**
	 * Applies changes by setting new attribute values. Entries may be
	 * arbitrary OCL expressions. 
	 */
	private void applyChanges() {

		if (fTable.getCellEditor() != null) { 
			fTable.getCellEditor().stopCellEditing();
		}

		if (!haveObject()) { 
			return;
		}

		// Don't refresh after first change...
		fSystem.getEventBus().unregister(this);
		boolean error = false;

		for (int i = 0; i < fValues.length; ++i) {
			MAttribute attribute = fAttributes.get(i);
			String newValue = fValues[i];
			String oldValue = fAttributeValueMap.get(attribute).toString();

			if (!newValue.equals(oldValue)) {

				StringWriter errorOutput = new StringWriter();
				Expression valueAsExpression = 
						OCLCompiler.compileExpression(
								fSystem.model(),
								fSystem.state(),
								newValue, 
								"<input>", 
								new PrintWriter(errorOutput, true), 
								fSystem.varBindings());

				if (valueAsExpression == null) {
					JOptionPane.showMessageDialog(
							fMainWindow, 
							errorOutput, 
							"Error", 
							JOptionPane.ERROR_MESSAGE);
					error = true;
					continue;
				}

				try {
					fSystem.execute(
							new MAttributeAssignmentStatement(
									fObject, 
									attribute, 
									valueAsExpression));

				} catch (MSystemException e) {
					JOptionPane.showMessageDialog(
							fMainWindow, 
							e.getMessage(), 
							"Error", 
							JOptionPane.ERROR_MESSAGE);
					error = true;
				}
			}
		}

		if (!error) update();

		fSystem.getEventBus().register(this);
	}

	private boolean haveObject() {
		return fObject != null && fObject.exists(fSystem.state());
	}

	/**
	 * An object has been selected from the list. Update the table of
	 * properties.
	 */
	public int selectObject(String objName) {
		int idx = -1;
		MSystemState state = fSystem.state();
		fObject = state.objectByName(objName);
		fTableModel.update();

		// Buscar en lista
		lObjects.setSelectedIndex(0);
		int nObjs= lObjects.getModel().getSize();
		for (int nObj=0;nObj<nObjs;nObj++) {
			if (lObjects.getModel().getElementAt(nObj).equals(objName)) {
				lObjects.setSelectedIndex(nObj);
				idx=nObj;
				return idx;
			}
		}
		return idx;
	}

	public void selectClasInCombo(JComboBox<MClass> cmb, String className) {

		// Buscar en lista
		int nObjs= cmb.getModel().getSize();
		for (int nObj=0;nObj<nObjs;nObj++) {
			MClass cl = (MClass) cmb.getModel().getElementAt(nObj);
			if (cl.name().equals(className)) {
				cmb.setSelectedIndex(nObj);
				System.out.println("indice " + nObj);
				return;
			}
		}
		return;
	}


	private void update() {
		fTableModel.update();
	}

	@Subscribe
	public void onStateChanged(SystemStateChangedEvent e) {
		update();
	}

	/**
	 * Detaches the view from its model.
	 */
	public void detachModel() {
		fSystem.getEventBus().unregister(this);
		fSystem.unregisterRequiresAllDerivedValues();
	}
	private void tile() {
		JDesktopPane fDesk = fMainWindow.getFdesk();
//		allframes = fDesk.getAllFrames();
		allframes = fMainWindow.sortInternalFrames(fDesk.getAllFrames());
		int count = allframes.length;
		if (count == 0)
			return;

		// Determine the necessary grid size
		int sqrt = (int) Math.sqrt(count);
		int rows = sqrt;
		int cols = sqrt;
		if (rows * cols < count) {
			cols++;
			if (rows * cols < count) {
				rows++;
			}
		}

		// Define some initial values for size & location
		Dimension size = fDesk.getSize();

		int w = size.width / cols;
		int h = size.height / rows;
		int x = 0;
		int y = 0;

		// Iterate over the frames, deiconifying any iconified frames and
		// then relocating & resizing each
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols && ((i * cols) + j < count); j++) {
				JInternalFrame f = allframes[(i * cols) + j];

				if (f.isIcon() && !f.isClosed()) {
					try {
						f.setIcon(false);
					} catch (PropertyVetoException ex) {
						// ignored
					}
				}
				fDesk.getDesktopManager().resizeFrame(f, x, y, w, h);
				x += w;
			}
			y += h; // start the next row
			x = 0;
		}

	}
	public final boolean isCancelled() {
		//        return future.isCancelled();
		return false;
	}
	private class EvalResult {
		public final int index;
		public final Value result;
		public final String message;
		public final long duration;

		public EvalResult(int index, Value result, String message, long duration) {
			this.index = index;
			this.result = result;
			this.message = message;
			this.duration = duration;
		}
	}
	private class MyEvaluatorCallable implements Callable<EvalResult> {
		final int index;
		final MSystemState state;
		final MClassInvariant inv;

		public MyEvaluatorCallable(MSystemState state, int index, MClassInvariant inv) {
			this.state = state;
			this.index = index;
			this.inv = inv;
		}

		@Override
		public EvalResult call() throws Exception {
			if (isCancelled()) return null;

			Evaluator eval = new Evaluator();
			Value v = null;
			String message = null;
			long start = System.currentTimeMillis();

			try {
				v = eval.eval(inv.flaggedExpression(), state);
			} catch (MultiplicityViolationException e) {
				message = e.getMessage();
			}

			return new EvalResult(index, v, message, System.currentTimeMillis() - start);
		}
	}
}

