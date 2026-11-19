-- The table was written by the application at its exit and read by the heartbeat: the exit is now in the logs (Loki),
-- in the history file and in the /fail signal of the health check, so the table has no job left (#1449).
DROP INDEX IF EXISTS log_key;
DROP TABLE IF EXISTS log;
