import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Cristiano
 */
public class buscarFicheiro {

    private File file;

    public buscarFicheiro() {
    }

    public File transformToFile(String content) throws FileNotFoundException, IOException {
        file = File.createTempFile("tempFile", ".txt");
        file.deleteOnExit();
        

        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(content);
        bw.close();

        return file;
    }

}
