package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.domene.saksopplysning.lagFaktaFraFraOgMedDatospørsmål
import no.nav.tiltakspenger.domene.saksopplysning.lagFaktaFraJaNeiSpørsmål
import no.nav.tiltakspenger.domene.saksopplysning.lagFaktaFraPeriodespørsmål
import no.nav.tiltakspenger.domene.saksopplysning.lagVurdering
import no.nav.tiltakspenger.domene.vilkår.Utfall
import no.nav.tiltakspenger.domene.vilkår.Vilkår
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId

sealed interface Søknadsbehandling : Behandling {
    val søknader: List<Søknad>

    fun søknad(): Søknad {
        return søknader.maxBy { it.opprettet }
    }

    data class Opprettet(
        override val id: BehandlingId,
        override val sakId: SakId,
        override val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
        override val saksopplysninger: List<Saksopplysning>,
        override val tiltak: List<Tiltak>,
        override val saksbehandler: String?,

    ) : Søknadsbehandling {
        // TODO Vurder om vi skal ha behandlingService som er ansvarlig for å opprette denne,
        //      eller om vi skal beholde denne (eller begge :-) )
        companion object {
            fun fromDb(
                id: BehandlingId,
                sakId: SakId,
                søknader: List<Søknad>,
                vurderingsperiode: Periode,
                saksopplysninger: List<Saksopplysning>,
                tiltak: List<Tiltak>,
                saksbehandler: String?,
            ): Opprettet {
                return Opprettet(
                    id = id,
                    sakId = sakId,
                    søknader = søknader,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysninger = saksopplysninger,
                    tiltak = tiltak,
                    saksbehandler = saksbehandler,
                )
            }

            fun opprettBehandling(sakId: SakId, søknad: Søknad): Opprettet {
                return Opprettet(
                    id = BehandlingId.random(),
                    sakId = sakId,
                    søknader = listOf(søknad),
                    vurderingsperiode = søknad.vurderingsperiode(),
                    saksopplysninger = listOf(
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
                    ) + lagFaktaAvSøknad(søknad),
                    tiltak = emptyList(),
                    saksbehandler = null,
                )
            }
        }

        fun vilkårsvurder(): BehandlingVilkårsvurdert {
            // Først lager vi Vurderinger
            // todo Her kan vi vurdere å lage bare en map og ta som en forutsetning at det er en saksopplysning for hvert vilkår

            val vurderinger = saksopplysninger().flatMap {
                it.lagVurdering(vurderingsperiode)
            }

            // Etter at vi har laget vurderinger, sjekker vi utfallet

            if (vurderinger.any { it.utfall == Utfall.KREVER_MANUELL_VURDERING }) {
                return BehandlingVilkårsvurdert.Manuell(
                    id = id,
                    sakId = sakId,
                    søknader = søknader,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysninger = saksopplysninger,
                    tiltak = tiltak,
                    vilkårsvurderinger = vurderinger,
                    saksbehandler = saksbehandler,
                )
            }
            if (vurderinger.any { it.utfall == Utfall.KREVER_MANUELL_VURDERING }) {
                return BehandlingVilkårsvurdert.Manuell(
                    id = id,
                    sakId = sakId,
                    søknader = søknader,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysninger = saksopplysninger,
                    tiltak = tiltak,
                    vilkårsvurderinger = vurderinger,
                    saksbehandler = saksbehandler,
                )
            }
            if (vurderinger.all { it.utfall == Utfall.OPPFYLT }) {
                return BehandlingVilkårsvurdert.Innvilget(
                    id = id,
                    sakId = sakId,
                    søknader = søknader,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysninger = saksopplysninger,
                    tiltak = tiltak,
                    vilkårsvurderinger = vurderinger,
                    saksbehandler = saksbehandler,
                )
            }
            if (vurderinger.all { it.utfall == Utfall.IKKE_OPPFYLT }) {
                return BehandlingVilkårsvurdert.Avslag(
                    id = id,
                    sakId = sakId,
                    søknader = søknader,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysninger = saksopplysninger,
                    tiltak = tiltak,
                    vilkårsvurderinger = vurderinger,
                    saksbehandler = saksbehandler,
                )
            }
            return BehandlingVilkårsvurdert.Avslag(
                id = id,
                sakId = sakId,
                søknader = søknader,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
                tiltak = tiltak,
                vilkårsvurderinger = vurderinger,
                saksbehandler = saksbehandler,
            )
        }

        override fun leggTilSøknad(søknad: Søknad): BehandlingVilkårsvurdert {
            return Opprettet(
                id = id,
                sakId = sakId,
                søknader = søknader + søknad,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
                tiltak = tiltak,
                saksbehandler = saksbehandler,
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

        override fun oppdaterTiltak(tiltak: List<Tiltak>): Søknadsbehandling =
            this.copy(
                tiltak = tiltak,
            )

        override fun startBehandling(saksbehandler: String): Søknadsbehandling =
            this.copy(
                saksbehandler = saksbehandler,
            )

        override fun avbrytBehandling(): Søknadsbehandling =
            this.copy(
                saksbehandler = null,
            )
    }
}

private fun lagFaktaAvSøknad(søknad: Søknad): List<Saksopplysning> {
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
