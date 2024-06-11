package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDager
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.Tiltak
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.LivsoppholdYtelseSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.YtelseSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkårdata.VilkårData

data class LeggTilSaksopplysningRespons(
    val behandling: Behandling,
    val erEndret: Boolean,
)

interface Behandling {
    val id: BehandlingId
    val sakId: SakId
    val vurderingsperiode: Periode
    val søknader: List<Søknad>
    val saksbehandler: String?
    val beslutter: String?
    val vilkårData: VilkårData
    val utfallsperioder: Periodisering<Utfallsdetaljer>
    val status: BehandlingStatus
    val tilstand: BehandlingTilstand

    fun leggTilSøknad(søknad: Søknad): Behandling
    fun leggTilSaksopplysning(ytelseSaksopplysning: YtelseSaksopplysning): LeggTilSaksopplysningRespons
    fun leggTilSaksopplysning(livsoppholdYtelseSaksopplysning: LivsoppholdYtelseSaksopplysning): LeggTilSaksopplysningRespons
    fun oppdaterTiltak(tiltak: List<Tiltak>): Behandling
    fun startBehandling(saksbehandler: Saksbehandler): Behandling
    fun avbrytBehandling(saksbehandler: Saksbehandler): Behandling
    fun tilBeslutting(saksbehandler: Saksbehandler): Behandling
    fun iverksett(utøvendeBeslutter: Saksbehandler): Behandling
    fun sendTilbake(utøvendeBeslutter: Saksbehandler): Behandling
    fun vilkårsvurder(): Behandling
    fun søknad(): Søknad
    fun oppdaterAntallDager(
        tiltakId: String,
        nyPeriodeMedAntallDager: PeriodeMedVerdi<AntallDager>,
        saksbehandler: Saksbehandler,
    ): Behandling
}
