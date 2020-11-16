import java.io.File;

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

    public static byte[] getFullFileByHeadChunkId() {

        return null;
    }
}
