package EncryptF;

import java.util.LinkedList;
import java.util.Arrays;

import java.io.File;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

//this holds the encryption utils
//including encrypt, decrypt, create key, etc
//TODO: Make data.crypt a serialized tree, and turn the file structure into a tree
public class CryptUtil {

    private static LinkedList<String> workingDirectory;
    private static String workingDirectoryString;

    private static String dirFlag = "y";
    private static String fileFlag = "n";

    public static byte[] encryptBytes(byte[] u, SecretKey k) {
        Cipher cipher = null;
        try{
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

            cipher.init(Cipher.ENCRYPT_MODE, k);

            return cipher.doFinal(u);

        } catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] decryptBytes(byte[] u, SecretKey k) {
        Cipher cipher = null;
        try{
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

            cipher.init(Cipher.DECRYPT_MODE, k);

            return cipher.doFinal(u);

        
        } catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public static SecretKey getKeyFromPassword(String p1, String p2) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String password = new String(md.digest(p1.getBytes()));
        String salt = new String(md.digest(p2.getBytes()));

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec)
            .getEncoded(), "AES");
        return secret;
    }

    /*
     * PRE: exists one and only one .crypt file in the root directory
     */
    public static void encryptFile(String filePath, SecretKey k) throws Exception {
        //check if we are trying to encrypt a .crypt file
        String[] fileArray = filePath.split("/");
        String[] fileNameArray = fileArray[fileArray.length - 1].split(".");
        String fileName = fileNameArray[fileNameArray.length - 1];
        if(fileName.equals("crypt")) return;
        
        //Get the crypt file
    }

    public static void decryptFile(String filePath, SecretKey k) throws Exception {
        String[] fileArray = filePath.split("/");
        if(fileArray[fileArray.length - 1].equals("data.crypt")) return;
        byte[] fileData = FileUtil.getFileData(filePath);
        fileData = decryptBytes(fileData, k);
        FileUtil.writeFileData(filePath, fileData);
    }

    public static String getPasswordHashString(String pass) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return new String(md.digest(pass.getBytes()));
    }

    public static void encryptDirectory(String rootDir, SecretKey k) throws Exception {
        File f = new File(rootDir);

        if(f.isDirectory()){

            LinkedList<byte[]> dataQueue = new LinkedList<byte[]>();//queue

            //call get directory bytes on the root
            byte[] dirData = getDirectoryBytes(rootDir + "/", k);
            byte[] dirPathBytes = rootDir.getBytes();
            long dirPathLen = dirPathBytes.length;
            long dirDataLen = dirData.length;

            dataQueue.add(longToBytes(dirPathLen));
            dataQueue.add(longToBytes(dirDataLen));
            dataQueue.add(dirFlag.getBytes());
            dataQueue.add(dirPathBytes);
            dataQueue.add(dirData);
            dataQueue.add("<".getBytes());//add the poptag

            int dataSize = 0;
            for(int i = 0; i < dataQueue.size(); i++){
                dataSize += dataQueue.get(i).length;
            }

            byte[] cryptData = new byte[dataSize];
            int counter = 0;
            while(!dataQueue.isEmpty()){
                byte[] popData = dataQueue.remove();
                for(int i = 0; i < popData.length; i++){
                    cryptData[counter] = popData[i];
                    counter += 1;
                }
            }

            //printCryptData(cryptData);
        }
        //when we pop the final thing, we know to stop in decrypt
    }

    //recursive
    public static byte[] getDirectoryBytes(String directoryPath, SecretKey k) throws Exception {
        LinkedList<byte[]> dataQueue = new LinkedList<byte[]>();//queue
        //directory path is absolute here
        //add the directory data
        //the path we add will not be though
        //get adn add path name data

        //the workingDir is pushed onto because of the 1 flag
        //get all the files and things from this directory
        File dir = new File(directoryPath);
        String[] dirFiles = dir.list();

        LinkedList<String> fileStack = new LinkedList<String>();
        LinkedList<String> dirStack = new LinkedList<String>();

        for(String f : dirFiles){
            File tempFile = new File(dir, f);
            if(tempFile.isDirectory()) dirStack.add(f);
            else if(tempFile.isFile()) fileStack.add(f);
        }

        //process files and add them
        while(!fileStack.isEmpty()){
            String filePath = fileStack.removeLast();
            long filePathLen = filePath.getBytes().length;
            File tempFile = new File(dir, filePath);
            byte[] fileContents = Files.readAllBytes(tempFile.toPath());
            long fileLen = fileContents.length;

            dataQueue.add(longToBytes(filePathLen));
            dataQueue.add(longToBytes(fileLen));
            dataQueue.add(fileFlag.getBytes());
            dataQueue.add(filePath.getBytes());
            dataQueue.add(fileContents);
        }
        //process dirs and add them(with this function)

        while(!dirStack.isEmpty()){
            String dirPath = dirStack.removeLast();
            byte[] dirData = getDirectoryBytes(directoryPath + dirPath + "/", k);
            byte[] dirPathBytes = dirPath.getBytes();
            long dirPathLen = dirPathBytes.length;
            long dirDataLen = dirData.length;

            dataQueue.add(longToBytes(dirPathLen));
            dataQueue.add(longToBytes(dirDataLen));
            dataQueue.add(dirFlag.getBytes());
            dataQueue.add(dirPathBytes);
            dataQueue.add(dirData);
            dataQueue.add("<".getBytes());//add the poptag
        }

        int dataSize = 0;
        for(int i = 0; i < dataQueue.size(); i++){
            dataSize += dataQueue.get(i).length;
        }

        byte[] returnData = new byte[dataSize];
        int counter = 0;
        while(!dataQueue.isEmpty()){
            byte[] popData = dataQueue.remove();
            for(int i = 0; i < popData.length; i++){
                returnData[counter] = popData[i];
                counter += 1;
            }
        }

        return returnData;
    }

    public static void decryptDirectory(String rootDir, SecretKey k) throws Exception {
        File f = new File(rootDir);
        String[] files = f.list();
        //base cases: empty directory, or no directories in directory
        if(files.length < 1) return;
        LinkedList<String> fileStack = new LinkedList<String>();

        //directories are recursively called upon, files are put on a stack to be processed
        for(String file : files){
            if(new File(rootDir + file).isDirectory()){
                System.out.println("Decrypting Directory: " + rootDir + file);
                decryptDirectory(rootDir + file + "/", k);
                continue;
            }
            
            fileStack.push(rootDir + file);
        }

        while(!fileStack.isEmpty()){
            String toEnc = fileStack.pop();
            System.out.println("Decrypting File: " + toEnc);
            decryptFile(toEnc, k);
        }
    }

    //long to bytes and back
    public static byte[] longToBytes(long l) {
        byte[] result = new byte[Long.BYTES];
        for (int i = Long.BYTES - 1; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>= Byte.SIZE;
        }
        return result;
    }
    
    public static long bytesToLong(final byte[] b) {
        long result = 0;
        for (int i = 0; i < Long.BYTES; i++) {
            result <<= Byte.SIZE;
            result |= (b[i] & 0xFF);
        }
        return result;
    }

    //test utils
    //wrapper
}
