package no.nav.tiltakspenger.saksbehandling.domene.saksopplysning

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Tiltak
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering

data class TiltakSaksopplysning(
    override val kilde: Kilde,
    override val vilkår: Vilkår,
    override val detaljer: String,
    override val saksbehandler: String?,
    val tiltaksdeltagelser: List<Tiltak>,
) : SaksopplysningInterface {
    fun erTom(): Boolean {
        return tiltaksdeltagelser.isEmpty()
    }

    fun vilkårsvurder(): List<Vurdering> =
        tiltaksdeltagelser.map { tiltak ->
            Vurdering(
                vilkår = Vilkår.TILTAKDELTAKELSE,
                kilde = Kilde.ARENA,
                fom = tiltak.deltakelseFom,
                tom = tiltak.deltakelseTom,
                // todo: Vilkårsvurdering av tiltaksdeltagelsene
                utfall = Utfall.OPPFYLT,
                detaljer = detaljer,
            )
        }
}
