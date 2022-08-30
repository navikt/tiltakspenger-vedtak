package no.nav.tiltakspenger.vedtak.db

import kotliquery.Row
import kotliquery.Session
import kotliquery.queryOf

internal fun <T> String.hent(
    params: Map<String, Any> = emptyMap(),
    session: Session,
    rowMapping: (Row) -> T,
): T? {
    return session.run(queryOf(this, params).map { row -> rowMapping(row) }.asSingle)
}
