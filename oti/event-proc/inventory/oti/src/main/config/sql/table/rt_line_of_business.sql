CREATE TABLE IF NOT EXISTS dti.rt_line_of_business
(
    line_of_business    VARCHAR(150) NOT NULL,
    resource_version    VARCHAR(25),
    updated_on          VARCHAR(20),
    PRIMARY KEY (line_of_business)
);
