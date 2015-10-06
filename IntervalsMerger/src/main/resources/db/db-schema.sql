DROP TABLE IF EXISTS test_interval;
CREATE TABLE test_interval (
  start_i INTEGER NOT NULL,
  end_i INTEGER NOT NULL,
  used BOOLEAN DEFAULT FALSE,
  PRIMARY KEY(start_i,end_i),
  CONSTRAINT end_gte_start CHECK (end_i >= start_i)
);
