DO
$$
    BEGIN
        IF
            EXISTS
                (SELECT 1 from pg_roles where rolname = 'tpts_datastream')
        THEN
            ALTER DEFAULT PRIVILEGES IN SCHEMA PUBLIC GRANT SELECT ON TABLES TO "tpts_datastream";
            GRANT SELECT ON ALL TABLES IN SCHEMA PUBLIC TO "tpts_datastream";

            ALTER USER "tiltakspenger-vedtak" WITH REPLICATION;
            ALTER USER "tpts_datastream" WITH REPLICATION;
            select pg_create_logical_replication_slot('tpts_datastream_slot', 'test_slot');

            -- CREATE PUBLICATION "ds_publication" FOR ALL TABLES; -- Denne er kommentert ut fordi flyway kjører migrering hver gang V1 endres, og da får vi feil på at 'ds_publication' finnes fra før på deploy.
        END IF;
    END
$$;
