package EncryptF;

import java.util.Arrays;

import java.io.File;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

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


    //Saves a made file tree 
    public static void saveFileTree(FileTree toSave, String savePath) throws Exception{
        FileOutputStream f = new FileOutputStream(new File(savePath));
        ObjectOutputStream o = new ObjectOutputStream(f);

        o.writeObject(toSave);

        f.close();
        o.close();
    }

    //Loads a file tree from storage to memory
    public static FileTree readFileTree(String savePath) throws Exception{
        FileInputStream f = new FileInputStream(new File(savePath));
        ObjectInputStream o = new ObjectInputStream(f);

        return (FileTree) o.readObject();
    }

    //recursively create directories in a file tree
    public static void recursiveCreateDir(FileTree rootTree, DirNode dirNode) throws Exception{
        
        String completePath = rootTree.getCompletePath(dirNode);

        File rootFile = new File(completePath);

        String[] subFiles = rootFile.list();

        LinkedList<String> fileStack = new LinkedList<String>();
        LinkedList<String> dirStack = new LinkedList<String>();

        for(String fileName : subFiles){
            File temp = new File(completePath + fileName);

            if(temp.isDirectory()) dirStack.add(fileName);

            else if(temp.isFile()) fileStack.add(fileName);

        }

        while(!fileStack.isEmpty()){
            String fileStackPop = fileStack.removeLast();

            File temp = new File(completePath + fileStackPop);

            rootTree.insertFileNode(dirNode.getPath(), fileStackPop, Files.readAllBytes(temp.toPath()));
        }

        while(!dirStack.isEmpty()){
            String dirStackPop = dirStack.removeLast();
            
            rootTree.insertDirNode(dirNode.getPath(), dirStackPop);
            recursiveCreateDir(rootTree, rootTree.getDirNode(dirNode.getPath() + dirStackPop));
        }
    }

    //create a file tree from a root folder
    public static FileTree createTree(String rootDir) throws Exception{
        FileTree f = new FileTree(rootDir);

        File rootFile = new File(rootDir);
        String[] subFiles = rootFile.list();

        LinkedList<String> fileStack = new LinkedList<String>();
        LinkedList<String> dirStack = new LinkedList<String>();

        for(String fileName : subFiles){
            File temp = new File(f.getCompletePath(f.root) + fileName);

            if(temp.isDirectory()) dirStack.add(fileName);

            else if(temp.isFile()) fileStack.add(fileName);

        }

        while(!fileStack.isEmpty()){
            String fileStackPop = fileStack.removeLast();

            File temp = new File(f.getCompletePath(f.root) + fileStackPop);

            f.insertFileNode(f.root.getPath(), fileStackPop, Files.readAllBytes(temp.toPath()));
        }

        while(!dirStack.isEmpty()){
            String dirStackPop = dirStack.removeLast();
            
            f.insertDirNode(f.root.getPath(), dirStackPop);
            recursiveCreateDir(f, f.getDirNode(f.root.getPath() + dirStackPop));
        }
        
        return f;
    }

    //Deletes all files referenced in a file tree to hide thier contents after encryption.
    public static void deleteFileTreeContents(FileTree toDelete) throws Exception{
        //will require a recursive call too folders, who must delete their contents, call the function on their subdirs, and then delete themselves.
        deleteDirRecursive(toDelete, toDelete.root);
    }

    public static void deleteDirRecursive(FileTree f, DirNode dirNode) throws Exception{
        //get all files from a dirnode, delete them, then call this function on the sub-dirs of dirNode
        String absPath = f.getCompletePath(dirNode);
        for(FileNode fN : dirNode.fileChildren){
            File toDel = new File(absPath + fN.fileName);
            Files.write(toDel.toPath(), "".getBytes());
            toDel.delete();
        }

        for(DirNode dN : dirNode.dirChildren){
            deleteDirRecursive(f, dN);
        }
        //finally, delete the directory associated with dirNode
        File thisFolder = new File(absPath);
        thisFolder.delete();
    }

    public static void createFileTreeContents(FileTree toCreate){

    }

    public static void createDirNodeContents(FileTree f, DirNode dN) throws Exception {
        String absPath = f.getCompletePath(dN);
        Path rootPath = Paths.get(absPath);
        Files.createDirectory(rootPath);
    }
}
