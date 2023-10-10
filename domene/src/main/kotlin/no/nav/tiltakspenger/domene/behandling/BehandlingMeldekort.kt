package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import java.time.LocalDate

data class MeldekortDTO(
    val fom: LocalDate,
    val tom: LocalDate,
    val dagerMedOppmøte: List<LocalDate>,
)

sealed interface BehandlingMeldekort : Behandling {
    data class Opprettet(
        override val id: BehandlingId,
        override val sakId: SakId,
        override val vurderingsperiode: Periode,
        override val saksopplysninger: List<Saksopplysning>,

    ) : BehandlingMeldekort {

        companion object {
            fun opprettBehandling(sakId: SakId, periode: Periode, saksopplysninger: List<Saksopplysning>): Opprettet {
                return Opprettet(
                    id = BehandlingId.random(),
                    sakId = sakId,
                    vurderingsperiode = periode,
                    saksopplysninger = saksopplysninger,
                )
            }
        }

        fun beregn(meldekortDTO: MeldekortDTO): Beregnet {
            // todo gjør en vilkårsvurdering/beregning
            // og bestem

            return Beregnet(
                id = id,
                sakId = sakId,
                vurderingsperiode = Periode(fra = meldekortDTO.fom, til = meldekortDTO.tom),
                saksopplysninger = saksopplysninger,
                meldekort = meldekortDTO,
            )
        }
    }

    data class Beregnet(
        override val id: BehandlingId,
        override val sakId: SakId,
        override val vurderingsperiode: Periode,
        override val saksopplysninger: List<Saksopplysning>,
        val meldekort: MeldekortDTO,

    ) : BehandlingMeldekort {
        fun iverksett(): Iverksatt {
            return Iverksatt(
                id = id,
                sakId = sakId,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
                meldekort = meldekort,
            )
        }
    }

    data class Iverksatt(
        override val id: BehandlingId,
        override val sakId: SakId,
        override val vurderingsperiode: Periode,
        override val saksopplysninger: List<Saksopplysning>,
        val meldekort: MeldekortDTO,
    ) : BehandlingMeldekort
}
