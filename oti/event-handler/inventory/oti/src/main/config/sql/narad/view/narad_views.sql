BEGIN;
\echo "executing v_narad_chassis_pnf.sql"
\i v_narad_chassis_pnf.sql

\echo "executing v_narad_chassis_pserver.sql"
\i v_narad_chassis_pserver.sql
COMMIT;
