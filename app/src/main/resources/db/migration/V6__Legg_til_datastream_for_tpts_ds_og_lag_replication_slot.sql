DO
$$
BEGIN
    IF EXISTS(SELECT * FROM pg_roles WHERE rolname = 'tpts_ds') THEN
        ALTER DEFAULT PRIVILEGES IN SCHEMA PUBLIC GRANT SELECT ON TABLES TO "tpts_ds";
        GRANT SELECT ON ALL TABLES IN SCHEMA PUBLIC TO "tpts_ds";
    END IF;
END
$$ LANGUAGE 'plpgsql';

DO
$$
BEGIN
    IF EXISTS(SELECT * FROM pg_roles WHERE rolname = 'tpts_ds') THEN
        PERFORM PG_CREATE_LOGICAL_REPLICATION_SLOT('ds_replication', 'pgoutput');
    END IF;
END
$$ LANGUAGE 'plpgsql';