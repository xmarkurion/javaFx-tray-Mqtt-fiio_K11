package org.markurion.headphonetray;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Add {

    public static void main(String[] args) {
        new Add();
    }

    public Add() {
        checkFiles();
    }

    //method that checks if in the jar folder are files like svcl.exe, svcl.chm
    public void checkFiles() {
        System.out.println("Check..");
        ifNotExistCopy("svcl.exe", "/zip/svcl.exe", "svcl.exe");
        ifNotExistCopy("svcl.chm", "/zip/svcl.exe", "svcl.chm");

        createFolder("lib");
        ifNotExistCopy("lib/audioDevice.jar", "/zip/lib/audioDevice.jar", "lib/audioDevice.jar");
    }

    public void createFolder(String folder) {
        if (!Files.exists(Paths.get(folder))) {
            try {
                Files.createDirectories(Paths.get(folder));
            } catch (IOException ex) {
                Logger.getLogger(Add.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public boolean ifNotExistCopy(String file, String sourcePath, String destinationPath) {
        if (!Files.exists(Paths.get(file))) {
            System.out.println("File " + file + " not found. Copying from jar to folder.");
            try {
                Files.copy(getClass().getResourceAsStream(sourcePath), Paths.get(destinationPath), StandardCopyOption.REPLACE_EXISTING);
                return true;
            } catch (IOException ex) {
                Logger.getLogger(Add.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("File " + file + " found. No need to copy");
        return false;
    }

}
