ALTER TABLE personopplysninger_barn_med_ident
    ADD COLUMN IF NOT EXISTS
        skjermet    BOOLEAN NULL
            DEFAULT false;
