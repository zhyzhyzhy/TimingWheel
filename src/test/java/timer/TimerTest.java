package timer;

import cc.lovezhy.timer.SystemTimer;
import cc.lovezhy.timer.TimeUtils;
import cc.lovezhy.timer.Timer;
import cc.lovezhy.timer.TimerTask;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TimerTest {


    public static class ArrayBuffer<T> {
        private ArrayList<T> arrayList = new ArrayList<>();

        public void add(T i) {
            arrayList.add(i);
        }

        public Set<T> toSet() {
            return new HashSet<T>(arrayList);
        }

        public void foreach(Consumer<T> consumer) {
            arrayList.forEach(consumer);
        }

        public List<T> toList() {
            return new ArrayList<>(arrayList);
        }
    }

    private class TestTask extends TimerTask {
        private int id;
        private CountDownLatch latch;
        private ArrayBuffer<Integer> output;

        private AtomicBoolean completed = new AtomicBoolean(false);

        public TestTask(long delayMs, int id, CountDownLatch latch, ArrayBuffer<Integer> output) {
            this.delayMs = delayMs;
            this.id = id;
            this.latch = latch;
            this.output = output;
        }


        @Override
        public void run() {
            if (completed.compareAndSet(false, true)) {
                synchronized (output) {
                    output.add(id);
                }
                latch.countDown();
            }
        }
    }


    private Timer timer;

    @Before
    public void setup() {
        timer = new SystemTimer("test", 1, 3, TimeUtils.hiResClockMs());
    }

    @After
    public void teardown() {
        timer.shutdown();
    }

    @Test
    public void testAlreadyExpiredTask() {
        ArrayBuffer<Integer> output = new ArrayBuffer<>();

        List<CountDownLatch> lathes = IntStream.range(-5, 0).mapToObj(i -> {
            CountDownLatch latch = new CountDownLatch(1);
            timer.add(new TestTask(i, i, latch, output));
            return latch;
        }).collect(Collectors.toList());

        timer.advanceClock(0L);

        lathes.forEach(latch -> {
            try {
                assertTrue("already expired tasks should run immediately", latch.await(3, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                //
            }
        });

        Set<Integer> expectSet = new HashSet<>();
        IntStream.range(-5, 0).forEach(expectSet::add);
        assertEquals("output of already expired tasks", expectSet, output.toSet());
    }

    @Test
    public void testTaskExpiration() {
        ArrayBuffer<Integer> output = new ArrayBuffer<>();

        ArrayBuffer<TestTask> tasks = new ArrayBuffer<>();
        ArrayBuffer<Integer> ids = new ArrayBuffer<>();

        List<CountDownLatch> latches = new ArrayList<>();
        latches.addAll(IntStream.range(0, 6).mapToObj(i -> {
            CountDownLatch latch = new CountDownLatch(1);
            tasks.add(new TestTask(i, i, latch, output));
            ids.add(i);
            return latch;
        }).collect(Collectors.toList()));

        latches.addAll(IntStream.range(10, 101).mapToObj(i -> {
            CountDownLatch latch = new CountDownLatch(2);
            tasks.add(new TestTask(i, i, latch, output));
            tasks.add(new TestTask(i, i, latch, output));
            ids.add(i);
            ids.add(i);
            return latch;
        }).collect(Collectors.toList()));

        latches.addAll(IntStream.range(100, 501).mapToObj(i -> {
            CountDownLatch latch = new CountDownLatch(1);
            tasks.add(new TestTask(i, i, latch, output));
            ids.add(i);
            return latch;
        }).collect(Collectors.toList()));

        tasks.foreach(task -> timer.add(task));
        while (timer.advanceClock(2000L)) {
        }

        latches.forEach(latch -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                //
            }
        });

        List<Integer> sortedIds = ids.toList();
        sortedIds.sort(Integer::compareTo);
        assertEquals("output should match", sortedIds, output.toList());

    }
}
