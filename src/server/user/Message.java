package server.user;

public class Message implements Serializable {
    private String operation;
    private List<String> arguments;
    private LogicalClock logicalTimestamp;

    public Message(String operation, List<String> arguments, LogicalClock logicalTimestamp) {
        this.operation = operation;
        this.arguments = arguments;
        this.logicalTimestamp = logicalTimestamp;
    }

    public String getOperation() {
        return operation;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public LogicalClock getLogicalTimestamp() {
        return logicalTimestamp;
    }

    public void setLogicalTimestamp(LogicalClock logicalTimestamp) {
        this.logicalTimestamp = logicalTimestamp;
    }

    @Override
    public String toString() {
        return "Message [operation=" + operation + ", arguments=" + arguments + ", logicalTimestamp=" + logicalTimestamp + "]";
    }
}
