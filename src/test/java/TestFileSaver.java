import models.DataChunk;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class TestFileSaver {

    @Test
    public void test_chopChunks() {
        FileSaver fileSaver = new FileSaver();

        byte[] dataBytes;
        ArrayList<DataChunk> dataChunks;

        dataBytes = new byte[Constants.CHUNK_SIZE * 3 + 1];
        dataChunks = fileSaver.chopChunks(dataBytes, "test1");
        Assert.assertEquals(dataChunks.size(), 4);

        dataBytes = new byte[Constants.CHUNK_SIZE * 3];
        dataChunks = fileSaver.chopChunks(dataBytes, "test2");
        Assert.assertEquals(dataChunks.size(), 3);

        dataBytes = new byte[Constants.CHUNK_SIZE * 3 - 1];
        dataChunks = fileSaver.chopChunks(dataBytes, "test3");
        Assert.assertEquals(dataChunks.size(), 3);
    }

    @Test
    public void test_metaManager() {
        MetaManager metaManager = new MetaManager();
        metaManager.metaJsonObject.addProperty("test2", "123456");
        metaManager.finalize();
    }

    @Test
    public void test_getDisks() {
        FileSaver fileSaver = new FileSaver();
        File[] disks = fileSaver.getDisks();
        Assert.assertEquals(disks.length, 8);
    }

    @Test
    public void test_fileSaver() {
        File testFile = new File("src/test/resources/build.txt");
        if (!testFile.isFile()) {
            System.out.println("not a file");
            assert false;
        }
        FileSaver fileSaver = new FileSaver(testFile);
        fileSaver.saveFile(0);
    }

    @Test
    public void test_getDiskAddr() {
        FileSaver fileSaver = new FileSaver();
        for (File file: fileSaver.getDisks()) {
            System.out.println(file.getName());
        }
    }

    @Test
    public void cleanDisks(){
        Utils.diskClean();
        File metaFile = new File(StringUtils.join(
                Arrays.asList(Constants.META_ADDR, Constants.META_FILE_NAME), File.separator));
        try {
            FileWriter jsonWriter = new FileWriter(metaFile);
            jsonWriter.write("{}");
            jsonWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
