package no.nav.tiltakspenger.vedtak.repository.aktivitetslogg

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.IAktivitetsloggVisitor
import no.nav.tiltakspenger.vedtak.Kontekst
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime
import java.util.*

class AktivitetsloggDAO {
    private val toAktivitet: (Row) -> Aktivitetslogg.Aktivitet = { row ->
        Aktivitetslogg.Aktivitet.Info(
            kontekster = listOf(),
            melding = row.string("melding"),
            tidsstempel = row.localDateTime("tidsstempel")
        )
    }

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

    @Language("SQL")
    private val hentAktivitetslogg = "select * from aktivitet where søker_id = ?"

    fun lagre(søkerId: UUID, aktivitetslogg: Aktivitetslogg, txSession: TransactionalSession) {
        val vis = AktivitetsloggVisitor()
        aktivitetslogg.accept(vis)
        vis.aktiviteter.forEach { aktivitet ->

            txSession.run(
                queryOf(
                    lagreAktivitetslogg, mapOf(
                        "id" to UUID.randomUUID(),
                        "sokerId" to søkerId,
                        "alvorlighetsgrad" to 1,
                        "label" to "I",
                        "melding" to aktivitet.melding,
                        "tidsstempel" to aktivitet.tidsstempel,
                    )
                ).asUpdate
            )
        }
    }

    fun hent(søkerId: UUID, txSession: TransactionalSession) = txSession.run(
        queryOf(hentAktivitetslogg, søkerId).map(toAktivitet).asSingle
    )

}

class AktivitetDTO(
    val kontekster: List<Kontekst>,
    val aktivitet: Aktivitetslogg.Aktivitet,
//    val alvorlighetsgrad: Int,
    val melding: String,
    val tidsstempel: LocalDateTime,
)

class AktivitetsloggVisitor : IAktivitetsloggVisitor {
    val aktiviteter = mutableListOf<AktivitetDTO>()

    override fun visitInfo(
        kontekster: List<Kontekst>,
        aktivitet: Aktivitetslogg.Aktivitet.Info,
//        alvorlighetsgrad: Int,
        melding: String,
        tidsstempel: LocalDateTime
    ) {
        aktiviteter.add(
            AktivitetDTO(
                kontekster = kontekster,
                aktivitet = aktivitet,
//                alvorlighetsgrad = alvorlighetsgrad,
                melding = melding,
                tidsstempel = tidsstempel,
            )
        )
    }
}
