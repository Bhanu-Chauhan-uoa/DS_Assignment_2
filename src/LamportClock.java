public class LamportClock {
    private int time;
    // Update the clock based
    public synchronized void updateTime(int receivedTimestamp) {
        time = Math.max(time, receivedTimestamp) + 1;
    }

    // Increment the local time
    public synchronized int increment() {
        time++;
        return time;
    }

    // Get the current local time
    public synchronized int getCurrTime() {
        return time;
    }
}