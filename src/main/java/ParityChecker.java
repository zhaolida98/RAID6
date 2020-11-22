import models.DataChunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.plaf.synth.SynthTextAreaUI;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;


public class ParityChecker {

    private final int[] tab = {0x1, 0x2, 0x4, 0x8, 0x10, 0x20, 0x40, 0x80, 0x1d, 0x3a, 0x74, 0xe8, 0xcd, 0x87, 0x13, 0x26, 0x4c, 0x98, 0x2d, 0x5a, 0xb4, 0x75, 0xea,
            0xc9, 0x8f, 0x3, 0x6, 0xc, 0x18, 0x30, 0x60, 0xc0, 0x9d, 0x27, 0x4e, 0x9c, 0x25, 0x4a, 0x94, 0x35, 0x6a, 0xd4, 0xb5, 0x77, 0xee, 0xc1, 0x9f, 0x23, 0x46,
            0x8c, 0x5, 0xa, 0x14, 0x28, 0x50, 0xa0, 0x5d, 0xba, 0x69, 0xd2, 0xb9, 0x6f, 0xde, 0xa1, 0x5f, 0xbe, 0x61, 0xc2, 0x99, 0x2f, 0x5e, 0xbc, 0x65, 0xca, 0x89,
            0xf, 0x1e, 0x3c, 0x78, 0xf0, 0xfd, 0xe7, 0xd3, 0xbb, 0x6b, 0xd6, 0xb1, 0x7f, 0xfe, 0xe1, 0xdf, 0xa3, 0x5b, 0xb6, 0x71, 0xe2, 0xd9, 0xaf, 0x43, 0x86, 0x11,
            0x22, 0x44, 0x88, 0xd, 0x1a, 0x34, 0x68, 0xd0, 0xbd, 0x67, 0xce, 0x81, 0x1f, 0x3e, 0x7c, 0xf8, 0xed, 0xc7, 0x93, 0x3b, 0x76, 0xec, 0xc5, 0x97, 0x33, 0x66,
            0xcc, 0x85, 0x17, 0x2e, 0x5c, 0xb8, 0x6d, 0xda, 0xa9, 0x4f, 0x9e, 0x21, 0x42, 0x84, 0x15, 0x2a, 0x54, 0xa8, 0x4d, 0x9a, 0x29, 0x52, 0xa4, 0x55, 0xaa, 0x49,
            0x92, 0x39, 0x72, 0xe4, 0xd5, 0xb7, 0x73, 0xe6, 0xd1, 0xbf, 0x63, 0xc6, 0x91, 0x3f, 0x7e, 0xfc, 0xe5, 0xd7, 0xb3, 0x7b, 0xf6, 0xf1, 0xff, 0xe3, 0xdb, 0xab,
            0x4b, 0x96, 0x31, 0x62, 0xc4, 0x95, 0x37, 0x6e, 0xdc, 0xa5, 0x57, 0xae, 0x41, 0x82, 0x19, 0x32, 0x64, 0xc8, 0x8d, 0x7, 0xe, 0x1c, 0x38, 0x70, 0xe0, 0xdd,
            0xa7, 0x53, 0xa6, 0x51, 0xa2, 0x59, 0xb2, 0x79, 0xf2, 0xf9, 0xef, 0xc3, 0x9b, 0x2b, 0x56, 0xac, 0x45, 0x8a, 0x9, 0x12, 0x24, 0x48, 0x90, 0x3d, 0x7a, 0xf4,
            0xf5, 0xf7, 0xf3, 0xfb, 0xeb, 0xcb, 0x8b, 0xb, 0x16, 0x2c, 0x58, 0xb0, 0x7d, 0xfa, 0xe9, 0xcf, 0x83, 0x1b, 0x36, 0x6c, 0xd8, 0xad, 0x47, 0x8e};

    private final Logger logger = LogManager.getLogger(this.getClass());

    private byte[] pChunk, pCheck;
    private byte[] qChunk, qCheck;

    ParityGenerator checker = new ParityGenerator();
    FileExtractor fileExtractor = new FileExtractor();

    ParityChecker() {
        checker.generate();
    }

    private boolean checkByByte(byte[] src, byte[] dst) {
        try {
            assert src.length == dst.length;
        } catch (AssertionError e) {
            logger.error("Different length. Failed");
            return false;
        }

        try {
            for (int i = 0; i < src.length; i++) {
                assert src[i] == dst[i];
            }
        } catch (AssertionError e) {
            logger.error("Different content. Failed");
            return false;
        }

        return true;
    }

    private boolean checkP() {

        File pDisk = checker.getPDisk();
        File[] pFiles = pDisk.listFiles();
        if (pFiles == null) {
            logger.error("P Disk null.");
            return false;
        }

        assert checker.P.size() == pFiles.length;

        for (int i = 0; i < pFiles.length; i++) {
            pChunk = fileExtractor.readChunkFileAndGetContent(Constants.DISK_GROUP_ADDR + "/Disk8/p" + i);
            pCheck = checker.P.get(i).getContent();
            boolean ok = checkByByte(pCheck, pChunk);
            if (!ok)
                return false;
            System.out.println("p" + i + " benchmark passed.");
        }

        return true;
    }

