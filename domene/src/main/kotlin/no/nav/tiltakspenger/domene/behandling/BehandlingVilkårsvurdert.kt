package no.nav.tiltakspenger.domene.behandling

import mu.KotlinLogging
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.domene.vilkår.Vurdering
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import java.time.LocalDate

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

sealed interface BehandlingVilkårsvurdert : Søknadsbehandling {
    val vilkårsvurderinger: List<Vurdering>
    val utfallsperioder: List<Utfallsperiode>

    override fun søknad(): Søknad {
        return søknader.siste()
    }

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
            saksbehandler: String?,
            utfallsperioder: List<Utfallsperiode>,
        ): BehandlingVilkårsvurdert {
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
                    utfallsperioder = utfallsperioder,
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
                    utfallsperioder = utfallsperioder,
                )

                "Manuell" -> return Manuell(
                    id = id,
                    sakId = sakId,
                    søknader = søknader,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysninger = saksopplysninger,
                    tiltak = tiltak,
                    vilkårsvurderinger = vilkårsvurderinger,
                    saksbehandler = saksbehandler,
                    utfallsperioder = utfallsperioder,
                )

                else -> throw IllegalStateException("Ukjent BehandlingVilkårsvurdert $id med status $status")
            }
        }
    }

    data class Utfallsperiode(
        val fom: LocalDate,
        val tom: LocalDate,
        val antallBarn: Int,
        val tiltak: List<Tiltak>,
        val antDagerMedTiltak: Int,
        val utfall: UtfallForPeriode,
    )

    enum class UtfallForPeriode {
        GIR_RETT_TILTAKSPENGER,
        GIR_IKKE_RETT_TILTAKSPENGER,
        UKJENT,
    }

    data class Innvilget(
        override val id: BehandlingId,
        override val sakId: SakId,
        override val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
        override val saksopplysninger: List<Saksopplysning>,
        override val tiltak: List<Tiltak>,
        override val vilkårsvurderinger: List<Vurdering>,
        override val saksbehandler: String?,
        override val utfallsperioder: List<Utfallsperiode>,
    ) : BehandlingVilkårsvurdert {
        fun iverksett(): BehandlingIverksatt.Innvilget {
            return BehandlingIverksatt.Innvilget(
                id = id,
                sakId = sakId,
                søknader = søknader,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
                tiltak = tiltak,
                vilkårsvurderinger = vilkårsvurderinger,
                saksbehandler = "Automatisk",
                beslutter = "Automatisk",
            )
        }

        fun tilBeslutting(): BehandlingTilBeslutter.Innvilget {
            return BehandlingTilBeslutter.Innvilget(
                id = id,
                sakId = sakId,
                søknader = søknader,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
                tiltak = tiltak,
                vilkårsvurderinger = vilkårsvurderinger,
                saksbehandler = checkNotNull(saksbehandler) { "Ikke lov å sende Behandling til Beslutter uten saksbehandler" },
                beslutter = null,
            )
        }

        override fun erÅpen() = true

        override fun leggTilSøknad(søknad: Søknad): BehandlingVilkårsvurdert {
            return Søknadsbehandling.Opprettet.leggTilSøknad(
                behandling = this,
                søknad = søknad,
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

    data class Avslag(
        override val id: BehandlingId,
        override val sakId: SakId,
        override val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
        override val saksopplysninger: List<Saksopplysning>,
        override val tiltak: List<Tiltak>,
        override val vilkårsvurderinger: List<Vurdering>,
        override val saksbehandler: String?,
        override val utfallsperioder: List<Utfallsperiode>,
    ) : BehandlingVilkårsvurdert {
        fun iverksett(): BehandlingIverksatt.Avslag {
            return BehandlingIverksatt.Avslag(
                id = id,
                sakId = sakId,
                søknader = søknader,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
                tiltak = tiltak,
                vilkårsvurderinger = vilkårsvurderinger,
                saksbehandler = "Automatisk",
                beslutter = "Automatisk",
            )
        }

        fun tilBeslutting(): BehandlingTilBeslutter.Avslag {
            return BehandlingTilBeslutter.Avslag(
                id = id,
                sakId = sakId,
                søknader = søknader,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
                tiltak = tiltak,
                vilkårsvurderinger = vilkårsvurderinger,
                saksbehandler = checkNotNull(saksbehandler) { "Ikke lov å sende Behandling til Beslutter uten saksbehandler" },
                beslutter = null,
            )
        }

        override fun erÅpen() = true

        override fun leggTilSøknad(søknad: Søknad): BehandlingVilkårsvurdert {
            return Søknadsbehandling.Opprettet.leggTilSøknad(
                behandling = this,
                søknad = søknad,
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

    data class Manuell(
        override val id: BehandlingId,
        override val sakId: SakId,
        override val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
        override val saksopplysninger: List<Saksopplysning>,
        override val tiltak: List<Tiltak>,
        override val vilkårsvurderinger: List<Vurdering>,
        override val saksbehandler: String?,
        override val utfallsperioder: List<Utfallsperiode>,
    ) : BehandlingVilkårsvurdert {

        override fun erÅpen() = true

        override fun leggTilSøknad(søknad: Søknad): BehandlingVilkårsvurdert {
            return Søknadsbehandling.Opprettet.leggTilSøknad(
                behandling = this,
                søknad = søknad,
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
