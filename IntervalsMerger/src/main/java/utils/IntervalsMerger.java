package utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class IntervalsMerger {
    private int delay;
    private int parallelWorkers;
    private int initialDelay;

    public abstract IntervalsMergerWorker getWorker();

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public void setParallelWorkers(int parallelWorkers) {
        this.parallelWorkers = parallelWorkers;
    }

    public void setInitialDelay(int initialDelay) {
        this.initialDelay = initialDelay;
    }

    public void executeWorkers() {
        final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(parallelWorkers);
        for (int i = 0; i < parallelWorkers; i++) {
            executorService.scheduleWithFixedDelay(getWorker(), initialDelay, delay, TimeUnit.MILLISECONDS);
        }

    }

}
