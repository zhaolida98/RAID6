import models.DataChunk;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.nio.file.Paths;

public class TestParity {

    @Test
    public void test_storeParity() {
        ParityGenerator PG = new ParityGenerator();
        PG.storeParities();
    }

    @Test
    public void simulateCorruption() {
        FileExtractor fileExtractor = new FileExtractor();

        File diskGroups = new File(Constants.DISK_GROUP_ADDR);
        File[] disks = diskGroups.listFiles(File::isDirectory);
        if (disks == null) return;

        File corruptDisk = disks[0];                              // Corrupt Data Drive
//        File corruptDisk = disks[disks.length - 2];               // Corrupt P Drive
//        File corruptDisk = disks[disks.length - 1];               // Corrupt Q Drive
        if (corruptDisk == null) return;
        File[] files = corruptDisk.listFiles();
        if (files == null) return;;

        String fileName = files[0].getAbsolutePath();

        byte[] data = new byte[Constants.CHUNK_SIZE];

        DataChunk dataChunk = fileExtractor.readChunkFile(fileName);
        dataChunk.setContent(data);

        try {
            File chunkFile = new File(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(chunkFile));
            oos.writeObject(dataChunk);
            oos.close();

        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
            return;
        }
        System.out.println("Corruption Succeed.");
        System.out.println("Corrupt on " + corruptDisk.getName() + ".");
    }

    @Test
    public void test_checkParity() {
        ParityChecker PC = new ParityChecker();
        PC.checkAndRecovery();
    }
}
