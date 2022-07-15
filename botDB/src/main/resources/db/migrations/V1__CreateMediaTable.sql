CREATE TABLE Media(
  mediaName VARCHAR(255) PRIMARY KEY,
  mediaContent LONGBLOB NOT NULL,
  createdAt TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX Media__Key
ON Media(mediaName);
