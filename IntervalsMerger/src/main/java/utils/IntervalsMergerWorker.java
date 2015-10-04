package utils;

import dao.IntervalDao;
import model.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class IntervalsMergerWorker implements Runnable {
    private final static Logger LOGGER = LoggerFactory.getLogger(IntervalsMergerWorker.class);
    private IntervalDao dao;
    private int targetIterationsNumber;
    private int offset;
    private volatile boolean isInterrupted = false;


    @Override
    public void run() {
        try {
            int numberOfIterations = 0;
            while (!isInterrupted && (targetIterationsNumber > numberOfIterations)) {
                final Optional<Interval> intervalOptional = dao.selectAndMarkAsUsed(offset);
                if (intervalOptional.isPresent()) {
                    final Interval interval = intervalOptional.get();
                    LOGGER.info("Interval was selected and marked for merge: {}", interval);
                    final boolean wasMerged = dao.findOverlappedAndReplace(interval);
                    if (wasMerged) {
                        LOGGER.info("Result of merge: {}, offset: {}, targetIterationsNumber: {}", wasMerged, offset, targetIterationsNumber);
                    } else {
                        dao.unmarkAsUsed(interval);
                    }
                } else {
                    LOGGER.info("Reset offset to zero");
                    offset = -1;
                }
                numberOfIterations++;
                offset++;
            }
        } catch (RuntimeException e) {
            LOGGER.warn("Exception occurred during merge process, reason: {}", e.getCause());
        }
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setTargetIterationsNumber(int targetIterationsNumber) {
        this.targetIterationsNumber = targetIterationsNumber;
    }

    public void setDao(IntervalDao dao) {
        this.dao = dao;
    }
}
