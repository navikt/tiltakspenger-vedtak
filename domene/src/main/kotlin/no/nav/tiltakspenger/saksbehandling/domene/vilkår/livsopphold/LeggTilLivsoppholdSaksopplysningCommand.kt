package no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.periodisering.Periode

data class LeggTilLivsoppholdSaksopplysningCommand(
    val behandlingId: BehandlingId,
    val saksbehandler: Saksbehandler,
    val ytelseForPeriode: List<YtelseForPeriode>,
    val årsakTilEndring: AarsakTilEndring,
    val livsoppholdsytelse: Livsoppholdsytelse,
) {
    data class YtelseForPeriode(
        val periode: Periode,
        val harYtelse: Boolean,
    ) {
        fun tilYtelse(): HarYtelse {
            return if (harYtelse) HarYtelse.HAR_YTELSE else HarYtelse.HAR_IKKE_YTELSE
        }
    }
}
