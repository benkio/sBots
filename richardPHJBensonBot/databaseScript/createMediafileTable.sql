DROP TABLE IF EXISTS Mediafile;

CREATE TABLE Mediafile (
	file_id    INTEGER PRIMARY KEY,
	file_name  TEXT NOT NULL,
	file_type  TEXT NOT NULL,
	file_data  BLOB NOT NULL,
        modifiedAt DATETIME DEFAULT CURRENT_TIMESTAMP
);
