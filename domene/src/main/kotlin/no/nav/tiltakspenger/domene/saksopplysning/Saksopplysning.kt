package no.nav.tiltakspenger.domene.saksopplysning

import no.nav.tiltakspenger.domene.behandling.Søknad
import no.nav.tiltakspenger.domene.saksopplysning.TypeSaksopplysning.HAR_IKKE_YTELSE
import no.nav.tiltakspenger.domene.saksopplysning.TypeSaksopplysning.HAR_YTELSE
import no.nav.tiltakspenger.domene.vilkår.Vilkår
import no.nav.tiltakspenger.domene.vilkår.Vurdering
import no.nav.tiltakspenger.felles.Periode
import java.time.LocalDate

enum class TypeSaksopplysning {
    IKKE_INNHENTET_ENDA,
    HAR_YTELSE,
    HAR_IKKE_YTELSE,
}

data class Saksopplysning(
    val fom: LocalDate,
    val tom: LocalDate,
    val kilde: Kilde,
    val vilkår: Vilkår,
    val detaljer: String,
    val typeSaksopplysning: TypeSaksopplysning,
    val saksbehandler: String? = null,
) {
    companion object {
        fun initFakta(periode: Periode, vilkår: Vilkår): Saksopplysning {
            return Saksopplysning(
                fom = periode.fra,
                tom = periode.til,
                vilkår = vilkår,
                kilde = settKilde(vilkår),
                detaljer = "",
                typeSaksopplysning = TypeSaksopplysning.IKKE_INNHENTET_ENDA,
            )
        }

        fun lagSaksopplysningFraSBH(
            fom: LocalDate,
            tom: LocalDate,
            vilkår: Vilkår,
            detaljer: String,
            typeSaksopplysning: TypeSaksopplysning,
            saksbehandler: String,
        ): Saksopplysning {
            return Saksopplysning(
                fom = fom,
                tom = tom,
                vilkår = vilkår,
                kilde = Kilde.SAKSB,
                detaljer = detaljer, // Her blir detaljer brukt til begrunnelse, bør kanskje revurderes
                typeSaksopplysning = typeSaksopplysning,
                saksbehandler = saksbehandler,
            )
        }
    }
}

fun lagFaktaFraSøknad(søknad: Søknad): Saksopplysning {
    if (søknad.kvp is Søknad.PeriodeSpm.Ja) {
        return Saksopplysning(
            fom = søknad.kvp.periode.fra,
            tom = søknad.kvp.periode.til,
            vilkår = Vilkår.KVP,
            kilde = Kilde.SØKNAD,
            detaljer = "Har svart Ja i søknaden",
            typeSaksopplysning = HAR_YTELSE,
        )
    } else {
        return Saksopplysning(
            fom = søknad.vurderingsperiode().fra,
            tom = søknad.vurderingsperiode().til,
            vilkår = Vilkår.KVP,
            kilde = Kilde.SØKNAD,
            detaljer = "Har svart Nei i søknaden",
            typeSaksopplysning = HAR_IKKE_YTELSE,
        )
    }
}

fun lagFaktaFraPeriodespørsmål(vilkår: Vilkår, periodeSpm: Søknad.PeriodeSpm, periode: Periode): Saksopplysning {
    return Saksopplysning(
        fom = if (periodeSpm is Søknad.PeriodeSpm.Ja) periodeSpm.periode.fra else periode.fra,
        tom = if (periodeSpm is Søknad.PeriodeSpm.Ja) periodeSpm.periode.til else periode.til,
        vilkår = vilkår,
        kilde = Kilde.SØKNAD,
        detaljer = "",
        typeSaksopplysning = if (periodeSpm is Søknad.PeriodeSpm.Ja) HAR_YTELSE else HAR_IKKE_YTELSE,
    )
}

fun lagFaktaFraJaNeiSpørsmål(vilkår: Vilkår, jaNeiSpm: Søknad.JaNeiSpm, periode: Periode): Saksopplysning {
    return Saksopplysning(
        fom = periode.fra,
        tom = periode.til,
        vilkår = vilkår,
        kilde = Kilde.SØKNAD,
        detaljer = "",
        typeSaksopplysning = if (jaNeiSpm is Søknad.JaNeiSpm.Ja) HAR_YTELSE else HAR_IKKE_YTELSE,
    )
}

