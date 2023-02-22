CREATE PUBLICATION tiltakspenger_publication FOR ALL TABLES;

SELECT PG_CREATE_LOGICAL_REPLICATION_SLOT
           ('tiltakspenger_replication_slot', 'pgoutput');