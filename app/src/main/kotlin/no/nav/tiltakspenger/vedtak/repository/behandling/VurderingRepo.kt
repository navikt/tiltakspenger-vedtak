package no.nav.tiltakspenger.vedtak.repository.behandling

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.VedtakId
import no.nav.tiltakspenger.felles.VurderingId
import no.nav.tiltakspenger.felles.nå
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import org.intellij.lang.annotations.Language

internal class VurderingRepo {
    fun hent(behandlingId: BehandlingId, txSession: TransactionalSession): List<Vurdering> {
        return txSession.run(
            queryOf(
                sqlHentForBehandling,
                mapOf(
                    "behandlingId" to behandlingId.toString(),
                ),
            ).map { row ->
                row.toVurdering()
            }.asList,
        )
    }

    fun hent(vedtakId: VedtakId, txSession: TransactionalSession): List<Vurdering> {
        return txSession.run(
            queryOf(
                sqlHentForVedtak,
                mapOf(
                    "vedtakId" to vedtakId.toString(),
                ),
            ).map { row ->
                row.toVurdering()
            }.asList,
        )
    }

    fun lagre(vedtakId: VedtakId, vurderinger: List<Vurdering>, txSession: TransactionalSession) {
        slett(vedtakId, txSession)
        vurderinger.forEach { vurdering ->
            lagre(
                behandlingId = null,
                vedtakId = vedtakId,
                vurdering = vurdering,
                txSession = txSession,
            )
        }
    }

    fun lagre(behandlingId: BehandlingId, vurderinger: List<Vurdering>, txSession: TransactionalSession) {
        slett(behandlingId, txSession)
        vurderinger.forEach { vurdering ->
            lagre(
                behandlingId = behandlingId,
                vedtakId = null,
                vurdering = vurdering,
                txSession = txSession,
            )
        }
    }

    private fun lagre(
        behandlingId: BehandlingId?,
        vedtakId: VedtakId?,
        vurdering: Vurdering,
        txSession: TransactionalSession,
    ) {
        txSession.run(
            queryOf(
                sqlLagreVurdering,
                mapOf(
                    "id" to VurderingId.random().toString(),
                    "behandlingId" to behandlingId?.toString(),
                    "vedtakId" to vedtakId?.toString(),
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
                sqlSlettForBehandling,
                mapOf("behandlingId" to behandlingId.toString()),
            ).asUpdate,
        )
    }

    private fun slett(vedtakId: VedtakId, txSession: TransactionalSession) {
        txSession.run(
            queryOf(
                sqlSlettForVedtak,
                mapOf("vedtakId" to vedtakId.toString()),
            ).asUpdate,
        )
    }

    private fun Row.toVurdering(): Vurdering {
        val vilkår = hentVilkår(string("vilkår"))
        val kilde = Kilde.valueOf(string("kilde"))
        val detaljer = string("detaljer")
        val utfall = when (val utfallString = string("utfall")) {
            "OPPFYLT" -> Utfall.OPPFYLT
            "IKKE_OPPFYLT" -> Utfall.IKKE_OPPFYLT
            "KREVER_MANUELL_VURDERING" -> Utfall.KREVER_MANUELL_VURDERING
            else -> {
                throw IllegalStateException("Vurdering med ukjent utfall $utfallString")
            }
        }
        return Vurdering(
            vilkår = vilkår,
            kilde = kilde,
            detaljer = detaljer,
            fom = localDate("fom"),
            tom = localDate("tom"),
            utfall = utfall,
        )
    }

    @Language("SQL")
    private val sqlLagreVurdering = """
        insert into vurdering (
            id,
            behandlingId,
            vedtakId,
            fom,
            tom,
            kilde,
            vilkår,
            detaljer,
            utfall,
            opprettet
        ) values (
            :id,
            :behandlingId,
            :vedtakId,
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
    private val sqlSlettForBehandling = """
        delete from vurdering where behandlingId = :behandlingId
    """.trimIndent()

    @Language("SQL")
    private val sqlSlettForVedtak = """
        delete from vurdering where vedtakid = :vedtakId
    """.trimIndent()

    @Language("SQL")
    private val sqlHentForBehandling = """
        select * from vurdering where behandlingId = :behandlingId
    """.trimIndent()

    @Language("SQL")
    private val sqlHentForVedtak = """
        select * from vurdering where vedtakId = :vedtakId
    """.trimIndent()
}

fun hentVilkår(vilkår: String) =
    when (vilkår) {
        "AAP" -> Vilkår.AAP
        "ALDER" -> Vilkår.ALDER
        "ALDERSPENSJON" -> Vilkår.ALDERSPENSJON
        "DAGPENGER" -> Vilkår.DAGPENGER
        "FORELDREPENGER" -> Vilkår.FORELDREPENGER
        "GJENLEVENDEPENSJON" -> Vilkår.GJENLEVENDEPENSJON
        "INSTITUSJONSOPPHOLD" -> Vilkår.INSTITUSJONSOPPHOLD
        "INTROPROGRAMMET" -> Vilkår.INTROPROGRAMMET
        "JOBBSJANSEN" -> Vilkår.JOBBSJANSEN
        "KVP" -> Vilkår.KVP
        "LØNNSINNTEKT" -> Vilkår.LØNNSINNTEKT
        "OMSORGSPENGER" -> Vilkår.OMSORGSPENGER
        "OPPLÆRINGSPENGER" -> Vilkår.OPPLÆRINGSPENGER
        "OVERGANGSSTØNAD" -> Vilkår.OVERGANGSSTØNAD
        "PENSJONSINNTEKT" -> Vilkår.PENSJONSINNTEKT
        "PLEIEPENGER_NÆRSTÅENDE" -> Vilkår.PLEIEPENGER_NÆRSTÅENDE
        "PLEIEPENGER_SYKT_BARN" -> Vilkår.PLEIEPENGER_SYKT_BARN
        "SUPPLERENDESTØNADALDER" -> Vilkår.SUPPLERENDESTØNADALDER
        "SUPPLERENDESTØNADFLYKTNING" -> Vilkår.SUPPLERENDESTØNADFLYKTNING
        "SVANGERSKAPSPENGER" -> Vilkår.SVANGERSKAPSPENGER
        "SYKEPENGER" -> Vilkår.SYKEPENGER
        "TILTAKSPENGER" -> Vilkår.TILTAKSPENGER
        "UFØRETRYGD" -> Vilkår.UFØRETRYGD
        "ETTERLØNN" -> Vilkår.ETTERLØNN
        else -> {
            throw IllegalStateException("Vurdering med ukjent vilkår $vilkår")
        }
    }
