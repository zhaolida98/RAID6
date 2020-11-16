import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class App {
    public static void main(String[] args) {
        //save files
        List<String> targetFileList = Arrays.asList("file1", "file2");
        FileSaver fileSaver = new FileSaver();
        for (String targetFile :
                targetFileList) {
            File tmpFile = new File(targetFile);
            if (!tmpFile.isFile()) {
                continue;
            }
            fileSaver.setTargetFile(tmpFile);
            fileSaver.saveFile();
        }

        // save parities
        ParityGenerator parityGenerator = new ParityGenerator();
        parityGenerator.generateP();
        parityGenerator.generateQ();
        parityGenerator.storeParities();
    }
}
