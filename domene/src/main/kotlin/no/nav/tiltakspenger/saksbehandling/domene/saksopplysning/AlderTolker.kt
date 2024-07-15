package no.nav.tiltakspenger.saksbehandling.domene.saksopplysning

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import java.time.LocalDate

// Vet ikke om dette er riktig plassering av denne klassen, men den skal vel uansett dø?
class AlderTolker {
    companion object {
        fun tolkeData(fdato: LocalDate, periode: Periode): List<Saksopplysning> =
            fdato.plusYears(18).let {
                if (periode.inneholder(it)) {
                    listOf(
                        Saksopplysning(
                            fom = periode.fraOgMed,
                            tom = it.minusDays(1),
                            kilde = Kilde.PDL,
                            vilkår = Vilkår.ALDER,
                            detaljer = "",
                            typeSaksopplysning = TypeSaksopplysning.HAR_YTELSE,
                            saksbehandler = null,
                        ),
                        Saksopplysning(
                            fom = it,
                            tom = periode.tilOgMed,
                            kilde = Kilde.PDL,
                            vilkår = Vilkår.ALDER,
                            detaljer = "",
                            typeSaksopplysning = TypeSaksopplysning.HAR_IKKE_YTELSE,
                            saksbehandler = null,
                        ),
                    )
                } else {
                    if (periode.før(it)) {
                        listOf(
                            Saksopplysning(
                                fom = periode.fraOgMed,
                                tom = periode.tilOgMed,
                                kilde = Kilde.PDL,
                                vilkår = Vilkår.ALDER,
                                detaljer = "",
                                typeSaksopplysning = TypeSaksopplysning.HAR_YTELSE,
                                saksbehandler = null,
                            ),
                        )
                    } else {
                        listOf(
                            Saksopplysning(
                                fom = periode.fraOgMed,
                                tom = periode.tilOgMed,
                                kilde = Kilde.PDL,
                                vilkår = Vilkår.ALDER,
                                detaljer = "",
                                typeSaksopplysning = TypeSaksopplysning.HAR_IKKE_YTELSE,
                                saksbehandler = null,
                            ),
                        )
                    }
                }
            }
    }
}
