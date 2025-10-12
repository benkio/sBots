CREATE TABLE IF NOT EXISTS show(
  show_id TEXT PRIMARY KEY NOT NULL,
  bot_id TEXT NOT NULL,
  show_title TEXT NOT NULL,
  show_upload_date TEXT NOT NULL,
  show_duration  INTEGER NOT NULL,
  show_description TEXT NULL,
  show_is_live BOOLEAN NOT NULL,
  show_origin_automatic_caption TEXT NULL,
  FOREIGN KEY (bot_id) REFERENCES bot(id)
);
