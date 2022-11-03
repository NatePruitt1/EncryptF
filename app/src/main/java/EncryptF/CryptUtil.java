package EncryptF;

import java.util.LinkedList;

import java.io.File;
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
public class CryptUtil {
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

    public static SecretKey getKeyFromPassword(String p1, String p2)
        throws NoSuchAlgorithmException, InvalidKeySpecException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String password = new String(md.digest(p1.getBytes()));
        String salt = new String(md.digest(p2.getBytes()));

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec)
            .getEncoded(), "AES");
        return secret;
    }

    public static void encryptFile(String filePath, SecretKey k) throws Exception {
        String[] fileArray = filePath.split("/");
        if(fileArray[fileArray.length - 1].equals("data.crypt")) return;
        byte[] fileData = FileUtil.getFileData(filePath);
        fileData = encryptBytes(fileData, k);
        FileUtil.writeFileData(filePath, fileData);
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
        String[] files = f.list();
        //base cases: empty directory, or no directories in directory
        if(files.length < 1) return;
        LinkedList<String> fileStack = new LinkedList<String>();

        //directories are recursively called upon, files are put on a stack to be processed
        for(String file : files){
            if(new File(rootDir + file).isDirectory()){
                System.out.println("Encrypting Directory: " + rootDir + file);
                encryptDirectory(rootDir + file + "/", k);
                continue;
            }
            
            fileStack.push(rootDir + file);
        }

        while(!fileStack.isEmpty()){
            String toEnc = fileStack.pop();
            System.out.println("Encrypting File: " + toEnc);
            encryptFile(toEnc, k);
        }
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
}
