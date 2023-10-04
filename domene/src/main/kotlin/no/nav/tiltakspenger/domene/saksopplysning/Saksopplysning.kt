package no.nav.tiltakspenger.domene.saksopplysning

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
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
) {
    companion object {
        fun initFakta(periode: Periode, vilkår: Vilkår): Saksopplysning {
            return Saksopplysning(
                fom = periode.fra,
                tom = periode.til,
                vilkår = vilkår,
                kilde = Kilde.ARENA,
                detaljer = "",
                typeSaksopplysning = TypeSaksopplysning.IKKE_INNHENTET_ENDA,
            )
        }
        fun lagSaksopplysningFraSBH(fom: LocalDate, tom: LocalDate, vilkår: Vilkår, detaljer: String, typeSaksopplysning: TypeSaksopplysning): Saksopplysning {
            return Saksopplysning(
                fom = fom,
                tom = tom,
                vilkår = vilkår,
                kilde = Kilde.SAKSB,
                detaljer = detaljer, // Her blir detaljer brukt til begrunnelse, bør kanskje revurderes
                typeSaksopplysning = typeSaksopplysning,
            )
        }
    }
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

fun List<Saksopplysning>.lagVurdering(vilkår: Vilkår): List<Vurdering> =
// TODO Her må vi kanskje lage Vurderinger for Oppfylte perioder for at vi skal kunne lage DelvisInnvilget?

// Lag liste med Vurdering av alle Saksopplysninger som har opphør... false (egentlig alle som er kilde != SAKSB + kilde == SAKSB && Opphør == true )

// Trenger vi å slå sammen overlappende perioder her ?

// Lag liste med Vurderinger av alle Saksopplysniger som har opphør.. true
// Fjern disse periodene fra den første listen
    // Slå sammen periodene

    this.map { fakta ->
        when (fakta.typeSaksopplysning) {
            TypeSaksopplysning.IKKE_INNHENTET_ENDA -> Vurdering.KreverManuellVurdering(
                vilkår = fakta.vilkår,
                kilde = fakta.kilde,
                fom = fakta.fom,
                tom = fakta.tom,
                detaljer = fakta.detaljer,
            )
            TypeSaksopplysning.HAR_YTELSE -> Vurdering.IkkeOppfylt(
                vilkår = fakta.vilkår,
                kilde = fakta.kilde,
                fom = fakta.fom,
                tom = fakta.tom,
                detaljer = fakta.detaljer,
            )
            TypeSaksopplysning.HAR_IKKE_YTELSE -> Vurdering.Oppfylt(
                vilkår = fakta.vilkår,
                kilde = fakta.kilde,
                fom = fakta.fom,
                tom = fakta.tom,
                detaljer = fakta.detaljer,
            )
        }
    }.ifEmpty {
        listOf(
            Vurdering.Oppfylt(
                vilkår = vilkår,
                kilde = settKilde(vilkår),
                detaljer = "",
            ),
        )
    }

private fun settKilde(vilkår: Vilkår): Kilde {
    return when (vilkår) {
        Vilkår.AAP -> Kilde.ARENA
        Vilkår.ALDER -> TODO()
        Vilkår.ALDERSPENSJON -> TODO()
        Vilkår.DAGPENGER -> Kilde.ARENA
        Vilkår.FORELDREPENGER -> TODO()
        Vilkår.GJENLEVENDEPENSJON -> TODO()
        Vilkår.INSTITUSJONSOPPHOLD -> TODO()
        Vilkår.INTROPROGRAMMET -> TODO()
        Vilkår.KOMMUNALEYTELSER -> TODO()
        Vilkår.KVP -> TODO()
        Vilkår.LØNNSINNTEKT -> TODO()
        Vilkår.OMSORGSPENGER -> TODO()
        Vilkår.OPPLÆRINGSPENGER -> TODO()
        Vilkår.OVERGANGSSTØNAD -> TODO()
        Vilkår.PENSJONSINNTEKT -> TODO()
        Vilkår.PLEIEPENGER_NÆRSTÅENDE -> TODO()
        Vilkår.PLEIEPENGER_SYKT_BARN -> TODO()
        Vilkår.STATLIGEYTELSER -> TODO()
        Vilkår.SUPPLERENDESTØNADALDER -> TODO()
        Vilkår.SUPPLERENDESTØNADFLYKTNING -> TODO()
        Vilkår.SVANGERSKAPSPENGER -> TODO()
        Vilkår.SYKEPENGER -> TODO()
        Vilkår.TILTAKSPENGER -> TODO()
        Vilkår.UFØRETRYGD -> TODO()
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
