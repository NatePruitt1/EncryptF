package EncryptF;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

//get files, recusively(loop variant) get folder contents
//store passwords and data in Data.crypt, and check them from a Data.crypt file
public class FileUtil {
    public static byte[] getFileData(String filename) throws Exception {
        Path path = Paths.get(filename);
        return Files.readAllBytes(path);
    }

    public static void writeFileData(String filename, byte[] fileData) throws Exception {
        Path path = Paths.get(filename);
        Files.write(path, fileData);
    }

    public static void createFile(String filename) throws Exception {
        Path path = Paths.get(filename);
        Files.createFile(path);
    }

    public static boolean isDirectory(String filename) throws Exception {
        File f = new File(filename);
        return f.isDirectory();
    }

    /* If Data.crypt is a file, check if the passwords are there
     * If Data.crypt is not a file, add the passwords to it and return true(first time)
     */
    public static boolean checkPasswords(String rootDir, String pass1, String pass2) throws Exception {
        //TODO: add functionality to if the data.crypt file does not exist and the files are encrypted.
        File dataFile = new File(rootDir + "data.crypt");

        if(dataFile.exists()){
            //password file exists, pass1 hash is the first line pass2 hash is the second line
            
            byte[] data = Files.readAllBytes(dataFile.toPath());
            
            byte[] pass1Hash = CryptUtil.getPasswordHashString(pass1).getBytes();
            byte[] pass2Hash = CryptUtil.getPasswordHashString(pass2).getBytes();


            for(int i = 0; i < 32; i++){
                if(pass1Hash[i] != data[i] || pass2Hash[i] != data[i+32]) return false;
            }

            dataFile.delete();

            return true;
        }  
        else {
            //create the file
            
            Path dataFilePath = Paths.get(rootDir + "data.crypt");
            dataFilePath = Files.createFile(dataFilePath);
            dataFile = new File(dataFilePath.toString());

            String pass1Hash = CryptUtil.getPasswordHashString(pass1);
            String pass2Hash = CryptUtil.getPasswordHashString(pass2);

            String fileString = pass1Hash + pass2Hash;

            Files.write(dataFile.toPath(), fileString.getBytes());

            return true;
        }
    }

}
