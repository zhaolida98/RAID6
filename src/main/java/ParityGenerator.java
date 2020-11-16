import models.DataChunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;

public class ParityGenerator {
    private Logger logger = LogManager.getLogger(this.getClass());
    byte[] P;
    byte[] Q;
    FileSaver fileSaver = new FileSaver();
    public byte[] generateP() {
        //TODO
        return null;
    }

    public byte[] generateQ() {
        //TODO
        return null;
    }

    public boolean storeParities() {
        File[] diskList = fileSaver.getDisks();
        if (diskList == null || diskList.length == 0){
            logger.error("get diskList fail");
            return false;
        }
        P = generateP();
        if (P == null || P.length == 0) {
            logger.error("get parity P fail");
            return false;
        }
        ArrayList<DataChunk> PChunkList = fileSaver.chopChunks(P, "parityP");
        if (PChunkList == null || PChunkList.size() == 0) {
            logger.error("get chopChunks of P failed");
            return false;
        }
        boolean ok = fileSaver.storeChunks(PChunkList, diskList);
        if (!ok) {
            logger.error("Store P chunk fail");
            return false;
        }


        Q = generateQ();
        if (Q == null || Q.length == 0) {
            logger.error("get parity Q fail");
            return false;
        }
        ArrayList<DataChunk> QChunkList = fileSaver.chopChunks(Q, "parityQ");
        if (QChunkList == null || QChunkList.size() == 0) {
            logger.error("get chopChunks of Q failed");
            return false;
        }
        ok = fileSaver.storeChunks(QChunkList, diskList);
        if (!ok) {
            logger.error("Store Q chunk fail");
            return false;
        }

        return true;
    }

}
