package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import java.time.LocalDate

class KVPVilkårsvurdering(
    private val søknad: Søknad,
    private val vurderingsperiode: Periode //TODO: ikke i bruk, fjerne?
) {
    private val søknadVurdering = lagSøknadVurdering()

    private var manuellVurdering: Vurdering? = null

    private fun lagSøknadVurdering() = Vurdering(
        kilde = "Søknad",
        fom = null,
        tom = null,
        utfall = avgjørUtfall()
    )

    private fun avgjørUtfall() = if (søknad.deltarKvp) Utfall.KREVER_MANUELL_VURDERING else Utfall.OPPFYLT

    fun vurderinger(): List<Vurdering> = listOfNotNull(søknadVurdering, manuellVurdering)

    fun settManuellVurdering(fom: LocalDate, tom: LocalDate, utfall: Utfall) {
        manuellVurdering = Vurdering(kilde = "Saksbehandler", fom = fom, tom = tom, utfall = utfall)
    }

    fun samletUtfall() = manuellVurdering?.utfall ?: søknadVurdering.utfall
}
