import com.google.gson.JsonObject;
import models.DataChunk;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class FileExtractor {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private MetaManager metaManager = new MetaManager();

    public byte[] getFullFiByHeadChunkId(String chunkId) {
        ArrayList<Byte> fullFileByte = new ArrayList<>();
        byte[] res;
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
            nextChunkId = dataChunkMetaInfo.get("nextChunkId").getAsString();
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
            if (nextChunkId == null) {
                break;
            }
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
