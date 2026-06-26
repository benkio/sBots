CREATE TABLE IF NOT EXISTS log(
  log_time BIGINT NOT NULL,
  message TEXT NOT NULL,
  PRIMARY KEY(log_time)
);

CREATE UNIQUE INDEX IF NOT EXISTS log_key
ON log(log_time);
