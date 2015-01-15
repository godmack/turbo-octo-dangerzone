import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.services.drive.Drive;

import com.google.api.services.drive.Drive.Children;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.FileList;

import com.google.api.services.drive.model.File;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Rúben
 */
public class MenuPrincipal extends javax.swing.JFrame {

    /**
     * Creates new form Main
     */
    //usar o 13 da aula
    private Drive service;
    private List<File> files;
    private About about;
    File currentFolder = new File();
    File openedFile = new File();
    DefaultListModel listModelOriginal = new DefaultListModel();
    DefaultListModel listModelChanged = new DefaultListModel();

    public MenuPrincipal(Drive service) throws IOException {
        initComponents();
        this.service = service;
        files = new LinkedList<File>();
        System.out.println(service);
        about = service.about().get().execute();
        showFilesInRoot(retrieveAllFiles(service));
        
        
        
        
        //DELETE
        /*byte[] usedPass = cifrarDados_1AES("C:\\Users\\Cristiano\\Desktop\\andre.txt");
        cifrarDados_2CERT(usedPass,"C:\\Users\\Cristiano\\Desktop\\2111226@my.ipleiria.pt.cer");
        
        InputStream encryptedFileStream = new FileInputStream("encrypted");
        InputStream aesPassFileStream = new FileInputStream("encrypted.pass");
        byte[] foundPass = decifrarDados_1CERT(aesPassFileStream, "C:\\Users\\Cristiano\\Desktop\\2111226@my.ipleiria.pt.pfx");
        decifrarDados_2AES(encryptedFileStream, foundPass);*/
    }

    private static List<File> retrieveAllFiles(Drive service) throws IOException {
        System.out.println("Entrei!");
        List<File> result = new ArrayList<File>();
        String query = "trashed=false";
        Files.List request = service.files().list().setQ(query);
        do {
            try {
                FileList files = request.execute();

                result.addAll(files.getItems());
                request.setPageToken(files.getNextPageToken());
                System.out.println(request.setPageToken(files.getNextPageToken()));
            } catch (IOException e) {
                System.out.println("An error occurred: " + e);
                request.setPageToken(null);
            }
        } while (request.getPageToken() != null
                && request.getPageToken().length() > 0);
        System.out.println("Vou sair");
        return result;
    }

    private void showFilesInRoot(List<File> files) throws IOException {
        System.out.println(files);
        this.files = files;
        listModelChanged.clear();
        listModelOriginal.clear();
        currentFolder.setId(about.getRootFolderId());
        for (File file : files) {
            if (isFileInFolder(service, about.getRootFolderId(), file.getId())) {
                if (isFolder(service, file.getId())) {
                    listModelChanged.addElement(file.getTitle() + " - Folder");
                } else {
                    listModelChanged.addElement(file.getTitle() + " - File");
                }
                listModelOriginal.addElement(file.getTitle());
            }
        }
        jList1.setModel(listModelChanged);

//         File file = (File) jList1.getSelectedValue();
//         file.getId();
    }

    private void showFiles(String folderID) throws IOException {
        listModelChanged.clear();;
        listModelOriginal.clear();
        //parentFolder.setId(currentFolder.getId());
        currentFolder.setId(folderID);
        for (File file : this.files) {
            if (isFileInFolder(service, folderID, file.getId())) {
                if (isFolder(service, file.getId())) {
                    if(!file.getTitle().equals("Certificados")){
                        listModelChanged.addElement(file.getTitle() + " - Folder");
                    }
                } else {
                    listModelChanged.addElement(file.getTitle() + " - File");
                }
                listModelOriginal.addElement(file.getTitle());

            }
        }
        jList1.setModel(listModelChanged);
    }

