package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.TiltakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDager
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.Tiltak
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.TiltakVilkår
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.vurderingsperiodeFraTiltak
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysninger.oppdaterSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkårssett
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder.AlderVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder.alderSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.institusjonsopphold.InstitusjonsoppholdVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.institusjonsopphold.institusjonsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.introduksjonsprogrammet.IntroVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.introduksjonsprogrammet.introSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravdato.KravdatoVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravdato.kravdatoSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.KVPVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.kvpSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.livsoppholdSaksopplysning
import java.time.LocalDate

data class Førstegangsbehandling(
    override val id: BehandlingId,
    override val sakId: SakId,
    override val vurderingsperiode: Periode,
    override val søknader: List<Søknad>,
    override val saksbehandler: String?,
    override val beslutter: String?,
    override val vilkårssett: Vilkårssett,
    override val tiltak: TiltakVilkår,
    override val status: BehandlingStatus,
    override val tilstand: BehandlingTilstand,
) : Behandling {

    init {
        // TODO jah: Brekker for mange tester. Bør legges inn når vi er ferdig med vilkår 2.0
        // require(vilkårssett.totalePeriode == vurderingsperiode) { "Vilkårssettets periode (${vilkårssett.totalePeriode} må være lik vurderingsperioden $vurderingsperiode" }
    }

    companion object {

        fun opprettBehandling(sakId: SakId, søknad: Søknad, fødselsdato: LocalDate): Førstegangsbehandling {
            val vurderingsperiode = søknad.vurderingsperiode()
            return Førstegangsbehandling(
                id = BehandlingId.random(),
                sakId = sakId,
                søknader = listOf(søknad),
                vurderingsperiode = vurderingsperiode,
                vilkårssett = Vilkårssett(
                    saksopplysninger = Saksopplysninger.initSaksopplysningerFraSøknad(søknad) + Saksopplysninger.lagSaksopplysningerAvSøknad(
                        søknad,
                    ),
                    vilkårsvurderinger = emptyList(),
                    utfallsperioder = emptyList(),
                    institusjonsoppholdVilkår = InstitusjonsoppholdVilkår.opprett(søknad.institusjonsoppholdSaksopplysning(vurderingsperiode)),
                    kvpVilkår = KVPVilkår.opprett(søknad.kvpSaksopplysning(vurderingsperiode)),
                    introVilkår = IntroVilkår.opprett(søknad.introSaksopplysning(vurderingsperiode)),
                    livsoppholdVilkår = LivsoppholdVilkår.opprett(
                        søknad.livsoppholdSaksopplysning(vurderingsperiode),
                        vurderingsperiode,
                    ),
                    alderVilkår = AlderVilkår.opprett(søknad.alderSaksopplysning(fødselsdato = fødselsdato), vurderingsperiode),
                    kravdatoVilkår = KravdatoVilkår.opprett(søknad.kravdatoSaksopplysning(), vurderingsperiode),
                ),
                tiltak = TiltakVilkår(),
                saksbehandler = null,
                beslutter = null,
                status = BehandlingStatus.Manuell,
                tilstand = BehandlingTilstand.OPPRETTET,
            )
        }
    }

    override fun søknad(): Søknad = sisteSøknadMedOpprettetFraFørste()

    private fun sisteSøknadMedOpprettetFraFørste(): Søknad =
        søknader.maxBy { it.opprettet }.copy(opprettet = søknader.minBy { it.opprettet }.opprettet)

    override fun saksopplysninger(): List<Saksopplysning> {
        return saksopplysninger.groupBy { it.vilkår }.map { entry ->
            entry.value.reduce { acc, saksopplysning ->
                if (saksopplysning.kilde == Kilde.SAKSB) saksopplysning else acc
            }
        }
    }

    override fun leggTilSøknad(søknad: Søknad): Førstegangsbehandling {
        require(
            this.tilstand in listOf(
                BehandlingTilstand.OPPRETTET,
                BehandlingTilstand.VILKÅRSVURDERT,
                BehandlingTilstand.TIL_BESLUTTER,
            ),
        ) { "Kan ikke oppdatere tiltak, feil tilstand $tilstand" }

        if (tilstand == BehandlingTilstand.TIL_BESLUTTER) {
            // TODO Gjør noe ekstra (notifiser beslutter/behandler)
        }

        val fakta = if (søknad.vurderingsperiode() != this.vurderingsperiode) {
            Saksopplysninger.initSaksopplysningerFraSøknad(søknad) +
                Saksopplysninger.lagSaksopplysningerAvSøknad(søknad)
        } else {
            Saksopplysninger.lagSaksopplysningerAvSøknad(søknad)
                .fold(saksopplysninger) { acc, saksopplysning ->
                    acc.oppdaterSaksopplysninger(saksopplysning)
                }
        }
        // Avgjørelse jah: Vi skal ikke oppdatere vilkårsettet her mens vi skriver om til vilkår 2.0.
        // TODO jah: Fjern mulighet for samtidige søknader.
        //  Dersom avklaringen er basert på saksopplysning fra søknaden, bør vi nullstille avklaringen i påvente av en saksbehandler-opplysning.
        //  Dersom vi allerede har en saksbehander-opplysning, bør vi kreve at saksbehandler tar stilling til alle vilkårene på nytt.
        return this.copy(
            søknader = this.søknader + søknad,
            vurderingsperiode = søknad.vurderingsperiode(),
            vilkårssett = vilkårssett.copy(
                saksopplysninger = fakta,
            ),
            tilstand = BehandlingTilstand.OPPRETTET,
        ).vilkårsvurder()
    }

    override fun leggTilSaksopplysning(saksopplysning: Saksopplysning): LeggTilSaksopplysningRespons {
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

        val oppdatertVilkårssett = vilkårssett.oppdaterSaksopplysning(saksopplysning)
        return if (oppdatertVilkårssett == vilkårssett) {
            LeggTilSaksopplysningRespons(
                behandling = this,
                erEndret = false,
            )
        } else {
            LeggTilSaksopplysningRespons(
                behandling = this.copy(vilkårssett = oppdatertVilkårssett).vilkårsvurder(),
                erEndret = true,
            )
        }
    }

    override fun oppdaterAntallDager(
        tiltakId: TiltakId,
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
            tiltak = tiltak.oppdaterAntallDager(tiltakId, nyPeriodeMedAntallDager, saksbehandler),
        )
    }

    override fun tilbakestillAntallDager(
        tiltakId: TiltakId,
        saksbehandler: Saksbehandler,
    ): Behandling {
        require(
            this.tilstand in listOf(
                BehandlingTilstand.OPPRETTET,
                BehandlingTilstand.VILKÅRSVURDERT,
                BehandlingTilstand.TIL_BESLUTTER,
            ),
        ) { "Kan ikke tilbakestille antall dager i tiltak, feil tilstand $tilstand" }

        if (tilstand == BehandlingTilstand.TIL_BESLUTTER) {
            // TODO Gjør noe ekstra
        }
        check(saksbehandler.isSaksbehandler() || saksbehandler.isAdmin()) { "Man kan ikke tilbakestille antall dager uten å være saksbehandler eller admin" }

        return this.copy(tiltak = tiltak.tilbakestillAntallDager(tiltakId, saksbehandler))
    }

    override fun oppdaterTiltak(nyeTiltak: List<Tiltak>): Førstegangsbehandling {
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

        if (vurderingsperiodeRommerPeriodeFraTiltak(nyeTiltak.vurderingsperiodeFraTiltak())) {
            // Vurderingsperioden endres ikke
            return this.copy(
                tiltak = tiltak.oppdaterTiltak(nyeTiltak),
                tilstand = BehandlingTilstand.OPPRETTET,
            ).vilkårsvurder()
        }

        // Vurderingsperioden må endres
        val vurderingsperiodeFraTiltak = nyeTiltak.vurderingsperiodeFraTiltak()!!
        val nyVurderingsperiode = Periode(
            minOf(vurderingsperiode.fraOgMed, vurderingsperiodeFraTiltak.fraOgMed),
            maxOf(vurderingsperiode.tilOgMed, vurderingsperiodeFraTiltak.tilOgMed),
        )

        return this.copy(
            vurderingsperiode = nyVurderingsperiode,
            vilkårssett = vilkårssett.vurderingsperiodeEndret(nyVurderingsperiode),
            tiltak = tiltak.oppdaterTiltak(nyeTiltak),
            tilstand = BehandlingTilstand.OPPRETTET,
        ).vilkårsvurder()
    }

    override fun startBehandling(saksbehandler: Saksbehandler): Førstegangsbehandling {
        require(
            this.tilstand in listOf(BehandlingTilstand.OPPRETTET, BehandlingTilstand.VILKÅRSVURDERT),
        ) { "Kan ikke starte behandling, feil tilstand $tilstand" }

        check(this.saksbehandler == null) { "Denne behandlingen er allerede tatt" }
        check(saksbehandler.isSaksbehandler()) { "Saksbehandler må være saksbehandler" }
        return this.copy(saksbehandler = saksbehandler.navIdent)
    }

    override fun startGodkjenning(saksbehandler: Saksbehandler): Førstegangsbehandling {
        require(
            this.tilstand in listOf(BehandlingTilstand.TIL_BESLUTTER),
        ) { "Kan ikke godkjenne behandling, feil tilstand $tilstand" }

        check(this.beslutter == null) { "Denne behandlingen er allerede tatt" }
        check(saksbehandler.isBeslutter()) { "Saksbehandler må være beslutter" }
        return this.copy(beslutter = saksbehandler.navIdent)
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

    /**
     * Endrer tilstand til VILKÅRSVURDERT
     */
    override fun vilkårsvurder(): Førstegangsbehandling {
        val deltagelseVurderinger = tiltak.vilkårsvurder()

        val vurderinger = saksopplysninger().flatMap {
            it.lagVurdering(vurderingsperiode)
        } + deltagelseVurderinger

        val utfallsperioder =
            vurderingsperiode.fraOgMed.datesUntil(vurderingsperiode.tilOgMed.plusDays(1)).toList().map { dag ->
                val idag = vurderinger.filter { dag >= it.fom && dag <= it.tom }
                val utfallYtelser = when {
                    idag.any { it.utfall == Utfall.KREVER_MANUELL_VURDERING } -> UtfallForPeriode.KREVER_MANUELL_VURDERING
                    idag.all { it.utfall == Utfall.OPPFYLT } -> UtfallForPeriode.GIR_RETT_TILTAKSPENGER
                    else -> UtfallForPeriode.GIR_IKKE_RETT_TILTAKSPENGER
                }

                val harManuelleBarnUnder16 = this.søknad().barnetillegg.filterIsInstance<Barnetillegg.Manuell>()
                    .filter { it.oppholderSegIEØS == Søknad.JaNeiSpm.Ja }.count { it.under16ForDato(dag) } > 0

                val utfall =
                    if (utfallYtelser == UtfallForPeriode.GIR_RETT_TILTAKSPENGER && harManuelleBarnUnder16) UtfallForPeriode.KREVER_MANUELL_VURDERING else utfallYtelser

                Utfallsperiode(
                    fom = dag,
                    tom = dag,
                    antallBarn = this.søknad().barnetillegg.filter { it.oppholderSegIEØS == Søknad.JaNeiSpm.Ja }
                        .count { it.under16ForDato(dag) },
                    utfall = utfall,
                )
            }.fold(emptyList<Utfallsperiode>()) { periodisertliste, nesteDag ->
                periodisertliste.slåSammen(nesteDag)
            }

        val status = if (utfallsperioder.any { it.utfall == UtfallForPeriode.KREVER_MANUELL_VURDERING }) {
            BehandlingStatus.Manuell
        } else if (utfallsperioder.any { it.utfall == UtfallForPeriode.GIR_RETT_TILTAKSPENGER }) {
            BehandlingStatus.Innvilget
        } else {
            BehandlingStatus.Avslag
        }

        return this.copy(
            vilkårssett = vilkårssett.oppdaterVilkårsvurderinger(vurderinger, utfallsperioder),
            status = status,
            tilstand = BehandlingTilstand.VILKÅRSVURDERT,
        )
    }

    private fun List<Utfallsperiode>.slåSammen(neste: Utfallsperiode): List<Utfallsperiode> {
        if (this.isEmpty()) return listOf(neste)
        val forrige = this.last()
        return if (forrige.kanSlåsSammen(neste)) {
            this.dropLast(1) + forrige.copy(
                tom = neste.tom,
            )
        } else {
            this + neste
        }
    }

    private fun lagFristForFramsettingAvKravVurdering(utfall: Utfall, periode: Periode, kilde: Kilde): Vurdering =
        Vurdering(
            utfall = utfall,
            kilde = kilde,
            fom = periode.fraOgMed,
            tom = periode.tilOgMed,
            vilkår = Vilkår.FRIST_FOR_FRAMSETTING_AV_KRAV,
            detaljer = "",
            grunnlagId = null,
        )

    private fun vurderingsperiodeRommerPeriodeFraTiltak(periode: Periode?): Boolean =
        periode?.let { vurderingsperiode.inneholderHele(it) } ?: true
}
