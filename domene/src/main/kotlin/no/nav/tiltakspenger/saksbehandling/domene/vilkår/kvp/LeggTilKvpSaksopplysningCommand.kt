package no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.Deltagelse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring

data class LeggTilKvpSaksopplysningCommand(
    val behandlingId: BehandlingId,
    val saksbehandler: Saksbehandler,
    val deltakelseForPeriode: List<DeltakelseForPeriode>,
    val årsakTilEndring: ÅrsakTilEndring,
) {
    data class DeltakelseForPeriode(
        val periode: Periode,
        val deltar: Boolean,
    ) {
        fun tilDeltagelse(): Deltagelse {
            return if (deltar) Deltagelse.DELTAR else Deltagelse.DELTAR_IKKE
        }
    }
}
