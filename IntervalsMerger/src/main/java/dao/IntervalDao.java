package dao;

import model.Interval;

import java.util.Optional;

public interface IntervalDao {
    /**
     * Inserts new interval to database.
     *
     * @param interval some valid interval
     */
    void insert(Interval interval);

    /**
     * Deletes interval from database.
     *
     * @param interval some valid existing interval
     */
    void delete(Interval interval);

    /**
     * Finds interval that overlapped by given.
     *
     * @param interval some valid interval
     * @return interval that overlaps with passed
     */
    Optional<Interval> findOverlapped(Interval interval);

    /**
     * Selects interval from database.
     *
     * @param offset means how many intervals will be skipped during selection
     * @return interval
     */
    Optional<Interval> select(int offset);

    /**
     * Marks passed interval as used by some operations, this also means that this interval not available for
     * selection for other queries.
     *
     * @param interval some valid interval
     * @return <code>true</code> if interval was successfully marked as <i>used</i>, <code>false</code> otherwise
     */
    boolean markAsUsed(Interval interval);

    /**
     * Undo marking for given interval.
     *
     * @param interval some valid interval
     * @return <code>true</code> if interval was successfully marked as <i>unused</i>, <code>false</code> otherwise
     */
    boolean markAsUnused(Interval interval);

    /**
     * Returns total number of intervals available in database.
     *
     * @return number of intervals
     */
    int getTotalNumberOfIntervals();

    /**
     * Finds interval overlapped by given, merge them into single interval, then delete both source interval and
     * finally inserts newly created merged interval to database.
     *
     * @param interval some valid interval
     * @return <code>true</code> if all operations were done and merged interval was inserted, <code>false</code>
     * otherwise
     */
    boolean findOverlappedAndReplace(Interval interval);

    /**
     * Selects some interval from database and marks it as used.
     *
     * @param offset means how many intervals will be skipped during selection.
     * @return interval (if it was successfully selected and marked) wrapped by {@link Optional} object
     */
    Optional<Interval> selectAndMarkAsUsed(int offset);
}
