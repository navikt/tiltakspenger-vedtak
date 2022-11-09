package no.nav.tiltakspenger.vedtak.repository.behandling

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.vilkårsvurdering.Inngangsvilkårsvurderinger
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.VurderingType
import org.intellij.lang.annotations.Language

class VurderingDAO {

    fun hentForBehandling(
        behandlingId: BehandlingId,
        txSession: TransactionalSession
    ): List<Vurdering> {
        return txSession.run(
            queryOf(hentVurderinger, behandlingId.toString())
                .map { row -> row.toVurdering() }
                .asList
        )
    }

    fun lagre(
        behandlingId: BehandlingId,
        vilkårsvurderinger: Inngangsvilkårsvurderinger,
        txSession: TransactionalSession
    ) {
        slettVurderinger(behandlingId, txSession)
        vilkårsvurderinger.vurderinger().forEach { vurdering ->
            lagreVurdering(behandlingId, vurdering, txSession)
        }
    }

    private fun lagreVurdering(
        behandlingId: BehandlingId,
        vurdering: Vurdering,
        txSession: TransactionalSession
    ) {
        txSession.run(
            queryOf(
                lagreVurdering, mapOf(
                    "behandlingId" to behandlingId.toString(),
                    "vilkar" to vurdering.vilkår.tittel,
                    "vurderingType" to vurdering.vurderingType.name,
                    "kilde" to vurdering.kilde,
                    "fom" to vurdering.fom,
                    "tom" to vurdering.tom,
                    "utfall" to vurdering.utfall.name,
                    "detaljer" to vurdering.detaljer,
                    "tidsstempel" to vurdering.tidspunkt,
                )
            ).asUpdate
        )
    }

    private fun slettVurderinger(behandlingId: BehandlingId, txSession: TransactionalSession) {
        txSession.run(queryOf(slettVurdering, behandlingId.toString()).asUpdate)
    }

    private fun Row.toVurdering(): Vurdering {
//        val id = BehandlingId.fromDb(string("id"))
        val vilkår = when (string("vilkår")) {
            "AAP" -> Vilkår.AAP
            "DAGPENGER" -> Vilkår.DAGPENGER
            "SYKEPENGER" -> Vilkår.SYKEPENGER
            "UFØRETRYGD" -> Vilkår.UFØRETRYGD
            "OVERGANGSSTØNAD" -> Vilkår.OVERGANGSSTØNAD
            "PLEIEPENGER" -> Vilkår.PLEIEPENGER
            "FORELDREPENGER" -> Vilkår.FORELDREPENGER
            "SVANGERSKAPSPENGER" -> Vilkår.SVANGERSKAPSPENGER
            "GJENLEVENDEPENSJON" -> Vilkår.GJENLEVENDEPENSJON
            "SUPPLERENDESTØNAD" -> Vilkår.SUPPLERENDESTØNAD
            "ALDERSPENSJON" -> Vilkår.ALDERSPENSJON
            "OPPLÆRINGSPENGER" -> Vilkår.OPPLÆRINGSPENGER
            "OMSORGSPENGER" -> Vilkår.OMSORGSPENGER
            "INTROPROGRAMMET" -> Vilkår.INTROPROGRAMMET
            "KVP" -> Vilkår.KVP
            "KOMMUNALE YTELSER" -> Vilkår.KOMMUNALEYTELSER
            "STATLIGE YTELSER" -> Vilkår.STATLIGEYTELSER
            "INSTITUSJONSOPPHOLD" -> Vilkår.INSTITUSJONSOPPHOLD
            "PENSJONSINNTEKT" -> Vilkår.PENSJONSINNTEKT
            "LØNNSINNTEKT" -> Vilkår.LØNNSINNTEKT
            else -> throw IllegalStateException("skjønte ikke vilkårtype")
        }
        val vurderingType = string("vurderingtype").let { VurderingType.valueOf(it) }
        val kilde = string("kilde")
        val fom = localDateOrNull("fom")
        val tom = localDateOrNull("tom")
        val utfall = string("utfall").let { Utfall.valueOf(it) }
        val detaljer = string("detaljer")
        val tidspunkt = localDateTime("tidsstempel")

        return Vurdering(
            vilkår = vilkår,
            vurderingType = vurderingType,
            kilde = kilde,
            fom = fom,
            tom = tom,
            utfall = utfall,
            detaljer = detaljer,
            tidspunkt = tidspunkt,
        )
    }

    @Language("SQL")
    private val lagreVurdering = """
        insert into vurdering (
            behandling_id,
            vilkår,
            vurderingtype,
            kilde,
            fom,
            tom,
            utfall,
            detaljer,
            tidsstempel
        ) values (
            :behandlingId,
            :vilkar,
            :vurderingType,
            :kilde,
            :fom,
            :tom,
            :utfall,
            :detaljer,
            :tidsstempel
        )""".trimIndent()

    @Language("SQL")
    private val slettVurdering = "delete from vurdering where behandling_id = ?"

    @Language("SQL")
    private val hentVurderinger = "select * from vurdering where behandling_id = ?"

    companion object {
        private const val ULID_PREFIX_VURDERING = "vurdering"
    }
}
