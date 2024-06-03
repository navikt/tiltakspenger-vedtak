DO
$$
    BEGIN
        IF EXISTS
            (SELECT * FROM pg_roles WHERE rolname = 'tpts_datastream')
        THEN
            PERFORM PG_CREATE_LOGICAL_REPLICATION_SLOT('tpts_replication', 'pgoutput');
        END IF;
    END
$$;
