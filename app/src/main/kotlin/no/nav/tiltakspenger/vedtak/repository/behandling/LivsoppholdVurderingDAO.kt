package no.nav.tiltakspenger.vedtak.repository.behandling

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.VurderingId
import no.nav.tiltakspenger.felles.nå
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import org.intellij.lang.annotations.Language

internal class LivsoppholdVurderingDAO {
    fun hent(behandlingId: BehandlingId, txSession: TransactionalSession): List<DenormalisertLivsoppholdVurdering> {
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

    fun lagre(
        behandlingId: BehandlingId,
        vurderinger: List<DenormalisertLivsoppholdVurdering>,
        txSession: TransactionalSession,
    ) {
        slett(behandlingId, txSession)
        vurderinger.forEach { vurdering ->
            lagre(
                behandlingId = behandlingId,
                vurdering = vurdering,
                txSession = txSession,
            )
        }
    }

    fun lagre(
        behandlingId: BehandlingId,
        vurdering: DenormalisertLivsoppholdVurdering,
        txSession: TransactionalSession,
    ) {
        txSession.run(
            queryOf(
                sqlLagreVurdering,
                mapOf(
                    "id" to VurderingId.random().toString(),
                    "behandlingId" to behandlingId?.toString(),
                    // "vedtakId" to vedtakId?.toString(),
                    "fom" to vurdering.periodeFra,
                    "tom" to vurdering.periodeTil,
                    // "kilde" to vurdering.kilde.name,
                    "vilkar" to vurdering.vilkår.tittel, // her burde vi kanskje lage en when over vilkår i stedet for å bruke tittel?
                    "detaljer" to vurdering.detaljer,
                    "utfall" to vurdering.periodeVerdi.name,
                    "opprettet" to nå(),
                    // "grunnlagId" to vurdering.grunnlagId,
                ),
            ).asUpdate,
        )
    }

    fun slett(behandlingId: BehandlingId, txSession: TransactionalSession) {
        txSession.run(
            queryOf(
                sqlSlettForBehandling,
                mapOf("behandlingId" to behandlingId.toString()),
            ).asUpdate,
        )
    }

    private fun Row.toVurdering(): DenormalisertLivsoppholdVurdering {
        val vilkår = hentVilkår(string("vilkår"))
        val detaljer = string("detaljer")
        val utfallStr = string("utfall")
        val utfall = when (utfallStr) {
            "OPPFYLT" -> Utfall.OPPFYLT
            "IKKE_OPPFYLT" -> Utfall.IKKE_OPPFYLT
            "KREVER_MANUELL_VURDERING" -> Utfall.KREVER_MANUELL_VURDERING
            else -> {
                throw IllegalStateException("Vurdering med ukjent utfall $utfallStr")
            }
        }
        return DenormalisertLivsoppholdVurdering(
            vilkår = vilkår,
            detaljer = detaljer,
            periodeFra = localDate("fom"),
            periodeTil = localDate("tom"),
            periodeVerdi = utfall,
        )
    }
}

@Language("SQL")
private val sqlLagreVurdering = """
        insert into livsoppholdvurdering (
            id,
            behandlingId,
            fom,
            tom,
            vilkår,
            detaljer,
            utfall,
            opprettet,
        ) values (
            :id,
            :behandlingId,
            :fom,
            :tom,
            :vilkar,
            :detaljer,
            :utfall,
            :opprettet,
        )
""".trimIndent()

@Language("SQL")
private val sqlSlettForBehandling = """
        delete from livsoppholdvurdering where behandlingId = :behandlingId
""".trimIndent()

@Language("SQL")
private val sqlHentForBehandling = """
        select * from livsoppholdvurdering where behandlingId = :behandlingId
""".trimIndent()

private fun hentVilkår(vilkår: String) =
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
        "UFØRETRYGD" -> Vilkår.UFØRETRYGD
        "ETTERLØNN" -> Vilkår.ETTERLØNN
        "TILTAKSDELTAGELSE" -> Vilkår.TILTAKSDELTAGELSE
        else -> {
            throw IllegalStateException("Vurdering med ukjent vilkår $vilkår")
        }
    }
