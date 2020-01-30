package cc.lovezhy.timer;

public class TimeUtils {

    /**
     * Returns the value returned by `nanoseconds` converted into milliseconds.
     */
    public static long hiResClockMs() {
        return System.currentTimeMillis();
    }
}
