\echo executing "alter table dti.vnodelist alter column vnodelistkey type BIGINT;"
alter table dti.vnodelist alter column vnodelistkey type BIGINT;


\echo executing "ALTER TABLE dti.dcae_event ALTER COLUMN dcae_target_collection_ip TYPE VARCHAR(45);"
ALTER TABLE dti.dcae_event ALTER COLUMN dcae_target_collection_ip TYPE VARCHAR(45);

\echo executing "ALTER TABLE dti.rt_vnfc ALTER COLUMN nfc_function TYPE VARCHAR(50);"
ALTER TABLE dti.rt_vnfc ALTER COLUMN nfc_function TYPE VARCHAR(50);