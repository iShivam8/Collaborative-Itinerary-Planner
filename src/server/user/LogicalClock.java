package server.user;

public class LogicalClock {
    private int timestamp = 0;

    public synchronized int getTimestamp() {
        return timestamp;
    }

    public synchronized void increment() {
        timestamp++;
    }

    public synchronized void update(int receivedTimestamp) {
        timestamp = Math.max(timestamp, receivedTimestamp) + 1;
    }
}
