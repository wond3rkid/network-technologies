package network_technologies;

public enum ClientStatus {
    NO_CONNECTION,
    CONNECTED,
    SENT_INIT_MESSAGE,
    SENT_PIECE,
    WAIT_ANSWER_FOR_PIECE,
    WAIT_INIT_ANSWER,
}
