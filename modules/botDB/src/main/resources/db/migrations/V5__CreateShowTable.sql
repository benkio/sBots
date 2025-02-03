CREATE TABLE IF NOT EXISTS show(
  show_url TEXT PRIMARY KEY NOT NULL,
  bot_name TEXT NOT NULL,
  show_title TEXT NOT NULL,
  show_upload_date TEXT NOT NULL,
  show_duration  INTEGER NOT NULL,
  show_description TEXT NULL,
  show_is_live BOOLEAN NOT NULL,
  show_origin_automatic_caption TEXT NULL
);
