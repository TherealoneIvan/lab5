package bmstu;
public class InMapMessage {
    private boolean url;
    private int delay;

    public InMapMessage(boolean inMap, int delay) {
        this.url = inMap;
        this.delay = delay;
    }

    public boolean isUrl() {
        return url;
    }

    public void setUrl(boolean url) {
        this.url = url;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }
}