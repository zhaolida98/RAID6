import models.DataChunk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;

public class Utils {
    public static void diskClean() {
        FileSaver fileSaver = new FileSaver();
        File[] disks = fileSaver.getDisks();
        for (File disk : disks) {
            File[] children = disk.listFiles();
            if (children == null || children.length == 0) continue;
            for (File child : children) {
                child.delete();
            }
        }
    }

    public static void reWriteChunk(String chunkId, byte[] newContent) {
        FileExtractor fileExtractor = new FileExtractor();

        DataChunk dataChunk = fileExtractor.readChunkFile(chunkId);
        dataChunk.setContent(newContent);
        String newChunkFileAddr = Paths.get(dataChunk.getDiskAddr(), dataChunk.getChunkId()).toString();
        File newChunkFile = new File(newChunkFileAddr);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(newChunkFile));
            oos.writeObject(dataChunk);
            oos.close();
        } catch (Exception e) {
        }

    }


}
