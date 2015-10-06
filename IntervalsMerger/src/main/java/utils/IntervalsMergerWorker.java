package utils;

import dao.IntervalDao;
import model.Interval;
import org.apache.commons.math3.util.Precision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Optional;

public class IntervalsMergerWorker implements Runnable {
    private final static Logger LOGGER = LoggerFactory.getLogger(IntervalsMergerWorker.class);

    private final int totalNumberOfWorkers;

    private volatile boolean isInterrupted = false;

    private int workerNumber;
    private IntervalDao dao;
    private int targetIterationsNumber;

    private long failsIterCounter = 0;
    private long mergeIterCounter = 0;
    private long mergeTotalCounter = 0;
    private long failsTotalCounter = 0;
    private long globalIter = 0;

    public IntervalsMergerWorker(int totalNumberOfWorkers) {
        this.totalNumberOfWorkers = totalNumberOfWorkers;
    }

    @Override
    public void run() {
        try {
            LOGGER.info("Worker number: {}, total number of workers: {}", workerNumber, totalNumberOfWorkers);
            int numberOfIterations = 0;
            int startOffset = calculateOffsetsRange();
            while (!isInterrupted && (targetIterationsNumber > numberOfIterations)) {
                final Optional<Interval> intervalOptional = dao.selectAndMarkAsUsed(startOffset);
                if (intervalOptional.isPresent()) {
                    final Interval interval = intervalOptional.get();
                    LOGGER.info("Interval was selected and marked for merge: {}", interval);
                    final boolean wasMerged = dao.findOverlappedAndReplace(interval);
                    if (wasMerged) {
                        LOGGER.info("Result of merge: {}, startOffset: {}, targetIterationsNumber: {}", wasMerged, startOffset, targetIterationsNumber);
                        mergeIterCounter++;
                    } else {
                        dao.markAsUnused(interval);
                        failsIterCounter++;
                    }
                } else {
                    LOGGER.info("Reset startOffset to zero");
//                    startOffset = -1;
                    failsIterCounter++;
                }
                numberOfIterations++;
//                startOffset++;
            }
            collectStatistics();
        } catch (RuntimeException e) {
            LOGGER.warn("Exception occurred during merge process, reason: {}", e.getCause());
        }
    }

    private void collectStatistics() {
        incrementCounters();
        LOGGER.warn("MERGED_ITER: {}, MERGED_TOTAL: {}, FAILS_ITER: {}, FAILS_TOTAL: {}, RATIO_ITER: {}, " +
                        "RATIO_TOTAL: {}, ITERATION: {}",
                mergeIterCounter, mergeTotalCounter, failsIterCounter, failsTotalCounter,
                Precision.round((double) failsIterCounter / mergeIterCounter, 1, BigDecimal.ROUND_UP), Precision.round((double)
                        failsTotalCounter / mergeTotalCounter, 1, BigDecimal.ROUND_UP), globalIter);
        resetCounters();
    }

    public void setTargetIterationsNumber(int targetIterationsNumber) {
        this.targetIterationsNumber = targetIterationsNumber;
    }

    public void setDao(IntervalDao dao) {
        this.dao = dao;
    }

    public void setWorkerNumber(int workerNumber) {
        this.workerNumber = workerNumber;
    }

    public void interrupt() {
        this.isInterrupted = true;
    }


    private void incrementCounters() {
        globalIter++;
        mergeTotalCounter += mergeIterCounter;
        failsTotalCounter += failsIterCounter;
    }

    private void resetCounters() {
        mergeIterCounter = 0;
        failsIterCounter = 0;
    }

    private int calculateOffsetsRange() {
        int startOffset = 0;
        final int totalNumberOfIntervals = dao.getTotalNumberOfIntervals();
        final int numberOfDiscoveredIntervals = totalNumberOfIntervals / totalNumberOfWorkers;
        startOffset = numberOfDiscoveredIntervals * (workerNumber - 1);
        LOGGER.warn("Start offset: {}, total intervals: {}", startOffset, totalNumberOfIntervals);
        return startOffset;
    }
}
