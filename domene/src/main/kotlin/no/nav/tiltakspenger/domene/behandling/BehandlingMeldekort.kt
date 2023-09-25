package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import java.time.LocalDate

data class MeldekortDTO(
    val fom: LocalDate,
    val tom: LocalDate,
    val dagerMedOppmøte: List<LocalDate>,
)

sealed interface BehandlingMeldekort : Behandling {
    data class Opprettet(
        override val id: BehandlingId,
        override val vurderingsperiode: Periode,
        override val saksopplysninger: List<Saksopplysning>,

    ) : BehandlingMeldekort {

        companion object {
            fun opprettBehandling(periode: Periode, saksopplysninger: List<Saksopplysning>): Opprettet {
                return Opprettet(
                    id = BehandlingId.random(),
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
                vurderingsperiode = Periode(fra = meldekortDTO.fom, til = meldekortDTO.tom),
                saksopplysninger = saksopplysninger,
                meldekort = meldekortDTO,
            )
        }
    }

    data class Beregnet(
        override val id: BehandlingId,
        override val vurderingsperiode: Periode,
        override val saksopplysninger: List<Saksopplysning>,
        val meldekort: MeldekortDTO,

    ) : BehandlingMeldekort {
        fun iverksett(): Iverksatt {
            return Iverksatt(
                id = id,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
                meldekort = meldekort,
            )
        }
    }

    data class Iverksatt(
        override val id: BehandlingId,
        override val vurderingsperiode: Periode,
        override val saksopplysninger: List<Saksopplysning>,
        val meldekort: MeldekortDTO,
    ) : BehandlingMeldekort
}
