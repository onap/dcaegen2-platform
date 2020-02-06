DROP TABLE IF EXISTS dti.rt_send_history CASCADE;
CREATE TABLE dti.rt_send_history
(
	SEND_QUEUED_DATETIME	VARCHAR(17) NOT NULL,
	SEND_PROCESSED_DATETIME VARCHAR(17) NOT NULL,
	UPDATED_OBJECT		VARCHAR(50) NOT NULL,	
	TIMESTAMP		VARCHAR(14) NOT NULL,
	RT_SEND_STATUS		VARCHAR(4) NOT NULL,
	UPDATED_ON                VARCHAR(20)
);

