package network_technologies;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class PieceManager {
    private final static Logger LOGGER = LogManager.getLogger();
    Map<Integer, Boolean> pieces = new HashMap<>();
    private int sentPieces = 0;
    private int allPieces;

    public PieceManager(int count) {
        allPieces = count;
        for (int i = 0; i < allPieces; i++) {
            pieces.put(i, false);
        }
    }

    public Integer getPieceToSent() {
        for (Map.Entry<Integer, Boolean> entry : pieces.entrySet()) {
            if (!entry.getValue()) {
                LOGGER.info("Now i will send piece {} ", entry.getKey());
                sentPieces++;
                return entry.getKey();
            }
        }
        LOGGER.info("No piece to sent");
        return null;
    }

    public boolean isAllPiecesSent() {
        return sentPieces == allPieces;
    }
}
