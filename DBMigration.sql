ATTACH DATABASE 'botDB.sqlite3' AS prodDB;
ATTACH DATABASE 'botDBBase.sqlite3' AS baseDB;

DELETE FROM prodDB.media;
DELETE FROM prodDB.show;

INSERT INTO prodDB.media SELECT * FROM baseDB.media;
INSERT INTO prodDB.show SELECT * FROM baseDB.show;

