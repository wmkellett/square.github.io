package examples.CertificateManagement;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.cleo.lexicom.external.*;
import com.cleo.lexicom.certmgr.external.*;

/*
 *  This class exports a new user certificate's private key in PKCS#12 format. 
 */


class ExportPrivateKey extends JDialog implements ActionListener, KeyListener {
 
  JFrame parent;
  ILexiCom lexicom;
  String productPath;
  
  
  JTextField aliasField = new JTextField(); 
  JPasswordField passwordField = new JPasswordField();
  
  JTextField p12DestField = new JTextField();
  
  public ExportPrivateKey(JFrame parent, ILexiCom lexicom, String productPath) {
    super(parent, true);
    this.parent = parent;
    this.lexicom = lexicom;
    this.productPath = productPath;
    
    this.setTitle("Export Certificate Private Key");
    this.init();
  }

  private void init() {
    JPanel panel = new JPanel(new GridBagLayout());
    JPanel buttonPanel = new JPanel(new FlowLayout());
    
    JLabel aliasLabel = new JLabel("Certificate Alias: ");       
    JLabel passwordLabel = new JLabel("Private Key Password: ");
    JLabel p12DestLabel = new JLabel("Certificate PKCS #12 File Destination: ");  

    JButton browseButton = new JButton("...");
    browseButton.addActionListener(this);
    browseButton.setActionCommand("Browse");

    JButton okButton = new JButton("OK");
    okButton.addActionListener(this);

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(this);

    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);
    
    aliasField.setPreferredSize(new Dimension(200, 21));
    passwordField.setPreferredSize(new Dimension(200, 21));
    p12DestField.setPreferredSize(new Dimension(200, 21));
    
    aliasField.addKeyListener(this);

    panel.add(aliasLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
          ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 0, 5), 0, 0));
    panel.add(aliasField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
          ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 0, 0, 5), 0, 0));

    panel.add(passwordLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
          ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
    panel.add(passwordField, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
          ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0));
    
    panel.add(p12DestLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
          ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
    panel.add(p12DestField, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
          ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 5), 0, 0));
    panel.add(browseButton, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
          ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 0, 5), 0, 0));

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
      p12DestField.setText(chooser.getSelectedFile().getPath() + File.separator);
    }
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals("Browse")) {
      this.choosePath();
    } else if (e.getActionCommand().equals("OK")) {
      if (exportKey())
        this.dispose();
    } else if (e.getActionCommand().equals("Cancel"))
      this.dispose();
  }
  
  public void keyTyped(KeyEvent e) {
    // Upcase only the alias fields and leave the password field intact      
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
  
  private boolean exportKey() {
    // Validate all the fields have been filled in
    if (aliasField.getText().trim().length() == 0) {
      aliasField.requestFocus();
      JOptionPane.showMessageDialog(this,
                                    "A Certificate Alias Must be Entered!",
                                    "Missing Certificate Alias",
                                    JOptionPane.ERROR_MESSAGE);
      return false;
      
    } else if (passwordField.getPassword().length == 0) {
      passwordField.requestFocus();
      JOptionPane.showMessageDialog(this,
                                    "A Password Must be Entered!",
                                    "Missing Password",
                                    JOptionPane.ERROR_MESSAGE);
      return false;
        
    } else if (p12DestField.getText().trim().length() == 0) {
      p12DestField.requestFocus();
      JOptionPane.showMessageDialog(this,
                                    "A PKCS#12 File Destination Path Must be Entered!",
                                    "Missing PKCS#12 File Path",
                                    JOptionPane.ERROR_MESSAGE);
      return false;
      
    } 
    
    File p12DestFile = new File(p12DestField.getText());    
    if (p12DestFile.isDirectory() || p12DestFile.getName().trim().length() == 0) {
      p12DestField.requestFocus();
      JOptionPane.showMessageDialog(this,
                                    "A PKCS#12 File Name Must be Entered!",
                                    "Missing PKCS#12 File Name",
                                    JOptionPane.ERROR_MESSAGE);
        return false;
    }

    // If the file name does not end with an extension, add it
    if (!p12DestFile.getPath().toLowerCase().endsWith(".p12")) {
      p12DestFile = new File(p12DestField.getText() + ".p12");
    }  

    // If the file already exists, ask if it should be overwritten        
    if (p12DestFile.exists()) {
      String msg = "The PKCS#12 File'" + p12DestFile.getName() + "' already\n" +
                   "exists in the '" + p12DestFile.getParent() + "' folder.\n\n" +
                   "Do you want to overwrite it?";

      if (JOptionPane.showOptionDialog(this, msg, "PKCS#12 Destination File Already Exists",
                                       JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE,
                                       null, null, null) == JOptionPane.NO_OPTION)
        return false;
    }

    try {
      // Now proceed with exporting of the entire certificate chain
      ICertManagerRunTime certmgr = lexicom.getCertManager();
      this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      String alias = aliasField.getText().trim();
      String password = new String(passwordField.getPassword());
      String friendlyName = "";  //Note: this cannot be null!
      certmgr.exportUserPKCS12(alias, p12DestFile, password, false, friendlyName, true); 

      JOptionPane.showMessageDialog(this,
                                    "Certificate '" + alias + "' was successfully exported to\n" +
                                    p12DestFile.getPath(),
                                    "Certificate Was Successfully Exported",
                                    JOptionPane.INFORMATION_MESSAGE);     
      return true;

    } catch (Exception ex) {     
      JOptionPane.showMessageDialog(this,
                                  "Exception occurred while attempting to\n" +
                                  "export certificate key: " + ex.getMessage(),
                                  "Exception", JOptionPane.ERROR_MESSAGE); 
    } finally {
      this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }    
    return false;
  }
}
  