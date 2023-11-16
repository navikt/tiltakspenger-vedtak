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
        override val tiltak: List<Tiltak>,
        override val saksbehandler: String?,

    ) : BehandlingMeldekort {

        companion object {
            fun opprettBehandling(
                sakId: SakId,
                periode: Periode,
                saksopplysninger: List<Saksopplysning>,
                tiltak: List<Tiltak>,
                saksbehandler: String?,
            ): Opprettet {
                return Opprettet(
                    id = BehandlingId.random(),
                    sakId = sakId,
                    vurderingsperiode = periode,
                    saksopplysninger = saksopplysninger,
                    tiltak = tiltak,
                    saksbehandler = saksbehandler,
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
                tiltak = tiltak,
                saksbehandler = saksbehandler,
                meldekort = meldekortDTO,
            )
        }
    }

    data class Beregnet(
        override val id: BehandlingId,
        override val sakId: SakId,
        override val vurderingsperiode: Periode,
        override val saksopplysninger: List<Saksopplysning>,
        override val tiltak: List<Tiltak>,
        override val saksbehandler: String?,
        val meldekort: MeldekortDTO,

    ) : BehandlingMeldekort {
        fun iverksett(): Iverksatt {
            return Iverksatt(
                id = id,
                sakId = sakId,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
                tiltak = tiltak,
                saksbehandler = saksbehandler,
                meldekort = meldekort,
            )
        }
    }

    data class Iverksatt(
        override val id: BehandlingId,
        override val sakId: SakId,
        override val vurderingsperiode: Periode,
        override val saksopplysninger: List<Saksopplysning>,
        override val tiltak: List<Tiltak>,
        override val saksbehandler: String?,
        val meldekort: MeldekortDTO,
    ) : BehandlingMeldekort
}
