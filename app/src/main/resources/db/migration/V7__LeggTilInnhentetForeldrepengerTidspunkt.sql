ALTER TABLE foreldrepenger_vedtak
    ADD COLUMN IF NOT EXISTS
        innhentet TIMESTAMP WITH TIME ZONE NULL
            DEFAULT null;
