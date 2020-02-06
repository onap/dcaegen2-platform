BEGIN;
\i sql/table/rt_line_of_business.sql
\i sql/table/rt_owning_entity.sql
\i sql/table/rt_platform.sql
\i sql/table/rt_project.sql
COMMIT;

alter table dti.vnodelist alter column PROV_STATUS TYPE varchar(20);
