package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.domene.vilkår.Utfall
import no.nav.tiltakspenger.domene.vilkår.Vilkår
import no.nav.tiltakspenger.domene.vilkår.Vurdering
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId

sealed interface BehandlingTilBeslutter : Søknadsbehandling {
    val vilkårsvurderinger: List<Vurdering>
    val beslutter: String?

    override fun søknad(): Søknad {
        return søknader.maxBy { it.opprettet }
    }

    // Denne kan sikkert generaliseres for alle søknadsbehandlinger med vilkårsvurderinger
    fun hentUtfallForVilkår(vilkår: Vilkår): Utfall {
        if (vilkårsvurderinger.any { it.vilkår == vilkår && it.utfall == Utfall.KREVER_MANUELL_VURDERING }) return Utfall.KREVER_MANUELL_VURDERING
        if (vilkårsvurderinger.any { it.vilkår == vilkår && it.utfall == Utfall.IKKE_OPPFYLT }) return Utfall.IKKE_OPPFYLT
        if (vilkårsvurderinger.filter { it.vilkår == vilkår }.all { it.utfall == Utfall.OPPFYLT }) return Utfall.OPPFYLT
        throw IllegalStateException("Kunne ikke finne utfall for vilkår $vilkår")
    }

    fun iverksett(): BehandlingIverksatt

    fun sendTilbake(): BehandlingVilkårsvurdert

    fun vurderPåNytt(): BehandlingVilkårsvurdert {
        return Søknadsbehandling.Opprettet(
            id = id,
            sakId = sakId,
            søknader = søknader,
            vurderingsperiode = vurderingsperiode,
            saksopplysninger = saksopplysninger,
            tiltak = tiltak,
            saksbehandler = saksbehandler,
        ).vilkårsvurder()
    }

    companion object {
        fun fromDb(
            id: BehandlingId,
            sakId: SakId,
            søknader: List<Søknad>,
            vurderingsperiode: Periode,
            saksopplysninger: List<Saksopplysning>,
            tiltak: List<Tiltak>,
            vilkårsvurderinger: List<Vurdering>,
            status: String,
            saksbehandler: String,
            beslutter: String?,
        ): BehandlingTilBeslutter {
            when (status) {
                "Innvilget" -> return Innvilget(
                    id = id,
                    sakId = sakId,
                    søknader = søknader,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysninger = saksopplysninger,
                    tiltak = tiltak,
                    vilkårsvurderinger = vilkårsvurderinger,
                    saksbehandler = saksbehandler,
                    beslutter = beslutter,
                )

                "Avslag" -> return Avslag(
                    id = id,
                    sakId = sakId,
                    søknader = søknader,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysninger = saksopplysninger,
                    tiltak = tiltak,
                    vilkårsvurderinger = vilkårsvurderinger,
                    saksbehandler = saksbehandler,
                    beslutter = beslutter,
                )

                else -> throw IllegalStateException("Ukjent BehandlingTilBeslutting $id med status $status")
            }
        }
    }

    data class Innvilget(
        override val id: BehandlingId,
        override val sakId: SakId,
        override val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
        override val saksopplysninger: List<Saksopplysning>,
        override val tiltak: List<Tiltak>,
        override val vilkårsvurderinger: List<Vurdering>,
        override val saksbehandler: String,
        override val beslutter: String?,
    ) : BehandlingTilBeslutter {
        override fun iverksett(): BehandlingIverksatt.Innvilget {
            checkNotNull(beslutter) { "Ikke lov å iverksette uten beslutter" }
            return BehandlingIverksatt.Innvilget(
                id = id,
                sakId = sakId,
                søknader = søknader,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
                tiltak = tiltak,
                vilkårsvurderinger = vilkårsvurderinger,
                saksbehandler = saksbehandler,
                beslutter = beslutter,
            )
        }

        override fun erTilBeslutter() = true

        override fun sendTilbake(): BehandlingVilkårsvurdert.Innvilget {
            return BehandlingVilkårsvurdert.Innvilget(
                id = id,
                sakId = sakId,
                søknader = søknader,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
                tiltak = tiltak,
                vilkårsvurderinger = vilkårsvurderinger,
                saksbehandler = saksbehandler,
            )
        }

        override fun leggTilSøknad(søknad: Søknad): BehandlingVilkårsvurdert {
            return Søknadsbehandling.Opprettet(
                id = id,
                sakId = sakId,
                søknader = søknader + søknad,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger.filterNot { it.kilde == Kilde.SØKNAD } + lagFaktaAvSøknad(søknad),
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
                    behandling = this.copy(saksopplysninger = oppdatertSaksopplysningListe).vurderPåNytt(),
                    erEndret = true,
                )
            }
        }

        override fun startBehandling(saksbehandler: String): Søknadsbehandling =
            this.copy(
                beslutter = saksbehandler,
            )
    }

    data class Avslag(
        override val id: BehandlingId,
        override val sakId: SakId,
        override val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
        override val saksopplysninger: List<Saksopplysning>,
        override val tiltak: List<Tiltak>,
        override val vilkårsvurderinger: List<Vurdering>,
        override val saksbehandler: String,
        override val beslutter: String?,
    ) : BehandlingTilBeslutter {
        override fun iverksett(): BehandlingIverksatt.Avslag {
            checkNotNull(beslutter) { "Ikke lov å iverksette uten beslutter" }
            return BehandlingIverksatt.Avslag(
                id = id,
                sakId = sakId,
                søknader = søknader,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
                tiltak = tiltak,
                vilkårsvurderinger = vilkårsvurderinger,
                saksbehandler = saksbehandler,
                beslutter = beslutter,
            )
        }

        override fun erTilBeslutter() = true

        override fun leggTilSøknad(søknad: Søknad): BehandlingVilkårsvurdert {
            return Søknadsbehandling.Opprettet(
                id = id,
                sakId = sakId,
                søknader = søknader + søknad,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger.filterNot { it.kilde == Kilde.SØKNAD } + lagFaktaAvSøknad(søknad),
                tiltak = tiltak,
                saksbehandler = saksbehandler,
            ).vilkårsvurder()
        }

        override fun sendTilbake(): BehandlingVilkårsvurdert.Avslag {
            return BehandlingVilkårsvurdert.Avslag(
                id = id,
                sakId = sakId,
                søknader = søknader,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
                tiltak = tiltak,
                vilkårsvurderinger = vilkårsvurderinger,
                saksbehandler = saksbehandler,
            )
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
                    behandling = this.copy(saksopplysninger = oppdatertSaksopplysningListe).vurderPåNytt(),
                    erEndret = true,
                )
            }
        }

        override fun startBehandling(saksbehandler: String): Søknadsbehandling =
            this.copy(
                beslutter = saksbehandler,
            )
    }
}
