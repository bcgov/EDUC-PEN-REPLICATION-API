ALTER TABLE PEN_REPLICATION_EVENT
MODIFY (
   CREATE_USER VARCHAR2(100),
   UPDATE_USER VARCHAR2(100)
);

ALTER TABLE PEN_REPLICATION_SAGA
MODIFY (
   CREATE_USER VARCHAR2(100),
   UPDATE_USER VARCHAR2(100)
);

ALTER TABLE PEN_REPLICATION_SAGA_EVENT_STATES
MODIFY (
   CREATE_USER VARCHAR2(100),
   UPDATE_USER VARCHAR2(100)
);
