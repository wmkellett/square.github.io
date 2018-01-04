package examples.CertificateManagement;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.cleo.lexicom.external.*;
import com.cleo.lexicom.certmgr.external.*;

/*
 *  This class imports a user certificate from the desired file path. 
 */

class ImportCertificate extends JDialog implements ActionListener {
  
  JFrame parent;
  ILexiCom lexicom;
  String productPath;

  JTextField certField = new JTextField(); 

  public ImportCertificate(JFrame parent, ILexiCom lexicom, String productPath) {
    super(parent, true);
    this.parent = parent;
    this.lexicom = lexicom;
    this.productPath = productPath;
    
    this.setTitle("Import Certificate");
    this.init();
  }
  private void init() {
    JPanel panel = new JPanel(new GridBagLayout());
    JPanel buttonPanel = new JPanel(new FlowLayout());

    JLabel certLabel = new JLabel("Certificate Source: ");  
    JButton browseButton = new JButton("...");
    browseButton.addActionListener(this);
    browseButton.setActionCommand("Browse");

    JButton okButton = new JButton("OK");
    okButton.addActionListener(this);

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(this);

    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);

    certField.setPreferredSize(new Dimension(200, 21));

    panel.add(certLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
          ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 0, 5), 0, 0));
    panel.add(certField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
          ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 0, 0, 5), 0, 0));
    panel.add(browseButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
          ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 0, 0, 5), 0, 0));

    panel.add(buttonPanel, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0
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
      if (importCert())
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
      certField.setText(chooser.getSelectedFile().getPath());
    }
  }

  private boolean importCert() {
    String certPath = certField.getText();
    if (certPath.trim().length() == 0) {
      certField.requestFocus();
      JOptionPane.showMessageDialog(this,
                                    "A Certificate Path Must be Entered!",
                                    "Missing Certificate Path",
                                    JOptionPane.ERROR_MESSAGE);
      return false;
    }
    try {
      // First make sure that this is a valid certificate
      this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      ICertManagerRunTime certmgr = lexicom.getCertManager();
      certmgr.readCert(certPath);

      // If we could successfully read the file, copy it to 
      // the product's cert folder
      File certSrcFile = new File(certPath);
      File certDestFile = new File(productPath + File.separator + "certs", certSrcFile.getName());

      // Check for the existence of the destination file and alert the user
      if (certDestFile.exists()) {
        String msg = "The certificate '" + certDestFile.getName() + "' already\n" +
                     "exists in the '" + certDestFile.getParent() + "' folder.\n\n" +
                     "Do you want to overwrite it?";

        if (JOptionPane.showOptionDialog(this, msg, "Certificate Already Exists",
                                         JOptionPane.YES_NO_OPTION,
                                         JOptionPane.QUESTION_MESSAGE,
                                         null, null, null) == JOptionPane.NO_OPTION)
          return false;
      }
      FileInputStream fin = new FileInputStream(certSrcFile);        
      FileOutputStream fout = new FileOutputStream(certDestFile);

      int numRead;
      byte[] buf = new byte[1024];
      while ((numRead = fin.read(buf)) > 0) {
        fout.write(buf, 0, numRead);
      }

      fin.close();
      fout.close();

      JOptionPane.showMessageDialog(this,
                                    "Certificate '" + certSrcFile.getName() + "' was successfully imported",
                                    "Certificate Was Successfully Imported",
                                    JOptionPane.INFORMATION_MESSAGE);
      return true;

    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this,
                                    "Exception occurred while attempting to\n" +
                                    "import new certificate: " + ex.getMessage(),
                                    "Exception", JOptionPane.ERROR_MESSAGE); 
    } finally {
      this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    return false;
  }
}
  
