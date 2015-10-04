package utils;

import dao.IntervalDao;
import model.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;

public abstract class IntervalsGenerator {
    private final static Logger LOGGER = LoggerFactory.getLogger(IntervalsGenerator.class);

    private IntervalDao dao;
    private int generatedIntervals;

    public void setDao(IntervalDao dao) {
        this.dao = dao;
    }

    public abstract Interval createInterval();

    public void generateIntervals() {
        for (int i = 0; i < generatedIntervals; i++) {
            final Interval interval = createInterval();
            dao.insert(interval);
            LOGGER.info("Iteration: {} from: {}, interval: {}", i, generatedIntervals, interval);
        }
    }


    public void setGeneratedIntervals(int generatedIntervals) {
        this.generatedIntervals = generatedIntervals;
    }

    public int getGeneratedIntervals() {
        return generatedIntervals;
    }
}
