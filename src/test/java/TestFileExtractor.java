import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

public class TestFileExtractor {
    @Test
    public void test_getAllHeadChunkIdsInDisk() {
        FileExtractor fileExtractor = new FileExtractor();
        ArrayList<String> heads = fileExtractor.getAllHeadChunkIdsInDisk("src/main/resources/DiskGroups/Disk2");
        Assert.assertEquals(heads.size(), 1);

        heads = fileExtractor.getAllHeadChunkIdsInDisk("src/main/resources/DiskGroups/Disk5");
        Assert.assertEquals(heads.size(), 0);
    }

    @Test
    public void test_getFullContentByHeadChunkId() {
        FileExtractor fileExtractor = new FileExtractor();
        byte[] fullFile = fileExtractor.getFullContentByHeadChunkId("51740f1b-f11b-4848-b10d-30e2afd39448");
        Assert.assertNotNull(fullFile);

        File testFile = new File("src/test/resources/build.txt");
        FileSaver fileSaver = new FileSaver(testFile);
        byte[] origin = fileSaver.readFileIntoBytes();
        for (int i = 0; i < origin.length; i++) {
            assert origin[i] == fullFile[i];
        }
        System.out.println("Benchmark 1 passed.");
        for (int i = origin.length; i < fullFile.length; i++) {
            assert fullFile[i] == 0;
        }
        System.out.println("Benchmark 2 passed.");
    }
}
