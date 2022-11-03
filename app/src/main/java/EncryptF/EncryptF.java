package EncryptF;

import java.util.Scanner;

import javax.crypto.SecretKey;

/* 
 * Main tooling of EncryptF package.
 * This should be input logic and work delegation only, other class methods
 * should be used for the actual encrypt, file travesal, password hashing, etc logic. 
 */
public class EncryptF {

    public static void main(String[] args) throws Exception{
        Scanner scanner = new Scanner(System.in);

        //turn into a cli tool
        System.out.print("(e)ncryption or (d)ecryption: ");
        char option = scanner.nextLine().toLowerCase().charAt(0);

        System.out.print("What is working filepath: ");
        String filePath = scanner.nextLine();
        String rootDir = "";//root directory. If filepath is a directory, this will be the filepath, otherwise it will be the folder above the file

        if(FileUtil.isDirectory(filePath)){
            System.out.print("This path is a directory, do you wish to recursively (d)encrypt " + filePath + ": ");
            String answer = scanner.nextLine();
            if(answer.toLowerCase().charAt(0) == 'n'){
                System.out.println("Ending Process");
                scanner.close();
                return;
            }

            //add a slash to the end of directories (for ease of use)
            if(!(filePath.charAt(filePath.length() - 1) == '/' || filePath.charAt(filePath.length() - 1) == '\\')){
                filePath = filePath + "/";
            }
            rootDir = filePath;
        }
        else{
            String[] splitPath = filePath.split("/");
            if(splitPath.length <= 1){
                rootDir = "../";
            }
            else{
                for(int i = 0; i < splitPath.length - 1; i++){
                    rootDir = rootDir + splitPath[i] + "/";
                }
            }
        }

        System.out.print("Enter Password 1: ");
        String pass1 = scanner.nextLine();

        System.out.print("Enter Password 2: ");
        String pass2 = scanner.nextLine();

        scanner.close();

        SecretKey key = CryptUtil.getKeyFromPassword(pass1, pass2);

        if(!FileUtil.isDirectory(filePath)){
            //guard statement            
            if(!FileUtil.checkPasswords(rootDir, pass1, pass2)){
                //handle password error
                System.out.println("Bad passwords");//TODO: make exception
                return;
            }

            if(option == 'e') CryptUtil.encryptFile(filePath, key);
            else if(option == 'd') CryptUtil.decryptFile(filePath, key);
        }
        else if(FileUtil.isDirectory(filePath)){
            //password gaurd statement
            if(!FileUtil.checkPasswords(rootDir, pass1, pass2)){
                //handle password error
                System.out.println("Bad passwords");//TODO: make exception
                return;
            }

            if(option == 'e') CryptUtil.encryptDirectory(rootDir, key);
            else if(option == 'd') CryptUtil.decryptDirectory(rootDir, key);
        }
    }
}