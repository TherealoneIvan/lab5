public class InMapMessage {
    private boolean inMap;
    private int delay;

    public InMapMessage(boolean inMap, int delay) {
        this.inMap = inMap;
        this.delay = delay;
    }

    public boolean isInMap() {
        return inMap;
    }

    public void setInMap(boolean inMap) {
        this.inMap = inMap;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }
}