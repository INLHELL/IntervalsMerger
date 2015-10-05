package dao;

import model.Interval;

import java.util.Optional;

public interface IntervalDao {
    void insert(Interval interval);

    void delete(Interval interval);
    
    Optional<Interval> findOverlapped(Interval interval);

    Optional<Interval> select(int offset);

    boolean markAsUsed(Interval interval);

    boolean unmarkAsUsed(Interval interval);

    int getTotalNumberOfIntervals();

    boolean findOverlappedAndReplace(Interval interval);

    Optional<Interval> selectAndMarkAsUsed(int offset);
}
