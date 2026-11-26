-- The prompt template is modified in place and git holds its history (#1594): the trace no longer records a version.
ALTER TABLE llm_trace DROP COLUMN prompt_version;
