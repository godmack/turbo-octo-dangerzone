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
import static com.sun.org.apache.xml.internal.serialize.OutputFormat.Defaults.Encoding;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

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
    DefaultListModel listModelOriginal = new DefaultListModel();
    DefaultListModel listModelChanged = new DefaultListModel();

    public MenuPrincipal(Drive service) throws IOException {
        initComponents();
        this.service = service;
        files = new LinkedList<File>();
        System.out.println(service);
        about = service.about().get().execute();
        showFilesInRoot(retrieveAllFiles(service));
    }

    private static List<File> retrieveAllFiles(Drive service) throws IOException {
        System.out.println("Entrei!");
        List<File> result = new ArrayList<File>();
        Files.List request = service.files().list();
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
        for (File file : this.files) {
            if (isFileInFolder(service, folderID, file.getId())) {
                if (isFolder(service, file.getId())) {
                    listModelChanged.addElement(file.getTitle() + " - Folder");
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

        jButton2.setText("Send File");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(32, 32, 32)
                                .addComponent(jButton2))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(100, 100, 100)
                        .addComponent(jButton1)))
                .addContainerGap(41, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(22, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(50, 50, 50)
                        .addComponent(jButton2)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1)
                .addGap(32, 32, 32))
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
                        
                        
                       
                    }
//                    printFilesInFolder(service, file.getId());
                } catch (IOException ex) {
                    Logger.getLogger(MenuPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        JFileChooser chooser = new JFileChooser();
    FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES", "txt", "text");
    chooser.setFileFilter(filter);
    int returnVal = chooser.showOpenDialog(this);
    if(returnVal == JFileChooser.APPROVE_OPTION) {
        
            java.io.File ficheiro = chooser.getSelectedFile();
            
            try {
                String path = ficheiro.getPath();
                java.util.Date javaDate = new java.util.Date();
                java.nio.file.Files lol = new java.nio.file.Files();
                
                byte[] data = Files.readAllBytes(path);
                
            
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MenuPrincipal.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
    }
    }//GEN-LAST:event_jButton2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
