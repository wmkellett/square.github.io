package examples.CertificateManagement;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.cleo.lexicom.external.*;

/*
 *  This is the main class and provides the GUI to invoke the
 *  Certificate Management API examples included in this package. 
 *
 *  This example application attaches to VLTrader which must be running.
 *
 */

public class CertificateAPIDemo extends JFrame implements ActionListener {
  JScrollPane scrollPane = new JScrollPane();
  JPanel mainPanel = new JPanel(new GridBagLayout());
  JPanel buttonPanel = new JPanel(new FlowLayout());
  
  JLabel label = new JLabel("Select Certificate Option:");
  
  JRadioButton createUserCert = new JRadioButton("Create User Certificate/Private Key");  
  JRadioButton importPrivateKey = new JRadioButton("Import User Certificate/Private Key");
  JRadioButton importCert = new JRadioButton("Import Certificate");
  JRadioButton exportPrivateKey = new JRadioButton("Export User Certificate/Private Key");
  JRadioButton exportUserCert = new JRadioButton("Export User Certificate");
  JRadioButton exportCert = new JRadioButton("Export Certificate");
      
  JButton selectButton = new JButton("Select");
  JButton quitButton = new JButton("Quit");
              
  ILexiCom lexicom = null;
    
  String productPath = null;
  
  public CertificateAPIDemo(String productPath) throws Exception {

    super("Cleo Certificate Management API Demonstration");
    
    this.productPath = productPath;
    
/*------------------------------------------------------------------------------
 *    Get a client instance of VLTRADER.  
 *
 *    To run against LexiCom instead, change the first parameter 
 *    to LexiComFactory.LEXICOM.
 *
 *----------------------------------------------------------------------------*/
    lexicom = LexiComFactory.getVersaLex(LexiComFactory.VLTRADER,
                                         productPath, LexiComFactory.CLIENT_ONLY);
    this.initDisplay();
  }
  
  private void initDisplay() {
    mainPanel.setPreferredSize(new Dimension(325, 275));
    
    ButtonGroup certOptions = new ButtonGroup();
    certOptions.add(createUserCert);    
    certOptions.add(importPrivateKey);
    certOptions.add(importCert);
    certOptions.add(exportPrivateKey);
    certOptions.add(exportUserCert);
    certOptions.add(exportCert);
        
    createUserCert.setFocusable(false);
    importCert.setFocusable(false);
    importPrivateKey.setFocusable(false);
    exportUserCert.setFocusable(false);
    exportCert.setFocusable(false);
    exportPrivateKey.setFocusable(false);
        
    selectButton.addActionListener(this);
    selectButton.setActionCommand("Select");
    
    quitButton.addActionListener(this);
    quitButton.setActionCommand("Quit");
        
    buttonPanel.add(selectButton);            
    buttonPanel.add(quitButton);
    
    createUserCert.setSelected(true);

    mainPanel.add(label, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 10, 0), 0, 0));
    
    mainPanel.add(createUserCert, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));   
    mainPanel.add(importPrivateKey, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));   
    mainPanel.add(importCert, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));  
    mainPanel.add(exportPrivateKey, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));   
    mainPanel.add(exportUserCert, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));  
    mainPanel.add(exportCert, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));   
           
    mainPanel.add(buttonPanel, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
            ,GridBagConstraints.SOUTH, GridBagConstraints.NONE, new Insets(20, 5, 5, 5), 0, 0));

    scrollPane.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
    scrollPane.getViewport().setView(mainPanel);

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(scrollPane, BorderLayout.CENTER);               
  }
  
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals("Select")) 
      this.performCertOption();      
    else if (e.getActionCommand().equals("Quit"))
      System.exit(0);
  }
  
  private void performCertOption() {          
     if (createUserCert.isSelected())
       new CreateUserCert(this, lexicom);
     else if (importCert.isSelected()) 
       new ImportCertificate(this, lexicom, productPath);
     else if (importPrivateKey.isSelected()) 
       new ImportPrivateKey(this, lexicom, productPath);
     else if (exportUserCert.isSelected()) 
       new ExportUserCertificate(this, lexicom, productPath);
     else if (exportCert.isSelected()) 
       new ExportCertificate(this, lexicom, productPath);        
     else if (exportPrivateKey.isSelected())
       new ExportPrivateKey(this, lexicom, productPath);
  }
    
  public static void main(String[] args) {   
    /* This class takes an optional argument that specifies the VLTrader path
     * if it is not running in that directory.  If no argument is provided,
     * the current directory is assumed to be the VLTrader (or LexiCom) path.
     */

    try {
      String productPath = null;
      if (args.length == 1)
        productPath = args[0];
      else
        productPath = System.getProperty("user.dir");
       
      UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
                             
      JFrame frame = new CertificateAPIDemo(productPath);
      frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
         System.exit(0);
        }
      });
   
      frame.setIconImage(new ImageIcon(CertificateAPIDemo.class.getResource("image/cert.gif")).getImage());      
      frame.pack();

      frame.setLocationRelativeTo(null);      
      frame.setVisible(true);    
      
    } catch (Exception ex) {
      ex.printStackTrace();
      String message = "The following exception occurred:\n" + ex.getMessage();
      JOptionPane.showMessageDialog(null, message, "Uh oh, there was a problem...", JOptionPane.ERROR_MESSAGE);
    }
  } 
}
