--------------------------------------------------------
--  DDL for Table PEN_DEMOG_TX
--------------------------------------------------------
CREATE TABLE API_PEN_REPLICATION.PEN_DEMOG_TX
(
    TX_ID                  VARCHAR2(10),
    TX_TYPE                VARCHAR2(4),
    TX_STATUS              VARCHAR2(4),
    TX_INSERT_DATE_TIME    TIMESTAMP(6),
    TX_PROCESSED_DATE_TIME TIMESTAMP(6),
    STUD_NO                VARCHAR2(10),
    STUD_SURNAME           VARCHAR2(25),
    STUD_GIVEN             VARCHAR2(25),
    STUD_MIDDLE            VARCHAR2(25),
    USUAL_SURNAME          VARCHAR2(25),
    USUAL_GIVEN            VARCHAR2(25),
    USUAL_MIDDLE           VARCHAR2(25),
    STUD_BIRTH             VARCHAR2(8),
    STUD_SEX               VARCHAR2(1),
    STUD_DEMOG_CODE        VARCHAR2(1),
    STUD_STATUS            VARCHAR2(1),
    PEN_LOCAL_ID           VARCHAR2(12),
    PEN_MINCODE            VARCHAR2(8),
    POSTAL                 VARCHAR2(7),
    STUD_TRUE_NO           VARCHAR2(10),
    MERGE_TO_USER_NAME     VARCHAR2(15),
    MERGE_TO_CODE          VARCHAR2(2),
    CREATE_DATE            DATE,
    CREATE_USER_NAME       VARCHAR2(15),
    UPDATE_DATE            DATE,
    UPDATE_USER_NAME       VARCHAR2(15),
    STUD_GRADE             VARCHAR2(2),
    STUD_GRADE_YEAR        NUMBER(4, 0),
    UPDATE_DEMOG_DATE      DATE,
    MERGE_TO_DATE          DATE
);

--------------------------------------------------------
--  DDL for Table PEN_DEMOG_TX
--------------------------------------------------------
CREATE TABLE API_PEN_REPLICATION.PEN_TWINS_TX
(
    TX_ID                  VARCHAR2(10),
    TX_TYPE                VARCHAR2(4),
    TX_STATUS              VARCHAR2(4),
    TX_INSERT_DATE_TIME    TIMESTAMP(6),
    TX_PROCESSED_DATE_TIME TIMESTAMP(6),
    PEN_TWIN1              VARCHAR2(10),
    PEN_TWIN2              VARCHAR2(10),
    TWIN_REASON            VARCHAR2(2),
    RUN_DATE               VARCHAR2(8),
    TWIN_USER_ID           VARCHAR2(15)
);

--------------------------------------------------------
--  DDL for Index PEN_DEMOG_TX_PK
--------------------------------------------------------
CREATE UNIQUE INDEX API_PEN_REPLICATION.PEN_DEMOG_TX_PK ON API_PEN_REPLICATION.PEN_DEMOG_TX (TX_ID) TABLESPACE API_PEN_IDX;

--------------------------------------------------------
--  DDL for Index PEN_TWINS_TX_PK
--------------------------------------------------------
CREATE UNIQUE INDEX API_PEN_REPLICATION.PEN_TWINS_TX_PK ON API_PEN_REPLICATION.PEN_TWINS_TX (TX_ID) TABLESPACE API_PEN_IDX;

--------------------------------------------------------
--  DDL for table privileges
--------------------------------------------------------
GRANT SELECT ON API_PEN_REPLICATION.PEN_DEMOG_TX TO RDB_TX_TO_PEN;
GRANT SELECT ON API_PEN_REPLICATION.PEN_TWINS_TX TO RDB_TX_TO_PEN;

CREATE INDEX PEN_DEMOG_TX_STATUS_IDX ON PEN_DEMOG_TX (TX_STATUS) TABLESPACE API_PEN_IDX;
CREATE INDEX PEN_DEMOG_TX_STUD_NO_IDX ON PEN_DEMOG_TX (STUD_NO) TABLESPACE API_PEN_IDX;

CREATE INDEX PEN_TWINS_TX_PEN_TWIN1_IDX ON PEN_TWINS_TX (PEN_TWIN1) TABLESPACE API_PEN_IDX;
CREATE INDEX PEN_TWINS_TX_PEN_TWIN2_IDX ON PEN_TWINS_TX (PEN_TWIN2) TABLESPACE API_PEN_IDX;
CREATE INDEX PEN_TWINS_TX_STATUS_IDX ON PEN_TWINS_TX (TX_STATUS) TABLESPACE API_PEN_IDX;
