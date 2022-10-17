package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import java.time.LocalDate

abstract class KommunalYtelseVilkårsvurdering(
    protected val søknad: Søknad,
    protected val vurderingsperiode: Periode,
    val lovreferanse: Lovreferanse
) {
    companion object {
        const val KILDE = "Søknad"
    }

    private val søknadVurdering = this.lagVurderingFraSøknad()
    private var manuellVurdering: Vurdering? = null

    protected abstract fun lagVurderingFraSøknad(): Vurdering

    protected abstract fun avgjørUtfall(): Utfall

    fun vurderinger(): List<Vurdering> = listOfNotNull(søknadVurdering, manuellVurdering)

    fun samletUtfall() = manuellVurdering?.utfall ?: søknadVurdering.utfall

    fun settManuellVurdering(fom: LocalDate, tom: LocalDate, utfall: Utfall, detaljer: String) {
        manuellVurdering = Vurdering(
            kilde = "Saksbehandler",
            fom = fom,
            tom = tom,
            utfall = utfall,
            detaljer = detaljer
        )
    }
}