fun lagFaktaFraFraOgMedDatospørsmål(
    vilkår: Vilkår,
    fraOgMedDatoSpm: Søknad.FraOgMedDatoSpm,
    periode: Periode,
): Saksopplysning {
    return Saksopplysning(
        fom = if (fraOgMedDatoSpm is Søknad.FraOgMedDatoSpm.Ja) fraOgMedDatoSpm.fra else periode.fra,
        tom = periode.til,
        vilkår = vilkår,
        kilde = Kilde.SØKNAD,
        detaljer = "",
        typeSaksopplysning = if (fraOgMedDatoSpm is Søknad.FraOgMedDatoSpm.Ja) HAR_YTELSE else HAR_IKKE_YTELSE,
    )
}

private fun prioriterOverlappendeSaksopplysning() {
}

private fun lagVurderingerForOppfyltePerioder() {
}

// Eksempel på bruker som ikke har andre ytelser
// Saksopplysninger
// |     fom     |    tom     | Vilkår| Kilde   | opphørTidligereSaksopplysning  |

// Periode som skal vurderes : 2023-01-01 - 2023-03-31

// Vurderinger
// | Vilkår | kilde  |    fom     |    tom     |
// |   AAP  | Arena  | 2023-01-01 | 2023-03-31 |  Oppfylt

// ----

// Eksempel på bruker går på AAP deler av perioden
// Saksopplysninger
// |     fom     |    tom     | Vilkår| Kilde   | opphørTidligereSaksopplysning  |
// |  2023-01-01 | 2023-01-31 |  AAP  | Arena   |        false                   |

// Periode som skal vurderes : 2023-01-01 - 2023-03-31

// Vurderinger
// | Vilkår | kilde  |    fom     |    tom     |
// |   AAP  | Arena  | 2023-01-01 | 2023-01-31 |  IkkeOppfylt
// |   AAP  | Arena  | 2023-02-01 | 2023-03-31 |  Oppfylt

// ---

// Eksempel med saksbehandler overstyrer saksopplysninger og sier at bruker ikke går på AAP
// Saksopplysninger
// |     fom     |    tom     | Vilkår| Kilde           | opphørTidligereSaksopplysning  |
// |  2023-01-01 | 2023-01-31 |  AAP  | Arena           |         false                  |
// |  2023-01-01 | 2023-01-31 |  AAP  | Saksbehandler   |         true                   |

// Periode som skal vurderes : 2023-01-01 - 2023-03-31

// Vurderinger
// | Vilkår | kilde  |    fom     |    tom     |
// |   AAP  | Arena  | 2023-01-01 | 2023-01-31 |   Oppfylt
// |   AAP  | Arena  | 2023-02-01 | 2023-03-31 |   Oppfylt
// Eller :
// | Vilkår | kilde  |    fom     |    tom     |
// |   AAP  | Arena  | 2023-01-01 | 2023-03-31 |   Oppfylt

// ---

// Eksempel med saksbehandler overstyrer saksopplysninger og sier at bruker ikke går på AAP i deler av perioden
// Saksopplysninger
// |     fom     |    tom     | Vilkår| Kilde           | opphørTidligereSaksopplysning  |
// |  2023-01-01 | 2023-01-31 |  AAP  | Arena           |         false                  |
// |  2023-01-01 | 2023-01-15 |  AAP  | Saksbehandler   |         true                   |

// Periode som skal vurderes : 2023-01-01 - 2023-03-31

// Vurderinger
// | Vilkår | kilde  |    fom     |    tom     |
// |   AAP  | Arena  | 2023-01-01 | 2023-01-15 |   Oppfylt
// |   AAP  | Arena  | 2023-01-16 | 2023-01-31 |   IkkeOppfylt
// |   AAP  | Arena  | 2023-02-01 | 2023-03-31 |   Oppfylt

// ---

// Eksempel med saksbehandler overstyrer saksopplysninger og sier at bruker går på AAP
// Saksopplysninger
// |     fom     |    tom     | Vilkår| Kilde           | opphørTidligereSaksopplysning  |
// |  2023-01-01 | 2023-01-31 |  AAP  | Saksbehandler   |         false                  |

// Periode som skal vurderes : 2023-01-01 - 2023-03-31

// Vurderinger
// | Vilkår | kilde  |    fom     |    tom     |
// |   AAP  | Arena  | 2023-01-01 | 2023-01-31 |   IkkeOppfylt
// |   AAP  | Arena  | 2023-02-01 | 2023-03-31 |   Oppfylt

