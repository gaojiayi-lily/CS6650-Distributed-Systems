CREATE SCHEMA IF NOT EXISTS PurchaseRecord;
USE PurchaseRecord;

DROP TABLE IF EXISTS Records;

CREATE TABLE Records (
	RecordId INT auto_increment,
    StoreId INT,
    CustomerId INT,
    Date VARCHAR(255),
    CONSTRAINT pk_Records_RecordId PRIMARY KEY(RecordId)
);

insert into Records(StoreId, CustomerId, Date)
values ('0','0','20210301');


