package model;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class IntervalTest {

    @Test
    public void testConstructionStartGreaterEnd() throws Exception {
        Interval interval = new Interval(10 , 1);
        Assert.assertEquals(1, interval.getStart());
        Assert.assertEquals(10, interval.getEnd());
    }

    @Test
    public void testConstruction() throws Exception {
        Interval interval = new Interval(1 , 10);
        Assert.assertEquals(1, interval.getStart());
        Assert.assertEquals(10, interval.getEnd());
    }

    @Test
    public void testMergeUsualOverlap() throws Exception {
        Interval firstInterval = new Interval(1 , 10);
        Interval secondInterval = new Interval(5 , 15);
        final Interval mergedInterval = firstInterval.mergeWith(secondInterval);
        Assert.assertEquals(1, mergedInterval.getStart());
        Assert.assertEquals(15, mergedInterval.getEnd());
    }

    @Test
    public void testMergeFirstIncludsSecond() throws Exception {
        Interval firstInterval = new Interval(1 , 15);
        Interval secondInterval = new Interval(5 , 10);
        final Interval mergedInterval = firstInterval.mergeWith(secondInterval);
        Assert.assertEquals(1, mergedInterval.getStart());
        Assert.assertEquals(15, mergedInterval.getEnd());
    }

    @Test
    public void testMergeSecondIncludsFirst() throws Exception {
        Interval firstInterval = new Interval(5 , 10);
        Interval secondInterval = new Interval(1 , 15);
        final Interval mergedInterval = firstInterval.mergeWith(secondInterval);
        Assert.assertEquals(1, mergedInterval.getStart());
        Assert.assertEquals(15, mergedInterval.getEnd());
    }

    @Test
    public void testMergeFirstIncludsSecondSameEnd() throws Exception {
        Interval firstInterval = new Interval(1 , 10);
        Interval secondInterval = new Interval(5 , 10);
        final Interval mergedInterval = firstInterval.mergeWith(secondInterval);
        Assert.assertEquals(1, mergedInterval.getStart());
        Assert.assertEquals(10, mergedInterval.getEnd());
    }

    @Test
    public void testMergeFirstIncludsSecondSameStart() throws Exception {
        Interval firstInterval = new Interval(1 , 10);
        Interval secondInterval = new Interval(1 , 5);
        final Interval mergedInterval = firstInterval.mergeWith(secondInterval);
        Assert.assertEquals(1, mergedInterval.getStart());
        Assert.assertEquals(10, mergedInterval.getEnd());
    }

    @Test
    public void testMergeOverlapByStartEnd() throws Exception {
        Interval firstInterval = new Interval(1 , 10);
        Interval secondInterval = new Interval(10 , 15);
        final Interval mergedInterval = firstInterval.mergeWith(secondInterval);
        Assert.assertEquals(1, mergedInterval.getStart());
        Assert.assertEquals(15, mergedInterval.getEnd());
    }

}