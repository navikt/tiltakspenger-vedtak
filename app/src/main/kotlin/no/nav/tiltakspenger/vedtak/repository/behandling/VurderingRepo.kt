package no.nav.tiltakspenger.vedtak.repository.behandling

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.nå
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import org.intellij.lang.annotations.Language

internal class VurderingDAO {
    fun hent(behandlingId: BehandlingId, txSession: TransactionalSession): List<Vurdering> {
        return txSession.run(
            queryOf(
                sqlHentVurderinger,
                mapOf(
                    "behandlingId" to behandlingId.toString(),
                ),
            ).map { row ->
                row.toVurdering()
            }.asList,
        )
    }

    fun lagre(behandlingId: BehandlingId, vurderinger: List<Vurdering>, txSession: TransactionalSession) {
        slett(behandlingId, txSession)
        vurderinger.forEach { vurdering ->
            lagre(behandlingId, vurdering, txSession)
        }
    }

    private fun lagre(behandlingId: BehandlingId, vurdering: Vurdering, txSession: TransactionalSession) {
        txSession.run(
            queryOf(
                sqlLagreVurdering,
                mapOf(
                    "behandlingId" to behandlingId.toString(),
                    "fom" to vurdering.fom,
                    "tom" to vurdering.tom,
                    "kilde" to vurdering.kilde.name,
                    "vilkar" to vurdering.vilkår.tittel, // her burde vi kanskje lage en when over vilkår i stedet for å bruke tittel?
                    "detaljer" to vurdering.detaljer,
                    "utfall" to vurdering.utfall.name,
                    "opprettet" to nå(),
                ),
            ).asUpdate,
        )
    }

    private fun slett(behandlingId: BehandlingId, txSession: TransactionalSession) {
        txSession.run(
            queryOf(
                sqlSlettVurderinger,
                mapOf("behandlingId" to behandlingId.toString()),
            ).asUpdate,
        )
    }

    private fun Row.toVurdering(): Vurdering {
        val vilkår = hentVilkår(string("vilkår"))
        val kilde = Kilde.valueOf(string("kilde"))
        val detaljer = string("detaljer")
        return when (val utfall = string("utfall")) {
            "OPPFYLT" -> Vurdering.Oppfylt(
                vilkår = vilkår,
                kilde = kilde,
                detaljer = detaljer,
                fom = localDate("fom"),
                tom = localDate("tom"),
            )

            "IKKE_OPPFYLT" -> Vurdering.IkkeOppfylt(
                vilkår = vilkår,
                kilde = kilde,
                detaljer = detaljer,
                fom = localDate("fom"),
                tom = localDate("tom"),
            )

            "KREVER_MANUELL_VURDERING" -> Vurdering.KreverManuellVurdering(
                vilkår = vilkår,
                kilde = kilde,
                detaljer = detaljer,
                fom = localDate("fom"),
                tom = localDate("tom"),
            )

            else -> {
                throw IllegalStateException("Vurdering med ukjent utfall $utfall")
            }
        }
    }

    @Language("SQL")
    private val sqlLagreVurdering = """
        insert into vurdering (
            behandlingId,
            fom,
            tom,
            kilde,
            vilkår,
            detaljer,
            utfall,
            opprettet
        ) values (
            :behandlingId,
            :fom,
            :tom,
            :kilde,
            :vilkar,
            :detaljer,
            :utfall,
            :opprettet
        )
    """.trimIndent()

    @Language("SQL")
    private val sqlSlettVurderinger = """
        delete from vurdering where behandlingId = :behandlingId
    """.trimIndent()

    @Language("SQL")
    private val sqlHentVurderinger = """
        select * from vurdering where behandlingId = :behandlingId
    """.trimIndent()
}

fun hentVilkår(vilkår: String) =
    when (vilkår) {
        "AAP" -> Vilkår.AAP
        "DAGPENGER" -> Vilkår.DAGPENGER
        "ALDER" -> Vilkår.ALDER
        // TODO her er det maaange flere....
        else -> {
            throw IllegalStateException("Vurdering med ukjent vilkår $vilkår")
        }
    }
