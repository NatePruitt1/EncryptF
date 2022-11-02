package EncryptF;

import javax.crypto.*;
import java.security.*;
import javax.crypto.spec.*;

public class EncryptF {
    static MessageDigest md;

    public static void main(String[] args) throws Exception{
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("NOT AN ALGO");
        }

        md.update("Hello!".getBytes());
        byte[] result = md.digest();

        
    }
}

//THIS CODE IS EXAMPLE ON HOW TO ENCRYPT AND DECRYPT SHIT
// Cipher c = Cipher.getInstance("AES");
// SecretKey k = new SecretKeySpec(result, "AES");
        
// c.init(c.ENCRYPT_MODE, k);
// byte[] enc = c.doFinal("Hey nate!".getBytes());
// System.out.println("Hey nate!");
// System.out.println(new String(enc));
// c.init(c.DECRYPT_MODE, new SecretKeySpec(result, "AES"));
// enc = c.doFinal(enc);
// System.out.println(new String(enc));