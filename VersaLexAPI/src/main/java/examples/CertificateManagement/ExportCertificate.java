package examples.CertificateManagement;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import javax.security.cert.*;

import com.cleo.lexicom.external.*;
import com.cleo.lexicom.certmgr.external.*;

/*
 *  This class exports a CA certificate to the desired file path. 
 */

class ExportCertificate extends JDialog implements ActionListener {
 
  JFrame parent;
  ILexiCom lexicom;
  String productPath;
  
  JTextField commonNameField = new JTextField();
  JTextField certDestField = new JTextField(); 

  public ExportCertificate(JFrame parent, ILexiCom lexicom, String productPath) {
    super(parent, true);
    this.parent = parent;
    this.lexicom = lexicom;
    this.productPath = productPath;
        
    this.setTitle("Export Certificate");
    this.init();
  }

  private void init() {
  JPanel panel = new JPanel(new GridBagLayout());
    JPanel buttonPanel = new JPanel(new FlowLayout());

    JLabel commonNameLabel = new JLabel("Certificate Common Name: ");  
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

    commonNameField.setPreferredSize(new Dimension(200, 21));
    certDestField.setPreferredSize(new Dimension(200, 21));

    panel.add(commonNameLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
          ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 0, 5), 0, 0));
    panel.add(commonNameField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
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
      String dest = chooser.getSelectedFile().getPath();
      if (!dest.toLowerCase().endsWith(".cer") && 
          !dest.toLowerCase().endsWith(".zip")) {
        dest += File.separator;
      }      
      certDestField.setText(dest);      
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

  private boolean exportCert() {
    // Validate all the fields have been entered
    if (commonNameField.getText().trim().length() == 0) {
      commonNameField.requestFocus();
      JOptionPane.showMessageDialog(this,
                                    "The Certificate Common Name Must Be Entered!",
                                    "Missing Certificate Common Name",
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
    
    try {       
      // First determine how many files need to be exported  
      this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      ICertManagerRunTime certmgr = lexicom.getCertManager();
      byte[][] certs = certmgr.getCACertificates();  
      if (certs.length == 0) {      
        JOptionPane.showMessageDialog(this,
                                      "No Certificates were found in the certificate store!",
                                      "No Certificates Found",
                                      JOptionPane.ERROR_MESSAGE);
        return false;
      }
      String commonName = commonNameField.getText().trim();
            
      // Walk through the CA certificates and find all that match
      // the common name that was supplied.  This example does not 
      // enforce case sensitivity.  Also note that there are other ways
      // to match certificates other than using the "Common Name"
      // depending on what is already known upfront (e.g., the file name
      // or the certificate serial number.

      Vector certVector = new Vector();
      for (int i=0; i < certs.length; i++) {
         // Add matching certs to the vector as a ByteArrayInputStream
        X509Certificate cert = X509Certificate.getInstance(certs[i]);                       
        if (commonNameMatches(cert, commonName)) {        
          certVector.add(new ByteArrayInputStream(certs[i]));
        }
      }
      
      if (certVector.size() == 0) {
        commonNameField.requestFocus();
        JOptionPane.showMessageDialog(this,
                                      "No CA certificates matched the supplied 'Common Name'!",
                                      "Matching CA Certificates Not Found",
                                      JOptionPane.ERROR_MESSAGE);
        return false;
      }

      // If more than one CA certificate is found, all matching certificates will be stored in a zip file

      String ext = (certVector.size() == 1) ? ".cer" : ".zip";
      
      // If the file name does not end with an extension, add it to the file name
      if (!dest.getPath().toLowerCase().endsWith(ext)) {
        dest = new File(certDestField.getText() + ext);
      }  

      // If the file already exists, ask if it should be overwritten        
      if (dest.exists()) {
        String msg = "The certificate destination file '" + dest.getName() + "'\n" +
                     " already exists in the '" + dest.getParent() + "' folder.\n\n" +
                     "Do you want to overwrite it?";

        if (JOptionPane.showOptionDialog(this, msg, "Certificate File Already Exists",
                                         JOptionPane.YES_NO_OPTION,
                                         JOptionPane.QUESTION_MESSAGE,
                                         null, null, null) == JOptionPane.NO_OPTION)
          return false;
      }

      exportCerts(certVector, dest, commonName);
      
      String title =  (certVector.size() == 1) ? "Certificate Was" : "Certificates Were";
      String msg = (certVector.size() == 1) ? "The certificate was" : certVector.size() + " certificates were";
      JOptionPane.showMessageDialog(this,
                                    msg + " successfully exported to \n" + dest.getPath(),
                                    "CA " + title + " Successfully Exported",
                                    JOptionPane.INFORMATION_MESSAGE); 
      return true;

    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this,
                                    "Exception occurred while attempting to\n" +
                                    "export the CA certificate: " + ex.getMessage(),
                                    "Exception", JOptionPane.ERROR_MESSAGE); 
    } finally {
      this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }    
    return false;
  }
  
  private boolean commonNameMatches(X509Certificate cert, String commonName) {
    // Parse out the 'CN=' portion of the distinguished name
    String DN = cert.getSubjectDN().toString();        
    int idx1 = DN.indexOf("CN=");
    if (idx1 < 0) {
      // Try in lower case
      idx1 = DN.indexOf("cn=");
      // There doesn't appear to be a CommonName in the SubjectDN so we can't make a match
      if (idx1 < 0)
        return false;
    }
    // Look for an ending comma, otherwise we will take everything to the EOL
    int idx2 = DN.indexOf(",", idx1);
    if (idx2 < 0)
      idx2 = DN.length();
    String thisCommonName = DN.substring(idx1+3, idx2);
    if (thisCommonName.equalsIgnoreCase(commonName))
      return true;
    else
      return false;
  }
  
  private void exportCerts(Vector certs, File destFile, String commonName) throws Exception {
    int numRead;
    byte[] bytes = new byte[1024];  
  
    if (certs.size() == 1) {
      // Write the single CA certificate to the destination file            
      ByteArrayInputStream bis = (ByteArrayInputStream)certs.get(0);      
      FileOutputStream fout = new FileOutputStream(destFile);
      while ((numRead = bis.read(bytes)) > 0) {
        fout.write(bytes, 0, numRead);
      }
      bis.close();
      fout.close();
      
    } else {
      // Zip all the CA certificates up and store in the destination file
      ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destFile)));      
      int idx = destFile.getName().lastIndexOf(".zip");
      String name = destFile.getName().substring(0, idx);
     
      for (int i=0; i < certs.size(); i++) {
        ByteArrayInputStream bis = (ByteArrayInputStream)certs.get(i);   
        ZipEntry entry = new ZipEntry(alterString(commonName) + (i+1) + ".cer");
         try {
            zos.putNextEntry(entry);
            while ( (numRead = bis.read(bytes)) > 0) {
              zos.write(bytes, 0, numRead);
            }
          } catch(ZipException e) {
            // Ignore "duplicate entry" exceptions but throw all other exceptions
            if (e.getMessage().indexOf("duplicate entry") < 0) {              
              throw e;
            }
          } finally {
            bis.close();
            zos.closeEntry();
         }         
      }  
      zos.close();
    }
  }

  private String alterString(String string) {
    // Remove quotes from the string and replace all embedded spaces,
    // backslashes, forward slashes, asterisks, quotes, colons,
    // semi-colons, commas and angle brackets, etc., with underscores
    // This is needed when creating a file name from an arbitrary string.

    String temp1String = string.replace(' ','_');
    String temp2String = temp1String.replace('\\','_');
    String temp3String = temp2String.replace('/','_');
    String temp4String = temp3String.replace('\"','_');
    String temp5String = temp4String.replace('*','_');
    String temp6String = temp5String.replace('<','_');
    String temp7String = temp6String.replace('>','_');
    String temp8String = temp7String.replace(':','_');
    String temp9String = temp8String.replace(',','_');
    String temp10String = temp9String.replace(';','_');
    String temp11String = temp10String.replace('?','_');
    String resultingString = temp11String.replace('#','_');

    return resultingString;
  }  
}