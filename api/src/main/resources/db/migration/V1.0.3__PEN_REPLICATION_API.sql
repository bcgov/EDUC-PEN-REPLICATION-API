ALTER TABLE API_PEN_REPLICATION.PEN_DEMOG_TX
    MODIFY (
        STUD_SURNAME NOT NULL,
        STUD_BIRTH NOT NULL,
        STUD_SEX NOT NULL,
        STUD_DEMOG_CODE NOT NULL,
        STUD_STATUS NOT NULL,
        TX_INSERT_DATE_TIME NOT NULL,
        TX_TYPE NOT NULL,
        TX_STATUS NOT NULL,
        TX_ID NOT NULL
        );
ALTER TABLE API_PEN_REPLICATION.PEN_TWINS_TX
    MODIFY (
        TX_INSERT_DATE_TIME NOT NULL,
        TX_TYPE NOT NULL,
        TX_STATUS NOT NULL,
        TX_ID NOT NULL,
        PEN_TWIN1 NOT NULL,
        PEN_TWIN2 NOT NULL,
        TWIN_REASON NOT NULL
        );
