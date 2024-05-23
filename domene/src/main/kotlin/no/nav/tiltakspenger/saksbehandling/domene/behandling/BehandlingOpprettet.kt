package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.Systembruker
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.libs.periodisering.Periodisering.Companion.reduser
import no.nav.tiltakspenger.saksbehandling.domene.barnetillegg.BarnetilleggVilkårData
import no.nav.tiltakspenger.saksbehandling.domene.barnetillegg.UtfallForBarnetilleggPeriode
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysninger.oppdaterSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import java.time.LocalDate

data class BehandlingOpprettet(
    override val id: BehandlingId,
    override val sakId: SakId,
    override val søknader: List<Søknad>,
    override val vurderingsperiode: Periode,
    override val saksopplysninger: List<Saksopplysning>,
    override val tiltak: List<Tiltak>,
    override val barnetillegg: BarnetilleggVilkårData,
    override val saksbehandler: String?,
    override val utfallsperioder: List<Utfallsperiode> = emptyList(),
) : Førstegangsbehandling {

    companion object {

        fun opprettBehandling(sakId: SakId, søknad: Søknad): BehandlingOpprettet {
            return BehandlingOpprettet(
                id = BehandlingId.random(),
                sakId = sakId,
                søknader = listOf(søknad),
                vurderingsperiode = søknad.vurderingsperiode(),
                saksopplysninger = Saksopplysninger.initSaksopplysningerFraSøknad(søknad) + Saksopplysninger.lagSaksopplysningerAvSøknad(
                    søknad,
                ),
                tiltak = emptyList(),
                barnetillegg = BarnetilleggVilkårData(søknad.vurderingsperiode()),
                saksbehandler = null,
            )
        }
    }

    override fun leggTilSøknad(søknad: Søknad): BehandlingVilkårsvurdert {
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
            barnetillegg = barnetillegg.oppdaterSøknad(søknad, Systembruker("TODO", emptyList())), // TODO
        ).vilkårsvurder()
    }

    override fun leggTilSaksopplysning(saksopplysning: Saksopplysning): LeggTilSaksopplysningRespons {
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

    override fun oppdaterTiltak(tiltak: List<Tiltak>): Førstegangsbehandling =
        this.copy(tiltak = tiltak)

    override fun startBehandling(saksbehandler: Saksbehandler): Førstegangsbehandling {
        check(this.saksbehandler == null) { "Denne behandlingen er allerede tatt" }
        check(saksbehandler.isSaksbehandler()) { "Saksbehandler må være saksbehandler" }
        return this.copy(saksbehandler = saksbehandler.navIdent)
    }

    override fun avbrytBehandling(saksbehandler: Saksbehandler): Førstegangsbehandling {
        check(saksbehandler.isSaksbehandler() || saksbehandler.isAdmin()) { "Kan ikke avbryte en behandling som ikke er din" }
        return this.copy(saksbehandler = null)
    }

    fun vilkårsvurder(): BehandlingVilkårsvurdert {
        val vurderinger: List<Vurdering> = vurderinger(this.vurderingsperiode, this.saksopplysninger)

        val utfallsperioder: List<Utfallsperiode> = utfallsperioder(
            vurderingsperiode = this.vurderingsperiode,
            barnetillegg = this.barnetillegg,
            vurderinger = vurderinger,
        )

        val status = status(this.utfallsperioder)

        return BehandlingVilkårsvurdert(
            id = id,
            sakId = sakId,
            søknader = søknader,
            vurderingsperiode = vurderingsperiode,
            saksopplysninger = saksopplysninger,
            tiltak = tiltak,
            barnetillegg = barnetillegg,
            vilkårsvurderinger = vurderinger,
            saksbehandler = saksbehandler,
            utfallsperioder = utfallsperioder,
            status = status,
        )
    }

    private fun status(utfallsperioder: List<Utfallsperiode>): BehandlingStatus =
        if (utfallsperioder.any { it.utfall == UtfallForPeriode.KREVER_MANUELL_VURDERING }) {
            BehandlingStatus.Manuell
        } else if (utfallsperioder.any { it.utfall == UtfallForPeriode.GIR_RETT_TILTAKSPENGER }) {
            BehandlingStatus.Innvilget
        } else {
            BehandlingStatus.Avslag
        }

    private fun vurderinger(vurderingsperiode: Periode, saksopplysninger: List<Saksopplysning>): List<Vurdering> =
        saksopplysninger.flatMap {
            it.lagVurdering(vurderingsperiode)
        }

    private fun utfallsperioder(
        vurderingsperiode: Periode,
        barnetillegg: BarnetilleggVilkårData,
        vurderinger: List<Vurdering>,
    ): List<Utfallsperiode> {
        data class UtfallOgAntallBarn(
            val utfall: UtfallForPeriode,
            val antallBarn: Int,
        )

        val utfallsperioderUtenAntallBarn: Periodisering<UtfallForPeriode> = vurderinger
            .map { vurdering ->
                Periodisering(Utfall.KREVER_MANUELL_VURDERING, vurderingsperiode).setVerdiForDelPeriode(
                    vurdering.utfall,
                    Periode(vurdering.fom ?: LocalDate.MIN, vurdering.tom ?: LocalDate.MAX),
                )
            }
            .reduser { utfall1, utfall2 ->
                when {
                    utfall1 == Utfall.IKKE_OPPFYLT || utfall2 == Utfall.IKKE_OPPFYLT -> Utfall.IKKE_OPPFYLT
                    utfall1 == Utfall.KREVER_MANUELL_VURDERING || utfall2 == Utfall.KREVER_MANUELL_VURDERING -> Utfall.KREVER_MANUELL_VURDERING
                    else -> Utfall.OPPFYLT
                }
            }.kombiner(barnetillegg.samletVurdering) { utfallYtelser, utfallBarnetillegg ->
                when {
                    utfallBarnetillegg == UtfallForBarnetilleggPeriode.KREVER_MANUELL_VURDERING -> UtfallForPeriode.KREVER_MANUELL_VURDERING
                    utfallYtelser == Utfall.OPPFYLT -> UtfallForPeriode.GIR_RETT_TILTAKSPENGER
                    utfallYtelser == Utfall.IKKE_OPPFYLT -> UtfallForPeriode.GIR_IKKE_RETT_TILTAKSPENGER
                    else -> UtfallForPeriode.KREVER_MANUELL_VURDERING
                }
            }
        val perioderAntallBarn = barnetillegg.antallBarnPeriodisering

        return utfallsperioderUtenAntallBarn
            .kombiner(perioderAntallBarn) { utfall, antallBarn ->
                UtfallOgAntallBarn(utfall, antallBarn)
            }
            .perioder()
            .map { Utfallsperiode(it.periode.fra, it.periode.til, it.verdi.antallBarn, it.verdi.utfall) }
    }
}
