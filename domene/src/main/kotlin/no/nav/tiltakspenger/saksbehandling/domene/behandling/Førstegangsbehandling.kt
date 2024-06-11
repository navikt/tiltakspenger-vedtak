package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.libs.periodisering.Periodisering.Companion.reduser
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
    override val utfallsperioder: List<Utfallsdetaljer>,
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

        val tiltakTilOppdatering = tiltak.find { it.id.toString() == tiltakId }
        check(tiltakTilOppdatering != null) { "Kan ikke oppdatere antall dager fordi vi fant ikke tiltaket på behandlingen" }

        val oppdatertTiltak = tiltakTilOppdatering.leggTilAntallDagerFraSaksbehandler(nyPeriodeMedAntallDager)

        val nyeTiltak = tiltak.map {
            if (it.eksternId == oppdatertTiltak.eksternId) {
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
        tiltakId: String,
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

        val tiltakTilOppdatering = tiltak.find { it.id.toString() == tiltakId }
        check(tiltakTilOppdatering != null) { "Kan ikke tilbakestille antall dager fordi vi fant ikke tiltaket på behandlingen" }

        val oppdatertTiltak = tiltakTilOppdatering.tilbakestillAntallDagerFraSaksbehandler()

        val nyeTiltak = tiltak.map {
            if (it.eksternId == oppdatertTiltak.eksternId) {
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
        val deltagelseVurdering: Vurdering = tiltak
            .map { tiltak -> tiltak.vilkårsvurderTiltaksdeltagelse(vurderingsperiode) }
            .map { it.copy(utfall = it.utfall.utvid(Utfall.IKKE_OPPFYLT, vurderingsperiode)) }
            .slåSammenTiltakVurderinger()

        val livsoppholdsytelserVurderinger: List<Vurdering> = saksopplysninger().map {
            it.lagVurdering(vurderingsperiode)
        }

        val samletUtfall: Periodisering<Utfall> = (livsoppholdsytelserVurderinger + deltagelseVurdering) // + alder mm
            .samletUtfall()

        /*
        val utfallsperioder =
            vurderingsperiode.fra.datesUntil(vurderingsperiode.til.plusDays(1)).toList().map { dag ->
                val idag = livsoppholdsytelserVurderinger.filter { dag >= it.fom && dag <= it.tom }
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

         */

        // TODO Barnetillegg gjenstår
        val utfallsperioder: Periodisering<Utfallsdetaljer> = samletUtfall.map {
            Utfallsdetaljer(
                antallBarn = 0,
                utfall = when (it) {
                    Utfall.OPPFYLT -> UtfallForPeriode.GIR_RETT_TILTAKSPENGER
                    Utfall.IKKE_OPPFYLT -> UtfallForPeriode.GIR_IKKE_RETT_TILTAKSPENGER
                    Utfall.KREVER_MANUELL_VURDERING -> UtfallForPeriode.KREVER_MANUELL_VURDERING
                },
            )
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
            vilkårsvurderinger = livsoppholdsytelserVurderinger,
            utfallsperioder = utfallsperioder,
            status = status,
            tilstand = BehandlingTilstand.VILKÅRSVURDERT,
        )
    }

    private fun List<Vurdering>.slåSammenTiltakVurderinger(): Vurdering =
        this.map { it.utfall }.reduser { utfall1, utfall2 ->
            when {
                utfall1 == Utfall.KREVER_MANUELL_VURDERING || utfall2 == Utfall.KREVER_MANUELL_VURDERING -> Utfall.KREVER_MANUELL_VURDERING
                utfall1 == Utfall.OPPFYLT || utfall2 == Utfall.OPPFYLT -> Utfall.OPPFYLT
                else -> Utfall.IKKE_OPPFYLT
            }
        }.let { Vurdering(Vilkår.TILTAKSDELTAGELSE, it, "") }

    private fun List<Vurdering>.samletUtfall(): Periodisering<Utfall> =
        this.map { it.utfall }.reduser { utfall1, utfall2 ->
            when {
                utfall1 == Utfall.IKKE_OPPFYLT || utfall2 == Utfall.IKKE_OPPFYLT -> Utfall.IKKE_OPPFYLT
                utfall1 == Utfall.KREVER_MANUELL_VURDERING || utfall2 == Utfall.KREVER_MANUELL_VURDERING -> Utfall.KREVER_MANUELL_VURDERING
                else -> Utfall.OPPFYLT
            }
        }
}
