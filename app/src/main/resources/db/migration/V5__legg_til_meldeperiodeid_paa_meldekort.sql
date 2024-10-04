alter table meldekort add column if not exists meldeperiode_id varchar;
UPDATE meldekort SET meldeperiode_id =  to_char(fraOgMed, 'YYYY-MM-DD') || '/' || to_char(tilOgMed, 'YYYY-MM-DD') where true;
alter table meldekort alter column meldeperiode_id set not null;