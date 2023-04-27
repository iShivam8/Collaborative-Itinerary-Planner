package server;

import java.io.Serializable;
import java.util.List;

public class Message implements Serializable {
    private String operation;
    private String key;
    private String value;
    private List<String> arguments;
    //private LogicalClock logicalTimestamp;
    private int logicalTimestamp;

    public Message(String operation, String key, String value, int logicalTimestamp) {
        this.operation = operation;
        this.key = key;
        this.value = value;
        this.logicalTimestamp = logicalTimestamp;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
    public String getOperation() {
        return operation;
    }
    public int getLogicalTimestamp() {
        return logicalTimestamp;
    }
    public void setLogicalTimestamp(int logicalTimestamp) {
        this.logicalTimestamp = logicalTimestamp;
    }

    @Override
    public String toString() {
        return "Message [operation=" + operation + ", arguments=" + arguments + ", logicalTimestamp=" + logicalTimestamp + "]";
    }
}
