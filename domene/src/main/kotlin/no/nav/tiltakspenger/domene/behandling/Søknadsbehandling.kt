package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.domene.saksopplysning.lagFaktaFraFraOgMedDatospørsmål
import no.nav.tiltakspenger.domene.saksopplysning.lagFaktaFraJaNeiSpørsmål
import no.nav.tiltakspenger.domene.saksopplysning.lagFaktaFraPeriodespørsmål
import no.nav.tiltakspenger.domene.saksopplysning.lagVurdering
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår

sealed interface Søknadsbehandling : Behandling {
    val søknader: List<Søknad>

    fun søknad(): Søknad {
        return søknader.maxBy { it.opprettet }
    }

    data class Opprettet(
        override val id: BehandlingId,
        override val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
        override val saksopplysninger: List<Saksopplysning>,

    ) : Søknadsbehandling {
        // TODO Vurder om vi skal ha behandlingService som er ansvarlig for å opprette denne,
        //      eller om vi skal beholde denne (eller begge :-) )
        companion object {
            fun fromDb(
                id: BehandlingId,
                søknader: List<Søknad>,
                vurderingsperiode: Periode,
                saksopplysninger: List<Saksopplysning>,
            ): Opprettet {
                return Opprettet(
                    id = id,
                    søknader = søknader,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysninger = saksopplysninger,
                )
            }

            fun opprettBehandling(søknad: Søknad): Opprettet {
                return Opprettet(
                    id = BehandlingId.random(),
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
                            Vilkår.LØNNSINNTEKT,
                            søknad.lønnetArbeid,
                            søknad.vurderingsperiode(),
                        ),
                        lagFaktaFraJaNeiSpørsmål(
                            Vilkår.ETTERLØNN,
                            søknad.etterlønn,
                            søknad.vurderingsperiode(),
                        ),

                        // TODO: Her må vi finne på noe lurt
                        // lagFaktaFraPeriodespørsmål(Vilkår.ALDERSPENSJON, søknad.alderspensjon, søknad.vurderingsperiode()),
                    ),
                )
            }
        }

        fun henteSaksopplysninger() {
            // TODO Skal vi ha det slik?
            // Kanskje dette skal gjøres i en service
        }

//        fun vilkårsvurder(): BehandlingVilkårsvurdert {
//            if (innsending == null) {
//                return BehandlingVilkårsvurdert.Manuell(
//                    id = id,
//                    søknader = søknader,
//                    vurderingsperiode = vurderingsperiode,
//                    saksopplysning = emptyList(),
//                    vilkårsvurderinger = emptyList(),
//                    innsending = innsending,
//                )
//            }
//            val saksopplysning = lagFaktaAvInnsending(innsending)
//
//        }

        //        private fun lagVilkårsvurderingerAvSaksopplysninger(): List<Vurdering> {