fun Saksopplysning.lagVurdering(periode: Periode): List<Vurdering> {
// TODO Her må vi kanskje lage Vurderinger for Oppfylte perioder for at vi skal kunne lage DelvisInnvilget?

// Lag liste med Vurdering av alle Saksopplysninger som har opphør... false (egentlig alle som er kilde != SAKSB + kilde == SAKSB && Opphør == true )

// Trenger vi å slå sammen overlappende perioder her ?

// Lag liste med Vurderinger av alle Saksopplysniger som har opphør.. true
// Fjern disse periodene fra den første listen
    // Slå sammen periodene

// |     fom     |    tom     | Vilkår| Kilde           | opphørTidligereSaksopplysning  |
// |  2023-01-01 | 2023-01-31 |  AAP  | Arena           |         false                  |
// |  2023-01-01 | 2023-01-15 |  AAP  | Saksbehandler   |         true                   |

//    // TODO Denne må det jobbes mere med......
//    val saksopplysning: Saksopplysning = this.reduce { acc, saksopplysning ->
//        if (saksopplysning.kilde == Kilde.SAKSB) {
//            saksopplysning
//        } else {
//            if (acc.kilde == Kilde.SAKSB) acc else saksopplysning
//        }
//    }

    val vurdering = when (this.typeSaksopplysning) {
        TypeSaksopplysning.IKKE_INNHENTET_ENDA -> Vurdering.KreverManuellVurdering(
            vilkår = this.vilkår,
            kilde = this.kilde,
            fom = this.fom,
            tom = this.tom,
            detaljer = this.detaljer,
        )

        HAR_YTELSE -> Vurdering.IkkeOppfylt(
            vilkår = this.vilkår,
            kilde = this.kilde,
            fom = this.fom,
            tom = this.tom,
            detaljer = this.detaljer,
        )

        HAR_IKKE_YTELSE -> Vurdering.Oppfylt(
            vilkår = this.vilkår,
            kilde = this.kilde,
            fom = this.fom,
            tom = this.tom,
            detaljer = this.detaljer,
        )
    }

    if (vurdering is Vurdering.IkkeOppfylt) {
        val oppfyltePerioder = periode.ikkeOverlappendePeriode(Periode(fra = this.fom, til = this.tom)).map {
            Vurdering.Oppfylt(
                vilkår = this.vilkår,
                kilde = this.kilde,
                fom = it.fra,
                tom = it.til,
                detaljer = this.detaljer,
            )
        }

        return oppfyltePerioder + vurdering
    }
    return listOf(vurdering)
}

fun settKilde(vilkår: Vilkår): Kilde {
    return when (vilkår) {
        Vilkår.AAP -> Kilde.ARENA
        Vilkår.ALDER -> Kilde.PDL
        Vilkår.ALDERSPENSJON -> Kilde.SØKNAD
        Vilkår.DAGPENGER -> Kilde.ARENA
        Vilkår.FORELDREPENGER -> Kilde.FPSAK
        Vilkår.GJENLEVENDEPENSJON -> Kilde.SØKNAD
        Vilkår.INSTITUSJONSOPPHOLD -> Kilde.SØKNAD
        Vilkår.INTROPROGRAMMET -> Kilde.SØKNAD
        Vilkår.JOBBSJANSEN -> Kilde.SØKNAD
        Vilkår.KVP -> Kilde.SØKNAD
        Vilkår.LØNNSINNTEKT -> Kilde.SØKNAD
        Vilkår.OMSORGSPENGER -> Kilde.K9SAK
        Vilkår.OPPLÆRINGSPENGER -> Kilde.K9SAK
        Vilkår.OVERGANGSSTØNAD -> Kilde.EF
        Vilkår.PENSJONSINNTEKT -> Kilde.SØKNAD
        Vilkår.PLEIEPENGER_NÆRSTÅENDE -> Kilde.K9SAK
        Vilkår.PLEIEPENGER_SYKT_BARN -> Kilde.K9SAK
        Vilkår.SUPPLERENDESTØNADALDER -> Kilde.SØKNAD
        Vilkår.SUPPLERENDESTØNADFLYKTNING -> Kilde.SØKNAD
        Vilkår.SVANGERSKAPSPENGER -> Kilde.FPSAK
        Vilkår.SYKEPENGER -> Kilde.SØKNAD
        Vilkår.TILTAKSPENGER -> Kilde.ARENA
        Vilkår.UFØRETRYGD -> Kilde.PESYS
        Vilkår.ETTERLØNN -> Kilde.SØKNAD
        Vilkår.KOMMUNALEYTELSER -> throw IllegalStateException("Denne skal kanskje fjernes?")
        Vilkår.STATLIGEYTELSER -> throw IllegalStateException("Denne skal kanskje fjernes?")
    }
}

enum class Kilde(val navn: String) {
    ARENA("Arena"),
    PDL("Pdl"),
    EF("EF"),
    FPSAK("FPSAK"),
    K9SAK("K9SAK"),
    PESYS("pesys"),
    SØKNAD("Søknad"),
    SAKSB("Saksbehandler"),
}
