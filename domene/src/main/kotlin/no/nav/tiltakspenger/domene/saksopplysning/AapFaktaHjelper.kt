package no.nav.tiltakspenger.domene.saksopplysning

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import java.time.LocalDate

class AapFaktaHjelper {
    companion object {
        fun lagFaktaHjelper(ytelser: List<YtelseSak>?, periode: Periode) =
            ytelser
                .orEmpty()
                .filter {
                    Periode(
                        it.fomGyldighetsperiode.toLocalDate(),
                        (it.tomGyldighetsperiode?.toLocalDate() ?: LocalDate.MAX),
                    ).overlapperMed(periode)
                }
                .filter { it.ytelsestype == YtelseSak.YtelseSakYtelsetype.AA }
                .map {
                    Fakta.Aap(
                        fom = periode.fra,
                        tom = periode.til,
                        vilkår = Vilkår.DAGPENGER,
                        kilde = "Arena",
                        detaljer = "",
                    )
                }
    }
}
