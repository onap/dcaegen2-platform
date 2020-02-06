BEGIN;

drop table dti.narad_subnet if exists;

\echo executing narad_subnet.sql
\i narad_subnet.sql

COMMIT;
