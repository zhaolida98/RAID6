import models.DataChunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.plaf.synth.SynthTextAreaUI;
import java.io.File;
import java.util.ArrayList;

public class ParityGenerator {
    private final Logger logger = LogManager.getLogger(this.getClass());
    ArrayList<DataChunk> P = null;
    ArrayList<DataChunk> Q = null;
    FileSaver fileSaver = new FileSaver();
    FileExtractor fileExtractor = new FileExtractor();

    private long multiply02(long v) {
        long vv = (v << 1) & 0xfefefefefefefefeL;
        vv ^= mask(v) & 0x1d1d1d1d1d1d1d1dL;
        return vv;
    }

    private long mask(long v) {
        v &= 0x8080808080808080L;
        return (v << 1) - (v >> 7);
    }

    public void generate() {

        File[] diskList = getDataDisk();

        int maxLength = -1;
        for (File file
                : diskList) {
            File[] tmpChunkList = file.listFiles();      // get chunks
            if (tmpChunkList != null && tmpChunkList.length > maxLength)
                maxLength = tmpChunkList.length;
        }

        if (maxLength == -1) {
            logger.error("No file saved in disks");
            return;
        }

        P = new ArrayList<>();
        Q = new ArrayList<>();

        for (int i = 0; i < maxLength; i++) {
            ArrayList<long[]> data = new ArrayList<>();

            for (File file : diskList) {
                File[] chunkFiles = file.listFiles();
                long[] chunk;
                if (chunkFiles != null && chunkFiles.length > i)
                    chunk = fileExtractor.readChunkFileAndGetContentByLong(chunkFiles[i].getAbsolutePath());
                else
                    chunk = new long[Constants.CHUNK_SIZE / Long.BYTES];
                data.add(chunk);
            }

            long[] resP = new long[Constants.CHUNK_SIZE / Long.BYTES];
            for (int j = 0; j < data.size(); j++) {
                for (int k = 0; k < resP.length; k++) {
                    if (j == 0)
                        resP[k] = data.get(j)[k];
                    else
                        resP[k] ^= data.get(j)[k];
                }
            }

            P.add(new DataChunk("p" + i, resP));

            long[] resQ = new long[Constants.CHUNK_SIZE / Long.BYTES];
            for (int j = data.size() - 1; j >= 0; j--) {
                for (int k = 0; k < resQ.length; k++) {
                    if (j == data.size() - 1)
                        resQ[k] = data.get(j)[k];
                    else
                        resQ[k] = (multiply02(resQ[k]) ^ data.get(j)[k]);
                }
            }
            Q.add(new DataChunk("q" + i, resQ));
        }
    }

    public boolean storeParities() {
        File[] diskList = fileSaver.getDisks();
        if (diskList == null || diskList.length == 0) {
            logger.error("get diskList fail");
            return false;
        }
        generate();
        if (P == null || P.size() == 0) {
            logger.error("get parity P fail");
            return false;
        }
        boolean ok = fileSaver.storeChunks(P, diskList, 1);
        if (!ok) {
            logger.error("Store P chunk fail");
            return false;
        }
        ok = fileSaver.storeChunks(Q, diskList, 2);
        if (!ok) {
            logger.error("Store Q chunk fail");
            return false;
        }
        return true;
    }

    public File getPDisk() {
        File diskGroups = new File(Constants.DISK_GROUP_ADDR);
        File[] disks = diskGroups.listFiles(File::isDirectory);
        if (disks == null) return null;
        return disks[disks.length - 2];
    }

    public File getQDisk() {
        File diskGroups = new File(Constants.DISK_GROUP_ADDR);
        File[] disks = diskGroups.listFiles(File::isDirectory);
        if (disks == null) return null;
        return disks[disks.length - 1];
    }

    public File[] getDataDisk() {

        File diskGroups = new File(Constants.DISK_GROUP_ADDR);
        File[] disks = diskGroups.listFiles(File::isDirectory);
        if (disks == null) return null;

        File[] dataDisks = new File[disks.length - 2];
        System.arraycopy(disks, 0, dataDisks, 0, dataDisks.length);
        return dataDisks;
    }
}
