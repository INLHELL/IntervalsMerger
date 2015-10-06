package dao;

import model.Interval;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import java.util.Optional;

@ContextConfiguration(locations = {"classpath:test/test-spring-context.xml"})
public class IntervalDaoImplTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private IntervalDao dao;


    @Before
    public void setUp() {
        executeSqlScript("classpath:test/db/db-schema.sql", false);
    }

    @Test
    public void testInsertSingleRecord() {
        dao.insert(new Interval(1, 10));
        final int totalNumberOfIntervals = dao.getTotalNumberOfIntervals();
        final Optional<Interval> intervalOptional = dao.select(0);
        Assert.assertEquals(1, totalNumberOfIntervals);
        Assert.assertEquals(new Interval(1, 10), intervalOptional.get());
    }

    @Test
    public void testInsertTwoRecords() {
        dao.insert(new Interval(1, 10));
        dao.insert(new Interval(1, 11));
        final int totalNumberOfIntervals = dao.getTotalNumberOfIntervals();
        final Optional<Interval> firstInterval = dao.select(0);
        final Optional<Interval> secondInterval = dao.select(1);
        Assert.assertEquals(2, totalNumberOfIntervals);
        Assert.assertEquals(new Interval(1, 10), firstInterval.get());
        Assert.assertEquals(new Interval(1, 11), secondInterval.get());
    }

    @Test
    public void testDeleteRecord() {
        dao.insert(new Interval(1, 10));
        int totalNumberOfIntervals = dao.getTotalNumberOfIntervals();
        Assert.assertEquals(1, totalNumberOfIntervals);
        dao.delete(new Interval(1, 10));
        totalNumberOfIntervals = dao.getTotalNumberOfIntervals();
        Assert.assertEquals(0, totalNumberOfIntervals);
    }

    @Test
    public void testFindOverlappedUsual() {
        dao.insert(new Interval(1, 10));
        dao.insert(new Interval(5, 15));
        dao.insert(new Interval(7, 16));
        final Optional<Interval> overlapped = dao.findOverlapped(new Interval(5, 15));
        Assert.assertEquals(new Interval(1, 10), overlapped.get());
    }

    @Test
    public void testFindOverlappedNoSuitable() {
        dao.insert(new Interval(1, 10));
        dao.insert(new Interval(55, 515));
        dao.insert(new Interval(11, 16));

        final boolean markAsUsed = dao.markAsUsed(new Interval(1, 10));
        Assert.assertTrue(markAsUsed);

        final Optional<Interval> overlapped = dao.findOverlapped(new Interval(1, 10));
        Assert.assertFalse(overlapped.isPresent());
    }

    @Test
    public void testMarkAsUsed() {
        dao.insert(new Interval(1, 10));
        final boolean markAsUsed = dao.markAsUsed(new Interval(1, 10));
        Assert.assertTrue(markAsUsed);
        final Optional<Interval> interval = dao.select(0);
        final Optional<Interval> overlapped = dao.findOverlapped(new Interval(5, 15));
        Assert.assertFalse(interval.isPresent());
        Assert.assertFalse(overlapped.isPresent());
    }

    @Test
    public void testUnMarkAsUsed() {
        dao.insert(new Interval(1, 10));
        final boolean markAsUsed = dao.markAsUsed(new Interval(1, 10));
        Assert.assertTrue(markAsUsed);
        Optional<Interval> interval = dao.select(0);
        Assert.assertFalse(interval.isPresent());
        final boolean markAsUnUsed = dao.markAsUnused(new Interval(1, 10));
        Assert.assertTrue(markAsUsed);
        interval = dao.select(0);
        Assert.assertTrue(interval.isPresent());
    }

    @Test
    public void testSelectAndMarkAsUsed() {
        dao.insert(new Interval(1, 10));
        dao.insert(new Interval(5, 15));
        dao.insert(new Interval(12, 48));
        dao.insert(new Interval(13, 14));
        dao.insert(new Interval(3, 24));

        final Optional<Interval> firstInterval = dao.selectAndMarkAsUsed(0);
        final Optional<Interval> secondInterval = dao.selectAndMarkAsUsed(0);
        Assert.assertTrue(firstInterval.isPresent());
        Assert.assertTrue(secondInterval.isPresent());

        boolean markAsUsed = dao.markAsUsed(firstInterval.get());
        Assert.assertFalse(markAsUsed);
        markAsUsed = dao.markAsUsed(secondInterval.get());
        Assert.assertFalse(markAsUsed);

        Assert.assertEquals(new Interval(1, 10), firstInterval.get());
        Assert.assertEquals(new Interval(3, 24), secondInterval.get());
    }

    @Test
    public void testFindOverlappedAndReplace() {
        dao.insert(new Interval(1, 10));
        dao.insert(new Interval(5, 15));
        dao.insert(new Interval(12, 48));
        dao.insert(new Interval(13, 14));
        dao.insert(new Interval(3, 24));

        boolean markAsUsed = dao.markAsUsed(new Interval(1, 10));
        Assert.assertTrue(markAsUsed);

        final boolean overlappedAndReplaced = dao.findOverlappedAndReplace(new Interval(1, 10));
        Assert.assertTrue(overlappedAndReplaced);

        final int totalNumberOfIntervals = dao.getTotalNumberOfIntervals();
        Assert.assertEquals(4, totalNumberOfIntervals);

        markAsUsed = dao.markAsUsed(new Interval(1, 24));
        Assert.assertTrue(markAsUsed);

        markAsUsed = dao.markAsUsed(new Interval(1, 10));
        Assert.assertFalse(markAsUsed);
        markAsUsed = dao.markAsUsed(new Interval(3, 24));
        Assert.assertFalse(markAsUsed);
    }

    @Test
    public void testFindOverlappedAndReplaceNextToMarked() {
        dao.insert(new Interval(1, 10));
        dao.insert(new Interval(5, 15));
        dao.insert(new Interval(12, 48));
        dao.insert(new Interval(13, 14));
        dao.insert(new Interval(3, 24));

        boolean markAsUsed = dao.markAsUsed(new Interval(1, 10));
        Assert.assertTrue(markAsUsed);
        markAsUsed = dao.markAsUsed(new Interval(3, 24));
        Assert.assertTrue(markAsUsed);

        final boolean overlappedAndReplaced = dao.findOverlappedAndReplace(new Interval(1, 10));
        Assert.assertTrue(overlappedAndReplaced);

        final int totalNumberOfIntervals = dao.getTotalNumberOfIntervals();
        Assert.assertEquals(4, totalNumberOfIntervals);

        markAsUsed = dao.markAsUsed(new Interval(1, 15));
        Assert.assertTrue(markAsUsed);

        markAsUsed = dao.markAsUsed(new Interval(1, 10));
        Assert.assertFalse(markAsUsed);
        markAsUsed = dao.markAsUsed(new Interval(5, 15));
        Assert.assertFalse(markAsUsed);
    }

}