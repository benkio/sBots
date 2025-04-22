ATTACH DATABASE 'botDB.sqlite3' AS prodDB;
ATTACH DATABASE 'botDBBase.sqlite3' AS baseDB;

UPDATE baseDB.media
SET media_count = (
    SELECT media_count
    FROM prodDB.media
    WHERE prodDB.media.media_name = baseDB.media.media_name
)
WHERE EXISTS (
    SELECT 1 FROM prodDB.media WHERE prodDB.media.media_name = baseDB.media.media_name
);

DELETE FROM prodDB.media;
DELETE FROM prodDB.show;

INSERT INTO prodDB.media SELECT * FROM baseDB.media;
INSERT INTO prodDB.show SELECT * FROM baseDB.show;

DETACH DATABASE prodDB;
DETACH DATABASE baseDB;
