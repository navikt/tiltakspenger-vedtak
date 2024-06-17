package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.TiltakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.saksbehandling.domene.behandling.kravdato.KravdatoSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.behandling.kravdato.KravdatoSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDager
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.Tiltak
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysninger.oppdaterSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering

data class Førstegangsbehandling(
    override val id: BehandlingId,
    override val sakId: SakId,
    override val vurderingsperiode: Periode,
    override val søknader: List<Søknad>,
    override val saksbehandler: String?,
    override val beslutter: String?,
    override val saksopplysninger: List<Saksopplysning>,
    override val tiltak: List<Tiltak>,
    override val vilkårsvurderinger: List<Vurdering>,
    override val utfallsperioder: List<Utfallsperiode>,
    override val status: BehandlingStatus,
    override val tilstand: BehandlingTilstand,
    override val kravdatoSaksopplysninger: KravdatoSaksopplysninger,
) : Behandling {

    companion object {

        fun opprettBehandling(sakId: SakId, søknad: Søknad): Førstegangsbehandling {
            return Førstegangsbehandling(
                id = BehandlingId.random(),
                sakId = sakId,
                søknader = listOf(søknad),
                vurderingsperiode = søknad.vurderingsperiode(),
                saksopplysninger = Saksopplysninger.initSaksopplysningerFraSøknad(søknad) + Saksopplysninger.lagSaksopplysningerAvSøknad(
                    søknad,
                ),
                tiltak = emptyList(),
                saksbehandler = null,
                beslutter = null,
                vilkårsvurderinger = emptyList(),
                utfallsperioder = emptyList(),
                status = BehandlingStatus.Manuell,
                tilstand = BehandlingTilstand.OPPRETTET,
                kravdatoSaksopplysninger = KravdatoSaksopplysninger(
                    kravdatoSaksopplysningFraSøknad = KravdatoSaksopplysning(
                        kravdato = søknad.opprettet.toLocalDate(),
                        kilde = Kilde.SØKNAD,
                    ),
                ).avklar(),
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
    }

    override fun leggTilSaksopplysning(saksopplysning: Saksopplysning): LeggTilSaksopplysningRespons {
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

        val oppdatertSaksopplysningListe = saksopplysninger.oppdaterSaksopplysninger(saksopplysning)
        return if (oppdatertSaksopplysningListe == this.saksopplysninger) {
            LeggTilSaksopplysningRespons(
                behandling = this,
                erEndret = false,
            )
        } else {
            LeggTilSaksopplysningRespons(
                behandling = this.copy(saksopplysninger = oppdatertSaksopplysningListe).vilkårsvurder(),
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

        val tiltakTilOppdatering = tiltak.find { it.id == tiltakId }
        check(tiltakTilOppdatering != null) { "Kan ikke oppdatere antall dager fordi vi fant ikke tiltaket på behandlingen" }

        val oppdatertTiltak = tiltakTilOppdatering.leggTilAntallDagerFraSaksbehandler(nyPeriodeMedAntallDager)

        val nyeTiltak = tiltak.map {
            if (it.id == oppdatertTiltak.id) {
                oppdatertTiltak
            } else {
                it
            }
        }

        return this.copy(
            tiltak = nyeTiltak,
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

        val tiltakTilOppdatering = tiltak.find { it.id == tiltakId }
        check(tiltakTilOppdatering != null) { "Kan ikke tilbakestille antall dager fordi vi fant ikke tiltaket på behandlingen" }

        val oppdatertTiltak = tiltakTilOppdatering.tilbakestillAntallDagerFraSaksbehandler()

        val nyeTiltak = tiltak.map {
            if (it.id == oppdatertTiltak.id) {
                oppdatertTiltak
            } else {
                it
            }
        }

        return this.copy(
            tiltak = nyeTiltak,
        )
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
        return this.copy(tiltak = tiltak, tilstand = BehandlingTilstand.OPPRETTET).vilkårsvurder()
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

    override fun vilkårsvurder(): Førstegangsbehandling {
        val deltagelseVurderinger = tiltak.map { tiltak -> tiltak.vilkårsvurderTiltaksdeltagelse() }

        val vurderinger = saksopplysninger().flatMap {
            it.lagVurdering(vurderingsperiode)
        } + deltagelseVurderinger + vilkårsvurderFristForFramsettingAvKrav()

        val utfallsperioder =
            vurderingsperiode.fra.datesUntil(vurderingsperiode.til.plusDays(1)).toList().map { dag ->
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
            vilkårsvurderinger = vurderinger,
            utfallsperioder = utfallsperioder,
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
            fom = periode.fra,
            tom = periode.til,
            vilkår = Vilkår.FRIST_FOR_FRAMSETTING_AV_KRAV,
            detaljer = "",
            grunnlagId = null,
        )

    fun vilkårsvurderFristForFramsettingAvKrav(): List<Vurdering> {
        // TODO: Må finne ut av hva man skal gjøre når det kommer inn en ny søknad på en behandling som er til beslutter før denne sjekken passerer gjennom tester.
        // check(this.tilstand != BehandlingTilstand.TIL_BESLUTTER) { "Man kan ikke vilkårsvurdere en behandling som er sendt til beslutter" }
        check(this.tilstand != BehandlingTilstand.IVERKSATT) { "Man kan ikke vilkårsvurdere en behandling som er iverksatt" }

        val kravdatoSaksopplysning = kravdatoSaksopplysninger.avklartKravdatoSaksopplysning
        val kravdato = kravdatoSaksopplysning?.kravdato
        check(kravdato != null) { "Man kan ikke vilkårsvurdere frist for krav til framsatt dato uten at søknadsdato er avklart" }

        val datoDetKanInnvilgesFra = kravdato.withDayOfMonth(1).minusMonths(3)
        if (datoDetKanInnvilgesFra <= vurderingsperiode.fra) {
            return listOf(
                lagFristForFramsettingAvKravVurdering(
                    utfall = Utfall.OPPFYLT,
                    kilde = kravdatoSaksopplysning.kilde,
                    periode = vurderingsperiode,
                ),
            )
        } else if (datoDetKanInnvilgesFra > vurderingsperiode.til) {
            return listOf(
                lagFristForFramsettingAvKravVurdering(
                    utfall = Utfall.IKKE_OPPFYLT,
                    kilde = kravdatoSaksopplysning.kilde,
                    periode = vurderingsperiode,
                ),
            )
        } else {
            return listOf(
                lagFristForFramsettingAvKravVurdering(
                    utfall = Utfall.IKKE_OPPFYLT,
                    periode = Periode(
                        fra = vurderingsperiode.fra,
                        til = datoDetKanInnvilgesFra.minusDays(1),
                    ),
                    kilde = kravdatoSaksopplysning.kilde,
                ),
                lagFristForFramsettingAvKravVurdering(
                    utfall = Utfall.OPPFYLT,
                    periode = Periode(
                        fra = datoDetKanInnvilgesFra,
                        til = vurderingsperiode.til,
                    ),
                    kilde = kravdatoSaksopplysning.kilde,
                ),
            )
        }
    }
}
