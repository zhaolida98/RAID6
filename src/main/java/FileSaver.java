
import models.DataChunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

import org.apache.commons.io.FileUtils;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;

public class FileSaver {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private int loadBalencePointer = 0;

    private File targetFile;
    private MetaManager metaManager = new MetaManager();
    public FileSaver() {

    }

    public FileSaver(File targetFile) {
        this.targetFile = targetFile;
    }

    public boolean saveFile() {
        if (!this.targetFile.isFile()) {
            logger.error("no targetFile found");
            return false;
        }
        byte[] dataBytes = readFileIntoBytes();
        if (dataBytes == null) return false;

        ArrayList<DataChunk> dataChunkArrayList = chopChunks(dataBytes, targetFile.getName());
        File[] diskAddrs = getDisks();
        if (dataChunkArrayList == null || diskAddrs == null) return false;

        boolean ok = storeChunks(dataChunkArrayList, diskAddrs);
        metaManager.finalize();
        return ok;
    }

    private byte[] readFileIntoBytes() {
        byte[] resBytes;
        try {
            resBytes = FileUtils.readFileToByteArray(targetFile);
        } catch (Exception e) {
            logger.error("cannot find file: " + e);
            return null;
        }
        return resBytes;
    }

    public ArrayList<DataChunk> chopChunks(byte[] dataBytes, String headChunkName) {
        String chunkId = UUID.randomUUID().toString();
        ArrayList<DataChunk> resList = new ArrayList<>();
        byte[] tempByteArray = new byte[Constants.CHUNK_SIZE];

        //save the first chunk of a file.
        metaManager.metaJsonObject.addProperty(chunkId, headChunkName);

        for (int i = 0; i < dataBytes.length; i++) {
            // 1. tmp end, data not
            if (i < Constants.CHUNK_SIZE) {
                tempByteArray[i % Constants.CHUNK_SIZE] = dataBytes[i];
            }
            if (((i + 1) % Constants.CHUNK_SIZE) == 0 && (i + 1) < dataBytes.length) {

                DataChunk dataChunk = new DataChunk(chunkId, tempByteArray);
                chunkId = UUID.randomUUID().toString();
                dataChunk.setNextChunkId(chunkId);
                tempByteArray = new byte[Constants.CHUNK_SIZE];
                // identify head chunk
                if ((i + 1) / Constants.CHUNK_SIZE == 1) {
                    dataChunk.setHead(true);
                }
                resList.add(dataChunk);
            }
        }
        // 2. tmp not, data end; both end
        DataChunk dataChunk = new DataChunk(chunkId, tempByteArray);
        dataChunk.setNextChunkId(null);
        resList.add(dataChunk);
        return resList;
    }

    public boolean storeChunks(ArrayList<DataChunk> chunkArrayList, File[] diskAddrs) {
        for (File file :
                diskAddrs) {
            logger.debug(file.getName());
        }
        for (int i = 0; i < chunkArrayList.size(); i++) {
            DataChunk dataChunk = chunkArrayList.get(i);
            String diskAddr;
            // head chunk only allows to appear in the first six disks. Thus, we can use disk number to represent
            // data strip number.
            if (dataChunk.isHead()) {
                diskAddr = diskAddrs[loadBalencePointer % (diskAddrs.length - 2)].getAbsolutePath();
            } else{
                diskAddr = diskAddrs[loadBalencePointer % diskAddrs.length].getAbsolutePath();
            }
            loadBalencePointer++;
            String newChunkAddr = Paths.get(diskAddr, dataChunk.getChunkId()).toString();
            try {
                File chunkFile = new File(newChunkAddr);
                if (chunkFile.createNewFile()) {
                    System.out.println("File created: " + chunkFile.getName());
                } else {
                    System.out.println("File already exists.");
                }
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(chunkFile));
                oos.writeObject(dataChunk);
                oos.close();
                //反序列化
//                File file = new File("template");
//                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
//                User1 newUser = (User1)ois.readObject();
//                System.out.println(newUser.toString());
            } catch (Exception e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }



    public File[] getDisks() {
        File diskGroups = new File(Constants.DISK_GROUP_ADDR);
        if (!diskGroups.isDirectory()) {
            logger.error("DiskGroup addr do not exists. Please specify the disk group's addr in Constants.java");
            return null;
        }
        File[] diskList = diskGroups.listFiles(File::isDirectory);
        if (diskList == null || diskList.length == 0) {
            logger.error("No disks found in DiskGroup. Please build some folders inside DiskGroup as disks");
            return null;
        }
        logger.info(diskList.length + " disks found.");
        return diskList;
    }

    public void setTargetFile(File targetFile) {
        this.targetFile = targetFile;
    }
}
