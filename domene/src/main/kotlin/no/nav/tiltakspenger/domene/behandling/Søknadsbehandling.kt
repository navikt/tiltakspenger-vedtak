package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
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
//        override val saksopplysningerFraFakta: List<Saksopplysning>,
//        override val saksopplysningerFraSaksbehandler: List<Saksopplysning>,
//        override val saksopplysningerFraSøknad: List<Saksopplysning>,
    ) : Søknadsbehandling {
        // TODO Vurder om vi skal ha behandlingService som er ansvarlig for å opprette denne,
        //      eller om vi skal beholde denne (eller begge :-) )
        companion object {
            fun opprettBehandling(søknad: Søknad): Opprettet {
                return Opprettet(
                    id = BehandlingId.random(),
                    søknader = listOf(søknad),
                    vurderingsperiode = søknad.vurderingsperiode(),
                    saksopplysninger = listOf(
                        Saksopplysning.initFakta(søknad.vurderingsperiode(), Vilkår.DAGPENGER),
                        Saksopplysning.initFakta(søknad.vurderingsperiode(), Vilkår.AAP),
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
                    saksopplysninger.filter { it.vilkår == Vilkår.DAGPENGER }.lagVurdering(Vilkår.DAGPENGER)

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
