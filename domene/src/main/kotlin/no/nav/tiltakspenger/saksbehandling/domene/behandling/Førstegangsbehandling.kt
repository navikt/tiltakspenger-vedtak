package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDager
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.Tiltak
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.LivsoppholdYtelseSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.SøknadTilSaksopplysningMapper
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.YtelseSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Inngangsvilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.LivsoppholdDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkårdata.VilkårSett

data class Førstegangsbehandling(
    override val id: BehandlingId,
    override val sakId: SakId,
    override val vurderingsperiode: Periode,
    override val søknader: List<Søknad>,
    override val saksbehandler: String?,
    override val beslutter: String?,
    val vilkårSett: VilkårSett,
    override val utfallsperioder: Periodisering<Utfallsdetaljer>,
    override val status: BehandlingStatus,
    override val tilstand: BehandlingTilstand,
) : Behandling {

    companion object {

        fun opprettBehandling(sakId: SakId, søknad: Søknad): Førstegangsbehandling {
            return Førstegangsbehandling(
                id = BehandlingId.random(),
                sakId = sakId,
                søknader = listOf(søknad),
                vurderingsperiode = søknad.vurderingsperiode(),
                vilkårSett = VilkårSett(søknad.vurderingsperiode()),
                saksbehandler = null,
                beslutter = null,
                utfallsperioder = Periodisering(
                    Utfallsdetaljer(0, UtfallForPeriode.KREVER_MANUELL_VURDERING),
                    søknad.vurderingsperiode(),
                ),
                status = BehandlingStatus.Manuell,
                tilstand = BehandlingTilstand.OPPRETTET,
            ).leggTilSøknad(søknad) // TODO: Denne likte jeg ikke helt, var kulere da det ble da vi opprettet LivsoppholdVilkårData?
        }
    }

    override fun søknad(): Søknad = sisteSøknadMedOpprettetFraFørste()

    private fun sisteSøknadMedOpprettetFraFørste(): Søknad =
        søknader.maxBy { it.opprettet }.copy(opprettet = søknader.minBy { it.opprettet }.opprettet)

    override fun leggTilSøknad(søknad: Søknad): Førstegangsbehandling {
        // TODO: Jeg synes ikke det bør opprettes revurdering herfra hvis behandlingen er Iverksatt,
        // det hører hjemme i SakService

        require(
            this.tilstand in listOf(
                BehandlingTilstand.OPPRETTET,
                BehandlingTilstand.VILKÅRSVURDERT,
                BehandlingTilstand.TIL_BESLUTTER,
            ),
        ) { "Kan ikke oppdatere tiltak, feil tilstand $tilstand" }

        if (tilstand == BehandlingTilstand.TIL_BESLUTTER) {
            // TODO Gjør noe ekstra
        }

        /*
        val fakta = if (søknad.vurderingsperiode() != this.vurderingsperiode) {
            Saksopplysninger.initSaksopplysningerFraSøknad(søknad) +
                Saksopplysninger.lagSaksopplysningerAvSøknad(søknad)
        } else {
            Saksopplysninger.lagSaksopplysningerAvSøknad(søknad)
                .fold(this.saksopplysninger) { acc, saksopplysning ->
                    acc.oppdaterSaksopplysninger(saksopplysning)
                }
        }

        return this.copy(
            søknader = this.søknader + søknad,
            vurderingsperiode = søknad.vurderingsperiode(),
            saksopplysninger = fakta,
        ).vilkårsvurder()

         */

        // TODO: Håndter flere søknader
        val ytelseSaksopplysninger: Map<Inngangsvilkår, YtelseSaksopplysning> = mapOf(
            Inngangsvilkår.KVP to søknad.kvp,
            Inngangsvilkår.INTROPROGRAMMET to søknad.intro,
            Inngangsvilkår.INSTITUSJONSOPPHOLD to søknad.institusjon,
        ).mapValues {
            SøknadTilSaksopplysningMapper.lagYtelseSaksopplysningFraPeriodeSpørsmål(
                it.key,
                it.value,
                søknad.vurderingsperiode(),
            )
        }
        val livsoppholdYtelseSaksopplysninger: Map<LivsoppholdDelVilkår, LivsoppholdYtelseSaksopplysning> = mapOf(
            LivsoppholdDelVilkår.GJENLEVENDEPENSJON to søknad.gjenlevendepensjon,
            LivsoppholdDelVilkår.SYKEPENGER to søknad.sykepenger,
            LivsoppholdDelVilkår.SUPPLERENDESTØNADALDER to søknad.supplerendeStønadAlder,
            LivsoppholdDelVilkår.SUPPLERENDESTØNADFLYKTNING to søknad.supplerendeStønadFlyktning,
            LivsoppholdDelVilkår.JOBBSJANSEN to søknad.jobbsjansen,
            LivsoppholdDelVilkår.PENSJONSINNTEKT to søknad.trygdOgPensjon,
        ).mapValues {
            SøknadTilSaksopplysningMapper.lagYtelseSaksopplysningFraPeriodeSpørsmål(
                it.key,
                it.value,
                søknad.vurderingsperiode(),
            )
        } + (
            LivsoppholdDelVilkår.ALDERSPENSJON to SøknadTilSaksopplysningMapper.lagSaksopplysningFraFraOgMedDatospørsmål(
                LivsoppholdDelVilkår.ALDERSPENSJON,
                søknad.alderspensjon,
                søknad.vurderingsperiode(),
            )
            ) + (
            LivsoppholdDelVilkår.ETTERLØNN to
                SøknadTilSaksopplysningMapper.lagSaksopplysningFraJaNeiSpørsmål(
                    LivsoppholdDelVilkår.ETTERLØNN,
                    søknad.etterlønn,
                    søknad.vurderingsperiode(),
                )
            )

        return this
            .copy(
                søknader = søknader + søknad,
                vurderingsperiode = søknad.vurderingsperiode(),

                vilkårSett = vilkårSett
                    .oppdaterSaksopplysninger(
                        livsoppholdYtelseSaksopplysninger[LivsoppholdDelVilkår.GJENLEVENDEPENSJON]!!,
                    ).oppdaterSaksopplysninger(
                        livsoppholdYtelseSaksopplysninger[LivsoppholdDelVilkår.SYKEPENGER]!!,
                    ).oppdaterSaksopplysninger(
                        livsoppholdYtelseSaksopplysninger[LivsoppholdDelVilkår.SUPPLERENDESTØNADALDER]!!,
                    ).oppdaterSaksopplysninger(
                        livsoppholdYtelseSaksopplysninger[LivsoppholdDelVilkår.SUPPLERENDESTØNADFLYKTNING]!!,
                    ).oppdaterSaksopplysninger(
                        livsoppholdYtelseSaksopplysninger[LivsoppholdDelVilkår.JOBBSJANSEN]!!,
                    ).oppdaterSaksopplysninger(
                        livsoppholdYtelseSaksopplysninger[LivsoppholdDelVilkår.PENSJONSINNTEKT]!!,
                    ).oppdaterSaksopplysninger(
                        livsoppholdYtelseSaksopplysninger[LivsoppholdDelVilkår.ALDERSPENSJON]!!,
                    ).oppdaterSaksopplysninger(
                        livsoppholdYtelseSaksopplysninger[LivsoppholdDelVilkår.ETTERLØNN]!!,
                    ),
            )
            .vilkårsvurder()
    }

    override fun leggTilSaksopplysning(ytelseSaksopplysning: YtelseSaksopplysning): LeggTilSaksopplysningRespons {
        // TODO: Jeg synes ikke det bør opprettes revurdering herfra hvis behandlingen er Iverksatt,
        // det hører hjemme i SakService
        require(
            this.tilstand in listOf(
                BehandlingTilstand.OPPRETTET,
                BehandlingTilstand.VILKÅRSVURDERT,
                BehandlingTilstand.TIL_BESLUTTER,
            ),
        ) { "Kan ikke oppdatere tiltak, feil tilstand $tilstand" }

        if (tilstand == BehandlingTilstand.TIL_BESLUTTER) {
            // TODO Gjør noe ekstra
        }

        val oppdatertYtelserVilkårData = vilkårSett.oppdaterSaksopplysninger(ytelseSaksopplysning)
        return if (oppdatertYtelserVilkårData == this.vilkårSett) {
            LeggTilSaksopplysningRespons(
                behandling = this,
                erEndret = false,
            )
        } else {
            LeggTilSaksopplysningRespons(
                behandling = this.copy(vilkårSett = oppdatertYtelserVilkårData).vilkårsvurder(),
                erEndret = true,
            )
        }
    }

    override fun leggTilSaksopplysning(livsoppholdSaksopplysning: LivsoppholdYtelseSaksopplysning): LeggTilSaksopplysningRespons {
        // TODO: Jeg synes ikke det bør opprettes revurdering herfra hvis behandlingen er Iverksatt,
        // det hører hjemme i SakService
        require(
            this.tilstand in listOf(
                BehandlingTilstand.OPPRETTET,
                BehandlingTilstand.VILKÅRSVURDERT,
                BehandlingTilstand.TIL_BESLUTTER,
            ),
        ) { "Kan ikke oppdatere tiltak, feil tilstand $tilstand" }

        if (tilstand == BehandlingTilstand.TIL_BESLUTTER) {
            // TODO Gjør noe ekstra
        }

        val oppdatertYtelserVilkårData = vilkårSett.oppdaterSaksopplysninger(livsoppholdSaksopplysning)
        return if (oppdatertYtelserVilkårData == this.vilkårSett) {
            LeggTilSaksopplysningRespons(
                behandling = this,
                erEndret = false,
            )
        } else {
            LeggTilSaksopplysningRespons(
                behandling = this.copy(vilkårSett = oppdatertYtelserVilkårData).vilkårsvurder(),
                erEndret = true,
            )
        }
    }

    override fun oppdaterAntallDager(
        tiltakId: String,
        nyPeriodeMedAntallDager: PeriodeMedVerdi<AntallDager>,
        saksbehandler: Saksbehandler,
    ): Behandling {
        require(
            this.tilstand in listOf(
                BehandlingTilstand.OPPRETTET,
                BehandlingTilstand.VILKÅRSVURDERT,
                BehandlingTilstand.TIL_BESLUTTER,
            ),
        ) { "Kan ikke oppdatere antall dager i tiltak, feil tilstand $tilstand" }

        if (tilstand == BehandlingTilstand.TIL_BESLUTTER) {
            // TODO Gjør noe ekstra
        }
        check(saksbehandler.isSaksbehandler() || saksbehandler.isAdmin()) { "Man kan ikke oppdatere antall dager uten å være saksbehandler eller admin" }

        return this.copy(
            vilkårSett = vilkårSett.oppdaterAntallDager(
                tiltakId,
                nyPeriodeMedAntallDager,
                saksbehandler,
            ),
            tilstand = BehandlingTilstand.OPPRETTET,
        ).vilkårsvurder()
    }

    override fun oppdaterTiltak(tiltak: List<Tiltak>): Førstegangsbehandling {
        // TODO: Jeg synes ikke det bør opprettes revurdering herfra hvis behandlingen er Iverksatt,
        // det hører hjemme i SakService

        require(
            this.tilstand in listOf(
                BehandlingTilstand.OPPRETTET,
                BehandlingTilstand.VILKÅRSVURDERT,
                BehandlingTilstand.TIL_BESLUTTER,
            ),
        ) { "Kan ikke oppdatere tiltak, feil tilstand $tilstand" }

        if (tilstand == BehandlingTilstand.TIL_BESLUTTER) {
            // TODO Gjør noe ekstra
        }

        return this.copy(
            vilkårSett = vilkårSett.oppdaterTiltak(tiltak),
            tilstand = BehandlingTilstand.OPPRETTET,
        ).vilkårsvurder()
    }

    override fun startBehandling(saksbehandler: Saksbehandler): Førstegangsbehandling {
        // TODO: Jeg liker ikke at denne brukes både for å assigne saksbehandler OG beslutter, burde lage en egen for beslutter
        require(
            this.tilstand in listOf(BehandlingTilstand.OPPRETTET, BehandlingTilstand.VILKÅRSVURDERT),
        ) { "Kan ikke starte behandling, feil tilstand $tilstand" }

        check(this.saksbehandler == null) { "Denne behandlingen er allerede tatt" }
        check(saksbehandler.isSaksbehandler()) { "Saksbehandler må være saksbehandler" }
        return this.copy(saksbehandler = saksbehandler.navIdent)
    }

    override fun avbrytBehandling(saksbehandler: Saksbehandler): Førstegangsbehandling {
        require(
            this.tilstand in listOf(BehandlingTilstand.OPPRETTET, BehandlingTilstand.VILKÅRSVURDERT),
        ) { "Kan ikke avbryte behandling, feil tilstand $tilstand" }

        check(saksbehandler.isSaksbehandler() || saksbehandler.isAdmin()) { "Kan ikke avbryte en behandling som ikke er din" }
        return this.copy(saksbehandler = null)
    }

    override fun tilBeslutting(saksbehandler: Saksbehandler): Førstegangsbehandling {
        require(this.tilstand == BehandlingTilstand.VILKÅRSVURDERT) { "Kan ikke sende behandling til beslutning, feil tilstand $tilstand" }

        checkNotNull(this.saksbehandler) { "Ikke lov å sende Behandling til Beslutter uten saksbehandler" }
        check(saksbehandler.navIdent == this.saksbehandler) { "Det er ikke lov å sende en annen sin behandling til beslutter" }
        check(status != BehandlingStatus.Manuell) { "Kan ikke sende denne behandlingen til beslutter" }
        return this.copy(tilstand = BehandlingTilstand.TIL_BESLUTTER)
    }

    override fun iverksett(utøvendeBeslutter: Saksbehandler): Førstegangsbehandling {
        require(this.tilstand == BehandlingTilstand.TIL_BESLUTTER) { "Kan ikke iverksette behandling, feil tilstand $tilstand" }
        // checkNotNull(saksbehandler) { "Kan ikke iverksette en behandling uten saksbehandler" }
        checkNotNull(beslutter) { "Ikke lov å iverksette uten beslutter" }
        check(utøvendeBeslutter.roller.contains(Rolle.BESLUTTER)) { "Saksbehandler må være beslutter" }
        check(this.beslutter == utøvendeBeslutter.navIdent) { "Kan ikke iverksette en behandling man ikke er beslutter på" }

        return when (status) {
            BehandlingStatus.Manuell -> throw IllegalStateException("En behandling til beslutter kan ikke være manuell")
            BehandlingStatus.Avslag -> throw IllegalStateException("Iverksett av Avslag fungerer, men skal ikke tillates i mvp 1 ${this.id}")
            else -> this.copy(tilstand = BehandlingTilstand.IVERKSATT)
        }
    }

    override fun sendTilbake(utøvendeBeslutter: Saksbehandler): Førstegangsbehandling {
        require(this.tilstand == BehandlingTilstand.TIL_BESLUTTER) { "Kan ikke sende tilbake behandling, feil tilstand $tilstand" }

        check(utøvendeBeslutter.isBeslutter() || utøvendeBeslutter.isAdmin()) { "Saksbehandler må være beslutter eller administrator" }
        check(this.beslutter == utøvendeBeslutter.navIdent || utøvendeBeslutter.isAdmin()) { "Det er ikke lov å sende en annen sin behandling tilbake til saksbehandler" }
        return this.copy(
            tilstand = BehandlingTilstand.VILKÅRSVURDERT,
        )
    }

    override fun vilkårsvurder(): Førstegangsbehandling {
        val samletUtfall: Periodisering<UtfallForPeriode> = this.vilkårSett.samletUtfall()
            .map {
                when (it) {
                    Utfall.UAVKLART -> UtfallForPeriode.KREVER_MANUELL_VURDERING
                    Utfall.IKKE_OPPFYLT -> UtfallForPeriode.GIR_IKKE_RETT_TILTAKSPENGER
                    Utfall.OPPFYLT -> UtfallForPeriode.GIR_RETT_TILTAKSPENGER
                }
            }

        // TODO: Mangler barnetillegg
        val utfallsperioder: Periodisering<Utfallsdetaljer> =
            samletUtfall.map {
                Utfallsdetaljer(1, it)
            }

        val status =
            if (utfallsperioder.perioder().any { it.verdi.utfall == UtfallForPeriode.KREVER_MANUELL_VURDERING }) {
                BehandlingStatus.Manuell
            } else if (utfallsperioder.perioder().any { it.verdi.utfall == UtfallForPeriode.GIR_RETT_TILTAKSPENGER }) {
                BehandlingStatus.Innvilget
            } else {
                BehandlingStatus.Avslag
            }

        return this.copy(
            utfallsperioder = utfallsperioder,
            status = status,
            tilstand = BehandlingTilstand.VILKÅRSVURDERT,
        )
    }
}
