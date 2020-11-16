import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class MetaManager {
    private Logger logger = LogManager.getLogger(this.getClass());

    public File metaFile;
    public JsonObject metaJsonObject;
    public MetaManager() {
        metaFile = new File(StringUtils.join(
                Arrays.asList(Constants.META_ADDR, Constants.META_FILE_NAME), File.separator));
        logger.debug(metaFile.getPath());
        if (!metaFile.isFile()) {
            logger.error("no meta.json found in META file. Fail to init");
            System.exit(1);
        }
        try {
            String metaContent = FileUtils.readFileToString(metaFile, "UTF-8");
            metaJsonObject = new JsonParser().parse(metaContent).getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finalize() {
        try {
            logger.debug("into finalize");
            FileWriter jsonWriter = new FileWriter(metaFile);
            jsonWriter.write(metaJsonObject.toString());
            jsonWriter.close();
        } catch (IOException e) {
            logger.error("fail to dump json.");
            e.printStackTrace();
        }

    }
    public void restoreMeta() {
        try {
            logger.debug("into finalize");
            FileWriter jsonWriter = new FileWriter(metaFile);
            jsonWriter.write("{}");
            jsonWriter.close();
        } catch (IOException e) {
            logger.error("fail to dump json.");
            e.printStackTrace();
        }
    }
}