    private boolean checkQ() {

        File qDisk = checker.getQDisk();
        File[] qFiles = qDisk.listFiles();
        if (qFiles == null) {
            logger.error("Q Disk null.");
            return false;
        }

        assert checker.Q.size() == qFiles.length;

        for (int i = 0; i < qFiles.length; i++) {
            qChunk = fileExtractor.readChunkFileAndGetContent(Constants.DISK_GROUP_ADDR + "/Disk7/q" + i);
            qCheck = checker.Q.get(i).getContent();
            boolean ok = checkByByte(qCheck, qChunk);
            if (!ok)
                return false;
            System.out.println("q" + i + " benchmark passed.");
        }

        return true;
    }

    public boolean checkAndRecovery() {

        boolean okP = checkP();
        boolean okQ = checkQ();

        if (okP && okQ)
            System.out.println("No corruption.");
        else if (!okP && okQ) {
            System.out.println("P Drive corruption.");
            checker.storeParities();
        }
        else if (okP) {
            System.out.println("Q Drive corruption.");
            checker.storeParities();
        }

        else {
            int z = -1;
            System.out.println("Data Drive corruption.");

            byte[] pXor = new byte[Constants.CHUNK_SIZE];
            byte[] qXor = new byte[Constants.CHUNK_SIZE];

            for (int i = 0; i < Constants.CHUNK_SIZE; i++) {
                pXor[i] = (byte) (pChunk[i] ^ pCheck[i]);
                qXor[i] = (byte) (qChunk[i] ^ qCheck[i]);

                if (pXor[i] != 0 && qXor[i] != 0) {
                    int logP, logQ;
                    for (logP = 0; logP < tab.length; logP++) {
                        if (pXor[i] == (byte) tab[logP]) {
                            break;
                        }
                    }

                    for (logQ = 0; logQ < tab.length; logQ++) {
                        if (qXor[i] == (byte) tab[logQ]) {
                            break;
                        }
                    }

                    z = logQ - logP;
                    z = z >= 0 ? z : z + 255;
                    break;
                }
            }

            File diskGroups = new File(Constants.DISK_GROUP_ADDR);
            File[] disks = diskGroups.listFiles(File::isDirectory);
            if (disks == null) {
                logger.error("Error in detecting which disk. ");
                return false;
            }

            if (z < 0 || z >= disks.length) {
                logger.error("z error");
                return false;
            }

            System.out.println("Data " + disks[z].getName() + " corruption.");
            System.out.println("Start Recover " + disks[z].getName() + ".");

            int maxLength = disks[disks.length - 2].listFiles().length;         // p/q has the longest length

            for (int i = 0; i < maxLength; i++) {
                ArrayList<long[]> data = new ArrayList<>();

                for (int j = 0; j < disks.length; j++) {
                    long[] chunk;
                    if (j == z || j == disks.length - 1) continue;          // j is corrupted disk or q disk

                    else if (j == disks.length - 2) {                       // j is p disk
                        String chunkFileName = disks[j].getAbsolutePath() + "/p" + i;
                        chunk = fileExtractor.readChunkFileAndGetContentByLong(chunkFileName);
                    }
                    else {
                        File[] chunkFiles = disks[j].listFiles();
                        if (chunkFiles != null && chunkFiles.length > i)
                            chunk = fileExtractor.readChunkFileAndGetContentByLong(chunkFiles[i].getAbsolutePath());
                        else
                            chunk = new long[Constants.CHUNK_SIZE / Long.BYTES];
                    }
                    data.add(chunk);
                }

                long[] newContent = new long[Constants.CHUNK_SIZE / Long.BYTES];
                for (int j = 0; j < data.size(); j++) {
                    for (int k = 0; k < newContent.length; k++) {
                        if (j == 0)
                            newContent[k] = data.get(j)[k];
                        else
                            newContent[k] ^= data.get(j)[k];
                    }
                }

                if (i < disks[z].listFiles().length) {
                    String fileName = disks[z].listFiles()[i].getAbsolutePath();
                    DataChunk dataChunk = fileExtractor.readChunkFile(fileName);
                    dataChunk.setContent(newContent);
                    try {
                        File chunkFile = new File(fileName);
                        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(chunkFile));
                        oos.writeObject(dataChunk);
                        System.out.println("Rewrite " + dataChunk.getChunkId() + ".");
                        oos.close();
                    } catch (Exception e) {
                        System.out.println("Rewrite error.");
                        return false;
                    }
                }

            }

            System.out.println("Recover Succeed.");

        }

        return true;
    }

}