    private boolean isFolder(Drive service, String fileID) throws IOException {

        String query = "mimeType='application/vnd.google-apps.folder' and trashed=false";
        Files.List request = service.files().list().setQ(query);
        FileList files = request.execute();
        List<File> result = new ArrayList<File>();
        result.addAll(files.getItems());

        for (File file : result) {
            if (file.getId().equals(fileID)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isFileInFolder(Drive service, String folderId,
            String fileId) throws IOException {
        try {
            service.parents().get(fileId, folderId).execute();
        } catch (HttpResponseException e) {
            if (e.getStatusCode() == 404) {
                return false;
            } else {
                System.out.println("An error occured: " + e);
                throw e;
            }
        } catch (IOException e) {
            System.out.println("An error occured: " + e);
            throw e;
        }
        return true;
    }

    private static void printFilesInFolder(Drive service, String folderId)
            throws IOException {
        Children.List request = service.children().list(folderId);

        do {
            try {
                ChildList children = request.execute();
                for (ChildReference child : children.getItems()) {
                    System.out.println("File Id: " + child.getId());
                }
                request.setPageToken(children.getNextPageToken());
            } catch (IOException e) {
                System.out.println("An error occurred: " + e);
                request.setPageToken(null);
            }
        } while (request.getPageToken() != null
                && request.getPageToken().length() > 0);
    }

    private static InputStream downloadFile(Drive service, File file) {
        if (file.getDownloadUrl() != null && file.getDownloadUrl().length() > 0) {
            try {
                HttpResponse resp
                        = service.getRequestFactory().buildGetRequest(new GenericUrl(file.getDownloadUrl()))
                        .execute();
                return resp.getContent();
            } catch (IOException e) {
                // An error occurred.
                e.printStackTrace();
                return null;
            }
        } else {
            // The file doesn't have any content stored on Drive.
            return null;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(jList1);

        jButton1.setText("Abrir");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel1.setText("Documentos");

        jButton2.setText("Criar Ficheiro");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane2.setViewportView(jTextArea1);

        jLabel2.setText("Ficheiro:");

        jButton3.setText("Root");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Guardar Alteracoes");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel3.setText(" ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(jButton1)
                                .addGap(40, 40, 40)
                                .addComponent(jButton3))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(39, 39, 39)
                                .addComponent(jButton2)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton4)
                        .addGap(88, 88, 88))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 63, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(21, 21, 21))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(19, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane2)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton1)
                            .addComponent(jButton3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jButton4)
                        .addGap(32, 32, 32))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        int index = jList1.getSelectedIndex();
        for (File file : files) {
            if (file.getTitle().equals(listModelOriginal.getElementAt(index))) {
                try {
                    if (isFolder(service, file.getId())) {
                        showFiles(file.getId());

                    } else {
                        InputStream ficheiro = downloadFile(service, file);
                        String content = getStringFromInputStream(ficheiro);
                        openedFile = file;
                        jLabel3.setText(file.getTitle());
                        jTextArea1.setText(content);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(MenuPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

    public byte[] cifrarDados_1AES(String originalDataFilePath) {
        //
        // AES Encrypt
        //
        java.io.File originalDataFile = new java.io.File(originalDataFilePath);
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            
            SecretKey sec = keyGen.generateKey();
            Cipher aesCipher = Cipher.getInstance("AES");
            
            //get original data
            byte[] bytesOriginal = new Scanner(originalDataFile).useDelimiter("\\Z").next().getBytes();
            
            //encrypt and save data
            aesCipher.init(Cipher.ENCRYPT_MODE, sec);
            byte[] bytesEncrypted = aesCipher.doFinal(bytesOriginal);
            FileOutputStream fout = new FileOutputStream(new java.io.File("encrypted"));
            fout.write(bytesEncrypted);
            fout.flush();
            fout.close();
            
            //save AES pass <-- para encriptar com o cert(!)
            return sec.getEncoded();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(MenuPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(MenuPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(MenuPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MenuPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(MenuPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(MenuPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MenuPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new byte[0];
    }
    
    
    public void cifrarDados_2CERT(byte[] aesPass,
                                  String CertKeyFilePath) { //<-- person to share with
        try {
            //
            // CERT ENCRYPT
            //
            //load cert
            FileInputStream fisCert = new FileInputStream(CertKeyFilePath);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate)cf.generateCertificate(fisCert);
            
            //init provider
            RSAPublicKey rsaPublicKey = (RSAPublicKey)cert.getPublicKey();
            BouncyCastleProvider bcp = new BouncyCastleProvider();
            Security.addProvider(bcp);
            
            //init cipher
            Cipher encCipher = Cipher.getInstance("RSA",bcp);
            encCipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
            
            //encrypt
            byte[] encAesPass = encCipher.doFinal(aesPass);
            FileOutputStream fout = new FileOutputStream(new java.io.File("encrypted.pass"));
            fout.write(encAesPass);
            fout.flush();
            fout.close();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MenuPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CertificateException ex) {
            Logger.getLogger(MenuPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(MenuPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(MenuPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(MenuPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(MenuPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(MenuPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MenuPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    
    public byte[] decifrarDados_1CERT(InputStream aesPassFileStream,
                                    String pfxFilePath) {

        //
        // CERT DECRYPT
        //

        return new byte[0];

    }
    
    public void decifrarDados_2AES(InputStream encryptedFileStream,
                                   byte[] aesPass) {
        
        try {
            //
            // AES decrypt
            //

            //get byte arrays - ecrypted file
            int nRead;
            byte[] tempbytes = new byte[1024];
            ByteArrayOutputStream encrFileBuff = new ByteArrayOutputStream();
            while ((nRead = encryptedFileStream.read(tempbytes, 0, tempbytes.length)) != -1) {
                encrFileBuff.write(tempbytes, 0, nRead);
            }
            encrFileBuff.flush();
            byte[] encrFileByteArray = encrFileBuff.toByteArray();//(!)
            
            //get byte arrays - aes pass
            //byte[] secByteArray = new byte[16];//(!)
            //aesPassFileStream.read(secByteArray, 0, secByteArray.length);
            
            //init AES
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            SecretKey sec = new SecretKeySpec(aesPass, "AES");
            Cipher aesCipher = Cipher.getInstance("AES");

            //decrypt with aes and save data
            aesCipher.init(Cipher.DECRYPT_MODE, sec);
            byte[] bytesOriginal = aesCipher.doFinal(encrFileByteArray);
            FileOutputStream fout = new FileOutputStream(new java.io.File("decrypted"));
            fout.write(bytesOriginal);
            fout.flush();
            fout.close();
            
        } catch (IOException ex) {
            Logger.getLogger(MenuPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(MenuPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(MenuPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(MenuPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(MenuPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(MenuPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static ChildReference insertFileIntoFolder(Drive service, String folderId,
            String fileId) {
        ChildReference newChild = new ChildReference();
        newChild.setId(fileId);
        try {
            return service.children().insert(folderId, newChild).execute();
        } catch (IOException e) {
            System.out.println("An error occurred: " + e);
        }
        return null;
    }

    private static void removeFileFromFolder(Drive service, String folderId,
            String fileId) {
        try {
            service.parents().delete(fileId, folderId).execute();
        } catch (IOException e) {
            System.out.println("An error occurred: " + e);
        }
    }


    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES", "txt", "text");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {

            java.io.File ficheiro = chooser.getSelectedFile();

            File body = new File();

            String titulo = JOptionPane.showInputDialog(this, "Introduza o titulo do ficheiro");
            body.setTitle(titulo);
            String descricao = JOptionPane.showInputDialog(this, "Introduza uma descricao");
            body.setDescription(descricao);
            body.setMimeType("text/plain");

            java.io.File fileContent = new java.io.File(ficheiro.getPath());

            FileContent mediaContent = new FileContent("text/plain", fileContent);

            try {
                File file = service.files().insert(body, mediaContent).execute();

                insertFileIntoFolder(service, currentFolder.getId(), file.getId());

                if (!currentFolder.getId().equals(about.getRootFolderId())) {
                    removeFileFromFolder(service, about.getRootFolderId(), file.getId());
                }
            } catch (IOException ex) {
                Logger.getLogger(MenuPrincipal.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        try {
            showFilesInRoot(retrieveAllFiles(service));
        } catch (IOException ex) {
            Logger.getLogger(MenuPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed

        buscarFicheiro bf = new buscarFicheiro();
        java.io.File fileContent;
        try {
            fileContent = bf.transformToFile(jTextArea1.getText());
            FileContent mediaContent = new FileContent(openedFile.getMimeType(), fileContent);
            service.files().update(openedFile.getId(), openedFile, mediaContent).execute();
        } catch (IOException ex) {
            Logger.getLogger(MenuPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }


    }//GEN-LAST:event_jButton4ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables
}
