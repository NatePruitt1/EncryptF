package EncryptF;

import java.util.LinkedList;
import java.io.Serializable;

public class FileTree implements Serializable {
    public DirNode root;
    public String rootDir;

    public String traversal;

    public FileTree(String rootPath){
        rootDir = rootPath;
        root = new DirNode(".", null);
    }


    //PRECOND: localPath is a path in the tree, and starts with ./
    public void insertDirNode(String localPath, String name){
        //traverse the tree, if the parent is found, add a new dir child
        DirNode parentNode = getDirNode(localPath);
        
        parentNode.dirChildren.add(new DirNode(name, parentNode));
    }

    public String getCompletePath(DirNode n){
        String localPath = n.getPath();
        localPath = localPath.replaceFirst(".", rootDir);
        System.out.println("Complete Path: " + localPath);
        return localPath;
    }

    public void insertFileNode(String localPath, String fileName, byte[] fileData){
        DirNode parentNode = getDirNode(localPath);
        parentNode.fileChildren.add(new FileNode(fileName, parentNode, fileData));
    }


    //PRECONDITION: the path starts with ./ and does not have more than one ./
    //POSTCONDITION returns node at path, or null if the path is invalid.
    public DirNode getDirNode(String path) {
        //split the path by slashes, which should be dirnames
        String[] pathArr = path.split("/");
        DirNode ret = root;
        for(int i = 1; i < pathArr.length; i++){
            if(ret != null) {
                ret = ret.getDirChild(pathArr[i]);
            }
        }
        
        return ret;
    }

    public void treeTraversal(){
        String ret = root.dirName + "/\n";
        ret = ret + traverse(root, 2);

        traversal = ret;
    }

    public String traverse(DirNode d, int dist){
        //print all directories
        String retString = "";
        for(int i = 0; i < d.dirChildren.size(); i++){
            retString = retString + getDashes(dist) + d.dirChildren.get(i).dirName + "/\n";
            retString = retString + traverse(d.dirChildren.get(i), dist + d.dirChildren.get(i).dirName.length() + 1);
        }

        for(int i = 0; i < d.fileChildren.size(); i++){
            retString = retString + getDashes(dist) + d.fileChildren.get(i).fileName + "\n";
        }

        return retString;
    }

    private String getDashes(int dash){
        String r = "";
        for(int i = 0; i < dash - 1; i++){
            r = r + "-";
        }
        r = r + "|";

        return r;
    }
}

class FileNode implements Serializable {
    //can have parent but no children
    public String fileName;
    public DirNode parent;
    public byte[] data;

    public FileNode(String name, DirNode p, byte[] d){
        fileName = name;
        parent = p;
        data = d;
    }

    public String toString(){
        return "Filename: " + fileName;
    }
}

class DirNode implements Serializable {
    //can have children and parent
    public String dirName;
    public DirNode parent;
    public LinkedList<FileNode> fileChildren;
    public LinkedList<DirNode> dirChildren;

    public DirNode(String name, DirNode p){
        parent = p;
        dirName = name;
        fileChildren = new LinkedList<FileNode>();
        dirChildren = new LinkedList<DirNode>();
    }

    public DirNode getDirChild(String name){
        for(DirNode d : dirChildren){
            if(d.dirName.equals(name)) return d;
        }

        return null;
    }

    public String getPath(){
        if(dirName.equals(".")) return "./";
        return parent.getPath() + dirName + "/";
    }


    public String toString(){
        //add all fileChildren, dirChildren, name
        String ret = "name: " + dirName + "\n";
        ret = ret + "File Children: ";
        for(FileNode f : fileChildren){
            ret = ret + f.fileName + ", ";
        }
        ret = ret + "\n";

        ret = ret + "Dir Children: ";
        for(DirNode f : dirChildren){
            ret = ret + f.dirName + ", ";
        }
        

        return ret; 
    }
}
