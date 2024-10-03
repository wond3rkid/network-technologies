package network_technologies;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.BitSet;

public class PieceManager {
    private final static Logger LOGGER = LogManager.getLogger();
    private final BitSet pieces;
    private int sentPieces = 0;
    private final int allPieces;

    public PieceManager(int count) {
        allPieces = count;
        pieces = new BitSet(allPieces);
    }

    public Integer getPieceToSent() {
        for (int i = 0; i < allPieces; i++) {
            if (!pieces.get(i)) {
                LOGGER.info("Now i will send piece {} ", i);
                sentPieces++;
                pieces.set(i);
                return i;
            }
        }
        LOGGER.info("No piece to sent");
        return null;
    }

    public boolean isAllPiecesSent() {
        return sentPieces == allPieces;
    }
}
