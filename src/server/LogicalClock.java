package server;

public class LogicalClock {
    private int timestamp = 0;
    private int time;
    public LogicalClock() {
        this.time = 0;
    }

    public synchronized int getTimestamp() {
        return timestamp;
    }

    public synchronized void increment() {
        timestamp++;
    }
    public synchronized void tick() {
        time++;
    }

    public synchronized void update(int receivedTimestamp) {
        timestamp = Math.max(timestamp, receivedTimestamp) + 1;
    }
}

