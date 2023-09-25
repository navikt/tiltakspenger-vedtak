package no.nav.tiltakspenger.domene.saksopplysning

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import java.time.LocalDate

class AapTolker {
    companion object {
        fun tolkeData(ytelser: List<YtelseSak>?, periode: Periode): List<Saksopplysning.Aap> {
            if (ytelser == null) {
                return listOf(
                    Saksopplysning.Aap(
                        fom = periode.fra,
                        tom = periode.til,
                        vilkår = Vilkår.AAP,
                        kilde = Kilde.ARENA,
                        detaljer = "",
//                        opphørTidligereSaksopplysning = false,
                        typeSaksopplysning = TypeSaksopplysning.IKKE_INNHENTET_ENDA,
                    ),
                )
            }

            val ytelseListe = ytelser
                .filter {
                    Periode(
                        it.fomGyldighetsperiode.toLocalDate(),
                        (it.tomGyldighetsperiode?.toLocalDate() ?: LocalDate.MAX),
                    ).overlapperMed(periode)
                }
                .filter { it.ytelsestype == YtelseSak.YtelseSakYtelsetype.AA }

            if (ytelseListe.isEmpty()) {
                return listOf(
                    Saksopplysning.Aap(
                        fom = periode.fra,
                        tom = periode.til,
                        vilkår = Vilkår.AAP,
                        kilde = Kilde.ARENA,
                        detaljer = "",
//                        opphørTidligereSaksopplysning = false,
                        typeSaksopplysning = TypeSaksopplysning.HAR_IKKE_YTELSE,
                    ),
                )
            }

            return ytelseListe
                .map {
                    Saksopplysning.Aap(
                        fom = maxOf(periode.fra, it.fomGyldighetsperiode.toLocalDate()),
                        tom = minOf(periode.til, (it.tomGyldighetsperiode?.toLocalDate() ?: LocalDate.MAX)),
                        vilkår = Vilkår.AAP,
                        kilde = Kilde.ARENA,
                        detaljer = "",
//                        opphørTidligereSaksopplysning = false,
                        typeSaksopplysning = TypeSaksopplysning.HAR_YTELSE,
                    )
                }
        }
    }
}
