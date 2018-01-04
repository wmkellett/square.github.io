package examples.CertificateManagement;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.cleo.lexicom.external.*;
import com.cleo.lexicom.certmgr.external.*;

/*
 *  This class creates a new user certificate. 
 *  It prompts for the Certificate Alias and Password.
 *  All other certificate parameters are hard-coded.
 */

public class CreateUserCert extends JDialog implements ActionListener, KeyListener {
  
  JFrame parent;
  ILexiCom lexicom;

  JTextField aliasField = new JTextField(); 
  JPasswordField passwordField = new JPasswordField();

  public CreateUserCert(JFrame parent, ILexiCom lexicom) {
    super(parent, true);
    this.parent = parent;
    this.lexicom = lexicom;
    
    this.setTitle("Create New User Certificate");
    this.init();
  }

  private void init() {      
    JPanel panel = new JPanel(new GridBagLayout());
    JPanel buttonPanel = new JPanel(new FlowLayout());

    JLabel aliasLabel = new JLabel("Certificate Alias: ");       
    JLabel passwordLabel = new JLabel("Password: ");

    JButton okButton = new JButton("OK");
    okButton.addActionListener(this);

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(this);

    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);

    aliasField.setPreferredSize(new Dimension(200, 21));
    passwordField.setPreferredSize(new Dimension(200, 21));
    aliasField.addKeyListener(this);

    panel.add(aliasLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
          ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 0, 5), 0, 0));
    panel.add(aliasField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
          ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 0, 0, 5), 0, 0));

    panel.add(passwordLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
          ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
    panel.add(passwordField, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
          ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0));

    panel.add(buttonPanel, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0
          ,GridBagConstraints.SOUTH, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));

    this.setLayout(new BorderLayout());
    this.add(panel, BorderLayout.CENTER);

    this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    this.pack();

    this.setLocationRelativeTo(parent);
    this.setVisible(true);
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals("OK")) {
      if (createCert())
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

  private boolean createCert() {
    // First do some error checking
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
    }

    // Create a new certificate with the supplied alias and password 
    // if it doesn't already exist in the product's certificate store

    String certAlias = aliasField.getText();
    String certPassword = new String(passwordField.getPassword());

    try {
      this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

/*------------------------------------------------------------------------------
 *    Get an instance of the Certificate Manager.
 *
 *    Note: This example only prompts for the alias and password and
 *          all the other certificate parameters are hard-coded.
 *          A real-world application would probably also include
 *          the ability to set these parameters through the user dialog.
 *
 *----------------------------------------------------------------------------*/
      ICertManagerRunTime certmgr = lexicom.getCertManager();
      if (certmgr.getCertificateChain(certAlias) == null) { 
        // All default values will be used unless specified below
        CertificateInfo certinfo = new CertificateInfo();
        certinfo.setCommonName("Demo Signing and Encryption Certificate");
        certinfo.setEmailAddress("demo@cleo.com");
        certinfo.setOrganizationalUnit("EDI Department");
        certinfo.setOrganization("Cleo");
        certinfo.setLocality("Loves Park");
        certinfo.setStateOrProvince("Illinois");
        certinfo.setCountry("US");        
        certinfo.setKeyEncipherment(true);
        certinfo.setDigitalSignature(true);
        certinfo.setStrength(1024);         // 1024 bytes 
        certinfo.setSignatureAlgorithm(1);  // SHA1
        certinfo.setValidFor(24);           // makes the certificate valid for 24 months
        
        certmgr.generateUserCertKey(certAlias, certinfo, certPassword, false);

        JOptionPane.showMessageDialog(this,
                                      "User Certificate '" + certAlias + "' was successfully created",
                                      "Certificate Was Successfully Created",
                                      JOptionPane.INFORMATION_MESSAGE);          
        return true;

      }  else {
        JOptionPane.showMessageDialog(this, "Cert '" + certAlias + "' already exists!",                                       
                                      "Certificate Could Not Be Created", 
                                      JOptionPane.ERROR_MESSAGE);
        return false; 
      }  
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this,
                                    "Exception occurred while attempting to\n" +
                                    "generate new certificate: " + ex.getMessage(),
                                    "Exception", JOptionPane.ERROR_MESSAGE);          
    } finally {
      this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    return false;
  }    
}
  