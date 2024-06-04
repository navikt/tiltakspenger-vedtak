package no.nav.tiltakspenger.vedtak.repository.behandling

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SakspplysningId
import no.nav.tiltakspenger.felles.nå
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.HarYtelse
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import org.intellij.lang.annotations.Language

internal class LivsoppholdSaksopplysningDAO {
    fun hent(
        behandlingId: BehandlingId,
        txSession: TransactionalSession,
    ): List<DenormalisertLivsoppholdSaksopplysning> {
        return txSession.run(
            queryOf(
                sqlHentForBehandling,
                mapOf(
                    "behandlingId" to behandlingId.toString(),
                ),
            ).map { row ->
                row.toSaksopplysning()
            }.asList,
        )
    }

    fun lagre(
        behandlingId: BehandlingId,
        saksopplysninger: List<DenormalisertLivsoppholdSaksopplysning>,
        txSession: TransactionalSession,
    ) {
        slett(behandlingId, txSession)
        saksopplysninger.forEach { saksopplysning ->
            lagre(
                behandlingId = behandlingId,
                livsoppholdSaksopplysning = saksopplysning,
                txSession = txSession,
            )
        }
    }

    fun lagre(
        behandlingId: BehandlingId,
        livsoppholdSaksopplysning: DenormalisertLivsoppholdSaksopplysning,
        txSession: TransactionalSession,
    ) {
        txSession.run(
            queryOf(
                sqlLagreSaksopplysning,
                mapOf(
                    "id" to SakspplysningId.random().toString(),
                    "behandlingId" to behandlingId.toString(),
                    // "vedtakId" to vedtakId?.toString(),
                    "fom" to livsoppholdSaksopplysning.periodeFra,
                    "tom" to livsoppholdSaksopplysning.periodeTil,
                    "kilde" to livsoppholdSaksopplysning.kilde.name,
                    "vilkar" to livsoppholdSaksopplysning.vilkår.tittel, // her burde vi kanskje lage en when over vilkår i stedet for å bruke tittel?
                    "detaljer" to livsoppholdSaksopplysning.detaljer,
                    "typeSaksopplysning" to livsoppholdSaksopplysning.type.name, // TODO: Sjekk type i db-script
                    "harYtelse" to livsoppholdSaksopplysning.periodeVerdi.name, // TODO: Legg til i db-script
                    "saksbehandler" to livsoppholdSaksopplysning.saksbehandler,
                    "opprettet" to nå(),
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

    private fun Row.toSaksopplysning(): DenormalisertLivsoppholdSaksopplysning {
        val vilkår = hentVilkår(string("vilkår"))
        return DenormalisertLivsoppholdSaksopplysning(
            vilkår = vilkår,
            type = DenormalisertLivsoppholdSaksopplysning.DenormalisertSaksopplysningType.valueOf(string("typeSaksopplysning")),
            kilde = Kilde.valueOf(string("kilde")),
            detaljer = string("detaljer"),
            saksbehandler = stringOrNull("saksbehandler"),
            periodeFra = localDate("fom"),
            periodeTil = localDate("tom"),
            periodeVerdi = HarYtelse.valueOf(string("harYtelse")),
        )
    }

    private val sqlHentForBehandling = """
        select * from saksopplysning where behandlingId = :behandlingId
    """.trimIndent()

    private val sqlSlettForBehandling = """
        delete from saksopplysning where behandlingId = :behandlingId
    """.trimIndent()

    @Language("SQL")
    private val sqlLagreSaksopplysning = """
        insert into saksopplysning (
                id,
                behandlingId,
                vedtakId,
                fom,
                tom,
                kilde,
                vilkår,
                detaljer,
                typeSaksopplysning,
                saksbehandler,
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
                :typeSaksopplysning,
                :saksbehandler,
                :opprettet
            )
    """.trimIndent()
}

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
