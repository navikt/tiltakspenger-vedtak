package no.nav.tiltakspenger.saksbehandling.domene.saksopplysning

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import java.time.LocalDate
import java.time.LocalDateTime

data class SøknadTidspunktSaksopplysning(
    override val kilde: Kilde,
    override val vilkår: Vilkår,
    override val detaljer: String,
    override val saksbehandler: String? = null,
    val søknadstidspunkt: LocalDateTime,
) : SaksopplysningInterface

fun List<SøknadTidspunktSaksopplysning>.vilkårsvurder(vurderingsperiode: Periode): List<Vurdering> {
    check(this.size == 1) { "Kan bare ha ett søknadstidspunkt" }
    return this.first().let {
        val sisteTilDatoSomGirRettTilÅSøke = LocalDate.of(it.søknadstidspunkt.year, it.søknadstidspunkt.month, 1).minusMonths(3)
        listOf(
            Vurdering(
                vilkår = it.vilkår,
                kilde = it.kilde,
                fom = vurderingsperiode.fra,
                tom = vurderingsperiode.til,
                utfall = if (vurderingsperiode.til.isAfter(sisteTilDatoSomGirRettTilÅSøke.minusDays(1))) Utfall.OPPFYLT else Utfall.IKKE_OPPFYLT,
                detaljer = "",
            ),
        )
    }
}
