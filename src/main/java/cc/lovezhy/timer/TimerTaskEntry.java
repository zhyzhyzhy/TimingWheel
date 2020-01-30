package cc.lovezhy.timer;

public class TimerTaskEntry implements Comparable<TimerTaskEntry> {

    volatile TimerTaskList list;
    TimerTaskEntry prev;
    TimerTaskEntry next;

    TimerTask timerTask;
    long expirationMs;


    public TimerTaskEntry(TimerTask timerTask, long expirationMs) {
        this.timerTask = timerTask;
        this.expirationMs = expirationMs;
        if (this.timerTask != null) {
            timerTask.setTimerTaskEntry(this);
        }
    }

    public boolean cancelled() {
        return timerTask.getTimerTaskEntry() != this;
    }

    public void remove() {
        TimerTaskList currentList = list;
        while (currentList != null) {
            currentList.remove(this);
            currentList = list;
        }
    }

    @Override
    public int compareTo(TimerTaskEntry o) {
        return (int) (this.expirationMs - o.expirationMs);
    }
}
