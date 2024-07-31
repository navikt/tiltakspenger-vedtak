package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import java.time.LocalDate

interface VurderingMother {
    fun fristForFramsettingAvKravVurdering( // TODO Slett
        utfall: Utfall = Utfall.OPPFYLT,
        fom: LocalDate = 1.januar(2026),
        tom: LocalDate = 31.januar(2026),
    ): Vurdering = Vurdering(
        vilkår = Vilkår.FRIST_FOR_FRAMSETTING_AV_KRAV,
        kilde = Kilde.SØKNAD,
        fom = fom,
        tom = tom,
        grunnlagId = null,
        detaljer = "",
        utfall = utfall,
    )
}
