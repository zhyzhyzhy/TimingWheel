package timer;

import cc.lovezhy.timer.TimerTask;
import cc.lovezhy.timer.TimerTaskEntry;
import cc.lovezhy.timer.TimerTaskList;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class TimerTaskListTest {

    private class TestTask extends TimerTask {
        public TestTask(long delayMs) {
            this.delayMs = delayMs;
        }

        @Override
        public void run() {

        }
    }


    private int size(TimerTaskList list) {
        AtomicReference<Integer> count = new AtomicReference<>();
        count.set(0);
        list.foreach(timerTask -> count.set(count.get() + 1));
        return count.get();
    }

    @Test
    public void testAll() {
        AtomicInteger sharedCounter = new AtomicInteger(0);

        TimerTaskList list1 = new TimerTaskList(sharedCounter);
        TimerTaskList list2 = new TimerTaskList(sharedCounter);
        TimerTaskList list3 = new TimerTaskList(sharedCounter);

        List<TestTask> tasks = IntStream.range(1, 11).mapToObj(i -> {
            TestTask task = new TestTask(0L);
            list1.add(new TimerTaskEntry(task, 10L));
            assertEquals(i, sharedCounter.get());
            return task;
        }).collect(Collectors.toList());

        assertEquals(tasks.size(), sharedCounter.get());

        for (int i = 0; i < 4; i++) {
            TestTask task = tasks.get(i);
            int prevCount = sharedCounter.get();
            list2.add(new TimerTaskEntry(task, 10L));
            assertEquals(prevCount, sharedCounter.get());
        }

        assertEquals(10 - 4, size(list1));

        assertEquals(4, size(list2));

        assertEquals(tasks.size(), sharedCounter.get());

        while (tasks.size() > 4) {
            TestTask task = tasks.remove(4);
            int prevCount = sharedCounter.get();
            list3.add(new TimerTaskEntry(task, 10L));
            assertEquals(prevCount, sharedCounter.get());
        }

        assertEquals(0, size(list1));

        assertEquals(4, size(list2));

        assertEquals(6, size(list3));

        // cancel tasks in lists
        list1.foreach(TimerTask::cancel);
        assertEquals(0, size(list1));
        assertEquals(4, size(list2));
        assertEquals(6, size(list3));

        list2.foreach(TimerTask::cancel);
        assertEquals(0, size(list1));
        assertEquals(0, size(list2));
        assertEquals(6, size(list3));

        list3.foreach(TimerTask::cancel);
        assertEquals(0, size(list1));
        assertEquals(0, size(list2));
        assertEquals(0, size(list3));

    }




}
