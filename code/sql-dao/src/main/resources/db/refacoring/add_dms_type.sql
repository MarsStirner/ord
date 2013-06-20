CREATE TABLE dms_type (id INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (id), name varchar(255));

INSERT INTO dms_type (name) VALUES ("PaperCopyDocument"), ("IncomingDocument"),("InternalDocument"), ("OutgoingDocument"), ("Task"), ("OfficeKeepingFile"), ("OfficeKeepingVolume"), ("RequestDocument");