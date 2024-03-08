package no.nav.tiltakspenger.saksbehandling.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.saksbehandling.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.saksopplysning.lagFaktaFraFraOgMedDatospørsmål
import no.nav.tiltakspenger.saksbehandling.saksopplysning.lagFaktaFraJaNeiSpørsmål
import no.nav.tiltakspenger.saksbehandling.saksopplysning.lagFaktaFraPeriodespørsmål
import no.nav.tiltakspenger.saksbehandling.vilkår.Vilkår

data class LeggTilSaksopplysningRespons(
    val behandling: no.nav.tiltakspenger.saksbehandling.behandling.Søknadsbehandling,
    val erEndret: Boolean,
)

interface Behandling {
    val id: BehandlingId
    val sakId: SakId
    val vurderingsperiode: Periode
    val saksopplysninger: List<Saksopplysning>
    val tiltak: List<no.nav.tiltakspenger.saksbehandling.behandling.Tiltak>
    val saksbehandler: String?

    fun saksopplysninger(): List<Saksopplysning> {
        return saksopplysninger.groupBy { it.vilkår }.map { entry ->
            entry.value.reduce { acc, saksopplysning ->
                if (saksopplysning.kilde == Kilde.SAKSB) saksopplysning else acc
            }
        }
    }

    fun erÅpen(): Boolean = false
    fun erIverksatt(): Boolean = false
    fun erTilBeslutter(): Boolean = false

    fun leggTilSøknad(søknad: no.nav.tiltakspenger.saksbehandling.behandling.Søknad): no.nav.tiltakspenger.saksbehandling.behandling.BehandlingVilkårsvurdert {
        throw IllegalStateException("Kan ikke legge til søknad på denne behandlingen")
    }

    fun leggTilSaksopplysning(saksopplysning: Saksopplysning): no.nav.tiltakspenger.saksbehandling.behandling.LeggTilSaksopplysningRespons {
        throw IllegalStateException("Kan ikke legge til saksopplysning på denne behandlingen")
    }

    fun oppdaterTiltak(tiltak: List<no.nav.tiltakspenger.saksbehandling.behandling.Tiltak>): no.nav.tiltakspenger.saksbehandling.behandling.Søknadsbehandling {
        throw IllegalStateException("Kan ikke oppdatere tiltak på denne behandlingen")
    }

    fun startBehandling(saksbehandler: String): no.nav.tiltakspenger.saksbehandling.behandling.Søknadsbehandling {
        throw IllegalStateException("Kan ikke starte en behandling med denne statusen")
    }

    fun avbrytBehandling(): no.nav.tiltakspenger.saksbehandling.behandling.Søknadsbehandling {
        throw IllegalStateException("Kan ikke avbryte en behandling med denne statusen")
    }
}

fun List<Saksopplysning>.oppdaterSaksopplysninger(saksopplysning: Saksopplysning) =
    if (saksopplysning.kilde != Kilde.SAKSB) {
        if (this.first { it.vilkår == saksopplysning.vilkår && it.kilde != Kilde.SAKSB } == saksopplysning) {
            this.filterNot { it.vilkår == saksopplysning.vilkår && it.kilde != Kilde.SAKSB }
        } else {
            this.filterNot { it.vilkår == saksopplysning.vilkår }
        }
    } else {
        this.filterNot { it.vilkår == saksopplysning.vilkår && it.kilde == Kilde.SAKSB }
    }.plus(saksopplysning)

fun lagFaktaAvSøknad(søknad: no.nav.tiltakspenger.saksbehandling.behandling.Søknad): List<Saksopplysning> {
    return listOf(
        lagFaktaFraPeriodespørsmål(Vilkår.KVP, søknad.kvp, søknad.vurderingsperiode()),
        lagFaktaFraPeriodespørsmål(Vilkår.INTROPROGRAMMET, søknad.intro, søknad.vurderingsperiode()),
        lagFaktaFraPeriodespørsmål(
            Vilkår.INSTITUSJONSOPPHOLD,
            søknad.institusjon,
            søknad.vurderingsperiode(),
        ),
        lagFaktaFraPeriodespørsmål(
            Vilkår.GJENLEVENDEPENSJON,
            søknad.gjenlevendepensjon,
            søknad.vurderingsperiode(),
        ),
        lagFaktaFraPeriodespørsmål(Vilkår.SYKEPENGER, søknad.sykepenger, søknad.vurderingsperiode()),
        lagFaktaFraPeriodespørsmål(
            Vilkår.SUPPLERENDESTØNADALDER,
            søknad.supplerendeStønadAlder,
            søknad.vurderingsperiode(),
        ),
        lagFaktaFraPeriodespørsmål(
            Vilkår.SUPPLERENDESTØNADFLYKTNING,
            søknad.supplerendeStønadFlyktning,
            søknad.vurderingsperiode(),
        ),
        lagFaktaFraPeriodespørsmål(Vilkår.JOBBSJANSEN, søknad.jobbsjansen, søknad.vurderingsperiode()),
        lagFaktaFraPeriodespørsmål(
            Vilkår.PENSJONSINNTEKT,
            søknad.trygdOgPensjon,
            søknad.vurderingsperiode(),
        ),
        lagFaktaFraFraOgMedDatospørsmål(
            Vilkår.ALDERSPENSJON,
            søknad.alderspensjon,
            søknad.vurderingsperiode(),
        ),
        lagFaktaFraJaNeiSpørsmål(
            Vilkår.ETTERLØNN,
            søknad.etterlønn,
            søknad.vurderingsperiode(),
        ),
    )
}

fun lagFaktaInit(søknad: no.nav.tiltakspenger.saksbehandling.behandling.Søknad) =
    listOf(
        Saksopplysning.initFakta(søknad.vurderingsperiode(), Vilkår.DAGPENGER),
        Saksopplysning.initFakta(søknad.vurderingsperiode(), Vilkår.AAP),
        Saksopplysning.initFakta(søknad.vurderingsperiode(), Vilkår.PLEIEPENGER_NÆRSTÅENDE),
        Saksopplysning.initFakta(søknad.vurderingsperiode(), Vilkår.PLEIEPENGER_SYKT_BARN),
        Saksopplysning.initFakta(søknad.vurderingsperiode(), Vilkår.FORELDREPENGER),
        Saksopplysning.initFakta(søknad.vurderingsperiode(), Vilkår.OPPLÆRINGSPENGER),
        Saksopplysning.initFakta(søknad.vurderingsperiode(), Vilkår.OMSORGSPENGER),
        Saksopplysning.initFakta(søknad.vurderingsperiode(), Vilkår.ALDER),
        Saksopplysning.initFakta(søknad.vurderingsperiode(), Vilkår.TILTAKSPENGER),
        Saksopplysning.initFakta(søknad.vurderingsperiode(), Vilkår.UFØRETRYGD),
        Saksopplysning.initFakta(søknad.vurderingsperiode(), Vilkår.SVANGERSKAPSPENGER),
    )

fun List<no.nav.tiltakspenger.saksbehandling.behandling.Søknad>.siste() = this.maxBy { it.opprettet }.copy(opprettet = this.minBy { it.opprettet }.opprettet)
