package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.TiltakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.saksbehandling.domene.behandling.kravdato.KravdatoSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDager
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.Tiltak
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.TiltakVilkår
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkårssett
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering

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
    val vilkårssett: Vilkårssett
    val tiltak: TiltakVilkår
    val status: BehandlingStatus
    val tilstand: BehandlingTilstand

    val saksopplysninger: List<Saksopplysning> get() = vilkårssett.saksopplysninger
    val vilkårsvurderinger: List<Vurdering> get() = vilkårssett.vilkårsvurderinger

    val kravdatoSaksopplysninger: KravdatoSaksopplysninger get() = vilkårssett.kravdatoSaksopplysninger

    val utfallsperioder: List<Utfallsperiode> get() = vilkårssett.utfallsperioder

    fun leggTilSøknad(søknad: Søknad): Behandling
    fun leggTilSaksopplysning(saksopplysning: Saksopplysning): LeggTilSaksopplysningRespons
    fun oppdaterTiltak(tiltak: List<Tiltak>): Behandling
    fun startBehandling(saksbehandler: Saksbehandler): Behandling
    fun startGodkjenning(saksbehandler: Saksbehandler): Behandling
    fun avbrytBehandling(saksbehandler: Saksbehandler): Behandling
    fun tilBeslutting(saksbehandler: Saksbehandler): Behandling
    fun iverksett(utøvendeBeslutter: Saksbehandler): Behandling
    fun sendTilbake(utøvendeBeslutter: Saksbehandler): Behandling
    fun vilkårsvurder(): Behandling
    fun saksopplysninger(): List<Saksopplysning>
    fun søknad(): Søknad
    fun oppdaterAntallDager(
        tiltakId: TiltakId,
        nyPeriodeMedAntallDager: PeriodeMedVerdi<AntallDager>,
        saksbehandler: Saksbehandler,
    ): Behandling

    fun tilbakestillAntallDager(
        tiltakId: TiltakId,
        saksbehandler: Saksbehandler,
    ): Behandling
}
