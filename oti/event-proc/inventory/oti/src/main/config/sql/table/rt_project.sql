CREATE TABLE IF NOT EXISTS dti.rt_project
(
    project_name        VARCHAR(50) NOT NULL,
    resource_version    VARCHAR(25),
    updated_on          VARCHAR(20),
    PRIMARY KEY (project_name)
);
