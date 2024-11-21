package no.nav.tiltakspenger.saksbehandling.domene.behandling

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import no.nav.tiltakspenger.felles.exceptions.StøtterIkkeUtfallException
import no.nav.tiltakspenger.felles.nå
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.Saksbehandler
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.tiltak.TiltakstypeSomGirRett
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandlingsstatus.INNVILGET
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandlingsstatus.KLAR_TIL_BEHANDLING
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandlingsstatus.KLAR_TIL_BESLUTNING
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandlingsstatus.UNDER_BEHANDLING
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandlingsstatus.UNDER_BESLUTNING
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.domene.stønadsdager.Stønadsdager
import no.nav.tiltakspenger.saksbehandling.domene.stønadsdager.tilStønadsdagerRegisterSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltak
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.SamletUtfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkårssett
import java.time.LocalDate
import java.time.LocalDateTime

data class Førstegangsbehandling(
    override val id: BehandlingId,
    override val sakId: SakId,
    override val saksnummer: Saksnummer,
    override val fnr: Fnr,
    override val vurderingsperiode: Periode,
    override val søknad: Søknad,
    override val saksbehandler: String?,
    override val sendtTilBeslutning: LocalDateTime?,
    override val beslutter: String?,
    override val vilkårssett: Vilkårssett,
    override val stønadsdager: Stønadsdager,
    override val status: Behandlingsstatus,
    override val attesteringer: List<Attestering>,
    override val opprettet: LocalDateTime,
    override val iverksattTidspunkt: LocalDateTime?,
    override val sendtTilDatadeling: LocalDateTime?,
    override val sistEndret: LocalDateTime,
) : Behandling {
    init {
        require(vilkårssett.vurderingsperiode == vurderingsperiode) {
            "Vilkårssettets periode (${vilkårssett.vurderingsperiode} må være lik vurderingsperioden $vurderingsperiode"
        }
        if (beslutter != null && saksbehandler != null) {
            require(beslutter != saksbehandler) { "Saksbehandler og beslutter kan ikke være samme person" }
        }
        require(søknad.barnetillegg.isEmpty()) {
            "Barnetillegg er ikke støttet i MVP 1"
        }
        if (status == KLAR_TIL_BEHANDLING) {
            require(saksbehandler == null) {
                "Behandlingen kan ikke være tilknyttet en saksbehandler når statusen er KLAR_TIL_BEHANDLING"
            }
            // Selvom beslutter har underkjent, må vi kunne ta hen av behandlingen.
            require(iverksattTidspunkt == null)
        }
        if (status == UNDER_BEHANDLING) {
            requireNotNull(saksbehandler) {
                "Behandlingen må være tilknyttet en saksbehandler når status er UNDER_BEHANDLING"
            }
            // Selvom beslutter har underkjent, må vi kunne ta hen av behandlingen.
            require(iverksattTidspunkt == null)
        }
        if (status == KLAR_TIL_BESLUTNING) {
            // Vi kan ikke ta saksbehandler av behandlingen før den underkjennes.
            requireNotNull(saksbehandler) { "Behandlingen må ha saksbehandler når status er KLAR_TIL_BESLUTNING" }
            require(beslutter == null) {
                "Behandlingen kan ikke være tilknyttet en beslutter når status er KLAR_TIL_BESLUTNING"
            }
            require(vilkårssett.samletUtfall != SamletUtfall.UAVKLART) {
                "Behandlingen kan ikke være KLAR_TIL_BESLUTNING når samlet utfall er UAVKLART"
            }
            require(iverksattTidspunkt == null)
        }
        if (status == UNDER_BESLUTNING) {
            // Vi kan ikke ta saksbehandler av behandlingen før den underkjennes.
            requireNotNull(saksbehandler) { "Behandlingen må ha saksbehandler når status er UNDER_BESLUTNING" }
            requireNotNull(beslutter) { "Behandlingen må tilknyttet en beslutter når status er UNDER_BESLUTNING" }
            require(
                vilkårssett.samletUtfall != SamletUtfall.UAVKLART,
            ) { "Behandlingen kan ikke være UNDER_BESLUTNING når samlet utfall er UAVKLART" }
            require(iverksattTidspunkt == null)
        }

        if (status == INNVILGET) {
            // Det er viktig at vi ikke tar saksbehandler og beslutter av behandlingen når status er INNVILGET.
            requireNotNull(beslutter) { "Behandlingen må ha beslutter når status er INNVILGET" }
            requireNotNull(saksbehandler) { "Behandlingen må ha saksbehandler når status er INNVILGET" }
            require(
                vilkårssett.samletUtfall != SamletUtfall.UAVKLART,
            ) { "Behandlingen kan ikke være innvilget når samlet utfall er UAVKLART" }
            requireNotNull(iverksattTidspunkt)
            requireNotNull(sendtTilBeslutning)
        }
    }

    val erIverksatt: Boolean = status == INNVILGET
    val maksDagerMedTiltakspengerForPeriode: Int = stønadsdager.registerSaksopplysning.antallDager

    val tiltaksnavn = vilkårssett.tiltakDeltagelseVilkår.registerSaksopplysning.tiltaksnavn
    val tiltakstype: TiltakstypeSomGirRett = vilkårssett.tiltakDeltagelseVilkår.registerSaksopplysning.tiltakstype
    val tiltaksid: String = vilkårssett.tiltakDeltagelseVilkår.registerSaksopplysning.eksternTiltakId
    val gjennomføringId: String? = vilkårssett.tiltakDeltagelseVilkår.registerSaksopplysning.gjennomføringId
    val samletUtfall = vilkårssett.samletUtfall

    companion object {
        fun opprettBehandling(
            sakId: SakId,
            saksnummer: Saksnummer,
            fnr: Fnr,
            søknad: Søknad,
            fødselsdato: LocalDate,
            saksbehandler: Saksbehandler,
            registrerteTiltak: List<Tiltak>,
        ): Either<KanIkkeOppretteBehandling, Førstegangsbehandling> {
            val vurderingsperiode = søknad.vurderingsperiode()
            if (søknad.barnetillegg.isNotEmpty()) {
                return KanIkkeOppretteBehandling.StøtterIkkeBarnetillegg.left()
            }
            val tiltak =
                registrerteTiltak.find {
                    it.eksternId == søknad.tiltak.id &&
                        it.deltakelsesperiode.overlapperMed(vurderingsperiode)
                }

            if (tiltak == null) {
                return KanIkkeOppretteBehandling.FantIkkeTiltak.left()
            }

            // TODO post-mvp B og H: Fjern denne når vi begynner å implementere delvis innvilgelse og/eller avslag
            val vilkårssett = Either.catch {
                Vilkårssett.opprett(
                    søknad = søknad,
                    fødselsdato = fødselsdato,
                    tiltak = tiltak,
                    vurderingsperiode = vurderingsperiode,
                )
            }.getOrElse {
                if (it is StøtterIkkeUtfallException) {
                    return KanIkkeOppretteBehandling.StøtterKunInnvilgelse.left()
                } else {
                    throw it
                }
            }

            val opprettet = nå()
            return Førstegangsbehandling(
                id = BehandlingId.random(),
                saksnummer = saksnummer,
                sakId = sakId,
                fnr = fnr,
                søknad = søknad,
                vurderingsperiode = vurderingsperiode,
                vilkårssett = vilkårssett,
                stønadsdager = Stønadsdager(
                    vurderingsperiode = vurderingsperiode,
                    tiltak.tilStønadsdagerRegisterSaksopplysning(),
                ),
                saksbehandler = saksbehandler.navIdent,
                sendtTilBeslutning = null,
                beslutter = null,
                status = UNDER_BEHANDLING,
                attesteringer = emptyList(),
                opprettet = opprettet,
                iverksattTidspunkt = null,
                sendtTilDatadeling = null,
                sistEndret = opprettet,
            ).right()
        }
    }

    /**
     * Saksbehandler/beslutter tar eller overtar behandlingen.
     */
    override fun taBehandling(saksbehandler: Saksbehandler): Førstegangsbehandling =
        when (this.status) {
            KLAR_TIL_BEHANDLING, UNDER_BEHANDLING -> {
                check(saksbehandler.erSaksbehandler()) {
                    "Saksbehandler må ha rolle saksbehandler. Utøvende saksbehandler: $saksbehandler"
                }
                this.copy(saksbehandler = saksbehandler.navIdent, status = UNDER_BEHANDLING).let {
                    // Dersom utøvende saksbehandler er beslutter, fjern beslutter fra behandlingen.
                    if (it.saksbehandler == it.beslutter) it.copy(beslutter = null) else it
                }
            }

            KLAR_TIL_BESLUTNING, UNDER_BESLUTNING -> {
                check(saksbehandler.navIdent != this.saksbehandler) {
                    "Beslutter ($saksbehandler) kan ikke være den samme som saksbehandleren (${this.saksbehandler}"
                }
                check(saksbehandler.erBeslutter()) {
                    "Saksbehandler må ha beslutterrolle. Utøvende saksbehandler: $saksbehandler"
                }
                this.copy(beslutter = saksbehandler.navIdent, status = UNDER_BESLUTNING)
            }

            INNVILGET -> {
                throw IllegalArgumentException(
                    "Kan ikke ta behandling når behandlingen er IVERKSATT. Behandlingsstatus: ${this.status}. Utøvende saksbehandler: $saksbehandler. Saksbehandler på behandling: ${this.saksbehandler}",
                )
            }
        }

    override fun tilBeslutning(saksbehandler: Saksbehandler): Førstegangsbehandling {
        if (vilkårssett.samletUtfall != SamletUtfall.OPPFYLT) {
            throw IllegalStateException("Kan ikke sende en behandling til beslutning som ikke er innvilget i MVP 1")
        }
        check(status == UNDER_BEHANDLING) {
            "Behandlingen må være under behandling, det innebærer også at en saksbehandler må ta saken før den kan sendes til beslutter. Behandlingsstatus: ${this.status}. Utøvende saksbehandler: $saksbehandler. Saksbehandler på behandling: ${this.saksbehandler}"
        }

        check(saksbehandler.navIdent == this.saksbehandler) { "Det er ikke lov å sende en annen sin behandling til beslutter" }
        check(samletUtfall != SamletUtfall.UAVKLART) { "Kan ikke sende en UAVKLART behandling til beslutter" }
        return this.copy(
            status = if (beslutter == null) KLAR_TIL_BESLUTNING else UNDER_BESLUTNING,
            sendtTilBeslutning = nå(),
        )
    }

    override fun iverksett(
        utøvendeBeslutter: Saksbehandler,
        attestering: Attestering,
    ): Førstegangsbehandling {
        if (vilkårssett.samletUtfall != SamletUtfall.OPPFYLT) {
            throw IllegalStateException("Kan ikke iverksette en behandling som ikke er innvilget i MVP 1")
        }
        return when (status) {
            UNDER_BESLUTNING -> {
                check(utøvendeBeslutter.erBeslutter()) { "utøvende saksbehandler må være beslutter" }
                check(this.beslutter == utøvendeBeslutter.navIdent) { "Kan ikke iverksette en behandling man ikke er beslutter på" }
                check(!this.attesteringer.any { it.isGodkjent() }) {
                    "Behandlingen er allerede godkjent"
                }
                this.copy(
                    status = INNVILGET,
                    attesteringer = attesteringer + attestering,
                    iverksattTidspunkt = nå(),
                )
            }

            KLAR_TIL_BEHANDLING, UNDER_BEHANDLING, KLAR_TIL_BESLUTNING, INNVILGET -> throw IllegalStateException(
                "Må ha status UNDER_BESLUTNING for å iverksette. Behandlingsstatus: $status",
            )
        }
    }

    override fun sendTilbake(
        utøvendeBeslutter: Saksbehandler,
        attestering: Attestering,
    ): Førstegangsbehandling =
        when (status) {
            UNDER_BESLUTNING -> {
                check(
                    utøvendeBeslutter.erBeslutter(),
                ) { "utøvende saksbehandler må være beslutter" }
                check(this.beslutter == utøvendeBeslutter.navIdent) {
                    "Kun beslutter som har saken kan sende tilbake"
                }
                check(!this.attesteringer.any { it.isGodkjent() }) {
                    "Behandlingen er allerede godkjent"
                }
                this.copy(status = UNDER_BEHANDLING, attesteringer = attesteringer + attestering)
            }

            KLAR_TIL_BEHANDLING, UNDER_BEHANDLING, KLAR_TIL_BESLUTNING, INNVILGET -> throw IllegalStateException(
                "Må ha status UNDER_BESLUTNING for å sende tilbake. Behandlingsstatus: $status",
            )
        }
}
