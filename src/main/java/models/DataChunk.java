package models;

import java.io.Serializable;

public class DataChunk implements Serializable {
    private String chunkId;
    private byte[] content;
    private String nextChunkId;
    private String type;
    private boolean isHead = false;


    public DataChunk(String chunkId, byte[] content) {
        this.chunkId = chunkId;
        this.content = content;
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
}
