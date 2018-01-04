package examples.CertificateManagement;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.cleo.lexicom.external.*;
import com.cleo.lexicom.certmgr.external.*;

/*
 *  This class exports a user certificate to the desired file path. 
 */

class ExportUserCertificate extends JDialog implements ActionListener, KeyListener {
  // Exports a User Certificate
  JFrame parent;
  ILexiCom lexicom;
  String productPath;
  
  JTextField aliasField = new JTextField();
  JTextField certDestField = new JTextField(); 

  public ExportUserCertificate(JFrame parent, ILexiCom lexicom, String productPath) {
    super(parent, true);
    this.parent = parent;
    this.lexicom = lexicom;
    this.productPath = productPath;
        
    this.setTitle("Export User Certificate");
    this.init();
  }

  private void init() {
  JPanel panel = new JPanel(new GridBagLayout());
    JPanel buttonPanel = new JPanel(new FlowLayout());

    JLabel aliasLabel = new JLabel("Certificate Alias: ");  
    JLabel certDestLabel = new JLabel("Certificate Destination: ");       

    JButton browseButton = new JButton("...");
    browseButton.addActionListener(this);
    browseButton.setActionCommand("Browse");
    browseButton.setToolTipText("Use this button to select the destination path");

    JButton okButton = new JButton("OK");
    okButton.addActionListener(this);

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(this);

    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);

    aliasField.setPreferredSize(new Dimension(200, 21));
    certDestField.setPreferredSize(new Dimension(200, 21));
    
    aliasField.addKeyListener(this);

    panel.add(aliasLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
          ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 0, 5), 0, 0));
    panel.add(aliasField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
          ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 0, 0, 5), 0, 0));

    panel.add(certDestLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
          ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
    panel.add(certDestField, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
          ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0));
    panel.add(browseButton, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
          ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));

    panel.add(buttonPanel, new GridBagConstraints(0, 3, 3, 1, 0.0, 0.0
          ,GridBagConstraints.SOUTH, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));

    this.setLayout(new BorderLayout());
    this.add(panel, BorderLayout.CENTER);

    this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    this.pack();

    this.setLocationRelativeTo(parent);
    this.setVisible(true);            
  }

   private void choosePath() {
    JFileChooser chooser = new JFileChooser();
    chooser.setCurrentDirectory(new File(productPath));
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int returnVal = chooser.showDialog(this, "Select");
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      certDestField.setText(chooser.getSelectedFile().getPath() + File.separator);
    }
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals("Browse")) {
      this.choosePath();
    } else if (e.getActionCommand().equals("OK")) {
      if (exportCert())
        this.dispose();
    } else if (e.getActionCommand().equals("Cancel"))
      this.dispose();
  }

  public void keyTyped(KeyEvent e) {
    // Upcases the alias field      
    char c = e.getKeyChar();
    // Convert lowercase values to uppercase
    if (c >= 97 && c <= 122) {
      c -= 32;
      e.setKeyChar(c);
    }      
  }

  public void keyPressed(KeyEvent e) {
  }

  public void keyReleased(KeyEvent e) {
  }
  
  private boolean exportCert() {
    // Validate all the fields have been filled in
    if (aliasField.getText().trim().length() == 0) {
      aliasField.requestFocus();
      JOptionPane.showMessageDialog(this,
                                    "The Certificate Alias Must Be Entered!",
                                    "Missing Certificate Alias",
                                    JOptionPane.ERROR_MESSAGE);
      return false;
    } else if (certDestField.getText().trim().length() == 0) {
      certDestField.requestFocus();
      JOptionPane.showMessageDialog(this,
                                    "A Certificate File Destination Path Must be Entered!",
                                    "Missing Certificate File Destination Path",
                                    JOptionPane.ERROR_MESSAGE);
      return false;
    } 
    File dest = new File(certDestField.getText());      
    if (dest.isDirectory() || dest.getName().trim().length() == 0) {
      certDestField.requestFocus();
      JOptionPane.showMessageDialog(this,
                                    "A Certificate File Name Must be Entered!",
                                    "Missing Certificate File Name",
                                    JOptionPane.ERROR_MESSAGE);
      return false;
    }

    // If the file name does not end with an extension, add it
    if (!dest.getPath().toLowerCase().endsWith(".cer")) {
      dest = new File(certDestField.getText() + ".cer");
    }  

    // If the file already exists, ask if it should be overwritten        
    if (dest.exists()) {
      String msg = "The certificate '" + dest.getName() + "' already\n" +
                   "exists in the '" + dest.getParent() + "' folder.\n\n" +
                   "Do you want to overwrite it?";

      if (JOptionPane.showOptionDialog(this, msg, "Certificate Already Exists",
                                       JOptionPane.YES_NO_OPTION,
                                       JOptionPane.QUESTION_MESSAGE,
                                       null, null, null) == JOptionPane.NO_OPTION)
        return false;
    }

    try {       
      // Now we can proceed with the export     
      this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      ICertManagerRunTime certmgr = lexicom.getCertManager();
      String alias = aliasField.getText().trim();
      certmgr.exportUserCert(alias, dest, true, true);  
      JOptionPane.showMessageDialog(this,
                                    "The user certificate for '" + alias + "' was successfully\n" +
                                    " exported to " + dest.getPath(),
                                    "User Certificate Was Successfully Exported",
                                    JOptionPane.INFORMATION_MESSAGE); 
      return true;

    } catch (Exception ex) {
      ex.printStackTrace();
      JOptionPane.showMessageDialog(this,
                                  "Exception occurred while attempting to\n" +
                                  "export the user certificate: " + ex.getMessage(),
                                  "Exception", JOptionPane.ERROR_MESSAGE); 
    } finally {
      this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }    
    return false;
  }
}
  
