import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import models.DataChunk;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class FileExtractor {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private MetaManager metaManager = new MetaManager();

    public byte[] getFullContentByHeadChunkId(String chunkId) {
        ArrayList<Byte> fullFileByte = new ArrayList<>();
        byte[] res;
        if (metaManager.metaJsonObject.get(chunkId)==null) {
            logger.error("no such chunkId recorded, please check: " + chunkId);
            return null;
        }
        JsonObject dataChunkMetaInfo = metaManager.metaJsonObject.get(chunkId).getAsJsonObject();
        String nextChunkId;
        byte[] currentChunkContent;
        String diskAddr;
        String chunkFileAddr;
        boolean isHead;

        isHead = dataChunkMetaInfo.get("isHead").getAsBoolean();
        if (!isHead) {
            logger.error("this chunk is not a head.");
            return null;
        }
        while (true) {
            chunkId = dataChunkMetaInfo.get("chunkId").getAsString();
            diskAddr = dataChunkMetaInfo.get("diskAddr").getAsString();
            chunkFileAddr = StringUtils.join(Arrays.asList(diskAddr, chunkId), File.separator);
            currentChunkContent = readChunkFileAndGetContent(chunkFileAddr);
            if (currentChunkContent == null) {
                logger.error(String.format("reading %s failed, chunk maybe broken.", chunkId));
                return null;
            }

            // add byte[] to List
            for (int i = 0; i < currentChunkContent.length; i++) {
                fullFileByte.add(currentChunkContent[i]);
            }
            if (dataChunkMetaInfo.get("nextChunkId").isJsonNull()) {
                break;
            }
            nextChunkId = dataChunkMetaInfo.get("nextChunkId").getAsString();
            dataChunkMetaInfo = metaManager.metaJsonObject.get(nextChunkId).getAsJsonObject();
        }

        res = new byte[fullFileByte.size()];
        for (int i = 0; i < fullFileByte.size(); i++) {
            res[i] = fullFileByte.get(i);
        }

        return res;
    }


    public byte[] readChunkFileAndGetContent(String chunkFileAddr) {
        File chunkFile = new File(chunkFileAddr);
        DataChunk dataChunk;
        if (!chunkFile.isFile()) {
            logger.error("cannot find " + chunkFileAddr);
            return null;
        }

        //反序列化
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(chunkFile));
            dataChunk = (DataChunk) ois.readObject();
        } catch (Exception e) {
            logger.error("un-serializing failed.");
            e.printStackTrace();
            return null;
        }
        return dataChunk.getContent();
    }

    public DataChunk readChunkFile(String chunkFileAddr) {
        File chunkFile = new File(chunkFileAddr);
        DataChunk dataChunk;
        if (!chunkFile.isFile()) {
            logger.error("cannot find " + chunkFileAddr);
            return null;
        }

        //反序列化
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(chunkFile));
            dataChunk = (DataChunk) ois.readObject();
        } catch (Exception e) {
            logger.error("un-serializing failed.");
            e.printStackTrace();
            return null;
        }
        return dataChunk;
    }

    public long[] readChunkFileAndGetContentByLong(String chunkFileAddr) {
        File chunkFile = new File(chunkFileAddr);
        DataChunk dataChunk;
        if (!chunkFile.isFile()) {
            logger.error("cannot find " + chunkFileAddr);
            return null;
        }

        //反序列化
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(chunkFile));
            dataChunk = (DataChunk) ois.readObject();
        } catch (Exception e) {
            logger.error("un-serializing failed.");
            e.printStackTrace();
            return null;
        }

        byte[] src = dataChunk.getContent();
        long[] contentByLong = new long[Constants.CHUNK_SIZE / Long.BYTES];

        for (int i = 0; i < contentByLong.length; i++) {
            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.put(src, i * 8, 8);
            buffer.flip();
            contentByLong[i] = buffer.getLong();
        }

        return contentByLong;
    }

    public ArrayList<String> getAllHeadChunkIdsInDisk(String diskAddr) {
        ArrayList<String> headChunkIds = new ArrayList<>();
        File disk = new File(diskAddr);
        if (!disk.isDirectory()) {
            logger.error(diskAddr + " is not valid");
            return null;
        }
        String[] chunkList = disk.list();
        for (int i = 0; i < chunkList.length; i++) {
            JsonObject dataChunk = metaManager.metaJsonObject.get(chunkList[i]).getAsJsonObject();
            if (dataChunk.get("isHead").getAsBoolean()) {
                headChunkIds.add(chunkList[i]);
            }
        }
        return headChunkIds;
    }
}
