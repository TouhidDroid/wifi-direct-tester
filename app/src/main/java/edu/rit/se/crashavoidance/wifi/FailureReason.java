package edu.rit.se.crashavoidance.wifi;

public enum FailureReason {
    ERROR("ERROR"),
    P2P_UNSUPPORTED("P2P UNSUPPORTED"),
    BUSY("BUSY"),
    NO_SERVICE_REQUESTS("NO SERVICE REQUESTS");

    private String reason;

    private FailureReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return reason;
    }
}