//
//        }
        fun vilkårsvurder(saksopplysninger: List<Saksopplysning>): BehandlingVilkårsvurdert {
            // Først lager vi Vurderinger
            // todo Her kan vi vurdere å lage bare en map og ta som en forutsetning at det er en saksopplysning for hvert vilkår
            val vurderinger =
                saksopplysninger.filter { it.vilkår == Vilkår.AAP }.lagVurdering(Vilkår.AAP) +
                    saksopplysninger.filter { it.vilkår == Vilkår.DAGPENGER }.lagVurdering(Vilkår.DAGPENGER) +
                    saksopplysninger.filter { it.vilkår == Vilkår.PLEIEPENGER_NÆRSTÅENDE }
                        .lagVurdering(Vilkår.PLEIEPENGER_NÆRSTÅENDE) +
                    saksopplysninger.filter { it.vilkår == Vilkår.PLEIEPENGER_SYKT_BARN }
                        .lagVurdering(Vilkår.PLEIEPENGER_SYKT_BARN) +
                    saksopplysninger.filter { it.vilkår == Vilkår.FORELDREPENGER }.lagVurdering(Vilkår.FORELDREPENGER) +
                    saksopplysninger.filter { it.vilkår == Vilkår.OPPLÆRINGSPENGER }
                        .lagVurdering(Vilkår.OPPLÆRINGSPENGER) +
                    saksopplysninger.filter { it.vilkår == Vilkår.OMSORGSPENGER }.lagVurdering(Vilkår.OMSORGSPENGER) +
                    saksopplysninger.filter { it.vilkår == Vilkår.ALDER }.lagVurdering(Vilkår.ALDER) +
                    saksopplysninger.filter { it.vilkår == Vilkår.TILTAKSPENGER }.lagVurdering(Vilkår.TILTAKSPENGER) +
                    saksopplysninger.filter { it.vilkår == Vilkår.UFØRETRYGD }.lagVurdering(Vilkår.UFØRETRYGD) +
                    saksopplysninger.filter { it.vilkår == Vilkår.SVANGERSKAPSPENGER }
                        .lagVurdering(Vilkår.SVANGERSKAPSPENGER) +
                    // Legg til flere vurderinger her
                    saksopplysninger.filter { it.vilkår == Vilkår.KVP }.lagVurdering(Vilkår.KVP) +
                    saksopplysninger.filter { it.vilkår == Vilkår.INTROPROGRAMMET }
                        .lagVurdering(Vilkår.INTROPROGRAMMET) +
                    saksopplysninger.filter { it.vilkår == Vilkår.INSTITUSJONSOPPHOLD }
                        .lagVurdering(Vilkår.INSTITUSJONSOPPHOLD) +
                    saksopplysninger.filter { it.vilkår == Vilkår.GJENLEVENDEPENSJON }
                        .lagVurdering(Vilkår.GJENLEVENDEPENSJON) +
                    saksopplysninger.filter { it.vilkår == Vilkår.SYKEPENGER }.lagVurdering(Vilkår.SYKEPENGER) +
                    saksopplysninger.filter { it.vilkår == Vilkår.SUPPLERENDESTØNADALDER }
                        .lagVurdering(Vilkår.SUPPLERENDESTØNADALDER) +
                    saksopplysninger.filter { it.vilkår == Vilkår.SUPPLERENDESTØNADFLYKTNING }
                        .lagVurdering(Vilkår.SUPPLERENDESTØNADFLYKTNING) +
                    saksopplysninger.filter { it.vilkår == Vilkår.JOBBSJANSEN }.lagVurdering(Vilkår.JOBBSJANSEN) +
                    saksopplysninger.filter { it.vilkår == Vilkår.PENSJONSINNTEKT }
                        .lagVurdering(Vilkår.PENSJONSINNTEKT) +
                    saksopplysninger.filter { it.vilkår == Vilkår.LØNNSINNTEKT }.lagVurdering(Vilkår.LØNNSINNTEKT) +
                    saksopplysninger.filter { it.vilkår == Vilkår.ETTERLØNN }.lagVurdering(Vilkår.ETTERLØNN) +
                    saksopplysninger.filter { it.vilkår == Vilkår.ALDERSPENSJON }.lagVurdering(Vilkår.ALDERSPENSJON)

            // Etter at vi har laget vurderinger, sjekker vi utfallet

            if (vurderinger.any { it.utfall == Utfall.KREVER_MANUELL_VURDERING }) {
                return BehandlingVilkårsvurdert.Manuell(
                    id = id,
                    søknader = søknader,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysninger = saksopplysninger,
                    vilkårsvurderinger = vurderinger,
                )
            }
            if (vurderinger.any { it.utfall == Utfall.KREVER_MANUELL_VURDERING }) {
                return BehandlingVilkårsvurdert.Manuell(
                    id = id,
                    søknader = søknader,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysninger = saksopplysninger,
                    vilkårsvurderinger = vurderinger,
                )
            }
            if (vurderinger.all { it.utfall == Utfall.OPPFYLT }) {
                return BehandlingVilkårsvurdert.Innvilget(
                    id = id,
                    søknader = søknader,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysninger = saksopplysninger,
                    vilkårsvurderinger = vurderinger,
                )
            }
            if (vurderinger.all { it.utfall == Utfall.IKKE_OPPFYLT }) {
                return BehandlingVilkårsvurdert.Avslag(
                    id = id,
                    søknader = søknader,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysninger = saksopplysninger,
                    vilkårsvurderinger = vurderinger,
                )
            }
            return BehandlingVilkårsvurdert.Avslag(
                id = id,
                søknader = søknader,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
                vilkårsvurderinger = vurderinger,
            )
        }

//        private fun lagFaktaAvInnsending(innsending: Innsending): List<Saksopplysning> {
//            val saksopplysningDagpenger =
//                Saksopplysning.Dagpenger.lagFakta(innsending.ytelser?.ytelserliste, innsending.filtreringsperiode())
//            val saksopplysningAap =
//                Saksopplysning.Aap.lagSaksopplysninger(
//                    innsending.ytelser?.ytelserliste,
//                    innsending.filtreringsperiode(),
//                )
//            return saksopplysningAap + saksopplysningDagpenger
//        }

        override fun leggTilSaksopplysning(saksopplysning: Saksopplysning): Søknadsbehandling {
            return this.copy(
                saksopplysninger = saksopplysninger + saksopplysning,
            )
        }
    }
}
