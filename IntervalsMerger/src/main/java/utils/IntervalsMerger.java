package utils;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

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
        BasicThreadFactory factory = new BasicThreadFactory.Builder().namingPattern("Worker-%d").build();
        final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(parallelWorkers, factory);
        for (int workerNumber = 1; workerNumber <= parallelWorkers; workerNumber++) {
            final IntervalsMergerWorker worker = getWorker();
            worker.setWorkerNumber(workerNumber);
            executorService.scheduleWithFixedDelay(worker, initialDelay, delay, TimeUnit.MILLISECONDS);
        }

    }

}
