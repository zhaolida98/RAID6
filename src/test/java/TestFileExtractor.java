import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

public class TestFileExtractor {
    @Test
    public void test_getAllHeadChunkIdsInDisk() {
        FileExtractor fileExtractor = new FileExtractor();
        ArrayList<String> heads = fileExtractor.getAllHeadChunkIdsInDisk("src/main/resources/DiskGroups/Disk6");
        Assert.assertEquals(heads.size(), 1);

        heads = fileExtractor.getAllHeadChunkIdsInDisk("src/main/resources/DiskGroups/Disk5");
        Assert.assertEquals(heads.size(), 0);
    }

    @Test
    public void test_getFullContentByHeadChunkId() {
        FileExtractor fileExtractor = new FileExtractor();
        byte[] fullFile = fileExtractor.getFullContentByHeadChunkId("1333864c-e835-4656-a227-ae9bd96b36c2");
        Assert.assertNotNull(fullFile);

        File testFile = new File("src/test/resources/build.txt");
        FileSaver fileSaver = new FileSaver(testFile);
        byte[] origin = fileSaver.readFileIntoBytes();
        for (int i = 0; i < origin.length; i++) {
            assert origin[i] == fullFile[i];
//            System.out.print(origin[i] + ":" + fullFile[i] + "\t");
            if ((i + 1) % 10 == 0) {
                System.out.println();
            }
        }
        for (int i = origin.length; i < fullFile.length; i++) {
            assert fullFile[i] == 0;
        }
    }
}
