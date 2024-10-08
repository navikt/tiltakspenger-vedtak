package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.domene.stønadsdager.Stønadsdager
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.AvklartUtfallForPeriode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.UtfallForPeriode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkårssett
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.toAvklartUtfallForPeriode
import java.time.LocalDateTime

interface Behandling {
    val id: BehandlingId
    val sakId: SakId
    val fnr: Fnr
    val saksnummer: Saksnummer
    val vurderingsperiode: Periode
    val søknad: Søknad
    val saksbehandler: String?
    val beslutter: String?
    val vilkårssett: Vilkårssett
    val stønadsdager: Stønadsdager
    val status: Behandlingsstatus
    val attesteringer: List<Attestering>
    val opprettet: LocalDateTime

    val utfallsperioder: Periodisering<UtfallForPeriode> get() = vilkårssett.utfallsperioder()
    val avklarteUtfallsperioder: Periodisering<AvklartUtfallForPeriode> get() = utfallsperioder.toAvklartUtfallForPeriode()

    fun taBehandling(saksbehandler: Saksbehandler): Behandling

    fun tilBeslutning(saksbehandler: Saksbehandler): Behandling

    fun iverksett(
        utøvendeBeslutter: Saksbehandler,
        attestering: Attestering,
    ): Behandling

    fun sendTilbake(
        utøvendeBeslutter: Saksbehandler,
        attestering: Attestering,
    ): Behandling
}
