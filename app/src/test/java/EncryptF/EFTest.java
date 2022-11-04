package EncryptF;

import org.junit.Test;

import javax.crypto.SecretKey;

public class EFTest {

    @Test
    public void testFileTreeInserts(){
        try{
            FileTree f = FileUtil.createTree("../TestDir");
            FileUtil.saveFileTree(f, "../Outputs.txt");
            FileUtil.deleteFileTreeContents(f);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
