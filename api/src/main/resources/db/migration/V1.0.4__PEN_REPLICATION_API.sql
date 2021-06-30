--------------------------------------------------------
--  DDL for Table PEN_DEMOG_TX
--------------------------------------------------------
CREATE TABLE API_PEN_REPLICATION.PEN_DEMOG_TX
(
    TX_ID                  VARCHAR(10),
    TX_TYPE                VARCHAR(4),
    TX_STATUS              VARCHAR(4),
    TX_INSERT_DATE_TIME    TIMESTAMP (6),
    TX_PROCESSED_DATE_TIME TIMESTAMP (6),
    STUD_NO                VARCHAR(10),
    STUD_SURNAME           VARCHAR(25),
    STUD_GIVEN             VARCHAR(25),
    STUD_MIDDLE            VARCHAR(25),
    USUAL_SURNAME          VARCHAR(25),
    USUAL_GIVEN            VARCHAR(25),
    USUAL_MIDDLE           VARCHAR(25),
    STUD_BIRTH             VARCHAR(8),
    STUD_SEX               VARCHAR(1),
    STUD_DEMOG_CODE        VARCHAR(1),
    STUD_STATUS            VARCHAR(1),
    PEN_LOCAL_ID           VARCHAR(12),
    PEN_MINCODE            VARCHAR(8),
    POSTAL                 VARCHAR(7),
    STUD_TRUE_NO           VARCHAR(10),
    MERGE_TO_USER_NAME     VARCHAR(15),
    MERGE_TO_CODE          VARCHAR(2),
    CREATE_DATE            DATE,
    CREATE_USER_NAME       VARCHAR(15),
    UPDATE_DATE            DATE,
    UPDATE_USER_NAME       VARCHAR(15),
    STUD_GRADE             VARCHAR(2),
    STUD_GRADE_YEAR        NUMBER (4,0),
    UPDATE_DEMOG_DATE      DATE,
    MERGE_TO_DATE          DATE
) ;

--------------------------------------------------------
--  DDL for Table PEN_DEMOG_TX
--------------------------------------------------------
CREATE TABLE API_PEN_REPLICATION.PEN_TWINS_TX
(
    TX_ID                  VARCHAR(10),
    TX_TYPE                VARCHAR(4),
    TX_STATUS              VARCHAR(4),
    TX_INSERT_DATE_TIME    TIMESTAMP(6),
    TX_PROCESSED_DATE_TIME TIMESTAMP(6),
    PEN_TWIN1              VARCHAR(10),
    PEN_TWIN2              VARCHAR(10),
    TWIN_REASON            VARCHAR(2),
    RUN_DATE               VARCHAR(8),
    TWIN_USER_ID           VARCHAR(15)
);

--------------------------------------------------------
--  DDL for Index PEN_DEMOG_TX_PK
--------------------------------------------------------
CREATE UNIQUE INDEX API_PEN_REPLICATION.PEN_DEMOG_TX_PK ON API_PEN_REPLICATION.PEN_DEMOG_TX (TX_ID);

--------------------------------------------------------
--  DDL for Index PEN_TWINS_TX_PK
--------------------------------------------------------
CREATE UNIQUE INDEX API_PEN_REPLICATION.PEN_TWINS_TX_PK ON API_PEN_REPLICATION.PEN_TWINS_TX (TX_ID);

--------------------------------------------------------
--  DDL for table privileges
--------------------------------------------------------
GRANT SELECT ON API_PEN_REPLICATION.PEN_DEMOG_TX TO RDB_TX_TO_PEN;
GRANT SELECT ON API_PEN_REPLICATION.PEN_TWINS_TX TO RDB_TX_TO_PEN;
