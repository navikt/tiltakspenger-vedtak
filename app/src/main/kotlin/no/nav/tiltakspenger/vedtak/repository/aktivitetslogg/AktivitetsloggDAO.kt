package no.nav.tiltakspenger.vedtak.repository.aktivitetslogg

import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import org.intellij.lang.annotations.Language
import java.util.*

class AktivitetsloggDAO {
    @Language("SQL")
    private val lagreAktivitetslogg = """
        insert into aktivitet (
        id, 
        søker_id, 
        alvorlighetsgrad, 
        label, 
        melding, 
        tidsstempel
        ) values (
        :id, 
        :sokerId, 
        :alvorlighetsgrad, 
        :label, 
        :melding, 
        :tidsstempel
        )
    """.trimIndent()

    fun lagre(søkerId: UUID, aktivitetslogg: Aktivitetslogg, txSession: TransactionalSession) {
        aktivitetslogg.getAktiviteter().forEach { aktivitet ->
            aktivitet.alleKonteksterAsMap()
        txSession.run(
            queryOf(
                lagreAktivitetslogg, mapOf(
                    "foo" to "bar"
                )
            ).asUpdate
        )
        }
    }

    fun hent(søkerId: UUID, txSession: TransactionalSession): Aktivitetslogg {
        return Aktivitetslogg()
    }
}
