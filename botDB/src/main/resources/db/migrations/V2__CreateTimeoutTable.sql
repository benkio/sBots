CREATE TABLE timeout(
  chat_id VARCHAR(255) PRIMARY KEY,
  timeout_value VARCHAR(255) NOT NULL,
  last_interaction TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX timeout_key
ON timeout(chat_id);
