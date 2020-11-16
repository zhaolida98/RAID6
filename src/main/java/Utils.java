import java.io.File;
import java.util.ArrayList;

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


}
