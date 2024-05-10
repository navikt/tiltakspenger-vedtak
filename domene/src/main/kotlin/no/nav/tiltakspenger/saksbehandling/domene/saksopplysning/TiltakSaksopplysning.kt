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
    val tiltak: Tiltak,
) : SaksopplysningInterface {
    fun vilkårsvurder(): Vurdering =
        Vurdering(
            vilkår = Vilkår.TILTAKDELTAKELSE,
            kilde = Kilde.ARENA,
            fom = tiltak.deltakelseFom,
            tom = tiltak.deltakelseTom,
            utfall = if (tiltak.gjennomføring.rettPåTiltakspenger) Utfall.OPPFYLT else Utfall.IKKE_OPPFYLT,
            detaljer = detaljer,
        )
}
