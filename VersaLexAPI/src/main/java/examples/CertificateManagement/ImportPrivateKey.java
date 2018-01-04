package examples.CertificateManagement;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.cleo.lexicom.external.*;
import com.cleo.lexicom.certmgr.external.*;

/*
 *  This class imports a certificate private key in 
 *  PKCS#12 format from the desired file path. 
 */

 class ImportPrivateKey extends JDialog implements ActionListener, KeyListener {
   JFrame parent;
  ILexiCom lexicom;
  String productPath;
 
  JTextField p12SrcField = new JTextField();
  JTextField aliasField = new JTextField(); 
  JPasswordField passwordField = new JPasswordField();

  public ImportPrivateKey(JFrame parent, ILexiCom lexicom, String productPath) {
    super(parent, true);
    this.parent = parent;
    this.lexicom = lexicom;
    this.productPath = productPath;
    
    this.setTitle("Import Certificate Private Key");
    this.init();
  }

  private void init() {
    JPanel panel = new JPanel(new GridBagLayout());
    JPanel buttonPanel = new JPanel(new FlowLayout());

    JLabel p12SrcLabel = new JLabel("Certificate PKCS#12 File: ");  
    JLabel aliasLabel = new JLabel("Certificate Alias: ");       
    JLabel passwordLabel = new JLabel("Private Key Password: ");

    JButton browseButton = new JButton("...");
    browseButton.addActionListener(this);
    browseButton.setActionCommand("Browse");

    JButton okButton = new JButton("OK");
    okButton.addActionListener(this);

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(this);

    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);

    p12SrcField.setPreferredSize(new Dimension(200, 21));
    aliasField.setPreferredSize(new Dimension(200, 21));
    passwordField.setPreferredSize(new Dimension(200, 21));
    aliasField.addKeyListener(this);

    panel.add(p12SrcLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
          ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 0, 5), 0, 0));
    panel.add(p12SrcField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
          ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 0, 0, 5), 0, 0));
    panel.add(browseButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
          ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 0, 0, 5), 0, 0));

    panel.add(aliasLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
          ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
    panel.add(aliasField, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
          ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 5), 0, 0));

    panel.add(passwordLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
          ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
    panel.add(passwordField, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
          ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0));

    panel.add(buttonPanel, new GridBagConstraints(0, 3, 3, 1, 0.0, 0.0
          ,GridBagConstraints.SOUTH, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));

    this.setLayout(new BorderLayout());
    this.add(panel, BorderLayout.CENTER);

    this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    this.pack();

    this.setLocationRelativeTo(parent);
    this.setVisible(true);      
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals("Browse")) {
      this.chooseFile();
    } else if (e.getActionCommand().equals("OK")) {
      if (importCertKey())
        this.dispose();
    } else if (e.getActionCommand().equals("Cancel"))
      this.dispose();
  }

  private void chooseFile() {
    JFileChooser chooser = new JFileChooser();
    chooser.setCurrentDirectory(new File(productPath));
    chooser.setDialogType(JFileChooser.OPEN_DIALOG);
    int returnVal = chooser.showOpenDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      p12SrcField.setText(chooser.getSelectedFile().getPath());
    }
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

  private boolean importCertKey() {
    // Validate all the fields have been filled in
    if (p12SrcField.getText().trim().length() == 0) {
      p12SrcField.requestFocus();
      JOptionPane.showMessageDialog(this,
                                    "A PKCS#12 Filename Must Be Entered!",
                                    "Missing PKCS#12 Source File",
                                    JOptionPane.ERROR_MESSAGE);
      return false;

    } else if (aliasField.getText().trim().length() == 0) {
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
    }

    try {       
      this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      // First make sure the PKCS#12 certificate password is correct
      File p12File = new File(p12SrcField.getText().trim());
      if (!p12File.exists()) {
        p12SrcField.requestFocus();
        JOptionPane.showMessageDialog(this,
                                    "The PKCS#12 Source File '" + p12File.getPath() + "' Does Not Exist!",
                                    "PKCS#12 Source File Does Not Exist",
                                    JOptionPane.ERROR_MESSAGE);
        return false;
      }
      ICertManagerRunTime certmgr = lexicom.getCertManager();
      String password = new String(passwordField.getPassword());
      FileInputStream fin = new FileInputStream(p12File);
      try {
        certmgr.checkPKCS12Password(fin, password);
      } catch (Exception ex) {
        passwordField.requestFocus();
        JOptionPane.showMessageDialog(this,
                                      "The password is incorrect!",
                                      "Incorrect Password",
                                      JOptionPane.ERROR_MESSAGE);
        return false;
      } finally {
        fin.close();
      }

      // Now import the PKCS#12 certificate but don't overwrite it if it is already there
      String alias = aliasField.getText().trim();
      certmgr.importUserPKCS12(alias, p12File, password, false); 
      
      JOptionPane.showMessageDialog(this,
                                      "Certificate '" + alias + "' was successfully imported",
                                      "Certificate Was Successfully Imported",
                                      JOptionPane.INFORMATION_MESSAGE);     
      return true;

    }  catch (Exception ex) {
      JOptionPane.showMessageDialog(this,
                                    "Exception occurred while attempting to\n" +
                                    "import certificate key: " + ex.getMessage(),
                                    "Exception", JOptionPane.ERROR_MESSAGE); 
    } finally {
      this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    return false;
  }    
}
  