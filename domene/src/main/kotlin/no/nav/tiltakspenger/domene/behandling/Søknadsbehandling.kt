package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.domene.saksopplysning.lagVurdering
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Innsending
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.toDTO
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår

sealed interface Søknadsbehandling : Behandling {
    val søknader: List<Søknad>

    fun søknad(): Søknad {
        return søknader.maxBy { it.opprettet }
    }

    override fun toDTO(): BehandlingDTO {
        return BehandlingDTO(
            behandlingId = this.id.toString(),
            fom = this.vurderingsperiode.fra,
            tom = this.vurderingsperiode.til,
            søknad = this.søknad().toDTO(),
            saksopplysninger = this.saksopplysninger,
            vurderinger = emptyList(),
            personopplysninger = PersonopplysningerDTO(
                ident = "12345678901",
                fornavn = "Ola",
                etternavn = "Nordmann",
                skjerming = true,
                strengtFortrolig = true,
                fortrolig = true,
            ),
        )
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
                        Saksopplysning.Dagpenger.initSaksopplysning(søknad.vurderingsperiode()),
                        Saksopplysning.Aap.initSaksopplysning(søknad.vurderingsperiode()),
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
            val vurderinger =
                saksopplysninger.filterIsInstance<Saksopplysning.Aap>().lagVurdering(Vilkår.AAP) +
                    saksopplysninger.filterIsInstance<Saksopplysning.Dagpenger>().lagVurdering(Vilkår.DAGPENGER)

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

        private fun lagFaktaAvInnsending(innsending: Innsending): List<Saksopplysning> {
            val saksopplysningDagpenger =
                Saksopplysning.Dagpenger.lagFakta(innsending.ytelser?.ytelserliste, innsending.filtreringsperiode())
            val saksopplysningAap =
                Saksopplysning.Aap.lagSaksopplysninger(
                    innsending.ytelser?.ytelserliste,
                    innsending.filtreringsperiode(),
                )
            return saksopplysningAap + saksopplysningDagpenger
        }

        override fun leggTilSaksopplysning(saksopplysning: Saksopplysning): Søknadsbehandling {
            return this.copy(
                saksopplysninger = saksopplysninger + saksopplysning,
            )
        }
    }
}
