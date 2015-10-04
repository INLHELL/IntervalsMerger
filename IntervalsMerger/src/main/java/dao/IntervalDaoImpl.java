package dao;

import model.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Optional;

public class IntervalDaoImpl extends JdbcDaoSupport implements IntervalDao {
    private final static Logger LOGGER = LoggerFactory.getLogger(IntervalDaoImpl.class);

    private static final String START_COLUMN = "start_i";
    private static final String END_COLUMN = "end_i";
    private PlatformTransactionManager transactionManager;

    @Override
    public void insert(Interval interval) {
        try {
            String query = "INSERT INTO test_interval(start_i, end_i) VALUES (?, ?);";
            final int rowsUpdated = getJdbcTemplate().update(query, new Integer[]{interval.getStart(), interval.getEnd()});
            LOGGER.info("Number of updated rows: {}", rowsUpdated);
        } catch (DataAccessException e) {
            LOGGER.warn("Interval: {} insert query failed, reason: {}", interval, e.getMessage());
        }
    }

    @Override
    public void delete(Interval interval) {
        try {
            String query = "DELETE FROM test_interval WHERE start_i = ? AND end_i = ?;";
            final int rowsUpdated = getJdbcTemplate().update(query, new Integer[]{interval.getStart(), interval.getEnd()});
            LOGGER.info("Number of deleted rows: {}", rowsUpdated);
        } catch (DataAccessException e) {
            LOGGER.warn("Interval: {} delete query failed, reason: {}", interval, e.getMessage());
        }
    }

    @Override
    public Optional<Interval> select(int offset) {
        Interval result = null;
        try {
            String query = "SELECT start_i, end_i FROM test_interval WHERE used = FALSE ORDER BY start_i LIMIT 1 OFFSET ?;";
            result = getJdbcTemplate().queryForObject(query, new Integer[]{offset}, (resultSet, rowIndex) -> {
                return new Interval(resultSet.getInt(START_COLUMN), resultSet.getInt(END_COLUMN));
            });
            LOGGER.info("Interval: {} selected with offset: {}", result, offset);
        } catch (DataAccessException e) {
            LOGGER.warn("Interval selection with offset: {} failed, reason: {}", offset, e.getMessage());
        }
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<Interval> findOverlapped(Interval interval) {
        Interval result = null;
        try {
            String query = "SELECT start_i, end_i  FROM test_interval WHERE start_i <= ?  AND end_i >= ? AND used = FALSE ORDER BY start_i LIMIT 1;";
            result = getJdbcTemplate().queryForObject(query, new Integer[]{interval.getEnd(), interval.getStart()}, (resultSet, rowIndex) -> {
                return new Interval(resultSet.getInt(START_COLUMN), resultSet.getInt(END_COLUMN));
            });
            LOGGER.info("Interval: {} overlapped with given: {}, was found", result, interval);
        } catch (DataAccessException e) {
            LOGGER.warn("Interval: {} find overlapped query failed, reason: {}", interval, e.getMessage());
        }
        return Optional.ofNullable(result);
    }

    @Override
    public boolean markAsUsed(Interval interval) {
        boolean wasMarked = false;
        try {
            String query = "UPDATE test_interval SET used = TRUE WHERE start_i = ? AND end_i = ? AND used = FALSE;";
            final int rowsUpdated = getJdbcTemplate().update(query, new Integer[]{interval.getStart(), interval.getEnd()});
            wasMarked = rowsUpdated > 0 ? true : false;
            LOGGER.info("Interval: {},  was marked as used: {}", interval, wasMarked);
        } catch (DataAccessException e) {
            LOGGER.warn("Marking interval: {} as used was failed: {}", interval, e.getMessage());
        }
        return wasMarked;
    }

    @Override
    public void unmarkAsUsed(Interval interval) {
        boolean wasMarked = false;
        try {
            String query = "UPDATE test_interval SET used = FALSE WHERE start_i = ? AND end_i = ? AND used = TRUE;";
            final int rowsUpdated = getJdbcTemplate().update(query, new Integer[]{interval.getStart(), interval.getEnd()});
            LOGGER.info("Interval: {},  was marked as unused: {}", interval, wasMarked);
        } catch (DataAccessException e) {
            LOGGER.warn("Marking interval: {} as unused was failed: {}", interval, e.getMessage());
        }
    }

    @Override
    public boolean findOverlappedAndReplace(Interval interval) {
        boolean wasSucceeded = false;
        TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
        try {
            LOGGER.info("Trying to find interval that overlaps: given {}", interval);
            final Optional<Interval> intervalOptional = findOverlapped(interval);
            if (intervalOptional.isPresent()) {
                final Interval overlappedInterval = intervalOptional.get();
                LOGGER.info("Found interval that overlapped by given - {} /\\ {}", interval, overlappedInterval);
                LOGGER.info("Marking interval: {} as used", overlappedInterval);
                boolean wasMarked = markAsUsed(overlappedInterval);
                if (!wasMarked) {
                    LOGGER.info("Interval: {}, was NOT marked as used", intervalOptional.get());
                } else {
                    LOGGER.info("Interval: {}, was marked as used", overlappedInterval);
                    LOGGER.info("Deleting both intervals: {}, {}", interval, overlappedInterval);
                    delete(interval);
                    delete(overlappedInterval);
                    final Interval mergedInterval = interval.mergeWith(overlappedInterval);
                    LOGGER.info("Inserting new merged interval: {} <- {} /\\ {}", mergedInterval, interval, overlappedInterval);
                    insert(mergedInterval);
                    LOGGER.info("Interval: {} was successfully inserted!", mergedInterval);
                    wasSucceeded = true;
                }
            }
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            logger.error("Transactions for finding overlapped interval failed", e);
        }
        return wasSucceeded;
    }

    @Override
    public Optional<Interval> selectAndMarkAsUsed(int offset) {
        Optional<Interval> intervalOptional = Optional.empty();
        TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
        try {
            LOGGER.info("Trying to find interval for merge, offset: {}", offset);
            intervalOptional = select(offset);
            if (intervalOptional.isPresent()) {
                LOGGER.info("Interval for merge found: {}", intervalOptional.get());
                LOGGER.info("Marking interval: {} as used", intervalOptional.get());
                boolean wasMarked = markAsUsed(intervalOptional.get());
                if (!wasMarked) {
                    LOGGER.info("Interval: {}, was NOT marked as used", intervalOptional.get());
                    intervalOptional = Optional.empty();
                } else {
                    LOGGER.info("Interval: {}, was marked as used", intervalOptional.get());
                }
            }
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            logger.error("Transactions failed, selecting and marking interval as used", e);
        }
        return intervalOptional;
    }


    public void setTransactionManager(PlatformTransactionManager txManager) {
        this.transactionManager = txManager;
    }

}
