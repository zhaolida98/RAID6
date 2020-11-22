package models;

import com.google.gson.JsonObject;

import java.io.Serializable;
import java.nio.ByteBuffer;

public class DataChunk implements Serializable {
    private String chunkId;
    private byte[] content;
    private String nextChunkId;
    private String type;
    private boolean isHead = false;
    private String diskAddr;


    public DataChunk(String chunkId, byte[] content) {
        this.chunkId = chunkId;
        this.content = content;
    }

    public DataChunk(String chunkId, long[] content) {
        this.chunkId = chunkId;
        byte[] content2byte = new byte[content.length * Long.BYTES];
        for (int i = 0; i < content.length; i++) {
            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
            buffer.putLong(content[i]);
            System.arraycopy(buffer.array(), 0, content2byte, i * 8, Long.BYTES);
        }
        this.content = content2byte;
    }

    public DataChunk(String chunkId, byte[] content, String nextChunkId) {
        this(chunkId, content);
        this.nextChunkId = nextChunkId;
    }

    public String getChunkId() {
        return chunkId;
    }

    public void setChunkId(String chunkId) {
        this.chunkId = chunkId;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setContent(long[] content) {
        byte[] content2byte = new byte[content.length * Long.BYTES];
        for (int i = 0; i < content.length; i++) {
            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
            buffer.putLong(content[i]);
            System.arraycopy(buffer.array(), 0, content2byte, i * 8, Long.BYTES);
        }
        this.content = content2byte;
    }

    public String getNextChunkId() {
        return nextChunkId;
    }

    public void setNextChunkId(String nextChunkId) {
        this.nextChunkId = nextChunkId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isHead() {
        return isHead;
    }

    public void setHead(boolean head) {
        isHead = head;
    }

    public String getDiskAddr() {
        return diskAddr;
    }

    public void setDiskAddr(String diskAddr) {
        this.diskAddr = diskAddr;
    }

    public JsonObject toJsonObject() {
        if (this.chunkId.isEmpty() || this.diskAddr.isEmpty()) {
            return null;
        }
        JsonObject dataChunkJson = new JsonObject();
        dataChunkJson.addProperty("chunkId", this.chunkId);
        dataChunkJson.addProperty("nextChunkId", this.nextChunkId);
        dataChunkJson.addProperty("diskAddr", this.diskAddr);
        dataChunkJson.addProperty("isHead", this.isHead);
        return dataChunkJson;
    }
}
